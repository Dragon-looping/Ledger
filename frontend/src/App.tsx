import { Suspense, lazy, useState, useEffect } from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import AppShell from './components/shell/AppShell';
import QuickEntryModal from './components/entry/QuickEntryModal';

// Code-split routes for optimal performance
const DashboardPage = lazy(() => import('./pages/DashboardPage'));
const TransactionsPage = lazy(() => import('./pages/TransactionsPage'));
const BudgetsPage = lazy(() => import('./pages/BudgetsPage'));
const InsightsPage = lazy(() => import('./pages/InsightsPage'));

export default function App() {
  const [commandOpen, setCommandOpen] = useState(false);

  // Global keyboard shortcut: Ctrl+K / Cmd+K
  useEffect(() => {
    const handleKeyDown = (e: KeyboardEvent) => {
      if (e.key === 'k' && (e.metaKey || e.ctrlKey)) {
        e.preventDefault();
        setCommandOpen((prev) => !prev);
      }
    };
    document.addEventListener('keydown', handleKeyDown);
    return () => document.removeEventListener('keydown', handleKeyDown);
  }, []);

  return (
    <BrowserRouter>
      <AppShell onOpenCommand={() => setCommandOpen(true)}>
        <Suspense
          fallback={
            <div className="flex items-center justify-center min-h-[60vh] text-xs font-mono text-slate-500 animate-pulse">
              Initializing Ledger OS View...
            </div>
          }
        >
          <Routes>
            <Route path="/" element={<DashboardPage />} />
            <Route path="/transactions" element={<TransactionsPage />} />
            <Route path="/budgets" element={<BudgetsPage />} />
            <Route path="/insights" element={<InsightsPage />} />
            <Route path="*" element={<Navigate to="/" replace />} />
          </Routes>
        </Suspense>
      </AppShell>

      <QuickEntryModal
        isOpen={commandOpen}
        onClose={() => setCommandOpen(false)}
      />
    </BrowserRouter>
  );
}
