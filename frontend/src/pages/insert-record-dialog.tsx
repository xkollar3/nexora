import { useState, useMemo } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { Plus, Loader2 } from 'lucide-react';
import type { RecordSchema, ColumnDefinition } from '@/api/record-schemas';
import { useInsertRecord } from '@/api/record-schemas';
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogFooter,
  DialogTrigger,
} from '@/components/ui/dialog';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Switch } from '@/components/ui/switch';

function buildZodSchema(columns: ColumnDefinition[]) {
  const shape: Record<string, z.ZodType> = {};

  for (const col of columns) {
    let field: z.ZodType;

    switch (col.type) {
      case 'NUMBER':
        field = z.coerce.number();
        break;
      case 'BOOLEAN':
        field = z.boolean();
        break;
      default:
        field = z.string();
        break;
    }

    if (!col.required && col.type !== 'BOOLEAN') {
      field = field.optional();
    }

    if (col.required && col.type !== 'BOOLEAN' && col.type !== 'NUMBER') {
      field = z.string().min(1, `${col.name} is required`);
    }

    shape[col.name] = field;
  }

  return z.object(shape);
}

function getDefaults(columns: ColumnDefinition[]): Record<string, unknown> {
  const defaults: Record<string, unknown> = {};
  for (const col of columns) {
    defaults[col.name] = col.type === 'BOOLEAN' ? false : '';
  }
  return defaults;
}

interface InsertRecordDialogProps {
  schema: RecordSchema;
}

export function InsertRecordDialog({ schema }: InsertRecordDialogProps) {
  const [open, setOpen] = useState(false);
  const insertRecord = useInsertRecord(schema.name);

  const zodSchema = useMemo(() => buildZodSchema(schema.columns), [schema.columns]);
  const defaults = useMemo(() => getDefaults(schema.columns), [schema.columns]);

  const {
    register,
    handleSubmit,
    reset,
    setValue,
    watch,
    formState: { errors },
  } = useForm({
    resolver: zodResolver(zodSchema),
    defaultValues: defaults,
  });

  const onSubmit = (data: Record<string, unknown>) => {
    const fields: Record<string, unknown> = {};
    for (const col of schema.columns) {
      const val = data[col.name];
      if (val !== '' && val !== undefined) {
        fields[col.name] = val;
      }
    }

    insertRecord.mutate(fields, {
      onSuccess: () => {
        setOpen(false);
        reset(defaults);
      },
    });
  };

  const handleOpenChange = (next: boolean) => {
    setOpen(next);
    if (!next) {
      reset(defaults);
      insertRecord.reset();
    }
  };

  return (
    <Dialog open={open} onOpenChange={handleOpenChange}>
      <DialogTrigger asChild>
        <Button size="sm" className="gap-1.5">
          <Plus className="w-3.5 h-3.5" />
          Add record
        </Button>
      </DialogTrigger>
      <DialogContent className="max-h-[80vh] overflow-y-auto">
        <DialogHeader>
          <DialogTitle>Add record to {schema.name}</DialogTitle>
        </DialogHeader>

        <form onSubmit={handleSubmit(onSubmit)} className="grid gap-4">
          {schema.columns.map((col) => (
            <div key={col.name} className="grid gap-1.5">
              <Label htmlFor={col.name}>
                {col.name}
                {col.required && <span className="text-destructive ml-0.5">*</span>}
              </Label>

              {col.type === 'BOOLEAN' ? (
                <Switch
                  id={col.name}
                  checked={!!watch(col.name)}
                  onCheckedChange={(checked: boolean) => setValue(col.name, checked)}
                />
              ) : (
                <Input
                  id={col.name}
                  type={col.type === 'NUMBER' ? 'number' : col.type === 'DATE' ? 'date' : 'text'}
                  placeholder={col.type === 'REFERENCE' ? 'UUID' : ''}
                  {...register(col.name)}
                  className="font-mono text-sm"
                />
              )}

              {errors[col.name] && (
                <p className="text-xs text-destructive">
                  {errors[col.name]?.message as string}
                </p>
              )}
            </div>
          ))}

          {insertRecord.isError && (
            <p className="text-xs text-destructive">
              Failed to insert record. Please try again.
            </p>
          )}

          <DialogFooter>
            <Button type="submit" disabled={insertRecord.isPending}>
              {insertRecord.isPending && <Loader2 className="w-3.5 h-3.5 animate-spin mr-1.5" />}
              Insert
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
}
