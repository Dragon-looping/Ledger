import type { ReactNode } from 'react';
import { NavLink, useLocation } from 'react-router-dom';
import { motion, AnimatePresence } from 'framer-motion';
import { useThemeStore } from '../../store/useThemeStore';
import {
  LayoutDashboard,
  ReceiptText,
  Sliders,
  Activity,
  Sun,
  Moon,
  Cpu,
  Radio,
  Command as CommandIcon
} from 'lucide-react';

interface AppShellProps {
  children: ReactNode;
  onOpenCommand?: () => void;
}

export default function AppShell({ children, onOpenCommand }: AppShellProps) {
  const { theme, toggleTheme } = useThemeStore();
  const location = useLocation();

  const navItems = [
    { to: '/', label: 'Dashboard', icon: LayoutDashboard },
    { to: '/transactions', label: 'Journal', icon: ReceiptText },
    { to: '/budgets', label: 'Budgets', icon: Sliders },
    { to: '/insights', label: 'Insights', icon: Activity },
  ];

  return (
    <div className="min-h-screen bg-[var(--bg-app)] text-[var(--text-main)] relative flex flex-col md:flex-row font-sans selection:bg-[#C6FF3D] selection:text-black">
      {/* Noise Texture Overlay */}
      <div className="fixed inset-0 pointer-events-none noise-overlay z-0" />

      {/* Slow Animated Aurora Mesh Background */}
      <div className="fixed inset-0 pointer-events-none overflow-hidden z-0">
        <div className="absolute top-[-10%] left-[10%] w-[500px] h-[500px] rounded-full bg-[#C6FF3D]/[0.05] blur-[150px] aurora-blob-1" />
        <div className="absolute bottom-[10%] right-[10%] w-[600px] h-[600px] rounded-full bg-cyan-600/[0.08] blur-[160px] aurora-blob-2" />
      </div>

      {/* Desktop Sidebar (Left Dock) */}
      <aside className="hidden md:flex flex-col justify-between w-64 border-r border-black/[0.06] dark:border-white/[0.06] p-6 z-20 glass-panel h-screen sticky top-0">
        {/* Brand Header */}
        <div className="space-y-8">
          <div className="flex items-center gap-3">
            <div className="w-9 h-9 rounded-xl bg-[#C6FF3D] flex items-center justify-center text-black font-bold shadow-[0_0_20px_rgba(198,255,61,0.4)]">
              <Cpu className="w-5 h-5 text-black" />
            </div>
            <div>
              <div className="text-base font-bold tracking-tight font-display flex items-center gap-1.5 text-slate-900 dark:text-slate-100">
                Ledger OS
                <span className="w-1.5 h-1.5 rounded-full bg-[#C6FF3D]" />
              </div>
              <div className="text-[10px] font-mono text-slate-500 uppercase tracking-widest">
                Intelligent FinOps
              </div>
            </div>
          </div>

          {/* Navigation Links */}
          <nav className="space-y-1.5">
            {navItems.map((item) => {
              const isActive = location.pathname === item.to;
              const Icon = item.icon;

              return (
                <NavLink
                  key={item.to}
                  to={item.to}
                  className={`relative flex items-center gap-3 px-3.5 py-2.5 rounded-xl text-xs font-mono font-medium transition-colors ${
                    isActive
                      ? 'text-slate-950 dark:text-[#C6FF3D] font-bold'
                      : 'text-slate-600 dark:text-slate-400 hover:text-slate-900 dark:hover:text-slate-200 hover:bg-black/[0.03] dark:hover:bg-white/[0.03]'
                  }`}
                >
                  {isActive && (
                    <motion.div
                      layoutId="active-desktop-pill"
                      className="absolute inset-0 rounded-xl bg-[#C6FF3D]/25 dark:bg-[#C6FF3D]/10 border border-[#C6FF3D]/40 dark:border-[#C6FF3D]/25 shadow-[inset_0_1px_1px_rgba(198,255,61,0.2)]"
                      transition={{ type: 'spring', stiffness: 350, damping: 30 }}
                    />
                  )}
                  <Icon className="w-4 h-4 relative z-10" />
                  <span className="relative z-10">{item.label}</span>
                </NavLink>
              );
            })}
          </nav>
        </div>

        {/* Sidebar Footer: System Status & Theme Switcher */}
        <div className="space-y-3 pt-6 border-t border-black/[0.06] dark:border-white/[0.06]">
          {/* Quick command search trigger */}
          {onOpenCommand && (
            <button
              onClick={onOpenCommand}
              className="w-full flex items-center justify-between px-3 py-2 rounded-xl text-xs font-mono bg-black/[0.03] dark:bg-white/[0.03] hover:bg-black/[0.06] dark:hover:bg-white/[0.06] border border-black/[0.06] dark:border-white/[0.06] text-slate-600 dark:text-slate-400 transition-colors cursor-pointer"
            >
              <span className="flex items-center gap-2">
                <CommandIcon className="w-3.5 h-3.5" />
                Quick Action
              </span>
              <kbd className="text-[10px] px-1.5 py-0.5 rounded bg-black/10 dark:bg-black/40 border border-black/[0.1] dark:border-white/[0.08]">
                Ctrl+K
              </kbd>
            </button>
          )}

          {/* Live SSE Pulse Pill */}
          <div className="flex items-center justify-between text-[11px] font-mono text-slate-500 dark:text-slate-400 px-1">
            <span className="flex items-center gap-2">
              <span className="relative flex h-2 w-2">
                <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-[#C6FF3D] opacity-75" />
                <span className="relative inline-flex rounded-full h-2 w-2 bg-[#C6FF3D]" />
              </span>
              SSE Stream Active
            </span>
            <Radio className="w-3 h-3 text-[#C6FF3D] dark:text-[#C6FF3D] text-[#6b9900]" />
          </div>

          {/* Theme Toggle Button */}
          <button
            onClick={toggleTheme}
            className="w-full flex items-center justify-between px-3 py-2 rounded-xl text-xs font-mono text-slate-700 dark:text-slate-300 glass-panel glass-panel-interactive cursor-pointer"
          >
            <span className="flex items-center gap-2">
              {theme === 'dark' ? <Moon className="w-3.5 h-3.5" /> : <Sun className="w-3.5 h-3.5 text-amber-500" />}
              <span>{theme === 'dark' ? 'Obsidian Glass' : 'Ceramic Light'}</span>
            </span>
            <span className="text-[10px] text-slate-500 uppercase font-semibold">Toggle</span>
          </button>
        </div>
      </aside>

      {/* Mobile Top Header */}
      <header className="md:hidden flex items-center justify-between p-4 border-b border-black/[0.06] dark:border-white/[0.06] glass-panel sticky top-0 z-30">
        <div className="flex items-center gap-2.5">
          <div className="w-8 h-8 rounded-lg bg-[#C6FF3D] flex items-center justify-center text-black font-bold shadow-[0_0_15px_rgba(198,255,61,0.4)]">
            <Cpu className="w-4 h-4 text-black" />
          </div>
          <div>
            <div className="text-sm font-bold tracking-tight font-display flex items-center gap-1 text-slate-900 dark:text-slate-100">
              Ledger OS
              <span className="w-1.5 h-1.5 rounded-full bg-[#C6FF3D]" />
            </div>
          </div>
        </div>

        <div className="flex items-center gap-2">
          {onOpenCommand && (
            <button
              onClick={onOpenCommand}
              className="p-2 rounded-lg glass-panel text-slate-700 dark:text-slate-300"
              aria-label="Search"
            >
              <CommandIcon className="w-4 h-4" />
            </button>
          )}
          <button
            onClick={toggleTheme}
            className="p-2 rounded-lg glass-panel text-slate-700 dark:text-slate-300"
            aria-label="Toggle Theme"
          >
            {theme === 'dark' ? <Moon className="w-4 h-4" /> : <Sun className="w-4 h-4 text-amber-500" />}
          </button>
        </div>
      </header>

      {/* Main Content Area */}
      <main className="flex-1 p-4 sm:p-8 lg:p-10 pb-28 md:pb-10 z-10 overflow-y-auto">
        <AnimatePresence mode="wait">
          <motion.div
            key={location.pathname}
            initial={{ opacity: 0, y: 8 }}
            animate={{ opacity: 1, y: 0 }}
            exit={{ opacity: 0, y: -8 }}
            transition={{ duration: 0.2 }}
          >
            {children}
          </motion.div>
        </AnimatePresence>
      </main>

      {/* Mobile Bottom Tab Bar */}
      <nav className="md:hidden fixed bottom-3 left-4 right-4 z-40 glass-panel rounded-2xl p-1.5 flex items-center justify-around shadow-2xl border border-black/[0.08] dark:border-white/[0.1]">
        {navItems.map((item) => {
          const isActive = location.pathname === item.to;
          const Icon = item.icon;

          return (
            <NavLink
              key={item.to}
              to={item.to}
              className={`relative flex flex-col items-center justify-center py-2 px-3 rounded-xl transition-all ${
                isActive
                  ? 'text-slate-950 dark:text-[#C6FF3D] font-bold'
                  : 'text-slate-600 dark:text-slate-400 hover:text-slate-900 dark:hover:text-white'
              }`}
            >
              {isActive && (
                <motion.div
                  layoutId="active-mobile-pill"
                  className="absolute inset-0 rounded-xl bg-[#C6FF3D]/25 dark:bg-[#C6FF3D]/15 border border-[#C6FF3D]/40 dark:border-[#C6FF3D]/30 shadow-[0_0_12px_rgba(198,255,61,0.2)]"
                  transition={{ type: 'spring', stiffness: 350, damping: 30 }}
                />
              )}
              <Icon className="w-4 h-4 relative z-10" />
              <span className="text-[10px] font-mono mt-1 relative z-10 font-medium">
                {item.label}
              </span>
            </NavLink>
          );
        })}
      </nav>
    </div>
  );
}
