import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { apiClient } from './client';

export const COLUMN_TYPES = ['TEXT', 'NUMBER', 'BOOLEAN', 'DATE', 'REFERENCE'] as const;
export type ColumnType = (typeof COLUMN_TYPES)[number];

export interface ColumnDefinition {
  name: string;
  type: ColumnType;
  required: boolean;
}

export interface AgentOperations {
  createEnabled: boolean;
  updateEnabled: boolean;
  deleteEnabled: boolean;
}

export interface RecordSchema {
  id: string;
  name: string;
  columns: ColumnDefinition[];
  agentOperations: AgentOperations;
  agentSystemPrompt: string | null;
  additionalContext: string | null;
}

export interface CreateRecordSchemaRequest {
  name: string;
  columns: ColumnDefinition[];
  agentOperations: AgentOperations;
}

export interface CreateRecordSchemaResponse {
  id: string;
}

export function useRecordSchemas() {
  return useQuery({
    queryKey: ['record-schemas'],
    queryFn: async () => {
      const { data } = await apiClient.get<RecordSchema[]>('/record-schemas');
      return data;
    },
  });
}

// --- Records ---

export interface RecordEntry {
  id: string;
  fields: Record<string, unknown>;
}

export interface PagedRecords {
  content: RecordEntry[];
  number: number;
  size: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
  empty: boolean;
}

export function useRecords(
  schemaName: string | null,
  page: number,
  size: number,
  filter: string,
) {
  return useQuery({
    queryKey: ['records', schemaName, page, size, filter],
    queryFn: async () => {
      const { data } = await apiClient.get<PagedRecords>(
        `/records/${schemaName}`,
        { params: { page, size, filter: filter || '{}' } },
      );
      return data;
    },
    enabled: !!schemaName,
  });
}

// --- Mutations ---

export function useCreateRecordSchema() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async (request: CreateRecordSchemaRequest) => {
      const { data } = await apiClient.post<CreateRecordSchemaResponse>(
        '/record-schemas',
        request,
      );
      return data;
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['record-schemas'] });
    },
  });
}
