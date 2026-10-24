import { useMonthlySummary } from '../api/hooks';
import { Sliders } from 'lucide-react';

export default function BudgetsPage() {
  const { data: summary, isLoading } = useMonthlySummary('2026-10');

  return (
    <div className="space-y-6 max-w-6xl mx-auto">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl sm:text-3xl font-bold tracking-tight text-slate-100 font-display">
            Budget Policies & Rules
          </h1>
          <p className="text-xs text-slate-400 mt-1 font-mono">
            Strategy Pattern: MonthlyCapRule, CategoryCapRule & Thresholds
          </p>
        </div>

        <div className="glass-panel px-3.5 py-1.5 rounded-full text-xs font-mono text-[#C6FF3D] flex items-center gap-2 self-start sm:self-auto">
          <Sliders className="w-3.5 h-3.5" />
          <span>80% Warning &bull; 100% Exceeded</span>
        </div>
      </div>

      {/* Rules Grid */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        {isLoading ? (
          <div className="col-span-2 p-12 text-center text-xs font-mono text-slate-500 animate-pulse">
            Loading budget evaluation rules...
          </div>
        ) : (
          summary?.budgetStatuses?.map((rule, idx) => {
            const isWarning = rule.level === 'WARNING';
            const isExceeded = rule.level === 'EXCEEDED';

            return (
              <div
                key={idx}
                className="glass-panel p-5 rounded-2xl flex flex-col justify-between space-y-4 relative overflow-hidden"
              >
                <div className="flex items-center justify-between">
                  <div>
                    <h3 className="text-sm font-semibold text-white font-display">
                      {rule.category ? `${rule.category} Cap Rule` : 'Overall Monthly Cap'}
                    </h3>
                    <div className="text-[11px] font-mono text-slate-500 mt-0.5 truncate max-w-[260px]">
                      {rule.ruleName}
                    </div>
                  </div>

                  <span
                    className={`text-[10px] font-mono px-2 py-0.5 rounded-full font-semibold border ${
                      isExceeded
                        ? 'bg-rose-500/15 text-rose-400 border-rose-500/30'
                        : isWarning
                        ? 'bg-amber-500/15 text-amber-400 border-amber-500/30'
                        : 'bg-[#C6FF3D]/10 text-[#C6FF3D] border-[#C6FF3D]/20'
                    }`}
                  >
                    {rule.level}
                  </span>
                </div>

                <div className="space-y-2">
                  <div className="flex items-center justify-between text-xs font-mono">
                    <span className="text-slate-400">
                      Spent: <strong className="text-white numeral">₹ {rule.spent.toLocaleString('en-IN')}</strong>
                    </span>
                    <span className="text-slate-400">
                      Limit: <strong className="text-white numeral">₹ {rule.limit.toLocaleString('en-IN')}</strong>
                    </span>
                  </div>

                  {/* Progress bar */}
                  <div className="w-full h-2 bg-slate-800/80 rounded-full overflow-hidden">
                    <div
                      className={`h-full rounded-full transition-all duration-500 ${
                        isExceeded
                          ? 'bg-rose-500'
                          : isWarning
                          ? 'bg-amber-400'
                          : 'bg-[#C6FF3D]'
                      }`}
                      style={{ width: `${Math.min(rule.percentUsed, 100)}%` }}
                    />
                  </div>

                  <div className="flex items-center justify-between text-[11px] font-mono text-slate-500">
                    <span>{rule.percentUsed.toFixed(1)}% consumed</span>
                    <span>
                      {rule.limit - rule.spent > 0
                        ? `₹ ${(rule.limit - rule.spent).toLocaleString('en-IN')} remaining`
                        : 'Limit breached'}
                    </span>
                  </div>
                </div>
              </div>
            );
          })
        )}
      </div>
    </div>
  );
}
