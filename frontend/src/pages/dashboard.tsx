import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Plus } from 'lucide-react';
import { useRecordSchemas } from '@/api/record-schemas';
import { EmptyState } from '@/pages/empty-state';
import { SchemaTabs } from '@/pages/schema-tabs';
import { RecordsTable } from '@/pages/records-table';
import { Button } from '@/components/ui/button';
import { Skeleton } from '@/components/ui/skeleton';

export function DashboardPage() {
  const navigate = useNavigate();
  const { data: schemas, isLoading } = useRecordSchemas();
  const [activeSchema, setActiveSchema] = useState<string | null>(null);

  if (isLoading) {
    return (
      <div className="px-6 py-6 space-y-4">
        <Skeleton className="h-9 w-64" />
        <Skeleton className="h-[400px] w-full" />
      </div>
    );
  }

  if (!schemas || schemas.length === 0) {
    return <EmptyState />;
  }

  const selected = activeSchema ?? schemas[0].name;
  const schema = schemas.find((s) => s.name === selected) ?? schemas[0];

  return (
    <div className="flex flex-col h-[calc(100vh-49px)]">
      <div className="border-b bg-muted/30">
        <div className="flex items-center justify-between px-6">
          <SchemaTabs
            schemas={schemas}
            active={schema.name}
            onSelect={setActiveSchema}
          />
          <Button
            size="sm"
            variant="outline"
            className="gap-1.5 shrink-0"
            onClick={() => navigate('/schemas/new')}
          >
            <Plus className="w-3.5 h-3.5" />
            New schema
          </Button>
        </div>
      </div>

      <div className="flex-1 overflow-hidden">
        <RecordsTable key={schema.name} schema={schema} />
      </div>
    </div>
  );
}
