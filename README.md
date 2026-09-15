# OpsPilot

Plataforma de inteligência operacional para gestão de contas. O backend Spring Boot calcula indicadores explicáveis de risco, potencial e prioridade; o dashboard Angular apresenta o ranking e o contexto operacional de cada conta.

## Stack

- Java 17
- Spring Boot 3.5
- Maven
- PostgreSQL 17
- Spring Web, Spring Data JPA e Bean Validation
- Flyway
- Docker Compose
- Angular 22
- TypeScript
- SCSS

## Desenvolvimento local

### Backend

Pré-requisitos: JDK 17 ou superior e Docker com Docker Compose. O Maven Wrapper já faz parte do projeto.

Entre no diretório do backend antes de executar os comandos:

```bash
cd backend
```

Inicie o PostgreSQL:

```bash
docker compose up -d
```

Execute a aplicação no Linux ou macOS:

```bash
./mvnw spring-boot:run
```

No Windows PowerShell:

```powershell
.\mvnw.cmd spring-boot:run
```

A aplicação aceita as variáveis `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD` e `SERVER_PORT`. Os valores padrão correspondem ao PostgreSQL do `docker-compose.yml` e à porta `8080`.

### Dados de demonstração

O profile `dev` carrega uma base fictícia, determinística e idempotente com diferentes cenários operacionais. O seed usa a referência fixa `2026-09-01T12:00:00Z` e não é executado em nenhum outro profile.

No Linux ou macOS:

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

No Windows PowerShell:

```powershell
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=dev"
```

### Frontend

Com Node.js e npm instalados, abra outro terminal a partir da raiz do repositório:

```bash
cd frontend
npm install
npm start
```

O dashboard estará disponível em `http://localhost:4200`. Durante o desenvolvimento, o Angular encaminha chamadas para `/api` ao backend em `http://localhost:8080` por meio do proxy configurado em `frontend/proxy.conf.json`.

## Health check

```bash
curl http://localhost:8080/api/health
```

Resposta esperada:

```json
{
  "status": "UP",
  "service": "OpsPilot API"
}
```

## Available API Endpoints

```http
GET /api/health
GET /api/accounts
GET /api/accounts/{accountId}
GET /api/accounts/{accountId}/snapshot
GET /api/accounts/{accountId}/analytics
GET /api/accounts/priorities
```

Com a aplicação executando no profile `dev`:

```bash
curl http://localhost:8080/api/accounts
curl http://localhost:8080/api/accounts/{accountId}
curl http://localhost:8080/api/accounts/{accountId}/snapshot
curl http://localhost:8080/api/accounts/{accountId}/analytics
curl http://localhost:8080/api/accounts/priorities
```

O analytics engine usa regras determinísticas e explicáveis. O Risk Score representa exposição operacional, o Potential Score representa oportunidade comercial e o Priority Score combina ambos para ordenar as contas que merecem atenção primeiro.

## Testes e build

No Linux ou macOS:

```bash
./mvnw test
./mvnw package
```

No Windows:

```powershell
.\mvnw.cmd test
.\mvnw.cmd package
```

Os testes de persistência usam o PostgreSQL real iniciado pelo Docker Compose para validar conjuntamente as migrations Flyway e os mappings Hibernate.

## Modelo de domínio

O core operacional é composto por:

- `Account`: conta empresarial acompanhada pela plataforma;
- `Order`: pedido associado a uma conta;
- `SupportTicket`: ticket de suporte associado a uma conta;
- `Interaction`: interação registrada com uma conta.

Os relacionamentos são unidirecionais e partem das entidades operacionais para `Account`, com carregamento lazy e sem cascata.

## Service layer

A camada de serviço contém regras determinísticas para variação de receita, pedidos atrasados e tickets ativos. O `AccountOperationalService` coordena essas regras e produz um snapshot operacional consolidado da conta.

## Estrutura

```text
OpsPilot/
├── backend/                  Spring Boot operational intelligence API
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/opspilot/
│   │   │   └── resources/
│   │   └── test/java/com/opspilot/
│   ├── pom.xml
│   ├── mvnw
│   ├── mvnw.cmd
│   └── docker-compose.yml
├── frontend/                 Angular operational dashboard
│   ├── src/app/
│   │   ├── core/             API client and HTTP contract models
│   │   ├── features/         Dashboard and account details
│   │   └── shared/           Reusable presentation components
│   ├── angular.json
│   └── package.json
├── README.md
└── .gitignore
```

O schema é versionado exclusivamente por migrations Flyway e o Hibernate usa `ddl-auto: validate`. Autenticação, integrações de AI e implantação em produção permanecem fora desta etapa.
