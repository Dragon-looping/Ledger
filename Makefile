.PHONY: all dev dev-backend dev-frontend build test clean

# Default command
all: dev

# Start both backend and frontend concurrently
dev:
	npm run dev

# Start only Spring Boot backend
dev-backend:
	cd backend && ./mvnw spring-boot:run

# Start only Vite frontend
dev-frontend:
	cd frontend && npm run dev

# Build both applications
build:
	npm run build

# Run tests
test:
	npm run test

# Clean build artifacts
clean:
	cd backend && ./mvnw clean
	rm -rf frontend/dist
