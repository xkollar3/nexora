import { useState } from 'react';
import {
  ChevronLeft,
  ChevronRight,
  ChevronsLeft,
  ChevronsRight,
  Search,
  Loader2,
  Inbox,
  Trash2,
} from 'lucide-react';
import { useRecords, useDeleteRecord, type RecordSchema } from '@/api/record-schemas';
import { InsertRecordDialog } from '@/pages/insert-record-dialog';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/ui/table';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Badge } from '@/components/ui/badge';
import { Skeleton } from '@/components/ui/skeleton';

const PAGE_SIZE = 20;

function edgePad(index: number, total: number): string {
  if (total === 1) return 'pl-6 pr-6';
  if (index === 0) return 'pl-6';
  if (index === total - 1) return 'pr-6';
  return '';
}

interface RecordsTableProps {
  schema: RecordSchema;
}

export function RecordsTable({ schema }: RecordsTableProps) {
  const [page, setPage] = useState(0);
  const [filterInput, setFilterInput] = useState('');
  const [appliedFilter, setAppliedFilter] = useState('');

  const { data, isLoading, isFetching } = useRecords(
    schema.name,
    page,
    PAGE_SIZE,
    appliedFilter,
  );

  const deleteRecord = useDeleteRecord(schema.name);

  const handleFilterSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    setPage(0);
    setAppliedFilter(filterInput);
  };

  const columns = schema.columns;

  return (
    <div className="flex flex-col h-full">
      {/* Toolbar */}
      <div className="flex items-center gap-3 px-6 py-3 border-b">
        <form onSubmit={handleFilterSubmit} className="flex items-center gap-2 flex-1 max-w-lg">
          <div className="relative flex-1">
            <Search className="absolute left-2.5 top-1/2 -translate-y-1/2 w-3.5 h-3.5 text-muted-foreground" />
            <Input
              placeholder='MongoDB filter, e.g. {"status": "active"}'
              value={filterInput}
              onChange={(e) => setFilterInput(e.target.value)}
              className="pl-8 h-8 text-sm font-mono"
            />
          </div>
          <Button type="submit" size="sm" variant="secondary">
            Apply
          </Button>
          {appliedFilter && (
            <Button
              type="button"
              size="sm"
              variant="ghost"
              onClick={() => {
                setFilterInput('');
                setAppliedFilter('');
                setPage(0);
              }}
            >
              Clear
            </Button>
          )}
        </form>

        {isFetching && !isLoading && (
          <Loader2 className="w-4 h-4 animate-spin text-muted-foreground" />
        )}

        <InsertRecordDialog schema={schema} />
      </div>

      {/* Table */}
      <div className="flex-1 overflow-auto">
        <Table>
          <TableHeader>
            <TableRow>
              {columns.map((col, i) => (
                <TableHead key={col.name} className={edgePad(i, columns.length)}>
                  <div className="flex items-center gap-1.5">
                    {col.name}
                    <Badge variant="secondary" className="text-[10px] font-normal px-1 py-0">
                      {col.type.toLowerCase()}
                    </Badge>
                  </div>
                </TableHead>
              ))}
              <TableHead className="w-10 pr-6" />
            </TableRow>
          </TableHeader>
          <TableBody>
            {isLoading ? (
              Array.from({ length: 8 }).map((_, i) => (
                <TableRow key={i}>
                  {columns.map((col, j) => (
                    <TableCell key={col.name} className={edgePad(j, columns.length)}>
                      <Skeleton className="h-5 w-full" />
                    </TableCell>
                  ))}
                  <TableCell className="pr-6" />
                </TableRow>
              ))
            ) : !data || data.empty ? (
              <TableRow>
                <TableCell colSpan={columns.length + 1} className="h-48 pl-6 pr-6">
                  <EmptyRecords hasFilter={!!appliedFilter} />
                </TableCell>
              </TableRow>
            ) : (
              data.content.map((record) => (
                <TableRow key={record.id}>
                  {columns.map((col, j) => (
                    <TableCell key={col.name} className={`font-mono text-sm ${edgePad(j, columns.length)}`}>
                      {formatValue(record.fields[col.name])}
                    </TableCell>
                  ))}
                  <TableCell className="pr-6">
                    <Button
                      size="icon-xs"
                      variant="ghost"
                      onClick={() => deleteRecord.mutate(record.id)}
                    >
                      <Trash2 className="w-3.5 h-3.5 text-muted-foreground" />
                    </Button>
                  </TableCell>
                </TableRow>
              ))
            )}
          </TableBody>
        </Table>
      </div>

      {/* Pagination */}
      {data && !data.empty && (
        <div className="flex items-center justify-between px-6 py-3 border-t text-sm text-muted-foreground">
          <span>
            {data.totalElements} record{data.totalElements !== 1 && 's'}
            {data.totalPages > 1 &&
              ` \u00b7 Page ${data.number + 1} of ${data.totalPages}`}
          </span>

          {data.totalPages > 1 && (
            <div className="flex items-center gap-1">
              <Button
                size="icon-xs"
                variant="outline"
                disabled={data.first}
                onClick={() => setPage(0)}
              >
                <ChevronsLeft className="w-3.5 h-3.5" />
              </Button>
              <Button
                size="icon-xs"
                variant="outline"
                disabled={data.first}
                onClick={() => setPage((p) => Math.max(0, p - 1))}
              >
                <ChevronLeft className="w-3.5 h-3.5" />
              </Button>
              <Button
                size="icon-xs"
                variant="outline"
                disabled={data.last}
                onClick={() => setPage((p) => p + 1)}
              >
                <ChevronRight className="w-3.5 h-3.5" />
              </Button>
              <Button
                size="icon-xs"
                variant="outline"
                disabled={data.last}
                onClick={() => setPage(data.totalPages - 1)}
              >
                <ChevronsRight className="w-3.5 h-3.5" />
              </Button>
            </div>
          )}
        </div>
      )}
    </div>
  );
}

function formatValue(value: unknown): string {
  if (value === null || value === undefined) return '\u2014';
  if (typeof value === 'boolean') return value ? 'true' : 'false';
  if (typeof value === 'object') return JSON.stringify(value);
  return String(value);
}

function EmptyRecords({ hasFilter }: { hasFilter: boolean }) {
  return (
    <div className="flex flex-col items-center justify-center h-full text-center py-20">
      <Inbox className="w-10 h-10 text-muted-foreground/40 mb-3" />
      <p className="text-sm font-medium text-muted-foreground">
        {hasFilter ? 'No records match this filter' : 'No records yet'}
      </p>
      {!hasFilter && (
        <p className="text-xs text-muted-foreground/70 mt-1">
          Records will appear here once created via the API or agent.
        </p>
      )}
    </div>
  );
}
