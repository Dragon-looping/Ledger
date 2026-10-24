import { motion } from 'framer-motion';
import { useMonthlySummary } from '../api/hooks';
import { ArrowUpRight, ArrowDownRight, Sparkles, TrendingUp, ShieldCheck } from 'lucide-react';

export default function DashboardPage() {
  const { data: summary, isLoading } = useMonthlySummary('2026-10');

  return (
    <div className="space-y-8 max-w-6xl mx-auto">
      {/* Hero Welcome & Quick Glance */}
      <section className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 pb-2">
        <div>
          <div className="flex items-center gap-2 text-xs font-mono uppercase tracking-widest text-[#C6FF3D] dark:text-[#C6FF3D] text-[#6b9900] font-semibold mb-1.5">
            <span className="w-1.5 h-1.5 rounded-full bg-[#C6FF3D] animate-pulse" />
            Active Session &bull; October 2026
          </div>
          <h1 className="text-2xl sm:text-3xl font-bold tracking-tight text-slate-900 dark:text-slate-100 font-display">
            Financial Cockpit
          </h1>
        </div>

        <div className="flex items-center gap-2 self-start sm:self-auto">
          <div className="glass-panel px-3.5 py-1.5 rounded-full text-xs font-mono flex items-center gap-2 text-slate-700 dark:text-slate-300">
            <ShieldCheck className="w-3.5 h-3.5 text-[#C6FF3D] dark:text-[#C6FF3D] text-[#6b9900]" />
            <span>H2 File DB Linked</span>
          </div>
        </div>
      </section>

      {/* Primary KPI Grid */}
      <section className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        {/* Net Balance */}
        <motion.div
          initial={{ opacity: 0, y: 12 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.3 }}
          className="glass-panel glass-panel-interactive p-5 rounded-2xl flex flex-col justify-between relative overflow-hidden"
        >
          <div className="text-xs font-mono text-slate-500 dark:text-slate-400 uppercase tracking-wider flex items-center justify-between">
            <span>Net Balance</span>
            <span className="text-[#C6FF3D] text-[10px] font-semibold px-2 py-0.5 rounded-full bg-[#C6FF3D]/10 border border-[#C6FF3D]/25 dark:text-[#C6FF3D] text-[#4d6d00]">
              Live
            </span>
          </div>
          <div className="mt-4">
            <div className="text-3xl font-bold tracking-tight numeral text-slate-900 dark:text-white">
              {isLoading ? (
                <span className="animate-pulse">₹ --</span>
              ) : (
                `₹ ${summary?.netBalance?.toLocaleString('en-IN') ?? '0'}`
              )}
            </div>
            <div className="text-xs text-emerald-600 dark:text-emerald-400 flex items-center gap-1 mt-1 font-mono">
              <TrendingUp className="w-3 h-3" />
              <span>+{summary?.savingsRate ?? 0}% savings rate</span>
            </div>
          </div>
        </motion.div>

        {/* Monthly Inflow */}
        <motion.div
          initial={{ opacity: 0, y: 12 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.3, delay: 0.05 }}
          className="glass-panel glass-panel-interactive p-5 rounded-2xl flex flex-col justify-between"
        >
          <div className="text-xs font-mono text-slate-500 dark:text-slate-400 uppercase tracking-wider flex items-center justify-between">
            <span>Monthly Inflow</span>
            <ArrowUpRight className="w-4 h-4 text-emerald-600 dark:text-emerald-400" />
          </div>
          <div className="mt-4">
            <div className="text-2xl font-bold tracking-tight numeral text-slate-900 dark:text-white">
              {isLoading ? '₹ --' : `₹ ${summary?.totalIncome?.toLocaleString('en-IN') ?? '0'}`}
            </div>
            <div className="text-xs text-slate-500 dark:text-slate-400 mt-1">Salary + Consulting</div>
          </div>
        </motion.div>

        {/* Monthly Outflow */}
        <motion.div
          initial={{ opacity: 0, y: 12 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.3, delay: 0.1 }}
          className="glass-panel glass-panel-interactive p-5 rounded-2xl flex flex-col justify-between"
        >
          <div className="text-xs font-mono text-slate-500 dark:text-slate-400 uppercase tracking-wider flex items-center justify-between">
            <span>Monthly Outflow</span>
            <ArrowDownRight className="w-4 h-4 text-amber-500 dark:text-amber-400" />
          </div>
          <div className="mt-4">
            <div className="text-2xl font-bold tracking-tight numeral text-slate-900 dark:text-white">
              {isLoading ? '₹ --' : `₹ ${summary?.totalSpent?.toLocaleString('en-IN') ?? '0'}`}
            </div>
            <div className="text-xs text-slate-500 dark:text-slate-400 mt-1 font-mono">
              {summary?.transactionCount ?? 0} transactions
            </div>
          </div>
        </motion.div>

        {/* Active Budgets */}
        <motion.div
          initial={{ opacity: 0, y: 12 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.3, delay: 0.15 }}
          className="glass-panel glass-panel-interactive p-5 rounded-2xl flex flex-col justify-between"
        >
          <div className="text-xs font-mono text-slate-500 dark:text-slate-400 uppercase tracking-wider flex items-center justify-between">
            <span>Budget Health</span>
            <Sparkles className="w-4 h-4 text-[#C6FF3D] dark:text-[#C6FF3D] text-[#6b9900]" />
          </div>
          <div className="mt-4">
            <div className="text-2xl font-bold tracking-tight text-slate-900 dark:text-white flex items-center gap-2">
              <span className="numeral">{summary?.budgetStatuses?.length ?? 0}</span>
              <span className="text-xs font-mono text-slate-500 dark:text-slate-400 font-normal">rules active</span>
            </div>
            <div className="text-xs text-[#C6FF3D] dark:text-[#C6FF3D] text-[#6b9900] font-mono mt-1">All thresholds OK</div>
          </div>
        </motion.div>
      </section>

      {/* Main Grid: Category Distribution & Natural Language Banner */}
      <section className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Category Breakdown list */}
        <div className="lg:col-span-2 glass-panel p-6 rounded-2xl space-y-4">
          <div className="flex items-center justify-between pb-2 border-b border-black/[0.06] dark:border-white/[0.06]">
            <h2 className="text-base font-semibold text-slate-900 dark:text-white font-display">
              Category Distribution &bull; October
            </h2>
            <span className="text-xs font-mono text-slate-500 dark:text-slate-400">By Total Outflow</span>
          </div>

          <div className="space-y-3 pt-2">
            {summary?.categoryBreakdown?.map((cat) => (
              <div key={cat.category} className="space-y-1.5">
                <div className="flex items-center justify-between text-xs">
                  <span className="font-medium text-slate-800 dark:text-slate-200 flex items-center gap-2">
                    <span
                      className="w-2.5 h-2.5 rounded-full"
                      style={{ backgroundColor: cat.hexColor }}
                    />
                    {cat.categoryName}
                  </span>
                  <span className="font-mono text-slate-700 dark:text-slate-300">
                    ₹ {cat.spent.toLocaleString('en-IN')}{' '}
                    <span className="text-slate-400 dark:text-slate-500">({cat.percentage}%)</span>
                  </span>
                </div>
                <div className="w-full h-1.5 bg-slate-200 dark:bg-slate-800/80 rounded-full overflow-hidden">
                  <div
                    className="h-full rounded-full transition-all duration-500"
                    style={{
                      width: `${Math.min(cat.percentage, 100)}%`,
                      backgroundColor: cat.hexColor,
                    }}
                  />
                </div>
              </div>
            ))}
          </div>
        </div>

        {/* Quick Parser Info Card */}
        <div className="glass-panel p-6 rounded-2xl flex flex-col justify-between space-y-4 border-[#C6FF3D]/30">
          <div>
            <div className="w-9 h-9 rounded-xl bg-[#C6FF3D]/10 border border-[#C6FF3D]/20 flex items-center justify-center text-[#C6FF3D] dark:text-[#C6FF3D] text-[#6b9900] mb-3">
              <Sparkles className="w-5 h-5" />
            </div>
            <h3 className="text-base font-bold text-slate-900 dark:text-white font-display">
              Natural Language Ledger
            </h3>
            <p className="text-xs text-slate-600 dark:text-slate-400 mt-2 leading-relaxed">
              Type expenses naturally using English or Hinglish. Relative dates and keywords are
              automatically resolved to typed domain entities.
            </p>
          </div>

          <div className="space-y-2 bg-slate-100 dark:bg-black/40 p-3 rounded-xl border border-black/[0.05] dark:border-white/[0.05] font-mono text-[11px] text-slate-700 dark:text-slate-300">
            <div className="text-slate-500 text-[10px] uppercase font-semibold">Try Examples:</div>
            <div className="text-[#6b9900] dark:text-[#C6FF3D] font-medium">&bull; "chai 40 yesterday"</div>
            <div>&bull; "auto 150 today"</div>
            <div>&bull; "bijli bill 1850"</div>
          </div>
        </div>
      </section>
    </div>
  );
}
