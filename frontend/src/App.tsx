import { useState, useEffect } from 'react'
import { motion, AnimatePresence } from 'framer-motion'
import { useQuery } from '@tanstack/react-query'
import { create } from 'zustand'
import { Command } from 'cmdk'
import {
  Server,
  Layers,
  CheckCircle2,
  Database,
  Activity,
  Command as CommandIcon,
  Search,
  Sparkles,
  Terminal,
  Cpu
} from 'lucide-react'

// Simple Zustand store to confirm Zustand works
interface AppState {
  commandOpen: boolean
  setCommandOpen: (open: boolean) => void
  toggleCommand: () => void
}

const useAppStore = create<AppState>((set) => ({
  commandOpen: false,
  setCommandOpen: (open) => set({ commandOpen: open }),
  toggleCommand: () => set((state) => ({ commandOpen: !state.commandOpen })),
}))

export default function App() {
  const { commandOpen, setCommandOpen, toggleCommand } = useAppStore()
  const [currentTime, setCurrentTime] = useState(new Date().toLocaleTimeString())

  useEffect(() => {
    const timer = setInterval(() => setCurrentTime(new Date().toLocaleTimeString()), 1000)
    return () => clearInterval(timer)
  }, [])

  // Keyboard shortcut for cmdk
  useEffect(() => {
    const down = (e: KeyboardEvent) => {
      if (e.key === 'k' && (e.metaKey || e.ctrlKey)) {
        e.preventDefault()
        toggleCommand()
      }
    }
    document.addEventListener('keydown', down)
    return () => document.removeEventListener('keydown', down)
  }, [toggleCommand])

  // TanStack Query to test Backend Health Endpoint
  const { data: healthData, isLoading, error } = useQuery({
    queryKey: ['health'],
    queryFn: async () => {
      const res = await fetch('/api/health')
      if (!res.ok) throw new Error(`HTTP error ${res.status}`)
      return res.json()
    },
    refetchInterval: 10000,
  })

  const stackItems = [
    { name: 'Java 21', category: 'Backend Runtime', badge: 'LTS' },
    { name: 'Spring Boot 3.4', category: 'Backend Framework', badge: 'Active' },
    { name: 'Spring Data JPA', category: 'ORM / Persistence', badge: 'Ready' },
    { name: 'H2 File Database', category: 'Embedded Storage', badge: 'File Mode' },
    { name: 'React 19 + TypeScript', category: 'Frontend Core', badge: 'Strict' },
    { name: 'Vite 8', category: 'Build Engine', badge: 'Lightning Fast' },
    { name: 'Tailwind CSS v4', category: 'Styling', badge: 'Modern' },
    { name: 'TanStack Query v5', category: 'Server State', badge: 'Cached' },
    { name: 'Zustand', category: 'Client State', badge: 'Global Store' },
    { name: 'Framer Motion', category: 'UI Animations', badge: '60 FPS' },
    { name: 'Recharts', category: 'Data Visualization', badge: 'Ready' },
    { name: 'cmdk', category: 'Command Palette', badge: 'Ctrl+K' },
  ]

  return (
    <div className="min-h-screen bg-[#070b13] text-slate-100 flex flex-col items-center justify-between p-6 sm:p-12 relative overflow-hidden font-sans">
      {/* Decorative background glow */}
      <div className="absolute top-[-150px] left-1/2 -translate-x-1/2 w-[600px] h-[300px] bg-cyan-500/10 blur-[120px] rounded-full pointer-events-none" />

      {/* Header */}
      <header className="w-full max-w-4xl flex items-center justify-between border-b border-slate-800/80 pb-6 z-10">
        <div className="flex items-center gap-3">
          <div className="w-10 h-10 rounded-xl bg-gradient-to-tr from-cyan-500 to-blue-600 flex items-center justify-center shadow-lg shadow-cyan-500/20">
            <Cpu className="w-5 h-5 text-white" />
          </div>
          <div>
            <h1 className="text-xl font-bold tracking-tight text-white flex items-center gap-2">
              Ledger OS
              <span className="text-[10px] uppercase font-semibold px-2 py-0.5 rounded-full bg-cyan-500/10 text-cyan-400 border border-cyan-500/20">
                Scaffold
              </span>
            </h1>
            <p className="text-xs text-slate-400">Personal Expense Tracker Foundation</p>
          </div>
        </div>

        <div className="flex items-center gap-3">
          <button
            onClick={() => setCommandOpen(true)}
            className="flex items-center gap-2 text-xs bg-slate-800/80 hover:bg-slate-800 border border-slate-700/60 px-3 py-1.5 rounded-lg text-slate-300 transition-colors shadow-sm"
          >
            <CommandIcon className="w-3.5 h-3.5 text-slate-400" />
            <span>Search</span>
            <kbd className="bg-slate-900 border border-slate-700 text-[10px] px-1.5 py-0.5 rounded text-slate-400">
              Ctrl+K
            </kbd>
          </button>
          <div className="text-xs font-mono text-slate-500 hidden sm:block">{currentTime}</div>
        </div>
      </header>

      {/* Main Content */}
      <main className="w-full max-w-4xl py-10 flex flex-col gap-8 z-10">
        {/* Banner with Framer Motion */}
        <motion.div
          initial={{ opacity: 0, y: 15 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.5 }}
          className="rounded-2xl border border-slate-800 bg-slate-900/60 backdrop-blur-md p-6 sm:p-8 relative overflow-hidden"
        >
          <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-6">
            <div className="space-y-2">
              <div className="flex items-center gap-2 text-xs font-semibold uppercase tracking-wider text-cyan-400">
                <Sparkles className="w-4 h-4" />
                Monorepo Scaffolding Complete
              </div>
              <h2 className="text-2xl sm:text-3xl font-bold text-white tracking-tight">
                Backend & Frontend Environment Ready
              </h2>
              <p className="text-sm text-slate-400 max-w-xl">
                The monorepo structure, Spring Boot 3 API with H2 file database, and React + Vite 8 frontend stack are verified and wired up.
              </p>
            </div>

            {/* Live Backend Connection Card */}
            <div className="flex-shrink-0 bg-slate-950/80 border border-slate-800 rounded-xl p-4 min-w-[220px]">
              <div className="text-xs font-medium text-slate-400 mb-2 flex items-center justify-between">
                <span>Backend API Link</span>
                <Activity className="w-3.5 h-3.5 text-cyan-400 animate-pulse" />
              </div>
              {isLoading ? (
                <div className="text-xs text-amber-400 flex items-center gap-2">
                  <div className="w-2 h-2 rounded-full bg-amber-400 animate-ping" />
                  Connecting to /api/health...
                </div>
              ) : error ? (
                <div className="space-y-1">
                  <div className="text-xs text-amber-400 font-semibold flex items-center gap-1.5">
                    <span className="w-2 h-2 rounded-full bg-amber-400" />
                    Waiting for Backend
                  </div>
                  <p className="text-[11px] text-slate-500">Run backend to link API</p>
                </div>
              ) : (
                <div className="space-y-1">
                  <div className="text-xs text-emerald-400 font-semibold flex items-center gap-1.5">
                    <CheckCircle2 className="w-3.5 h-3.5" />
                    Connected: {healthData?.status}
                  </div>
                  <p className="text-[11px] text-slate-400 font-mono truncate">{healthData?.service}</p>
                </div>
              )}
            </div>
          </div>
        </motion.div>

        {/* Stack Grid */}
        <section className="space-y-4">
          <div className="flex items-center justify-between">
            <h3 className="text-sm font-semibold uppercase tracking-wider text-slate-400 flex items-center gap-2">
              <Layers className="w-4 h-4 text-cyan-400" />
              Integrated Tech Stack
            </h3>
            <span className="text-xs text-slate-500 font-mono">12 components configured</span>
          </div>

          <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 gap-3">
            {stackItems.map((item, idx) => (
              <motion.div
                key={item.name}
                initial={{ opacity: 0, y: 10 }}
                animate={{ opacity: 1, y: 0 }}
                transition={{ duration: 0.3, delay: idx * 0.03 }}
                className="p-3.5 rounded-xl border border-slate-800/80 bg-slate-900/40 hover:bg-slate-900/70 hover:border-slate-700/80 transition-all flex flex-col justify-between gap-3 group"
              >
                <div>
                  <div className="text-xs font-semibold text-slate-200 group-hover:text-cyan-300 transition-colors">
                    {item.name}
                  </div>
                  <div className="text-[11px] text-slate-500">{item.category}</div>
                </div>
                <div>
                  <span className="text-[10px] font-mono px-2 py-0.5 rounded-md bg-slate-800 text-slate-300 border border-slate-700/50">
                    {item.badge}
                  </span>
                </div>
              </motion.div>
            ))}
          </div>
        </section>

        {/* Quick Launch & Monorepo Paths */}
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div className="p-5 rounded-xl border border-slate-800 bg-slate-900/30 flex flex-col justify-between gap-3">
            <div className="flex items-center gap-2 text-xs font-semibold text-slate-300">
              <Server className="w-4 h-4 text-cyan-400" />
              Backend: /backend
            </div>
            <p className="text-xs text-slate-400">
              Spring Boot 3, Java 21, Spring Data JPA, H2 file database at <code className="text-cyan-300">./data/ledgerdb</code>, Maven wrapper (<code className="text-cyan-300">./mvnw</code>).
            </p>
            <div className="text-xs font-mono text-slate-500 bg-slate-950 p-2.5 rounded-lg border border-slate-800/70">
              cd backend && ./mvnw spring-boot:run
            </div>
          </div>

          <div className="p-5 rounded-xl border border-slate-800 bg-slate-900/30 flex flex-col justify-between gap-3">
            <div className="flex items-center gap-2 text-xs font-semibold text-slate-300">
              <Terminal className="w-4 h-4 text-blue-400" />
              Frontend: /frontend
            </div>
            <p className="text-xs text-slate-400">
              Vite 8 dev server with automatic <code className="text-blue-300">/api</code> proxy to backend, Tailwind CSS v4, Lucide icons, Framer Motion.
            </p>
            <div className="text-xs font-mono text-slate-500 bg-slate-950 p-2.5 rounded-lg border border-slate-800/70">
              cd frontend && npm run dev
            </div>
          </div>
        </div>
      </main>

      {/* Footer */}
      <footer className="w-full max-w-4xl border-t border-slate-800/80 pt-6 flex flex-col sm:flex-row items-center justify-between text-xs text-slate-500 gap-3 z-10">
        <div>Ledger OS Monorepo Scaffold &bull; Ready for Feature Development</div>
        <div className="flex items-center gap-4">
          <span className="flex items-center gap-1.5">
            <Database className="w-3.5 h-3.5 text-slate-400" /> H2 File DB
          </span>
          <span className="flex items-center gap-1.5">
            <CheckCircle2 className="w-3.5 h-3.5 text-emerald-400" /> Scaffolding Phase
          </span>
        </div>
      </footer>

      {/* cmdk Command Dialog */}
      <AnimatePresence>
        {commandOpen && (
          <div className="fixed inset-0 z-50 flex items-start justify-center pt-24 bg-black/60 backdrop-blur-sm">
            <div
              className="fixed inset-0"
              onClick={() => setCommandOpen(false)}
            />
            <motion.div
              initial={{ opacity: 0, scale: 0.95, y: -10 }}
              animate={{ opacity: 1, scale: 1, y: 0 }}
              exit={{ opacity: 0, scale: 0.95, y: -10 }}
              className="relative w-full max-w-lg bg-slate-900 border border-slate-700 rounded-xl shadow-2xl overflow-hidden z-10"
            >
              <Command className="w-full">
                <div className="flex items-center border-b border-slate-800 px-3">
                  <Search className="w-4 h-4 text-slate-400 mr-2" />
                  <Command.Input
                    autoFocus
                    placeholder="Type a command or search Ledger OS..."
                    className="w-full bg-transparent py-3 text-sm text-slate-100 outline-none placeholder:text-slate-500"
                  />
                </div>
                <Command.List className="max-h-60 overflow-y-auto p-2">
                  <Command.Empty className="p-4 text-xs text-center text-slate-500">
                    No results found.
                  </Command.Empty>
                  <Command.Group heading="Project Actions" className="text-[11px] text-slate-400 px-2 py-1.5">
                    <Command.Item
                      onSelect={() => {
                        window.open('http://localhost:8080/h2-console', '_blank')
                        setCommandOpen(false)
                      }}
                      className="flex items-center gap-2 p-2 text-xs rounded-lg text-slate-200 hover:bg-slate-800 cursor-pointer"
                    >
                      <Database className="w-4 h-4 text-cyan-400" />
                      Open H2 Database Console (/h2-console)
                    </Command.Item>
                    <Command.Item
                      onSelect={() => {
                        window.open('/api/health', '_blank')
                        setCommandOpen(false)
                      }}
                      className="flex items-center gap-2 p-2 text-xs rounded-lg text-slate-200 hover:bg-slate-800 cursor-pointer"
                    >
                      <Activity className="w-4 h-4 text-emerald-400" />
                      Check Health Endpoint (/api/health)
                    </Command.Item>
                  </Command.Group>
                </Command.List>
              </Command>
            </motion.div>
          </div>
        )}
      </AnimatePresence>
    </div>
  )
}
