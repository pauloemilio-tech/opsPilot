# OpsPilot

AI-assisted operational intelligence for B2B account management.

OpsPilot consolidates account and operational data, calculates explainable Risk, Potential, and Priority scores, ranks the accounts that need attention, and exposes the evidence behind every score. An on-demand AI Account Analyst can then interpret that trusted context into a summary, key concerns, and evidence-backed recommended actions.

This repository is a functional portfolio MVP. The deterministic analytics engine remains the source of truth; AI adds interpretation, not decision authority.

## The problem

Account teams often have relevant signals spread across revenue movement, orders, support tickets, and customer interactions. That fragmentation makes three basic questions harder to answer:

- Which accounts need attention first?
- Why do they need attention?
- What evidence supports that decision?

## The solution

OpsPilot turns persisted operational records into an auditable decision flow:

```text
Record operational data
        |
        v
Build a trusted account snapshot
        |
        v
Calculate Risk and Potential
        |
        v
Calculate Priority and rank the Attention Queue
        |
        v
Inspect account evidence and factor contributions
        |
        v
Request an optional AI interpretation
```

All business data displayed by the application comes from the backend API. The frontend does not use fake business data or calculate its own ranking.

## Core capabilities

- Create and update accounts.
- Record and list orders, support tickets, and account interactions.
- Persist operational data in PostgreSQL with a Flyway-managed schema.
- Build a consolidated account snapshot from current revenue, engagement, order, ticket, and interaction data.
- Calculate deterministic Risk, Potential, and Priority scores in the backend.
- Return the individual point contributions behind Risk and Potential.
- Rank active and onboarding accounts in a backend-defined Attention Queue.
- Investigate scores, evidence, and operational history in Account Detail.
- Request an on-demand AI Account Analyst response from trusted structured context.
- Use a responsive interface with light and dark themes, loading states, empty states, and actionable error states.

## Intelligence model

The scoring pipeline is deterministic and implemented in backend services.

### Risk

Risk represents operational exposure. Its factors are revenue decline, delayed orders, open support tickets, critical open tickets, engagement, and interaction inactivity.

### Potential

Potential represents commercial opportunity. Its factors are revenue growth, engagement, current revenue strength, interaction recency, and operational stability.

### Priority

Priority combines the two scores and determines the Attention Queue order:

```text
Priority = round((Risk x 0.60) + (Potential x 0.40))
```

The API returns the Risk and Potential factor contributions with each assessment, so an operator can trace a score to its inputs. The queue is sorted by descending Priority, then account name; inactive accounts are not included in the active ranking.

## AI boundary

The deterministic engine owns Risk, Potential, Priority, and queue ordering. It remains authoritative whether or not AI is configured.

The AI Account Analyst:

- consumes the trusted account snapshot and deterministic assessments;
- returns a structured summary, key concerns, and recommended actions with evidence;
- runs only when requested from Account Detail.

The LLM does not recalculate scores, replace deterministic rules, or execute actions. The current provider implementation calls the Anthropic Claude Messages API through Spring `RestClient`; application services depend on the provider-neutral `LlmClient` interface.

Missing AI configuration does not prevent backend startup. It affects only the analysis endpoint, which returns `503 Service Unavailable`. Provider unavailability also returns `503`, while an invalid provider response returns `502 Bad Gateway`.

## Architecture

OpsPilot is a modular monolith with a separate single-page frontend:

```text
Angular 22 SPA
      |
      | REST / JSON
      v
Spring Boot 3.5 API
      |
      +--> application and operational services
      |          |
      |          +--> deterministic analytics services
      |
      +--> Spring Data JPA --> PostgreSQL 17
      |                         (Flyway schema)
      |
      +--> account context --> LlmClient --> Anthropic Claude API
```

The AI provider is an outbound dependency of the backend. The browser never receives the API key and does not call Anthropic directly.

## Tech stack

| Area | Technology |
| --- | --- |
| Frontend | Angular 22, TypeScript 6, RxJS 7.8, SCSS, Vitest 4 |
| Backend | Java 17, Spring Boot 3.5.16, Spring Web, Spring Data JPA, Bean Validation, Maven |
| Database | PostgreSQL 17, Flyway |
| Local infrastructure | Docker Compose |
| AI | Anthropic Claude Messages API, Spring `RestClient`, `LlmClient` abstraction |

Dependency versions are defined in [`backend/pom.xml`](backend/pom.xml) and [`frontend/package.json`](frontend/package.json); lockfile-resolved frontend versions remain authoritative for an installation.

## Main screens

| Route | Screen | Purpose |
| --- | --- | --- |
| `/` | Home | Explains the implemented product and decision flow. |
| `/accounts` | Accounts | Lists persisted accounts and creates new ones. |
| `/dashboard` | Attention Queue | Displays the backend-ranked account priorities. |
| `/accounts/:accountId` | Account Detail | Updates the account, records and lists operational data, explains scores, and hosts the on-demand AI Account Analyst. |

The AI Account Analyst is part of Account Detail; it does not have a separate route.

## Run locally

### Prerequisites

- JDK 17 or newer
- A Node.js release supported by Angular 22, with npm
- Docker Desktop or Docker Engine with Docker Compose
- Git, if cloning the repository

### 1. Start PostgreSQL and the backend

From the repository root:

```bash
cd backend
docker compose up -d
```

Run with the optional development seed on Linux or macOS:

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

On Windows PowerShell:

```powershell
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=dev"
```

To start with an empty database, omit the profile argument:

```bash
./mvnw spring-boot:run
```

The backend is available at `http://localhost:8080`. Verify it with:

```bash
curl http://localhost:8080/api/health
```

Expected response:

```json
{
  "status": "UP",
  "service": "OpsPilot API"
}
```

### 2. Start the frontend

In another terminal, from the repository root:

```bash
cd frontend
npm install
npm start
```

Open `http://localhost:4200`. The Angular development server proxies `/api` requests to `http://localhost:8080` using [`frontend/proxy.conf.json`](frontend/proxy.conf.json).

## Database and development seed

The local Compose file runs PostgreSQL 17 on port `5432` with a named volume and a health check. Its credentials are local development defaults and match `application.yml`.

Flyway is the only schema creation mechanism; Hibernate uses `ddl-auto: validate`. The application works with an empty migrated database and does not depend on demonstration data.

The `dev` Spring profile enables an explicitly development-only seed:

- all data is fictional;
- it creates ten varied account scenarios plus related records;
- it is idempotent by account name and skips scenarios already present;
- it uses the fixed reference instant `2026-09-01T12:00:00Z` for reproducible dates;
- it is not loaded under the default profile or any non-`dev` profile.

## Configuration

The backend reads configuration directly from environment variables. No dotenv library is required, and the repository contains no real credentials.

### Application and database

| Variable | Required | Default |
| --- | --- | --- |
| `DB_HOST` | No | `localhost` |
| `DB_PORT` | No | `5432` |
| `DB_NAME` | No | `opspilot` |
| `DB_USER` | No | `opspilot` |
| `DB_PASSWORD` | No | `opspilot` (local development only) |
| `SERVER_PORT` | No | `8080` |

### AI Account Analyst

| Variable | Required | Default / purpose |
| --- | --- | --- |
| `AI_API_KEY` | For AI requests | No default; Anthropic credential. |
| `AI_MODEL` | For AI requests | No default; Claude model identifier. |
| `AI_BASE_URL` | No | `https://api.anthropic.com/v1` |
| `AI_CONNECT_TIMEOUT` | No | `5s` |
| `AI_READ_TIMEOUT` | No | `30s` |
| `AI_MAX_TOKENS` | No | `1200` |

Placeholder-only PowerShell example:

```powershell
$env:AI_API_KEY = "<your-anthropic-api-key>"
$env:AI_MODEL = "<claude-model-id>"
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=dev"
```

Linux or macOS:

```bash
export AI_API_KEY="<your-anthropic-api-key>"
export AI_MODEL="<claude-model-id>"
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

Use environment-specific secret management outside local development. Do not commit credentials.

## API overview

| Area | Method | Endpoint |
| --- | --- | --- |
| Health | `GET` | `/api/health` |
| Accounts | `GET` | `/api/accounts` |
| Accounts | `POST` | `/api/accounts` |
| Accounts | `GET` | `/api/accounts/{accountId}` |
| Accounts | `PUT` | `/api/accounts/{accountId}` |
| Orders | `GET`, `POST` | `/api/accounts/{accountId}/orders` |
| Support tickets | `GET`, `POST` | `/api/accounts/{accountId}/tickets` |
| Interactions | `GET`, `POST` | `/api/accounts/{accountId}/interactions` |
| Snapshot | `GET` | `/api/accounts/{accountId}/snapshot` |
| Analytics | `GET` | `/api/accounts/{accountId}/analytics` |
| Attention Queue | `GET` | `/api/accounts/priorities` |
| AI analysis | `POST` | `/api/accounts/{accountId}/ai-analysis` |

Write requests are validated in the backend. API errors use a structured response containing timestamp, status, error, message, and path.

## Testing and builds

Backend, from `backend/`:

```bash
./mvnw test
./mvnw package
```

Windows PowerShell:

```powershell
.\mvnw.cmd test
.\mvnw.cmd package
```

The persistence integration tests use the PostgreSQL instance from Docker Compose, validating the Flyway migration and JPA mappings against the actual database engine.

Frontend, from `frontend/`:

```bash
npm test
npm run build
```

## Design principles

- **Decision first:** the Attention Queue makes prioritization the primary operational surface.
- **Deterministic before AI:** rules calculate decisions; AI interprets their trusted context.
- **Explainability:** scores remain adjacent to observable evidence and exact point contributions.
- **Truthful data:** the UI renders API data and preserves backend-defined ranking.
- **Accessible resilience:** semantic structure, keyboard-visible focus, reduced-motion support, and explicit loading, empty, and error states.
- **Responsive themes:** light and dark modes share the same information hierarchy across desktop and mobile layouts.

The fuller visual rationale is documented in [`design/design.md`](design/design.md).

## Known MVP limitations

- There is no authentication or authorization; do not expose this build as a public production system.
- Operational records can be created and listed but not individually updated or deleted.
- Account and operational lists are not paginated.
- AI analysis is synchronous and depends on an external provider.
- AI analysis history is not persisted.
- There are no external CRM integrations, alerts, RAG, workflow automation, or action execution.
- There is no production observability stack or deployment infrastructure in this repository.
- No production deployment URL is currently documented.

## Future direction

Potential post-MVP work includes authentication and roles, production observability, pagination and performance work for larger datasets, external operational integrations, alerts and action workflows, and RAG only when a real unstructured knowledge corpus justifies it. These capabilities are not implemented in the current repository.

## Repository structure

```text
OpsPilot/
|-- backend/                  Spring Boot API and analytics engine
|   |-- src/main/java/       Application source
|   |-- src/main/resources/  Configuration and Flyway migration
|   |-- src/test/            Backend tests
|   |-- docker-compose.yml   Local PostgreSQL 17
|   `-- pom.xml
|-- frontend/                 Angular single-page application
|   |-- src/app/core/        API client, models, and theme service
|   |-- src/app/features/    Home, Accounts, Dashboard, Account Detail
|   |-- src/app/shared/      Reusable presentation components
|   `-- package.json
|-- design/                   Product UI direction and references
`-- README.md
```
