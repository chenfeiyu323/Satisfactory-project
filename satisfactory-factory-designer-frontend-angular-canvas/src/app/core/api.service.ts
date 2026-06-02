import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { firstValueFrom } from 'rxjs';
import { environment } from '../../environments/environment';
import type {
  ApiMessage,
  BusLine,
  BusLinePatchRequest,
  BusLineRequest,
  ExternalConnection,
  ExternalSourceOption,
  Factory,
  FactoryCalculation,
  FactoryRequest,
  Machine,
  Material,
  ProductionBucket,
  ProductionBucketRequest,
  ProductionNode,
  ProductionNodeRequest,
  Recipe,
  Snapshot,
} from './types';

@Injectable({ providedIn: 'root' })
export class ApiService {
  readonly baseUrl = environment.apiBaseUrl || '/api';
  private readonly headers = new HttpHeaders({ 'Content-Type': 'application/json' });

  constructor(private readonly http: HttpClient) {}

  private get<T>(path: string): Promise<T> {
    return firstValueFrom(this.http.get<T>(`${this.baseUrl}${path}`));
  }

  private post<T>(path: string, body?: unknown): Promise<T> {
    return firstValueFrom(this.http.post<T>(`${this.baseUrl}${path}`, body ?? {}, { headers: this.headers }));
  }

  private patch<T>(path: string, body: unknown): Promise<T> {
    return firstValueFrom(this.http.patch<T>(`${this.baseUrl}${path}`, body, { headers: this.headers }));
  }

  private delete<T>(path: string): Promise<T> {
    return firstValueFrom(this.http.delete<T>(`${this.baseUrl}${path}`));
  }

  seedAll(): Promise<ApiMessage> { return this.post<ApiMessage>('/admin/seed/all'); }

  materials(): Promise<Material[]> { return this.get<Material[]>('/materials'); }
  machines(): Promise<Machine[]> { return this.get<Machine[]>('/machines'); }
  recipes(): Promise<Recipe[]> { return this.get<Recipe[]>('/recipes'); }

  factories(): Promise<Factory[]> { return this.get<Factory[]>('/factories'); }
  factory(id: number): Promise<Factory> { return this.get<Factory>(`/factories/${id}`); }
  createFactory(body: FactoryRequest): Promise<Factory> { return this.post<Factory>('/factories', body); }
  updateFactory(id: number, body: FactoryRequest): Promise<Factory> { return this.patch<Factory>(`/factories/${id}`, body); }
  deleteFactory(id: number): Promise<ApiMessage> { return this.delete<ApiMessage>(`/factories/${id}`); }
  copyFactory(id: number): Promise<Factory> { return this.post<Factory>(`/factories/${id}/copy`); }
  snapshotFactory(id: number, name: string): Promise<Snapshot> { return this.post<Snapshot>(`/factories/${id}/snapshots`, { name }); }
  calculation(id: number): Promise<FactoryCalculation> { return this.get<FactoryCalculation>(`/factories/${id}/calculation`); }

  buckets(factoryId: number): Promise<ProductionBucket[]> { return this.get<ProductionBucket[]>(`/factories/${factoryId}/buckets`); }
  createBucket(factoryId: number, body: ProductionBucketRequest): Promise<ProductionBucket> { return this.post<ProductionBucket>(`/factories/${factoryId}/buckets`, body); }
  updateBucket(bucketId: number, body: ProductionBucketRequest): Promise<ProductionBucket> { return this.patch<ProductionBucket>(`/buckets/${bucketId}`, body); }
  deleteBucket(bucketId: number): Promise<ApiMessage> { return this.delete<ApiMessage>(`/buckets/${bucketId}`); }

  nodes(bucketId: number): Promise<ProductionNode[]> { return this.get<ProductionNode[]>(`/buckets/${bucketId}/nodes`); }
  createNode(bucketId: number, body: ProductionNodeRequest): Promise<ProductionNode> { return this.post<ProductionNode>(`/buckets/${bucketId}/nodes`, body); }
  updateNode(nodeId: number, body: ProductionNodeRequest): Promise<ProductionNode> { return this.patch<ProductionNode>(`/nodes/${nodeId}`, body); }
  deleteNode(nodeId: number): Promise<ApiMessage> { return this.delete<ApiMessage>(`/nodes/${nodeId}`); }

  busLines(factoryId: number): Promise<BusLine[]> { return this.get<BusLine[]>(`/factories/${factoryId}/bus-lines`); }
  createBusLine(factoryId: number, body: BusLineRequest): Promise<BusLine> { return this.post<BusLine>(`/factories/${factoryId}/bus-lines`, body); }
  updateBusLine(busLineId: number, body: BusLinePatchRequest): Promise<BusLine> { return this.patch<BusLine>(`/bus-lines/${busLineId}`, body); }
  deleteBusLine(busLineId: number): Promise<ApiMessage> { return this.delete<ApiMessage>(`/bus-lines/${busLineId}`); }

  availableExternalSources(targetBusLineId: number): Promise<ExternalSourceOption[]> { return this.get<ExternalSourceOption[]>(`/bus-lines/${targetBusLineId}/available-external-sources`); }
  createExternalConnection(sourceBusLineId: number, targetBusLineId: number): Promise<ExternalConnection> { return this.post<ExternalConnection>('/external-connections', { sourceBusLineId, targetBusLineId }); }
  externalConnectionsIntoFactory(factoryId: number): Promise<ExternalConnection[]> { return this.get<ExternalConnection[]>(`/factories/${factoryId}/external-connections`); }
  deleteExternalConnection(connectionId: number): Promise<ApiMessage> { return this.delete<ApiMessage>(`/external-connections/${connectionId}`); }
}
