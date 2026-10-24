import { useState } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { useCreateTransaction, useParseText } from '../../api/hooks';
import type { ParseResponse } from '../../api/types';
import { Search, Sparkles, CornerDownLeft, CheckCircle2 } from 'lucide-react';

interface QuickEntryModalProps {
  isOpen: boolean;
  onClose: () => void;
}

export default function QuickEntryModal({ isOpen, onClose }: QuickEntryModalProps) {
  const [inputText, setInputText] = useState('');
  const [parsedDraft, setParsedDraft] = useState<ParseResponse | null>(null);

  const parseMutation = useParseText();
  const createMutation = useCreateTransaction();

  const handleParse = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!inputText.trim()) return;

    try {
      const res = await parseMutation.mutateAsync(inputText.trim());
      setParsedDraft(res);
    } catch {
      // Handled by mutation error state
    }
  };

  const handleConfirmAdd = async () => {
    if (!parsedDraft) return;

    await createMutation.mutateAsync({
      type: parsedDraft.type,
      amount: parsedDraft.amount,
      currency: parsedDraft.currency,
      date: parsedDraft.date,
      note: parsedDraft.note,
      category: parsedDraft.category,
    });

    setParsedDraft(null);
    setInputText('');
    onClose();
  };

  return (
    <AnimatePresence>
      {isOpen && (
        <div className="fixed inset-0 z-50 flex items-start justify-center pt-20 px-4">
          {/* Backdrop */}
          <motion.div
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
            onClick={onClose}
            className="fixed inset-0 bg-black/75 backdrop-blur-md"
          />

          {/* Modal Container */}
          <motion.div
            initial={{ opacity: 0, scale: 0.96, y: -10 }}
            animate={{ opacity: 1, scale: 1, y: 0 }}
            exit={{ opacity: 0, scale: 0.96, y: -10 }}
            transition={{ type: 'spring', duration: 0.25 }}
            className="w-full max-w-xl glass-panel rounded-2xl shadow-2xl overflow-hidden relative z-10 border border-white/[0.12]"
          >
            {/* Header / Input Form */}
            <form onSubmit={handleParse} className="p-4 border-b border-white/[0.06] flex items-center gap-3">
              <Search className="w-5 h-5 text-slate-400 flex-shrink-0" />
              <input
                autoFocus
                type="text"
                value={inputText}
                onChange={(e) => {
                  setInputText(e.target.value);
                  setParsedDraft(null);
                }}
                placeholder="Try: 'chai 40 yesterday', 'rent 12000 on 1st', 'auto 150'..."
                className="w-full bg-transparent text-sm text-slate-100 placeholder:text-slate-500 outline-none font-mono"
              />
              <button
                type="submit"
                disabled={parseMutation.isPending || !inputText.trim()}
                className="px-3 py-1.5 rounded-xl bg-[#C6FF3D] hover:bg-[#b4ed2f] text-black font-semibold text-xs font-mono flex items-center gap-1.5 transition-colors disabled:opacity-50 cursor-pointer shadow-[0_0_12px_rgba(198,255,61,0.3)]"
              >
                <span>Parse</span>
                <CornerDownLeft className="w-3 h-3" />
              </button>
            </form>

            {/* Content Body */}
            <div className="p-4 space-y-3">
              {parseMutation.isPending && (
                <div className="p-4 text-center text-xs font-mono text-slate-400 animate-pulse">
                  Resolving natural keywords & relative dates...
                </div>
              )}

              {parseMutation.isError && (
                <div className="p-3 rounded-xl bg-rose-500/10 border border-rose-500/20 text-rose-400 text-xs font-mono">
                  {parseMutation.error?.message || 'Could not parse transaction from input'}
                </div>
              )}

              {parsedDraft && (
                <div className="p-4 rounded-xl bg-white/[0.03] border border-[#C6FF3D]/30 space-y-3">
                  <div className="flex items-center justify-between">
                    <span className="text-xs font-mono uppercase text-[#C6FF3D] font-medium flex items-center gap-1.5">
                      <Sparkles className="w-3.5 h-3.5" />
                      Parsed Entity Draft
                    </span>
                    <span className="text-xs font-mono px-2 py-0.5 rounded bg-white/[0.08] text-slate-300">
                      {parsedDraft.type}
                    </span>
                  </div>

                  <div className="grid grid-cols-2 gap-3 text-xs font-mono">
                    <div>
                      <div className="text-[10px] text-slate-500">Amount</div>
                      <div className="text-base font-bold text-white numeral">
                        ₹ {parsedDraft.amount.toLocaleString('en-IN', { minimumFractionDigits: 2 })}
                      </div>
                    </div>

                    <div>
                      <div className="text-[10px] text-slate-500">Category</div>
                      <div className="font-semibold text-slate-200 flex items-center gap-1.5 mt-0.5">
                        <span
                          className="w-2 h-2 rounded-full"
                          style={{ backgroundColor: parsedDraft.categoryHexColor }}
                        />
                        {parsedDraft.category}
                      </div>
                    </div>

                    <div>
                      <div className="text-[10px] text-slate-500">Date Resolved</div>
                      <div className="text-slate-300">{parsedDraft.date}</div>
                    </div>

                    <div>
                      <div className="text-[10px] text-slate-500">Note</div>
                      <div className="text-slate-300 truncate">{parsedDraft.note}</div>
                    </div>
                  </div>

                  <div className="pt-2 flex justify-end gap-2">
                    <button
                      type="button"
                      onClick={() => setParsedDraft(null)}
                      className="px-3 py-1.5 rounded-lg text-xs font-mono text-slate-400 hover:text-white"
                    >
                      Cancel
                    </button>
                    <button
                      type="button"
                      onClick={handleConfirmAdd}
                      disabled={createMutation.isPending}
                      className="px-4 py-1.5 rounded-xl bg-[#C6FF3D] text-black font-semibold text-xs font-mono flex items-center gap-1.5 hover:bg-[#b4ed2f] transition-all shadow-[0_0_15px_rgba(198,255,61,0.4)] cursor-pointer"
                    >
                      <CheckCircle2 className="w-3.5 h-3.5" />
                      <span>Confirm & Record</span>
                    </button>
                  </div>
                </div>
              )}

              {/* Sample hint pills */}
              {!parsedDraft && !parseMutation.isPending && (
                <div className="space-y-1.5 pt-1">
                  <div className="text-[10px] font-mono uppercase text-slate-500 tracking-wider">
                    Quick Suggestions
                  </div>
                  <div className="flex flex-wrap gap-1.5">
                    {[
                      'chai 40 yesterday',
                      'auto 150 today',
                      'rent 12000 on 1st',
                      'kirana 3500 parso',
                      'bijli bill 1800',
                    ].map((example) => (
                      <button
                        key={example}
                        type="button"
                        onClick={() => {
                          setInputText(example);
                          parseMutation.mutate(example, {
                            onSuccess: (data) => setParsedDraft(data),
                          });
                        }}
                        className="px-2.5 py-1 rounded-lg bg-white/[0.03] hover:bg-white/[0.08] text-[11px] font-mono text-slate-400 hover:text-[#C6FF3D] transition-colors border border-white/[0.04]"
                      >
                        {example} &rarr;
                      </button>
                    ))}
                  </div>
                </div>
              )}
            </div>
          </motion.div>
        </div>
      )}
    </AnimatePresence>
  );
}
