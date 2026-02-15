import { useNavigate } from 'react-router-dom';
import { Database, Plus, ArrowRight } from 'lucide-react';
import { Button } from '@/components/ui/button';

export function EmptyState() {
  const navigate = useNavigate();

  return (
    <div className="flex items-center justify-center min-h-[70vh]">
      <div className="flex flex-col items-center text-center max-w-md space-y-6">
        <div className="flex items-center justify-center w-16 h-16 rounded-2xl bg-primary/10 border border-primary/20">
          <Database className="w-8 h-8 text-primary" />
        </div>

        <div className="space-y-2">
          <h2 className="text-2xl font-semibold tracking-tight">
            No schemas yet
          </h2>
          <p className="text-muted-foreground leading-relaxed">
            Schemas define the structure of your data. Create your first one to
            start managing records.
          </p>
        </div>

        <Button size="lg" className="gap-2" onClick={() => navigate('/schemas/new')}>
          <Plus className="w-4 h-4" />
          Create schema
        </Button>

        <div className="flex items-center gap-4 pt-2 text-sm text-muted-foreground">
          <Step number={1} label="Define columns" />
          <ArrowRight className="w-3 h-3 shrink-0" />
          <Step number={2} label="Configure agent" />
          <ArrowRight className="w-3 h-3 shrink-0" />
          <Step number={3} label="Manage records" />
        </div>
      </div>
    </div>
  );
}

function Step({ number, label }: { number: number; label: string }) {
  return (
    <span className="flex items-center gap-1.5">
      <span className="flex items-center justify-center w-5 h-5 rounded-full bg-muted text-xs font-medium">
        {number}
      </span>
      {label}
    </span>
  );
}
