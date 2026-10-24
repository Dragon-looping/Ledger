import { useYearlyHeatmap } from '../api/hooks';
import { Calendar, BarChart3, TrendingUp, Sparkles } from 'lucide-react';

export default function InsightsPage() {
  const { data: heatmap, isLoading } = useYearlyHeatmap(2026);

  const getLevelColor = (level: number) => {
    switch (level) {
      case 1:
        return 'bg-[#C6FF3D]/25';
      case 2:
        return 'bg-[#C6FF3D]/50';
      case 3:
        return 'bg-[#C6FF3D]/75';
      case 4:
        return 'bg-[#C6FF3D] shadow-[0_0_8px_rgba(198,255,61,0.6)]';
      default:
        return 'bg-white/[0.04] border border-white/[0.04]';
    }
  };

  const activeDays = heatmap?.filter((d) => d.count > 0).length ?? 0;

  return (
    <div className="space-y-6 max-w-6xl mx-auto">
      <div>
        <h1 className="text-2xl sm:text-3xl font-bold tracking-tight text-slate-100 font-display">
          Financial Analytics & Heatmap
        </h1>
        <p className="text-xs text-slate-400 mt-1 font-mono">
          Annual Outflow Frequency & Daily Expense Velocity &bull; 2026
        </p>
      </div>

      {/* Heatmap Card */}
      <div className="glass-panel p-6 rounded-2xl space-y-4">
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-2 pb-2 border-b border-white/[0.06]">
          <div className="flex items-center gap-2 text-sm font-semibold text-white font-display">
            <Calendar className="w-4 h-4 text-[#C6FF3D]" />
            Expense Contribution Heatmap
          </div>
          <div className="flex items-center gap-3 text-xs font-mono text-slate-400">
            <span>{activeDays} active spending days</span>
            <div className="flex items-center gap-1.5 text-[10px]">
              <span>Less</span>
              <div className="w-2.5 h-2.5 rounded-sm bg-white/[0.04]" />
              <div className="w-2.5 h-2.5 rounded-sm bg-[#C6FF3D]/25" />
              <div className="w-2.5 h-2.5 rounded-sm bg-[#C6FF3D]/50" />
              <div className="w-2.5 h-2.5 rounded-sm bg-[#C6FF3D]/75" />
              <div className="w-2.5 h-2.5 rounded-sm bg-[#C6FF3D]" />
              <span>More</span>
            </div>
          </div>
        </div>

        {/* Heatmap Grid */}
        {isLoading ? (
          <div className="p-12 text-center text-xs font-mono text-slate-500 animate-pulse">
            Compiling 365 days of activity...
          </div>
        ) : (
          <div className="overflow-x-auto pb-2">
            <div className="grid grid-flow-col grid-rows-7 gap-1.5 w-max">
              {heatmap?.map((item) => (
                <div
                  key={item.date}
                  title={`${item.date}: ₹${item.amount.toLocaleString('en-IN')} (${item.count} expenses)`}
                  className={`w-3.5 h-3.5 rounded-sm cursor-pointer transition-transform hover:scale-125 ${getLevelColor(
                    item.level
                  )}`}
                />
              ))}
            </div>
          </div>
        )}
      </div>

      {/* Metric Cards */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
        <div className="glass-panel p-5 rounded-2xl space-y-2">
          <div className="flex items-center justify-between text-xs font-mono text-slate-400">
            <span>Peak Spending Day</span>
            <TrendingUp className="w-4 h-4 text-[#C6FF3D]" />
          </div>
          <div className="text-xl font-bold text-white numeral">Rent / Housing (2nd)</div>
          <p className="text-xs text-slate-400 leading-relaxed">
            Consistently the largest single transaction of each month.
          </p>
        </div>

        <div className="glass-panel p-5 rounded-2xl space-y-2">
          <div className="flex items-center justify-between text-xs font-mono text-slate-400">
            <span>Primary Frequency</span>
            <BarChart3 className="w-4 h-4 text-emerald-400" />
          </div>
          <div className="text-xl font-bold text-white font-mono">Micro-expenses</div>
          <p className="text-xs text-slate-400 leading-relaxed">
            Chai, metro and auto rides account for 65% of monthly transactions by count.
          </p>
        </div>

        <div className="glass-panel p-5 rounded-2xl space-y-2">
          <div className="flex items-center justify-between text-xs font-mono text-slate-400">
            <span>Savings Efficiency</span>
            <Sparkles className="w-4 h-4 text-[#C6FF3D]" />
          </div>
          <div className="text-xl font-bold text-white numeral">63.4% Saved</div>
          <p className="text-xs text-slate-400 leading-relaxed">
            Net surplus safely exceeds quarterly target goals.
          </p>
        </div>
      </div>
    </div>
  );
}
