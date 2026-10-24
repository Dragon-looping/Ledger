# Ledger OS 📊

> A modern, keyboard-centric personal expense tracker engineered as a high-performance monorepo with Spring Boot 3 and React.

---

## 🏗️ Architecture & Monorepo Structure

```text
ledger/
├── .gitignore                  # Root gitignore (H2 data, node_modules, build targets)
├── Makefile                    # Make targets for dev, build, test, and clean
├── package.json                # Root package manager & orchestration scripts
├── README.md                   # Project documentation
│
├── backend/                    # Spring Boot 3 + Java 21 Backend
│   ├── .mvn/wrapper/           # Maven wrapper binaries & properties
│   ├── data/                   # Embedded H2 file database files (.gitignore'd)
│   ├── mvnw / mvnw.cmd         # Maven wrapper scripts
│   ├── pom.xml                 # Maven configuration & dependencies
│   └── src/
│       ├── main/
│       │   ├── java/com/ledger/os/
│       │   │   ├── config/
│       │   │   │   └── CorsConfig.java
│       │   │   ├── controller/
│       │   │   │   └── HealthController.java
│       │   │   └── LedgerOsApplication.java
│       │   └── resources/
│       │       └── application.yml
│       └── test/
│           └── java/com/ledger/os/
│               └── LedgerOsApplicationTests.java
│
└── frontend/                   # React 19 + Vite 8 + TypeScript Frontend
    ├── package.json            # Frontend dependencies & scripts
    ├── tsconfig.json           # TypeScript configuration
    ├── vite.config.ts          # Vite configuration with Tailwind & /api proxy
    └── src/
        ├── App.tsx             # Scaffolding dashboard & health checker
        ├── index.css           # Tailwind CSS imports & global theme
        └── main.tsx            # App entrypoint with QueryClientProvider
```

---

## ⚡ Tech Stack

| Layer | Technology | Details |
| :--- | :--- | :--- |
| **Backend Runtime** | Java 21 LTS | Microsoft Build of OpenJDK 21 |
| **Backend Framework** | Spring Boot 3.4.3 | Spring Web, Spring Validation, Lombok |
| **ORM / Persistence** | Spring Data JPA | Hibernate 6 |
| **Database** | H2 Database (File Mode) | Stored at `./data/ledgerdb`, Web Console at `/h2-console` |
| **Build Tool (BE)** | Apache Maven 3.9 + Wrapper | `./mvnw` / `mvnw.cmd` |
| **Frontend Core** | React 19 + TypeScript | Strict typing and modern hooks |
| **Bundler & Dev Server**| Vite 8 | Proxy `/api` -> `http://localhost:8080` |
| **Styling** | Tailwind CSS v4 | `@tailwindcss/vite` |
| **UI Motion** | Framer Motion | Fluid micro-interactions & modals |
| **Charts & Analytics** | Recharts | Declarative SVG financial visualization |
| **Data Fetching** | TanStack Query v5 | Automatic caching, stale-while-revalidate |
| **State Management** | Zustand | Lightweight client store |
| **Command Palette** | cmdk | Keyboard-first OS navigation (`Ctrl+K` / `⌘K`) |

---

## 🚀 Getting Started

### Prerequisites

- **Java 21 LTS**
- **Node.js 18+** (Node v24 and npm 11+ tested)

### 1. Install Dependencies

Install root orchestration dependencies and frontend packages:

```bash
npm install
cd frontend && npm install && cd ..
```

### 2. Start Both Services (Single Command)

Using npm scripts (cross-platform):
```bash
npm run dev
```

Or using Make (Unix / macOS / Git Bash):
```bash
make dev
```

This starts:
- **Spring Boot Backend**: `http://localhost:8080`
- **Vite React Frontend**: `http://localhost:5173`

---

## 🔧 Individual Service Commands

### Backend Only

```bash
# Windows
cd backend && .\mvnw.cmd spring-boot:run

# Unix / macOS
cd backend && ./mvnw spring-boot:run
```

- **Health check**: [http://localhost:8080/api/health](http://localhost:8080/api/health)
- **H2 Web Console**: [http://localhost:8080/h2-console](http://localhost:8080/h2-console)
  - JDBC URL: `jdbc:h2:file:./data/ledgerdb`
  - User: `sa`
  - Password: *(leave blank)*

### Frontend Only

```bash
cd frontend
npm run dev
```

- **Frontend App**: [http://localhost:5173](http://localhost:5173) (auto-proxies `/api/*` to `localhost:8080`)

---

## 🧪 Verification & Build

```bash
# Build both backend & frontend
npm run build

# Run backend unit tests & frontend type check
npm run test
```
