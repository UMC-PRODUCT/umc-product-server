# Repository Guidelines

## Project Structure & Module Organization
- `src/main/java/com/umc/product` holds Spring Boot code organized by hexagonal layers: `domain/`, `application/`, `adapter/`.
- `src/main/resources` contains configuration (`application.yml`), logging, and templates.
- `src/test/java` and `src/test/resources` hold JUnit tests, RestDocs config, and snippets templates.
- `docs/` contains architecture notes and AsciiDoc API docs; generated docs land in `build/docs/asciidoc`.
- `docker/` and `docker-compose.yml` provide local database tooling.

## Build, Test, and Development Commands
- `./gradlew bootRun` runs the API locally on port 8080.
- `./gradlew test` runs unit/integration tests (JUnit 5 + Testcontainers).
- `./gradlew build` compiles and packages the app.
- `./gradlew asciidoctor` generates API docs from RestDocs snippets.
- `docker-compose up -d` starts the local PostgreSQL (PostGIS) database.

## Coding Style & Naming Conventions
- Java 21, Spring Boot 3.5; use 4-space indentation.
- Follow hexagonal conventions: controllers depend on UseCase interfaces; adapters implement ports.
- Entities must change state via domain methods (no `@Setter`); cross-domain references are IDs only.
- Naming patterns: `{Domain}Controller`, `{Domain}CommandService`, `{Domain}QueryService`, `Get{Domain}UseCase`, `Save{Domain}Port`.

## Testing Guidelines
- Use JUnit 5 (`@ExtendWith`), Spring Boot Test, and Testcontainers for DB-backed tests.
- Test classes live in `src/test/java` and follow `*Test` naming.
- RestDocs snippets are generated under `build/generated-snippets`; run `./gradlew asciidoctor` to build docs.

## Commit & Pull Request Guidelines
- Commit history favors bracketed tags, e.g. `[Feat] add notice CRUD (#134)`.
- Common tags: `[Feat]`, `[Fix]`, `[Hotfix]`, `[Refactor]`, `[Chore]`.
- PRs should describe the change, link issues/PR numbers, and mention API/doc updates when relevant.

## Security & Configuration Notes
- Default profile is `local`; configuration lives in `src/main/resources/application.yml`.
- App runs on port 8080; management/actuator on 9090.

## 참고 파일
[참고 레퍼런스](E:\hana\project_be\umc-product-server\CLAUDE.md)
