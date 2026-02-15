import { useNavigate } from 'react-router-dom';
import { useForm, useFieldArray, FormProvider } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { ArrowLeft, Plus, Loader2 } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Switch } from '@/components/ui/switch';
import { Separator } from '@/components/ui/separator';
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from '@/components/ui/card';
import { useCreateRecordSchema } from '@/api/record-schemas';
import { ColumnRow } from './column-row';
import {
  createSchemaFormSchema,
  defaultValues,
  type CreateSchemaFormValues,
} from './schema';

export function CreateSchemaPage() {
  const navigate = useNavigate();
  const createSchema = useCreateRecordSchema();

  const form = useForm<CreateSchemaFormValues>({
    resolver: zodResolver(createSchemaFormSchema),
    defaultValues,
  });

  const { fields, append, remove } = useFieldArray({
    control: form.control,
    name: 'columns',
  });

  const onSubmit = (values: CreateSchemaFormValues) => {
    createSchema.mutate(values, {
      onSuccess: () => navigate('/'),
    });
  };

  return (
    <div className="max-w-2xl mx-auto py-10 px-6">
      <button
        type="button"
        onClick={() => navigate('/')}
        className="flex items-center gap-1.5 text-sm text-muted-foreground hover:text-foreground transition-colors mb-6"
      >
        <ArrowLeft className="w-4 h-4" />
        Back
      </button>

      <div className="space-y-1 mb-8">
        <h1 className="text-2xl font-semibold tracking-tight">
          Create schema
        </h1>
        <p className="text-sm text-muted-foreground">
          Define the structure of your data and configure agent permissions.
        </p>
      </div>

      <FormProvider {...form}>
        <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-8">
          {/* Schema name */}
          <div className="space-y-2">
            <Label htmlFor="name">Name</Label>
            <Input
              id="name"
              placeholder="e.g. Customers, Invoices, Products"
              {...form.register('name')}
              aria-invalid={!!form.formState.errors.name}
              className={form.formState.errors.name ? 'border-destructive' : ''}
            />
            {form.formState.errors.name && (
              <p className="text-xs text-destructive">
                {form.formState.errors.name.message}
              </p>
            )}
          </div>

          {/* Columns */}
          <Card>
            <CardHeader>
              <CardTitle className="text-base">Columns</CardTitle>
              <CardDescription>
                Define the fields that each record will contain.
              </CardDescription>
            </CardHeader>
            <CardContent className="space-y-4">
              {/* Column header */}
              <div className="grid grid-cols-[16px_1fr_140px_60px_36px] items-center gap-3 text-xs font-medium text-muted-foreground">
                <div />
                <div>Name</div>
                <div>Type</div>
                <div className="text-center">Required</div>
                <div />
              </div>

              <Separator />

              {/* Column rows */}
              <div className="space-y-3">
                {fields.map((field, index) => (
                  <ColumnRow
                    key={field.id}
                    index={index}
                    onRemove={() => remove(index)}
                    canRemove={fields.length > 1}
                  />
                ))}
              </div>

              {form.formState.errors.columns?.root && (
                <p className="text-xs text-destructive">
                  {form.formState.errors.columns.root.message}
                </p>
              )}

              <Button
                type="button"
                variant="outline"
                size="sm"
                className="gap-1.5"
                onClick={() =>
                  append({ name: '', type: 'TEXT', required: false })
                }
              >
                <Plus className="w-3.5 h-3.5" />
                Add column
              </Button>
            </CardContent>
          </Card>

          {/* Agent operations */}
          <Card>
            <CardHeader>
              <CardTitle className="text-base">Agent permissions</CardTitle>
              <CardDescription>
                Choose which operations the AI agent can perform on records.
              </CardDescription>
            </CardHeader>
            <CardContent className="space-y-4">
              <AgentToggle
                label="Create"
                description="Agent can insert new records"
                checked={form.watch('agentOperations.createEnabled')}
                onCheckedChange={(v) =>
                  form.setValue('agentOperations.createEnabled', v)
                }
              />
              <Separator />
              <AgentToggle
                label="Update"
                description="Agent can modify existing records"
                checked={form.watch('agentOperations.updateEnabled')}
                onCheckedChange={(v) =>
                  form.setValue('agentOperations.updateEnabled', v)
                }
              />
              <Separator />
              <AgentToggle
                label="Delete"
                description="Agent can remove records"
                checked={form.watch('agentOperations.deleteEnabled')}
                onCheckedChange={(v) =>
                  form.setValue('agentOperations.deleteEnabled', v)
                }
              />
            </CardContent>
          </Card>

          {/* Submit */}
          {createSchema.isError && (
            <p className="text-sm text-destructive">
              Failed to create schema. Please try again.
            </p>
          )}

          <div className="flex items-center justify-end gap-3 pt-2">
            <Button
              type="button"
              variant="outline"
              onClick={() => navigate('/')}
            >
              Cancel
            </Button>
            <Button type="submit" disabled={createSchema.isPending}>
              {createSchema.isPending && (
                <Loader2 className="w-4 h-4 animate-spin" />
              )}
              Create schema
            </Button>
          </div>
        </form>
      </FormProvider>
    </div>
  );
}

function AgentToggle({
  label,
  description,
  checked,
  onCheckedChange,
}: {
  label: string;
  description: string;
  checked: boolean;
  onCheckedChange: (value: boolean) => void;
}) {
  return (
    <div className="flex items-center justify-between">
      <div className="space-y-0.5">
        <p className="text-sm font-medium">{label}</p>
        <p className="text-xs text-muted-foreground">{description}</p>
      </div>
      <Switch checked={checked} onCheckedChange={onCheckedChange} />
    </div>
  );
}
