import { Outlet } from 'react-router-dom';
import { Database } from 'lucide-react';

export function RootLayout() {
  return (
    <div className="min-h-screen bg-background text-foreground">
      <header className="border-b">
        <div className="flex items-center gap-2 px-6 py-3">
          <Database className="w-5 h-5 text-primary" />
          <span className="text-sm font-semibold tracking-tight">Nexora</span>
        </div>
      </header>
      <main>
        <Outlet />
      </main>
    </div>
  );
}
