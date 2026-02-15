import { z } from 'zod';
import { COLUMN_TYPES } from '@/api/record-schemas';

export const columnSchema = z.object({
  name: z
    .string()
    .min(1, 'Column name is required')
    .regex(/^[a-zA-Z][a-zA-Z0-9_]*$/, 'Must start with a letter and contain only letters, numbers, or underscores'),
  type: z.enum(COLUMN_TYPES),
  required: z.boolean(),
});

export const createSchemaFormSchema = z.object({
  name: z
    .string()
    .min(1, 'Schema name is required')
    .regex(/^[a-zA-Z][a-zA-Z0-9_]*$/, 'Must start with a letter and contain only letters, numbers, or underscores'),
  columns: z.array(columnSchema).min(1, 'At least one column is required'),
  agentOperations: z.object({
    createEnabled: z.boolean(),
    updateEnabled: z.boolean(),
    deleteEnabled: z.boolean(),
  }),
});

export type CreateSchemaFormValues = z.infer<typeof createSchemaFormSchema>;

export const defaultValues: CreateSchemaFormValues = {
  name: '',
  columns: [{ name: '', type: 'TEXT', required: false }],
  agentOperations: {
    createEnabled: true,
    updateEnabled: true,
    deleteEnabled: true,
  },
};
