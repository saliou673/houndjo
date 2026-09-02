# Frontend workspace

PNPM workspace with:

- `houndjo-apiclient`: Kubb-based OpenAPI client + TanStack Query hooks
- `houndjo-webapp`: Next.js app (standalone, no API client import)

## Start

1. Ensure the backend serves OpenAPI docs at `http://localhost:8080/api/docs`
2. Install deps: `pnpm install`
3. Generate API client: `pnpm generate:api`
4. Run web app: `pnpm dev`
