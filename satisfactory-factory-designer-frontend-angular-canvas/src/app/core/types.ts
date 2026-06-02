export type FactoryType = 'MAIN' | 'SUB' | 'HUB' | 'TEMP';
export type HealthStatus = 'GREEN' | 'YELLOW' | 'RED' | 'GRAY';
export type MaterialType = 'SOLID' | 'FLUID' | 'GAS';
export type TransportType = 'BELT' | 'PIPE';
export type MachineType =
  | 'MINER'
  | 'SMELTER'
  | 'FOUNDRY'
  | 'CONSTRUCTOR'
  | 'ASSEMBLER'
  | 'MANUFACTURER'
  | 'REFINERY'
  | 'BLENDER'
  | 'PARTICLE_ACCELERATOR'
  | 'PACKAGER'
  | 'WATER_EXTRACTOR'
  | 'OTHER';

export interface Factory {
  id: number;
  name: string;
  factoryType: FactoryType;
  enabled: boolean;
  maxBeltLevel: number | null;
  maxPipeLevel: number | null;
  description: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface FactoryRequest {
  name: string;
  factoryType: FactoryType;
  enabled: boolean;
  maxBeltLevel: number;
  maxPipeLevel: number;
  description?: string | null;
}

export interface Material {
  id: number;
  gameKey: string;
  name: string;
  materialType: MaterialType;
  stackSize: number | null;
  sinkable: boolean;
  enabled: boolean;
  description: string | null;
}

export interface Machine {
  id: number;
  gameKey: string;
  name: string;
  machineType: MachineType;
  powerMw: number | null;
  enabled: boolean;
}

export interface RecipeMaterialAmount {
  materialId: number;
  materialName: string;
  materialGameKey: string;
  amountPerCycle: number;
  amountPerMinuteAt100Percent: number;
}

export interface Recipe {
  id: number;
  gameKey: string;
  name: string;
  machineId: number | null;
  machineName: string | null;
  cycleTimeSeconds: number;
  alternate: boolean;
  source: string;
  gameVersion: string;
  enabled: boolean;
  inputs: RecipeMaterialAmount[];
  outputs: RecipeMaterialAmount[];
}

export interface ProductionBucket {
  id: number;
  factoryId: number;
  name: string;
  enabled: boolean;
  description: string | null;
  positionX: number | null;
  positionY: number | null;
  collapsed: boolean;
  sortOrder: number | null;
}

export interface ProductionBucketRequest {
  name: string;
  enabled: boolean;
  description?: string | null;
  positionX?: number | null;
  positionY?: number | null;
  collapsed?: boolean;
  sortOrder?: number | null;
}

export interface ProductionNode {
  id: number;
  bucketId: number;
  recipeId: number;
  recipeName: string;
  enabled: boolean;
  machineCount: number;
  clockPercent: number;
  outputMultiplier: number;
  name: string | null;
  positionX: number | null;
  positionY: number | null;
  sortOrder: number | null;
}

export interface ProductionNodeRequest {
  recipeId: number;
  enabled: boolean;
  machineCount: number;
  clockPercent: number;
  outputMultiplier: number;
  name?: string | null;
  positionX?: number | null;
  positionY?: number | null;
  sortOrder?: number | null;
}

export interface BusLine {
  id: number;
  factoryId: number;
  materialId: number;
  materialName: string;
  materialType: MaterialType;
  name: string;
  description: string | null;
  offsetAmount: number;
  visibleToOtherFactories: boolean;
  externalEnabled: boolean;
  sortOrder: number | null;
  collapsed: boolean;
  createdManually: boolean;
}

export interface BusLineRequest {
  materialId: number;
  name?: string | null;
  description?: string | null;
  offsetAmount?: number | null;
  visibleToOtherFactories?: boolean;
  externalEnabled?: boolean;
  sortOrder?: number | null;
  collapsed?: boolean;
  createdManually?: boolean;
}

export interface BusLinePatchRequest {
  name?: string | null;
  description?: string | null;
  offsetAmount?: number | null;
  visibleToOtherFactories?: boolean;
  externalEnabled?: boolean;
  sortOrder?: number | null;
  collapsed?: boolean;
}

export interface Contribution {
  sourceId: number;
  sourceName: string;
  amount: number;
  type: string;
}

export interface TransportAdvice {
  transportType: TransportType;
  requiredThroughput: number;
  currentMaxLevel: number | null;
  currentMaxCapacity: number | null;
  recommendedLevel: number | null;
  recommendedName: string | null;
  message: string;
  overCurrentMax: boolean;
}

export interface BusLineCalculation {
  busLineId: number;
  materialId: number;
  materialName: string;
  materialType: MaterialType;
  lineName: string;
  localOutput: number;
  localDemand: number;
  externalInput: number;
  offset: number;
  net: number;
  status: HealthStatus;
  warnings: string[];
  producers: Contribution[];
  consumers: Contribution[];
  externalSources: Contribution[];
  transportAdvice: TransportAdvice | null;
  externalEnabled: boolean;
  visibleToOtherFactories: boolean;
  connectedAsSource: boolean;
}

export interface FactoryCalculation {
  factoryId: number;
  factoryName: string;
  enabled: boolean;
  overallStatus: HealthStatus;
  warnings: string[];
  busLines: BusLineCalculation[];
}

export interface ExternalSourceOption {
  sourceBusLineId: number;
  sourceFactoryId: number;
  sourceFactoryName: string;
  sourceLineName: string;
  displayName: string;
  availableAmount: number;
}

export interface ExternalConnection {
  id: number;
  sourceBusLineId: number;
  sourceFactoryName: string;
  sourceLineName: string;
  targetBusLineId: number;
  targetFactoryName: string;
  targetLineName: string;
  enabled: boolean;
  createdAt: string;
}

export interface Snapshot {
  id: number;
  factoryId: number;
  name: string;
  snapshotJson: string;
  createdAt: string;
}

export interface ApiMessage {
  message: string;
}
