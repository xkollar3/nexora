import { useFormContext } from 'react-hook-form';
import { GripVertical, Trash2 } from 'lucide-react';
import { Input } from '@/components/ui/input';
import { Button } from '@/components/ui/button';
import { Switch } from '@/components/ui/switch';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select';
import { COLUMN_TYPES } from '@/api/record-schemas';
import type { CreateSchemaFormValues } from './schema';

const TYPE_LABELS: Record<string, string> = {
  TEXT: 'Text',
  NUMBER: 'Number',
  BOOLEAN: 'Boolean',
  DATE: 'Date',
  REFERENCE: 'Reference',
};

interface ColumnRowProps {
  index: number;
  onRemove: () => void;
  canRemove: boolean;
}

export function ColumnRow({ index, onRemove, canRemove }: ColumnRowProps) {
  const {
    register,
    setValue,
    watch,
    formState: { errors },
  } = useFormContext<CreateSchemaFormValues>();

  const columnType = watch(`columns.${index}.type`);
  const columnRequired = watch(`columns.${index}.required`);
  const columnErrors = errors.columns?.[index];

  return (
    <div className="group grid grid-cols-[16px_1fr_140px_60px_36px] items-center gap-3">
      <GripVertical className="w-4 h-4 text-muted-foreground/40" />

      <div>
        <Input
          placeholder="Column name"
          {...register(`columns.${index}.name`)}
          aria-invalid={!!columnErrors?.name}
          className={columnErrors?.name ? 'border-destructive' : ''}
        />
        {columnErrors?.name && (
          <p className="text-xs text-destructive mt-1">
            {columnErrors.name.message}
          </p>
        )}
      </div>

      <Select
        value={columnType}
        onValueChange={(val) =>
          setValue(`columns.${index}.type`, val as CreateSchemaFormValues['columns'][number]['type'], {
            shouldValidate: true,
          })
        }
      >
        <SelectTrigger className="w-full">
          <SelectValue />
        </SelectTrigger>
        <SelectContent>
          {COLUMN_TYPES.map((type) => (
            <SelectItem key={type} value={type}>
              {TYPE_LABELS[type]}
            </SelectItem>
          ))}
        </SelectContent>
      </Select>

      <div className="flex items-center justify-center">
        <Switch
          checked={columnRequired}
          onCheckedChange={(checked) =>
            setValue(`columns.${index}.required`, !!checked, {
              shouldValidate: true,
            })
          }
        />
      </div>

      <Button
        type="button"
        variant="ghost"
        size="icon-xs"
        onClick={onRemove}
        disabled={!canRemove}
        className="text-muted-foreground hover:text-destructive"
      >
        <Trash2 className="w-3.5 h-3.5" />
      </Button>
    </div>
  );
}
