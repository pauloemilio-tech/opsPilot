# OpsPilot Backend

Backend do OpsPilot, uma plataforma de inteligência operacional para apoiar a gestão de contas. Esta primeira etapa estabelece a base técnica da API; modelos de domínio, analytics e integrações serão adicionados posteriormente.

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

O teste do endpoint usa um slice MVC e não depende de uma instância do PostgreSQL.

## Estrutura

```text
src/
├── main/
│   ├── java/com/opspilot/
│   │   ├── OpsPilotApplication.java
│   │   ├── controller/
│   │   ├── dto/
│   │   └── exception/
│   └── resources/
│       ├── application.yml
│       └── db/migration/
└── test/java/com/opspilot/controller/
```

Pacotes de modelo, repositório e serviço serão criados quando houver casos de uso reais. O schema será versionado exclusivamente por migrations Flyway; `ddl-auto` está configurado como `validate`.
