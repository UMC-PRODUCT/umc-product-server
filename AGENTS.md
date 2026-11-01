# PROJECT KNOWLEDGE BASE

## OVERVIEW

UMC PRODUCT Backend is a Java 21 / Spring Boot 3.5 API server built with strict Hexagonal Architecture,
DDD-style domain rules, JPA, QueryDSL, Flyway, JWT/SSO, REST/GraphQL APIs, OpenAPI,
Prometheus, and OpenTelemetry.

All agent responses, generated reviews, and documentation comments must be in Korean unless identifiers or standard technical terms require English.

## RULE IDS

Rules in this file carry stable IDs (`ARCH`, `CONV`, `AUTHZ`, `TEST`, `GIT`). This file holds the only copy of
each rule's text; reviews and other documents cite rules by ID. Never renumber or reuse an ID. To retire a rule,
keep its ID and mark it `(retired)`.

Review priorities, the rule-to-enforcement table, and rejected review findings live in
[`docs/guides/코드_리뷰_기준.md`](docs/guides/코드_리뷰_기준.md).

## STRUCTURE

Gradle multi-module. `:monolith` holds everything not yet extracted, `:blog` is the first extracted
domain, and `:app` packages the boot jar and verifies what actually got assembled. See `docs/adr/030-blog-gradle-module-extraction.md`.

```text
umc-product-server/
├── app/                             # bootJar and assembly smoke test -> :monolith, :blog
│   └── src/test/.../ApplicationAssemblyTest   # every deployed module must register here
├── blog/                            # Extracted domain -> :monolith
│   ├── src/main/java/com/umc/product/blog/
│   └── src/test/java/com/umc/product/blog/
│       └── architecture/            # Outbound dependency allowlist
├── monolith/                        # Everything else. No project dependency
│   ├── src/main/java/com/umc/product/
│   │   ├── {domain}/domain              # Entity, VO, domain enum, domain exception
│   │   ├── {domain}/application/port    # UseCase and outbound Port contracts
│   │   ├── {domain}/application/service # Command/Query service implementations
│   │   ├── {domain}/adapter/in          # REST/GraphQL controllers, schedulers, aspects
│   │   ├── {domain}/adapter/out         # Persistence and external adapters
│   │   ├── global/                      # response, exception, security, config, observability
│   │   └── common/                      # BaseEntity and shared domain enums
│   ├── src/main/resources/db/migration/ # Flyway SQL migrations
│   ├── src/main/resources/graphql/      # Spring GraphQL schema files
│   ├── src/test/java/com/umc/product/   # domain tests
│   └── src/testFixtures/java/com/umc/product/support/  # shared test support
├── docs/adr                         # architecture decisions
├── docs/onboarding                  # domain/test maps
├── gradle/                          # dependencies, querydsl, quality, testing scripts
└── build.gradle.kts                 # root: subprojects config, spotless misc, doc catalog
```

Boot entry and `src/main/resources` stay in `:monolith` so `@SpringBootTest` can find the
configuration class.

Adding a module means four edits. Registering it in `settings.gradle.kts` alone is not enough,
and each omission below fails silently rather than breaking the build.

1. `settings.gradle.kts` - `include(":name")`
2. `app/build.gradle.kts` - add the dependency, otherwise it never reaches the boot jar
3. `gradle/documentation-catalog.gradle.kts` - add the source root, otherwise its error codes
   drop out of the catalog
4. `app/src/test/.../ApplicationAssemblyTest` - add a representative bean, so a missing step 2
   fails the build instead of shipping a jar without the module

Step 4 is what catches the others. Per-module tests only see their own module: `:blog:test` passes
even when `:blog` is absent from the boot jar.

## WHERE TO LOOK

| Task | Location | Notes |
|------|----------|-------|
| Boot entry | `monolith/src/main/java/com/umc/product/UmcProductApplication.java` | `@SpringBootApplication`, configuration properties scan |
| Security flow | `global/config/SecurityConfig.java`, `global/security/*` | JWT, `@Public`, access denied/auth entry points |
| SSO/PKCE auth | `authentication/adapter/in/web/Sso*`, `authentication/application/service/Sso*` | browser login, authorization code, token exchange |
| Web pipeline | `global/config/WebMvcConfig.java` | current member resolver, logging, rate limit, docs redirect |
| GraphQL API | `{domain}/adapter/in/graphql`, `monolith/src/main/resources/graphql` | Spring GraphQL controllers and schema contracts |
| Response envelope | `global/response/*`, `global/exception/*` | success wrapping and error response paths |
| Domain API | `{domain}/adapter/in/web/*Controller.java` | controllers delegate to UseCases only |
| Application logic | `{domain}/application/service` | split command/query services |
| Persistence | `{domain}/adapter/out/persistence` | JPA repositories plus QueryDSL query repositories |
| Public contracts | `{domain}/application/port/in`, `{domain}/application/port/out` | UseCase and Port interfaces |
| High-complexity project flows | `monolith/src/main/java/com/umc/product/project` | application forms, matching, statistics, permissions |
| Organization model | `monolith/src/main/java/com/umc/product/organization` | school, chapter, gisu, study group, UMC PRODUCT org |
| Test infrastructure | `monolith/src/testFixtures/java/com/umc/product/support` | Testcontainers, fixtures, MockMvc, isolation |
| Migrations | `monolith/src/main/resources/db/migration` | `VYYYY.MM.DD.HH.MM__snake_case.sql` |

## ARCHITECTURE RULES

- **ARCH-01** Layers are `domain`, `application`, and `adapter`; dependency direction must point inward.
- **ARCH-02** Allowed: `adapter/in -> application/port/in`, `application/service -> domain`, `adapter/out -> application/port/out`.
- **ARCH-03** Forbidden: `domain -> application/adapter`, `application/port -> application/service`, `adapter/in -> adapter/out`. This includes controller-to-repository dependencies.
- **ARCH-04** Cross-domain references must use IDs, not another domain aggregate object.
- **ARCH-05** When Domain A needs Domain B data, inject Domain B's public Query UseCase. Never access Domain B's entities, models, or repositories directly.
- **ARCH-06** Within the same aggregate, `@ManyToOne(fetch = FetchType.LAZY)` is allowed for parent/root references.
- **ARCH-07** `@OneToMany` collections are forbidden, even inside the same domain.
- **ARCH-08** State changes belong in explicit domain methods. Avoid anemic entities.
- **ARCH-09** No `@Setter` on entities.

## CONVENTIONS

- **CONV-01** Command UseCases and Query UseCases stay separate. Command services use `@Transactional`; Query services use `@Transactional(readOnly = true)`.
- **CONV-02** Controllers return adapter `Response` records or application `Info` values. They do not return entities and do not wrap success responses in `ApiResponse`.
- **CONV-03** Controllers contain no business logic and delegate to UseCases only.
- **CONV-04** REST controllers live under `adapter/in/web`; GraphQL controllers live under `adapter/in/graphql`.
- **CONV-05** REST API URIs must be resource-first: start with `/api/v{version}/{domain}` and use stable, kebab-case resource nouns after the domain segment.
- **CONV-06** Admin REST APIs must use `/api/v{version}/{domain}/admin/...`; do not create new admin APIs under `/api/v{version}/admin/{domain}/...`.
- **CONV-07** Treat `admin` as an access/control surface inside the owning domain, not as a top-level domain. When changing controller paths, update controller tests, security/maintenance allow paths, and onboarding/API guide documents together.
- **CONV-08** GraphQL schema files in `monolith/src/main/resources/graphql` are API contracts and must stay aligned with GraphQL DTOs.
- **CONV-09** SSO/PKCE flows must not log authorization codes, login tokens, refresh tokens, or client secrets.
- **CONV-10** Request records live under `adapter/in/web/dto/request` and convert to command/query objects near the adapter boundary.
- **CONV-11** Request bodies are annotated with `@Valid`.
- **CONV-12** Response records live under `adapter/in/web/dto/response` and usually expose `from(Info)`.
- **CONV-13** Query DTOs in `application/port/in/query/dto` end with `Info`; command DTOs end with `Command`.
- **CONV-14** JPA repositories follow Spring Data names (`findById`, `findAllBy...`, `existsBy...`). Custom Ports/UseCases use semantic names.
- **CONV-15** `get[By]` returns `T` and throws if missing; `find[By]` returns `Optional<T>` and never throws not-found; `list[By]` returns a non-null list.
- **CONV-16** `batchGet[By]` requires all inputs to exist; `search[By]` is for dynamic/complex filters.
- **CONV-17** Static factories: `of` for multiple params, `from` for one source, `create`/`newInstance` for guaranteed new instances.
- **CONV-18** Do not use `new` to create commands or domain entities outside adapter/test boundaries; use factories or builders.
- **CONV-19** QueryDSL code belongs in `*QueryRepository`; avoid N+1 by fetch joins, split queries, or IN/batch maps. No hidden N+1 query path.
- **CONV-20** Flyway files use `VYYYY.MM.DD.HH.MM__snake_case.sql`; duplicate versions are checked by Gradle.

## AUTHORIZATION

- **AUTHZ-01** ChallengerRole-backed staff authority is scoped to a gisu. New resource permission checks that have a resource gisu must use `...InGisu(...)` predicates with the resource's gisu.
- **AUTHZ-02** `...InGisu(...)` scopes ChallengerRole evaluation only. Member-wide system roles such as `SUPER_ADMIN` remain global overrides and may short-circuit a gisu-scoped policy.
- **AUTHZ-03** `...InAnyGisu(...)` predicates are allowed for intentionally global or gisu-agnostic surfaces. Existing cross-gisu behavior covered by compatibility tests must not be tightened inside a refactor; move it to `...InGisu(...)` only through an explicitly approved policy change.
- **AUTHZ-04** Use `ListChallengerRoleUseCase` for role read models and `CheckChallengerAuthorityUseCase` for boolean authority decisions. `GetChallengerRoleUseCase` exists as a compatibility facade; do not introduce it in new code unless an existing constructor/API boundary requires it.
- **AUTHZ-05** Boolean authority checks must fail closed for nonexistent members through the member domain's dedicated existence Query UseCase before consulting legacy role data.
- **AUTHZ-06** `ResourcePermissionEvaluator` implementations should evaluate `SubjectAttributes.toAuthoritySnapshot()` instead of re-querying ChallengerRole data or manually streaming raw role lists.
- **AUTHZ-07** When ChallengerRole data or member system role data changes, evict the `AUTHORITY_SNAPSHOT` cache through the authorization cache usecase. Cache serialized DTOs, not domain entities. The current Caffeine adapter is instance-local, so do not claim cross-instance immediate revocation without distributed invalidation or authority version checks.
- **AUTHZ-08** `SUPER_ADMIN` is a member-bound global system role stored only by the member domain (`member_system_role`). It must not be added to `ChallengerRoleType`, persisted in `challenger_role`, or exposed through ChallengerRole command/query APIs.
- **AUTHZ-09** Authorization refactors must preserve client-visible API paths, request/response shapes, and status semantics unless the issue explicitly includes a client contract change. Add tests that lock existing behavior before replacing predicates.

## TESTING

- **TEST-01** Unit tests prefer JUnit 5 + Mockito with `@ExtendWith(MockitoExtension.class)`.
- **TEST-02** Integration tests reuse `IntegrationTestSupport`; persistence slices reuse `PersistenceAdapterTest`; web slices reuse `ControllerTestSupport`.
- **TEST-03** Test names and `@DisplayName` values should be Korean and behavior-focused.
- **TEST-04** Given/When/Then structure is expected.
- **TEST-05** Fixture code lives in `monolith/src/testFixtures/java/com/umc/product/support/fixture` and should persist through SavePorts where possible.

## GIT

- **GIT-01** Commit format is `<type>: <subject>` with `feat`, `fix`, `refactor`, `docs`, `test`, or `chore`.
- **GIT-02** PR titles use `[Feat]`, `[Fix]`, `[HotFix]`, `[Refactor]`, `[Chore]`, `[Docs]`, or `[Release]`; development PRs target `develop`.
- **GIT-03** Never set AI authorship, AI committer metadata, or `Co-authored-by` trailers for AI tools.

## REVIEW

Review with [`docs/guides/코드_리뷰_기준.md`](docs/guides/코드_리뷰_기준.md). It defines finding priorities (P1-P5),
which rules are enforced by tests or tooling, and findings the team has rejected.

## COMMANDS

```bash
./gradlew :app:bootRun
./gradlew check                      # CI and completion criteria use this single entry point
./gradlew :monolith:test :blog:test
./gradlew :app:bootJar               # app/build/libs/app.jar
./gradlew checkstyleMain -PlintAll   # full scan instead of changed files only
```

`check` covers spotless, checkstyle for every source set, tests, the Flyway duplicate-version task,
and the documentation catalog. Listing tasks by hand silently drops newly added checks.

## NOTES

- Default profile is `local`; app port is `8080`; management/Prometheus port is `9090`.
- `bootJar`는 배포용 JAR을 생성한다. Checkstyle 작업은 변경 파일을 기준으로 실행한다.
- `CLAUDE.md`, `GEMINI.md`, and `.github/copilot-instructions.md` are pointers to this canonical file.
