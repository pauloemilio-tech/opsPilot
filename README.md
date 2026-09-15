# OpsPilot

Backend de inteligência operacional para gestão de contas, construído como um monólito modular com Java 17 e Spring Boot.

## Stack

- Java 17
- Spring Boot 3.5
- Maven
- PostgreSQL 17
- Spring Web, Spring Data JPA e Bean Validation
- Flyway
- Docker Compose

## Executando localmente

Pré-requisitos: JDK 17 ou superior e Docker com Docker Compose. O Maven Wrapper já faz parte do projeto.

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
```

Com a aplicação executando no profile `dev`:

```bash
curl http://localhost:8080/api/accounts
curl http://localhost:8080/api/accounts/{accountId}
curl http://localhost:8080/api/accounts/{accountId}/snapshot
```

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
src/
├── main/
│   ├── java/com/opspilot/
│   │   ├── controller/
│   │   ├── dto/
│   │   ├── exception/
│   │   ├── model/
│   │   ├── repository/
│   │   ├── seed/
│   │   └── service/
│   └── resources/
│       ├── application.yml
│       └── db/migration/
└── test/java/com/opspilot/
```

O schema é versionado exclusivamente por migrations Flyway e o Hibernate usa `ddl-auto: validate`. Analytics, scoring, autenticação, frontend e integrações de AI permanecem fora desta etapa.
