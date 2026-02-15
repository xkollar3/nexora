import { cn } from '@/lib/utils';
import type { RecordSchema } from '@/api/record-schemas';

interface SchemaTabsProps {
  schemas: RecordSchema[];
  active: string;
  onSelect: (name: string) => void;
}

export function SchemaTabs({ schemas, active, onSelect }: SchemaTabsProps) {
  return (
    <div className="flex items-center gap-0 overflow-x-auto" role="tablist">
      {schemas.map((schema) => (
        <button
          key={schema.id}
          role="tab"
          aria-selected={schema.name === active}
          onClick={() => onSelect(schema.name)}
          className={cn(
            'relative px-4 py-2.5 text-sm font-medium transition-colors whitespace-nowrap',
            'hover:text-foreground',
            schema.name === active
              ? 'text-foreground'
              : 'text-muted-foreground',
          )}
        >
          {schema.name}
          {schema.name === active && (
            <span className="absolute inset-x-0 bottom-0 h-0.5 bg-primary rounded-full" />
          )}
        </button>
      ))}
    </div>
  );
}
