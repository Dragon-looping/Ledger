import { useState } from 'react';
import { useTransactions } from '../api/hooks';
import type { Category } from '../api/types';
import { ArrowDownLeft, ArrowUpRight } from 'lucide-react';

export default function TransactionsPage() {
  const [selectedCategory, setSelectedCategory] = useState<Category | ''>('');
  const { data: transactions, isLoading } = useTransactions({
    month: '2026-10',
    category: selectedCategory || undefined,
  });

  const categories: Category[] = [
    'FOOD',
    'TRANSPORT',
    'RENT',
    'SHOPPING',
    'BILLS',
    'HEALTH',
    'ENTERTAINMENT',
    'SALARY',
  ];

  return (
    <div className="space-y-6 max-w-6xl mx-auto">
      {/* Header & Controls */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl sm:text-3xl font-bold tracking-tight text-slate-100 font-display">
            Ledger Journal
          </h1>
          <p className="text-xs text-slate-400 mt-1 font-mono">
            October 2026 &bull; {transactions?.length ?? 0} recorded entries
          </p>
        </div>

        {/* Filter Pills */}
        <div className="flex items-center gap-1.5 overflow-x-auto pb-1 max-w-full no-scrollbar">
          <button
            onClick={() => setSelectedCategory('')}
            className={`px-3 py-1.5 rounded-full text-xs font-mono transition-all ${
              selectedCategory === ''
                ? 'bg-[#C6FF3D] text-black font-semibold shadow-[0_0_15px_-3px_rgba(198,255,61,0.4)]'
                : 'glass-panel text-slate-400 hover:text-white'
            }`}
          >
            All
          </button>
          {categories.map((cat) => (
            <button
              key={cat}
              onClick={() => setSelectedCategory(cat)}
              className={`px-3 py-1.5 rounded-full text-xs font-mono transition-all whitespace-nowrap ${
                selectedCategory === cat
                  ? 'bg-[#C6FF3D] text-black font-semibold shadow-[0_0_15px_-3px_rgba(198,255,61,0.4)]'
                  : 'glass-panel text-slate-400 hover:text-white'
              }`}
            >
              {cat}
            </button>
          ))}
        </div>
      </div>

      {/* Transactions Feed / Table */}
      <div className="glass-panel rounded-2xl overflow-hidden divide-y divide-white/[0.05]">
        {isLoading ? (
          <div className="p-12 text-center text-xs font-mono text-slate-500 animate-pulse">
            Loading journal entries from H2 store...
          </div>
        ) : transactions?.length === 0 ? (
          <div className="p-12 text-center text-xs font-mono text-slate-500">
            No transactions found for the selected filter.
          </div>
        ) : (
          transactions?.map((tx) => {
            const isIncome = tx.type === 'INCOME';
            return (
              <div
                key={tx.id}
                className="p-4 sm:px-6 flex items-center justify-between hover:bg-white/[0.02] transition-colors gap-4"
              >
                <div className="flex items-center gap-3.5 min-w-0">
                  <div
                    className="w-10 h-10 rounded-xl flex items-center justify-center flex-shrink-0 text-white font-mono text-xs font-bold"
                    style={{ backgroundColor: `${tx.categoryHexColor}25`, color: tx.categoryHexColor }}
                  >
                    {isIncome ? (
                      <ArrowDownLeft className="w-4 h-4 text-emerald-400" />
                    ) : (
                      <ArrowUpRight className="w-4 h-4 text-amber-400" />
                    )}
                  </div>
                  <div className="min-w-0">
                    <div className="text-sm font-semibold text-slate-100 truncate">
                      {tx.note || tx.category}
                    </div>
                    <div className="flex items-center gap-2 text-[11px] text-slate-400 font-mono mt-0.5">
                      <span>{tx.date}</span>
                      <span>&bull;</span>
                      <span className="uppercase text-[10px] px-1.5 py-0.2 rounded bg-white/[0.06]">
                        {tx.category}
                      </span>
                      {tx.frequency && (
                        <span className="text-[10px] text-[#C6FF3D] border border-[#C6FF3D]/30 px-1 rounded">
                          {tx.frequency}
                        </span>
                      )}
                    </div>
                  </div>
                </div>

                <div className="text-right flex-shrink-0">
                  <div
                    className={`text-sm sm:text-base font-bold numeral ${
                      isIncome ? 'text-emerald-400' : 'text-slate-100'
                    }`}
                  >
                    {isIncome ? '+' : '-'}₹ {tx.amount.toLocaleString('en-IN', { minimumFractionDigits: 2 })}
                  </div>
                  <div className="text-[10px] text-slate-500 font-mono uppercase">
                    {tx.type}
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
