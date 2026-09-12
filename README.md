# OpsPilot Backend

Backend do OpsPilot, uma plataforma de inteligência operacional para apoiar a gestão de contas. A aplicação possui sua fundação técnica e o modelo de domínio operacional; analytics e integrações serão adicionados posteriormente.

## Stack

- Java 17
- Spring Boot 3.5
- Maven
- PostgreSQL 17
- Spring Web, Spring Data JPA e Bean Validation
- Flyway
- Docker Compose

## Pré-requisitos

- JDK 17 ou superior
- Docker com Docker Compose

Não é necessário instalar Maven: o projeto inclui Maven Wrapper para Linux, macOS e Windows.

## Executando localmente

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

A aplicação aceita as variáveis `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD` e `SERVER_PORT`. Sem configuração adicional, os valores correspondem ao PostgreSQL do `docker-compose.yml` e a API usa a porta `8080`.

## Health check

Com a aplicação em execução:

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

## Testes

No Linux ou macOS:

```bash
./mvnw test
```

No Windows:

```powershell
.\mvnw.cmd test
```

O teste do endpoint usa um slice MVC. Os testes de persistência usam o PostgreSQL real iniciado pelo Docker Compose para validar conjuntamente as migrations Flyway e os mappings Hibernate.

## Modelo de domínio

O core operacional é composto por:

- `Account`: conta empresarial acompanhada pela plataforma;
- `Order`: pedido associado a uma conta;
- `SupportTicket`: ticket de suporte associado a uma conta;
- `Interaction`: interação registrada com uma conta.

Os relacionamentos são unidirecionais e partem das entidades operacionais para `Account`, com carregamento lazy e sem cascata. O schema correspondente é criado pela migration `V1__create_core_domain_tables.sql`.

## Estrutura

```text
src/
├── main/
│   ├── java/com/opspilot/
│   │   ├── OpsPilotApplication.java
│   │   ├── controller/
│   │   ├── dto/
│   │   ├── exception/
│   │   ├── model/
│   │   └── repository/
│   └── resources/
│       ├── application.yml
│       └── db/migration/
└── test/java/com/opspilot/
```

O schema é versionado exclusivamente por migrations Flyway; `ddl-auto` está configurado como `validate`. A camada de serviço será criada quando houver casos de uso reais.
