import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../../core/api.service';
import {
  displayConnection,
  displayExternalOption,
  displayMachineName,
  displayMaterialName,
  displayMaterialType,
  displayRecipeAmount,
  displayRecipeName,
  displayTransportAdvice,
  displayWarning,
  translateLineLikeName,
} from '../../core/i18n';
import type {
  BusLine,
  BusLineCalculation,
  BusLinePatchRequest,
  ExternalConnection,
  ExternalSourceOption,
  Factory,
  FactoryCalculation,
  FactoryRequest,
  FactoryType,
  HealthStatus,
  Material,
  MaterialType,
  ProductionBucket,
  ProductionBucketRequest,
  ProductionNode,
  ProductionNodeRequest,
  Recipe,
} from '../../core/types';

type SelectionKind = 'bucket' | 'line' | null;
type Toast = { type: 'ok' | 'error'; text: string } | null;

type MaterialFlow = { materialId: number; materialName: string; amount: number };

type BucketSummary = {
  activeNodeCount: number;
  inputs: MaterialFlow[];
  outputs: MaterialFlow[];
};

type CanvasConnection = {
  key: string;
  d: string;
  kind: 'input' | 'output';
};

type DragState = {
  bucketId: number;
  startPointerX: number;
  startPointerY: number;
  startX: number;
  startY: number;
  moved: boolean;
} | null;

type LineDragState = {
  lineId: number;
  startPointerX: number;
  startIndex: number;
  currentDx: number;
  moved: boolean;
} | null;

type MachineOption = { machineId: number | null; machineName: string | null; label: string };

@Component({
  selector: 'app-designer-page',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './designer-page.component.html',
})
export class DesignerPageComponent implements OnInit {
  readonly factoryTypeOptions: { value: FactoryType; label: string }[] = [
    { value: 'MAIN', label: '总厂' },
    { value: 'SUB', label: '分厂' },
    { value: 'HUB', label: '中转' },
    { value: 'TEMP', label: '临时' },
  ];

  materials: Material[] = [];
  recipes: Recipe[] = [];
  factories: Factory[] = [];
  selectedFactoryId: number | null = null;
  selectedFactory: Factory | null = null;
  buckets: ProductionBucket[] = [];
  nodesByBucket: Record<number, ProductionNode[]> = {};
  busLines: BusLine[] = [];
  calculation: FactoryCalculation | null = null;
  connections: ExternalConnection[] = [];
  externalOptions: ExternalSourceOption[] = [];
  loading = false;
  toast: Toast = null;

  readonly bucketCardWidth = 300;
  readonly bucketCardHeight = 146;
  readonly lineColumnWidth = 168;
  readonly lineColumnGap = 24;
  readonly lineStartX = 360;
  readonly lineCardHeight = 92;
  readonly lineBottomGap = 44;
  readonly pillarTopPadding = 72;
  readonly minCanvasWidth = 1180;
  readonly minCanvasHeight = 820;
  canvasZoom = 1;
  dragState: DragState = null;
  lineDragState: LineDragState = null;
  suppressBucketClick = false;
  suppressLineClick = false;

  selectionKind: SelectionKind = null;
  selectedId: number | null = null;

  createFactoryName = '总厂主总线';
  createFactoryType: FactoryType = 'MAIN';
  newBucketName = '新生产桶';
  newLineMaterialType: MaterialType | null = null;
  newLineMaterialId: number | null = null;
  selectedSourceId: number | null = null;

  factoryDraft: FactoryRequest = this.emptyFactoryDraft();
  bucketDraft: ProductionBucketRequest = this.emptyBucketDraft();
  lineDraft: BusLinePatchRequest = {};
  newNodeProductMaterialType: MaterialType | null = 'SOLID';
  newNodeProductMaterialId: number | null = null;
  newNodeMachineId: number | null = null;
  newNodeDraft: ProductionNodeRequest = this.emptyNodeDraft();
  nodeDrafts: Record<number, ProductionNodeRequest> = {};
  nodeProductMaterialTypes: Record<number, MaterialType | null> = {};
  nodeProductMaterialIds: Record<number, number | null> = {};
  nodeMachineIds: Record<number, number | null> = {};

  constructor(public readonly api: ApiService) {}

  async ngOnInit(): Promise<void> {
    await this.run(async () => {
      await this.loadCatalog();
      await this.loadFactories(null);
    });
  }

  async run(action: () => Promise<void>, okText?: string): Promise<void> {
    try {
      this.toast = null;
      await action();
      if (okText) this.toast = { type: 'ok', text: okText };
    } catch (error) {
      this.toast = { type: 'error', text: this.formatError(error) };
      console.error(error);
    }
  }

  formatError(error: unknown): string {
    if (error instanceof HttpErrorResponse) {
      if (error.status === 0) {
        return '无法连接后端。请确认 Spring Boot 正在运行，并且后端 CORS 允许 http://localhost:4200。';
      }
      const body = error.error as any;
      const message = body?.message || body?.error || body?.detail || error.message;
      return `请求失败 ${error.status}: ${message}`;
    }
    if (error instanceof Error) return error.message;
    try { return JSON.stringify(error); } catch { return String(error); }
  }

  async loadCatalog(): Promise<void> {
    const [materials, recipes] = await Promise.all([this.api.materials(), this.api.recipes()]);
    this.materials = materials.filter((item) => item.enabled).sort((a, b) => this.materialLabel(a).localeCompare(this.materialLabel(b)));
    this.recipes = recipes.filter((item) => item.enabled).sort((a, b) => this.recipeLabel(a).localeCompare(this.recipeLabel(b)));
    this.ensureNewNodeSelection();
  }

  async loadFactories(nextSelectedId?: number | null): Promise<void> {
    const data = await this.api.factories();
    this.factories = data;
    const candidate = nextSelectedId ?? this.selectedFactoryId ?? data[0]?.id ?? null;
    this.selectedFactoryId = candidate && data.some((factory) => factory.id === candidate) ? candidate : data[0]?.id ?? null;
    if (this.selectedFactoryId) await this.loadFactoryData(this.selectedFactoryId);
    else this.clearFactoryData();
  }

  async loadFactoryData(factoryId: number): Promise<void> {
    this.loading = true;
    try {
      const [factory, buckets, busLines, calculation, connections] = await Promise.all([
        this.api.factory(factoryId),
        this.api.buckets(factoryId),
        this.api.busLines(factoryId),
        this.api.calculation(factoryId),
        this.api.externalConnectionsIntoFactory(factoryId),
      ]);
      const nodePairs = await Promise.all(buckets.map(async (bucket) => [bucket.id, await this.api.nodes(bucket.id)] as const));
      this.selectedFactory = factory;
      this.factoryDraft = this.factoryToRequest(factory);
      this.buckets = buckets;
      this.busLines = this.sortBusLines(busLines);
      this.calculation = calculation;
      this.connections = connections;
      this.nodesByBucket = Object.fromEntries(nodePairs);
      this.syncDetailDrafts();
    } finally {
      this.loading = false;
    }
  }

  clearFactoryData(): void {
    this.selectedFactory = null;
    this.buckets = [];
    this.busLines = [];
    this.calculation = null;
    this.connections = [];
    this.nodesByBucket = {};
    this.selectionKind = null;
    this.selectedId = null;
  }

  async selectFactory(id: number): Promise<void> {
    this.selectedFactoryId = id;
    this.selectionKind = null;
    this.selectedId = null;
    await this.run(() => this.loadFactoryData(id));
  }

  async refreshCurrentFactory(): Promise<void> {
    if (!this.selectedFactoryId) return;
    await this.loadFactoryData(this.selectedFactoryId);
  }

  async createFactory(): Promise<void> {
    const name = this.createFactoryName.trim();
    if (!name) return;
    await this.run(async () => {
      const created = await this.api.createFactory({ name, factoryType: this.createFactoryType, enabled: true, maxBeltLevel: 3, maxPipeLevel: 1, description: '' });
      await this.loadFactories(created.id);
    }, '工厂已创建');
  }

  async saveFactory(): Promise<void> {
    if (!this.selectedFactory) return;
    await this.run(async () => {
      const updated = await this.api.updateFactory(this.selectedFactory!.id, this.factoryDraft);
      this.selectedFactory = updated;
      await this.loadFactories(updated.id);
    }, '工厂设置已保存');
  }

  async copyFactory(): Promise<void> {
    if (!this.selectedFactory) return;
    await this.run(async () => {
      const copied = await this.api.copyFactory(this.selectedFactory!.id);
      await this.loadFactories(copied.id);
    }, '已复制为新工厂');
  }

  async saveSnapshot(): Promise<void> {
    if (!this.selectedFactory) return;
    const name = window.prompt('版本名称', `${this.selectedFactory.name} 快照`);
    if (!name) return;
    await this.run(async () => { await this.api.snapshotFactory(this.selectedFactory!.id, name); }, '版本快照已保存');
  }

  async deleteFactory(): Promise<void> {
    if (!this.selectedFactory) return;
    if (!window.confirm(`确认删除工厂：${this.selectedFactory.name}？`)) return;
    await this.run(async () => {
      await this.api.deleteFactory(this.selectedFactory!.id);
      this.selectionKind = null;
      this.selectedId = null;
      await this.loadFactories(null);
    }, '工厂已删除');
  }

  async seedAll(): Promise<void> {
    await this.run(async () => {
      await this.api.seedAll();
      await this.loadCatalog();
      await this.refreshCurrentFactory();
    }, '基础数据已重新导入');
  }

  async createBucket(): Promise<void> {
    if (!this.selectedFactory) return;
    const name = this.newBucketName.trim();
    if (!name) return;
    await this.run(async () => {
      await this.api.createBucket(this.selectedFactory!.id, {
        name,
        enabled: false,
        description: '',
        collapsed: false,
        sortOrder: this.buckets.length + 1,
        positionX: 72,
        positionY: this.defaultBucketY(this.buckets.length),
      });
      await this.refreshCurrentFactory();
    }, '生产桶已创建');
  }

  async toggleBucket(bucket: ProductionBucket, event?: Event): Promise<void> {
    event?.stopPropagation();
    await this.run(async () => {
      await this.api.updateBucket(bucket.id, { ...this.bucketToRequest(bucket), enabled: !bucket.enabled });
      await this.refreshCurrentFactory();
    });
  }

  async createBusLine(): Promise<void> {
    if (!this.selectedFactory || this.newLineMaterialId == null) return;
    await this.run(async () => {
      await this.api.createBusLine(this.selectedFactory!.id, { materialId: this.newLineMaterialId!, createdManually: true, offsetAmount: 0, sortOrder: this.busLines.length + 1 });
      this.newLineMaterialId = null;
      await this.refreshCurrentFactory();
    }, '物资线已创建');
  }

  openBucketFromClick(bucket: ProductionBucket, event?: MouseEvent): void {
    event?.stopPropagation();
    if (this.suppressBucketClick) { this.suppressBucketClick = false; return; }
    this.selectionKind = 'bucket';
    this.selectedId = bucket.id;
    this.syncDetailDrafts();
  }

  async openLineFromClick(line: BusLine, event?: MouseEvent): Promise<void> {
    event?.stopPropagation();
    if (this.suppressLineClick) { this.suppressLineClick = false; return; }
    this.selectionKind = 'line';
    this.selectedId = line.id;
    this.selectedSourceId = null;
    this.syncDetailDrafts();
    await this.run(async () => {
      this.externalOptions = await this.api.availableExternalSources(line.id);
    });
  }

  closeDetail(): void {
    this.selectionKind = null;
    this.selectedId = null;
    this.externalOptions = [];
  }

  async saveBucket(): Promise<void> {
    const bucket = this.selectedBucket;
    if (!bucket) return;
    await this.run(async () => {
      await this.api.updateBucket(bucket.id, this.bucketDraft);
      await this.refreshCurrentFactory();
    }, '生产桶已保存');
  }

  async deleteBucket(): Promise<void> {
    const bucket = this.selectedBucket;
    if (!bucket) return;
    await this.run(async () => {
      await this.api.deleteBucket(bucket.id);
      this.closeDetail();
      await this.refreshCurrentFactory();
    }, '生产桶已删除');
  }

  async createNode(): Promise<void> {
    const bucket = this.selectedBucket;
    if (!bucket || !this.newNodeDraft.recipeId) return;
    await this.run(async () => {
      await this.api.createNode(bucket.id, this.nodePayload(this.newNodeDraft));
      await this.refreshCurrentFactory();
    }, '生产节点已创建');
  }

  async saveNode(node: ProductionNode): Promise<void> {
    const draft = this.nodeDrafts[node.id];
    if (!draft) return;
    await this.run(async () => {
      await this.api.updateNode(node.id, this.nodePayload(draft));
      await this.refreshCurrentFactory();
    }, '生产节点已保存');
  }

  async deleteNode(node: ProductionNode): Promise<void> {
    await this.run(async () => {
      await this.api.deleteNode(node.id);
      await this.refreshCurrentFactory();
    }, '生产节点已删除');
  }

  async saveLine(): Promise<void> {
    const line = this.selectedLine;
    if (!line) return;
    await this.run(async () => {
      await this.api.updateBusLine(line.id, this.lineDraft);
      await this.refreshCurrentFactory();
    }, '物资线已保存');
  }

  async deleteLine(): Promise<void> {
    const line = this.selectedLine;
    if (!line) return;
    await this.run(async () => {
      await this.api.deleteBusLine(line.id);
      this.closeDetail();
      await this.refreshCurrentFactory();
    }, '物资线已删除');
  }

  async connectExternal(): Promise<void> {
    const line = this.selectedLine;
    if (!line || this.selectedSourceId == null) return;
    await this.run(async () => {
      await this.api.createExternalConnection(this.selectedSourceId!, line.id);
      await this.refreshCurrentFactory();
      this.externalOptions = await this.api.availableExternalSources(line.id);
    }, '外联输入已连接');
  }

  async disconnect(connectionId: number): Promise<void> {
    await this.run(async () => {
      await this.api.deleteExternalConnection(connectionId);
      await this.refreshCurrentFactory();
    }, '外联输入已解绑');
  }

  syncDetailDrafts(): void {
    const bucket = this.selectedBucket;
    if (bucket) {
      this.bucketDraft = this.bucketToRequest(bucket);
      const nodes = this.nodesByBucket[bucket.id] ?? [];
      this.nodeDrafts = Object.fromEntries(nodes.map((node) => [node.id, this.nodeToRequest(node)]));
      this.nodeProductMaterialIds = Object.fromEntries(nodes.map((node) => [node.id, this.primaryOutputMaterialId(node.recipeId)]));
      this.nodeProductMaterialTypes = Object.fromEntries(nodes.map((node) => {
        const material = this.materialById(this.primaryOutputMaterialId(node.recipeId));
        return [node.id, material?.materialType ?? null];
      }));
      this.nodeMachineIds = Object.fromEntries(nodes.map((node) => [node.id, this.recipeById(node.recipeId)?.machineId ?? null]));
      this.ensureNewNodeSelection();
    }
    const line = this.selectedLine;
    if (line) {
      this.lineDraft = {
        name: line.name,
        description: line.description ?? '',
        offsetAmount: line.offsetAmount ?? 0,
        visibleToOtherFactories: line.visibleToOtherFactories,
        externalEnabled: line.externalEnabled,
        collapsed: line.collapsed,
        sortOrder: line.sortOrder ?? 0,
      };
    }
  }

  get selectedBucket(): ProductionBucket | null {
    return this.selectionKind === 'bucket' ? this.buckets.find((bucket) => bucket.id === this.selectedId) ?? null : null;
  }

  get selectedLine(): BusLine | null {
    return this.selectionKind === 'line' ? this.busLines.find((line) => line.id === this.selectedId) ?? null : null;
  }

  get selectedLineCalculation(): BusLineCalculation | null {
    const line = this.selectedLine;
    return line ? this.getCalc(line.id) : null;
  }

  get selectedBucketNodes(): ProductionNode[] {
    const bucket = this.selectedBucket;
    return bucket ? this.nodesByBucket[bucket.id] ?? [] : [];
  }

  get incomingConnectionsForSelectedLine(): ExternalConnection[] {
    const line = this.selectedLine;
    return line ? this.connections.filter((connection) => connection.targetBusLineId === line.id) : [];
  }

  getCalc(busLineId: number): BusLineCalculation | null {
    return this.calculation?.busLines.find((line) => line.busLineId === busLineId) ?? null;
  }

  recipeById(recipeId: number): Recipe | undefined {
    return this.recipes.find((recipe) => recipe.id === recipeId);
  }

  materialById(materialId: number | null | undefined): Material | undefined {
    return materialId == null ? undefined : this.materials.find((material) => material.id === materialId);
  }

  readonly materialTypeOptions: { value: MaterialType; label: string }[] = [
    { value: 'SOLID', label: '固体' },
    { value: 'FLUID', label: '液体' },
    { value: 'GAS', label: '气体' },
  ];

  sortBusLines(lines: BusLine[]): BusLine[] {
    return [...lines].sort((a, b) => {
      const av = a.sortOrder ?? 999999;
      const bv = b.sortOrder ?? 999999;
      if (av !== bv) return av - bv;
      return this.materialLabel({ id: a.materialId, gameKey: '', name: a.materialName, materialType: a.materialType, stackSize: null, sinkable: false, enabled: true, description: null }).localeCompare(
        this.materialLabel({ id: b.materialId, gameKey: '', name: b.materialName, materialType: b.materialType, stackSize: null, sinkable: false, enabled: true, description: null }),
      );
    });
  }

  selectNewLineMaterialType(type: MaterialType | null): void {
    this.newLineMaterialType = type;
    this.newLineMaterialId = null;
  }

  productMaterials(): Material[] {
    const outputIds = new Set<number>();
    for (const recipe of this.recipes) {
      for (const output of recipe.outputs) outputIds.add(output.materialId);
    }
    return this.materials.filter((material) => outputIds.has(material.id));
  }

  productMaterialsByType(type: MaterialType | null | undefined): Material[] {
    if (!type) return [];
    return this.productMaterials().filter((material) => material.materialType === type);
  }

  recipesForProduct(materialId: number | null | undefined): Recipe[] {
    if (materialId == null) return [];
    return this.recipes.filter((recipe) => recipe.outputs.some((output) => output.materialId === materialId));
  }

  machineOptionsForProduct(materialId: number | null | undefined): MachineOption[] {
    const seen = new Set<string>();
    const options: MachineOption[] = [];
    for (const recipe of this.recipesForProduct(materialId)) {
      const key = String(recipe.machineId ?? 'none');
      if (seen.has(key)) continue;
      seen.add(key);
      options.push({ machineId: recipe.machineId ?? null, machineName: recipe.machineName ?? null, label: this.machineName(recipe.machineName) });
    }
    return options.sort((a, b) => a.label.localeCompare(b.label));
  }

  recipesForProductAndMachine(materialId: number | null | undefined, machineId: number | null | undefined): Recipe[] {
    if (materialId == null) return [];
    return this.recipesForProduct(materialId)
      .filter((recipe) => (recipe.machineId ?? null) === (machineId ?? null))
      .sort((a, b) => this.recipeLabel(a).localeCompare(this.recipeLabel(b)));
  }

  selectNewNodeProductType(type: MaterialType | null): void {
    this.newNodeProductMaterialType = type;
    this.newNodeProductMaterialId = null;
    this.newNodeMachineId = null;
    this.newNodeDraft.recipeId = 0;
  }

  selectNewNodeProduct(materialId: number | null): void {
    this.newNodeProductMaterialId = materialId;
    const firstMachine = this.machineOptionsForProduct(materialId)[0];
    this.newNodeMachineId = firstMachine?.machineId ?? null;
    const firstRecipe = this.recipesForProductAndMachine(materialId, this.newNodeMachineId)[0];
    this.newNodeDraft.recipeId = firstRecipe?.id ?? 0;
  }

  selectNewNodeMachine(machineId: number | null): void {
    this.newNodeMachineId = machineId;
    const firstRecipe = this.recipesForProductAndMachine(this.newNodeProductMaterialId, machineId)[0];
    this.newNodeDraft.recipeId = firstRecipe?.id ?? 0;
  }

  selectNodeProductType(nodeId: number, type: MaterialType | null): void {
    this.nodeProductMaterialTypes[nodeId] = type;
    this.nodeProductMaterialIds[nodeId] = null;
    this.nodeMachineIds[nodeId] = null;
    if (this.nodeDrafts[nodeId]) this.nodeDrafts[nodeId] = { ...this.nodeDrafts[nodeId], recipeId: 0 };
  }

  selectNodeProduct(nodeId: number, materialId: number | null): void {
    this.nodeProductMaterialIds[nodeId] = materialId;
    const firstMachine = this.machineOptionsForProduct(materialId)[0];
    this.nodeMachineIds[nodeId] = firstMachine?.machineId ?? null;
    const firstRecipe = this.recipesForProductAndMachine(materialId, this.nodeMachineIds[nodeId])[0];
    if (firstRecipe && this.nodeDrafts[nodeId]) {
      this.nodeDrafts[nodeId] = { ...this.nodeDrafts[nodeId], recipeId: firstRecipe.id };
    }
  }

  selectNodeMachine(nodeId: number, machineId: number | null): void {
    this.nodeMachineIds[nodeId] = machineId;
    const firstRecipe = this.recipesForProductAndMachine(this.nodeProductMaterialIds[nodeId], machineId)[0];
    if (firstRecipe && this.nodeDrafts[nodeId]) {
      this.nodeDrafts[nodeId] = { ...this.nodeDrafts[nodeId], recipeId: firstRecipe.id };
    }
  }

  primaryOutputMaterialId(recipeId: number): number | null {
    const recipe = this.recipeById(recipeId);
    return recipe?.outputs[0]?.materialId ?? null;
  }

  ensureNewNodeSelection(): void {
    if (!this.newNodeProductMaterialType) this.newNodeProductMaterialType = 'SOLID';
    if (!this.newNodeProductMaterialId) {
      const firstProduct = this.productMaterialsByType(this.newNodeProductMaterialType)[0];
      this.newNodeProductMaterialId = firstProduct?.id ?? null;
    }
    const machineOptions = this.machineOptionsForProduct(this.newNodeProductMaterialId);
    if (!machineOptions.some((option) => option.machineId === this.newNodeMachineId)) {
      this.newNodeMachineId = machineOptions[0]?.machineId ?? null;
    }
    const recipes = this.recipesForProductAndMachine(this.newNodeProductMaterialId, this.newNodeMachineId);
    if (!recipes.some((recipe) => recipe.id === this.newNodeDraft.recipeId)) {
      this.newNodeDraft.recipeId = recipes[0]?.id ?? 0;
    }
  }

  nodePayload(draft: ProductionNodeRequest): ProductionNodeRequest {
    return {
      ...draft,
      machineCount: Math.max(0, Math.round(Number(draft.machineCount || 0))),
      clockPercent: Number(draft.clockPercent || 0),
      outputMultiplier: Number(draft.outputMultiplier || 0),
    };
  }

  recipeInputsText(recipeId: number): string {
    const recipe = this.recipeById(recipeId);
    return recipe?.inputs.map((item) => displayRecipeAmount(item)).join(' / ') || '-';
  }

  recipeOutputsText(recipeId: number): string {
    const recipe = this.recipeById(recipeId);
    return recipe?.outputs.map((item) => displayRecipeAmount(item)).join(' / ') || '-';
  }

  summarizeBucket(bucket: ProductionBucket): BucketSummary {
    const inputs = new Map<number, MaterialFlow>();
    const outputs = new Map<number, MaterialFlow>();
    let activeNodeCount = 0;
    for (const node of this.nodesByBucket[bucket.id] ?? []) {
      if (!node.enabled) continue;
      const recipe = this.recipeById(node.recipeId);
      if (!recipe) continue;
      activeNodeCount += 1;
      const inputFactor = (node.machineCount ?? 0) * ((node.clockPercent ?? 100) / 100);
      const outputFactor = inputFactor * (node.outputMultiplier ?? 1);
      for (const input of recipe.inputs) {
        const current = inputs.get(input.materialId) ?? { materialId: input.materialId, materialName: input.materialName, amount: 0 };
        current.amount += input.amountPerMinuteAt100Percent * inputFactor;
        inputs.set(input.materialId, current);
      }
      for (const output of recipe.outputs) {
        const current = outputs.get(output.materialId) ?? { materialId: output.materialId, materialName: output.materialName, amount: 0 };
        current.amount += output.amountPerMinuteAt100Percent * outputFactor;
        outputs.set(output.materialId, current);
      }
    }
    return {
      activeNodeCount,
      inputs: [...inputs.values()].sort((a, b) => b.amount - a.amount),
      outputs: [...outputs.values()].sort((a, b) => b.amount - a.amount),
    };
  }

  fmt(value: number | null | undefined, digits = 2): string {
    if (value === null || value === undefined || Number.isNaN(value)) return '-';
    if (Math.abs(value) < 0.000001) return '0';
    return String(Number(value.toFixed(digits)));
  }

  statusText(status: HealthStatus | null | undefined): string {
    if (status === 'GREEN') return '健康';
    if (status === 'YELLOW') return '警告';
    if (status === 'RED') return '缺口';
    return '未启用';
  }

  statusClass(status: HealthStatus | null | undefined): string {
    return `status-badge status-${(status ?? 'GRAY').toLowerCase()}`;
  }

  factoryTypeLabel(type: FactoryType): string {
    return this.factoryTypeOptions.find((item) => item.value === type)?.label ?? type;
  }

  materialLabel(material: Material): string { return `${displayMaterialName(material.name, material.gameKey)} · ${displayMaterialType(material.materialType)}`; }
  materialName(name?: string | null, gameKey?: string | null): string { return displayMaterialName(name, gameKey); }
  materialType(type?: MaterialType | null): string { return displayMaterialType(type); }
  machineName(name?: string | null): string { return displayMachineName(name); }
  recipeLabel(recipe?: Recipe | null): string { return displayRecipeName(recipe); }
  recipeAmount = displayRecipeAmount;
  transportAdvice = displayTransportAdvice;
  warningText = displayWarning;
  externalOption = displayExternalOption;
  connectionText = displayConnection;
  lineLikeName = translateLineLikeName;

  emptyFactoryDraft(): FactoryRequest { return { name: '', factoryType: 'MAIN', enabled: true, maxBeltLevel: 3, maxPipeLevel: 1, description: '' }; }
  emptyBucketDraft(): ProductionBucketRequest { return { name: '', enabled: false, description: '', collapsed: false, sortOrder: 0 }; }
  emptyNodeDraft(): ProductionNodeRequest { return { recipeId: 0, enabled: true, machineCount: 1, clockPercent: 100, outputMultiplier: 1, name: '' }; }

  factoryToRequest(factory: Factory): FactoryRequest {
    return { name: factory.name, factoryType: factory.factoryType, enabled: factory.enabled, maxBeltLevel: factory.maxBeltLevel ?? 3, maxPipeLevel: factory.maxPipeLevel ?? 1, description: factory.description ?? '' };
  }

  bucketToRequest(bucket: ProductionBucket): ProductionBucketRequest {
    return { name: bucket.name, enabled: bucket.enabled, description: bucket.description ?? '', positionX: bucket.positionX, positionY: bucket.positionY, collapsed: bucket.collapsed, sortOrder: bucket.sortOrder };
  }

  nodeToRequest(node: ProductionNode): ProductionNodeRequest {
    return { recipeId: node.recipeId, enabled: node.enabled, machineCount: node.machineCount, clockPercent: node.clockPercent, outputMultiplier: node.outputMultiplier, name: node.name ?? '', positionX: node.positionX, positionY: node.positionY, sortOrder: node.sortOrder };
  }

  get canvasWidth(): number {
    const lastLineRight = this.lineStartX + this.busLines.length * (this.lineColumnWidth + this.lineColumnGap) + 80;
    const bucketRight = this.buckets.reduce((max, bucket, index) => Math.max(max, this.bucketX(bucket, index) + this.bucketCardWidth + 220), 0);
    return Math.max(this.minCanvasWidth, lastLineRight, bucketRight);
  }

  get canvasHeight(): number {
    const bucketBottom = this.buckets.reduce((max, bucket, index) => Math.max(max, this.bucketY(bucket, index) + this.bucketCardHeight + 140), 0);
    return Math.max(this.minCanvasHeight, bucketBottom);
  }

  get scaledCanvasWidth(): number { return this.canvasWidth * this.canvasZoom; }
  get scaledCanvasHeight(): number { return this.canvasHeight * this.canvasZoom; }
  get lineCardTop(): number { return this.canvasHeight - this.lineCardHeight - this.lineBottomGap; }
  get pillarTop(): number { return this.pillarTopPadding; }
  get pillarHeight(): number { return Math.max(260, this.lineCardTop - this.pillarTop - 24); }
  get lineLabelTop(): number { return Math.max(16, this.lineCardTop - 34); }

  zoomIn(): void { this.canvasZoom = Math.min(1.8, Number((this.canvasZoom + 0.1).toFixed(2))); }
  zoomOut(): void { this.canvasZoom = Math.max(0.55, Number((this.canvasZoom - 0.1).toFixed(2))); }
  resetZoom(): void { this.canvasZoom = 1; }

  availableMaterialsForLine(type: MaterialType | null | undefined = this.newLineMaterialType): Material[] {
    const existingMaterialIds = new Set(this.busLines.map((line) => line.materialId));
    return this.materials.filter((material) => !existingMaterialIds.has(material.id) && (!type || material.materialType === type));
  }

  bucketX(bucket: ProductionBucket, index: number): number {
    const value = bucket.positionX;
    if (typeof value === 'number' && Number.isFinite(value)) return value;
    return 72 + (index % 2) * 320;
  }

  defaultBucketY(index: number): number {
    return Math.max(92, this.minCanvasHeight - 250 - index * 176);
  }

  bucketY(bucket: ProductionBucket, index: number): number {
    const value = bucket.positionY;
    if (typeof value === 'number' && Number.isFinite(value)) return value;
    return this.defaultBucketY(index);
  }

  lineX(line: BusLine, index: number): number {
    const baseX = this.lineStartX + index * (this.lineColumnWidth + this.lineColumnGap);
    if (this.lineDragState?.lineId === line.id) {
      return baseX + this.lineDragState.currentDx;
    }
    return baseX;
  }

  pillarX(line: BusLine, index: number): number {
    return this.lineX(line, index) + this.lineColumnWidth / 2;
  }

  lineMeterHeight(line: BusLine): number {
    const calc = this.getCalc(line.id);
    const advice = calc?.transportAdvice;
    if (!advice?.requiredThroughput) return 10;
    const base = advice.currentMaxCapacity || advice.requiredThroughput || 1;
    return Math.max(8, Math.min(100, (advice.requiredThroughput / base) * 100));
  }

  canvasConnections(): CanvasConnection[] {
    const result: CanvasConnection[] = [];
    const lineByMaterialId = new Map<number, { line: BusLine; index: number }>();
    this.busLines.forEach((line, index) => lineByMaterialId.set(line.materialId, { line, index }));

    this.buckets.forEach((bucket, bucketIndex) => {
      const summary = this.summarizeBucket(bucket);
      const bucketLeft = this.bucketX(bucket, bucketIndex);
      const bucketRight = bucketLeft + this.bucketCardWidth;
      const bucketCenter = bucketLeft + this.bucketCardWidth / 2;
      const bucketTop = this.bucketY(bucket, bucketIndex);

      summary.outputs.forEach((flow, outputIndex) => {
        const target = lineByMaterialId.get(flow.materialId);
        if (!target) return;
        const px = this.pillarX(target.line, target.index);
        const startX = px >= bucketCenter ? bucketRight : bucketLeft;
        const y = bucketTop + 28 + outputIndex * 18;
        result.push({ key: `out-${bucket.id}-${target.line.id}-${outputIndex}`, kind: 'output', d: `M ${startX} ${y} L ${px} ${y}` });
      });

      summary.inputs.forEach((flow, inputIndex) => {
        const source = lineByMaterialId.get(flow.materialId);
        if (!source) return;
        const px = this.pillarX(source.line, source.index);
        const endX = px >= bucketCenter ? bucketRight : bucketLeft;
        const y = bucketTop + this.bucketCardHeight - 30 - inputIndex * 18;
        result.push({ key: `in-${source.line.id}-${bucket.id}-${inputIndex}`, kind: 'input', d: `M ${px} ${y} L ${endX} ${y}` });
      });
    });

    return result;
  }

  startBucketDrag(bucket: ProductionBucket, index: number, event: PointerEvent): void {
    const target = event.target as HTMLElement | null;
    if (target?.closest('input,button,select,textarea,label')) return;
    (event.currentTarget as HTMLElement | null)?.setPointerCapture?.(event.pointerId);
    this.dragState = {
      bucketId: bucket.id,
      startPointerX: event.clientX,
      startPointerY: event.clientY,
      startX: this.bucketX(bucket, index),
      startY: this.bucketY(bucket, index),
      moved: false,
    };
  }

  moveBucketDrag(event: PointerEvent): void {
    if (!this.dragState) return;
    event.preventDefault();
    const dx = (event.clientX - this.dragState.startPointerX) / this.canvasZoom;
    const dy = (event.clientY - this.dragState.startPointerY) / this.canvasZoom;
    if (Math.abs(dx) > 4 || Math.abs(dy) > 4) this.dragState.moved = true;
    const nextX = Math.max(24, Math.min(this.canvasWidth - this.bucketCardWidth - 48, this.dragState.startX + dx));
    const nextY = Math.max(this.pillarTop + 70, this.dragState.startY + dy);
    this.buckets = this.buckets.map((bucket) =>
      bucket.id === this.dragState!.bucketId ? { ...bucket, positionX: nextX, positionY: nextY } : bucket,
    );
  }

  finishBucketDrag(): void {
    if (!this.dragState) return;
    const moved = this.dragState.moved;
    const bucket = this.buckets.find((item) => item.id === this.dragState?.bucketId);
    this.dragState = null;
    if (moved) {
      this.suppressBucketClick = true;
    }
    if (!bucket || !moved) return;
    void this.run(async () => {
      await this.api.updateBucket(bucket.id, this.bucketToRequest(bucket));
      await this.refreshCurrentFactory();
    });
  }

  startLineDrag(line: BusLine, index: number, event: PointerEvent): void {
    const target = event.target as HTMLElement | null;
    if (target?.closest('input,select,textarea,label')) return;
    event.preventDefault();
    event.stopPropagation();
    (event.currentTarget as HTMLElement | null)?.setPointerCapture?.(event.pointerId);
    this.lineDragState = { lineId: line.id, startPointerX: event.clientX, startIndex: index, currentDx: 0, moved: false };
  }

  moveLineDrag(event: PointerEvent): void {
    if (!this.lineDragState) return;
    event.preventDefault();
    event.stopPropagation();
    const rawDx = (event.clientX - this.lineDragState.startPointerX) / this.canvasZoom;
    const step = this.lineColumnWidth + this.lineColumnGap;
    const minDx = -this.lineDragState.startIndex * step;
    const maxDx = (this.busLines.length - 1 - this.lineDragState.startIndex) * step;
    const dx = Math.max(minDx, Math.min(maxDx, rawDx));
    this.lineDragState.currentDx = dx;
    if (Math.abs(dx) > 3) this.lineDragState.moved = true;
  }

  finishLineDrag(): void {
    if (!this.lineDragState) return;
    const state = this.lineDragState;
    const moved = state.moved;
    this.lineDragState = null;
    if (moved) {
      this.suppressLineClick = true;
    }
    if (!moved) return;

    const step = this.lineColumnWidth + this.lineColumnGap;
    const targetIndex = Math.max(0, Math.min(this.busLines.length - 1, state.startIndex + Math.round(state.currentDx / step)));
    const currentIndex = this.busLines.findIndex((line) => line.id === state.lineId);
    if (currentIndex < 0 || targetIndex === currentIndex) return;

    const next = [...this.busLines];
    const [item] = next.splice(currentIndex, 1);
    next.splice(targetIndex, 0, item);
    this.busLines = next;

    const ordered = this.busLines.map((line, index) => ({ line, sortOrder: index + 1 }));
    void this.run(async () => {
      await Promise.all(ordered.map(({ line, sortOrder }) => this.api.updateBusLine(line.id, { sortOrder })));
      await this.refreshCurrentFactory();
    }, '物资线顺序已保存');
  }

  trackConnection(_index: number, item: CanvasConnection): string { return item.key; }
  trackById(_index: number, item: { id: number }): number { return item.id; }
}
