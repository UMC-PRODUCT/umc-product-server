# SEED/TEST UTILITY KNOWLEDGE

## OVERVIEW

`test` is production code for seed/test utility endpoints and scenario data setup; it is not the JUnit test tree.

## STRUCTURE

```text
test/
├── dto/                            # seed request/response DTOs
├── domain/roster                   # pure QA roster, account plan, rehearsal cast (no IO)
├── domain/startingline             # pure starting-line rules (next step per PO, quota apportionment)
├── domain/rehearsal                # pure matching rehearsal rules (snapshot, bot planners, round advance, no IO)
├── domain/exception                # SeedErrorCode (SEED-xxxx), SeedRehearsalException
├── application/port/in             # seed UseCases
├── application/port/out            # cleanup/persistence ports
├── application/service             # member/challenger/project/curriculum/notice seed flows, SeedFailureCode
├── application/service/qa          # /test/seed/qa steps, one runner per QaSeedStep
├── application/service/rehearsal   # starting line, round summary, bots, round advance
├── adapter/in/web                  # seed/test controllers
├── adapter/in/runner               # bulk seed runner
├── adapter/out/persistence         # seed cleanup and rehearsal native-SQL adapters
└── adapter/out/resource            # committed QA CSV loader (roster, standalone accounts, system roles)
```

## WHERE TO LOOK

| Task | Location | Notes |
|------|----------|-------|
| Seed API | `adapter/in/web/SeedController.java` | scenario seed surface |
| Test API | `adapter/in/web/TestController.java` | utility/test endpoint surface |
| QA seeds | `application/service/qa/*Step.java`, `QaSeedOrchestrator.java`, `domain/roster/*`, `resources/seed/qa/*.csv` | `/test/seed/qa`: committed 11th-gen roster, standalone accounts, and system-role grants -> accounts and per-domain QA data, one step per transaction |
| Matching rehearsal | `application/service/rehearsal/*`, `domain/startingline/*`, `domain/rehearsal/*` | `/test/seed/projects/starting-line` (per-PO convergence), `/test/seed/matching/rounds/{id}/summary`, `bot-applications`, `bot-decisions`: `RoundSnapshotReader` -> pure planner -> operational UseCases |
| Matching round advance | `MatchingRoundAdvanceController.java`, `rehearsal/MatchingRoundAdvanceService.java`, `domain/rehearsal/AdvancePlanner.java`, `AdvanceStep.java` | `/test/seed/matching/rounds/{id}/advance`: rewrite round times by test SQL, then resync timers; the real scheduler runs auto-decide and publication |
| Member/challenger seeds | `MemberSeedService.java`, `ChallengerSeedService.java` | member/challenger setup |
| Curriculum seeds | `CurriculumSeedService.java` | curriculum/workbook setup |
| Notice seeds | `NoticeSeedService.java` | notice setup |
| Cleanup | `ProjectSeedDataCleanupService.java`, `ProjectSeedDataCleanupPersistenceAdapter.java` | seed data cleanup |

## CONVENTIONS

- Treat this package as privileged utility code; check profile/security expectations before exposing endpoints.
- Seed services should create data through application/domain paths where possible, not by bypassing invariants.
- Scenario data must be deterministic enough for repeatable QA and local setup.
- Cleanup should target only seed-owned data and avoid deleting user-created production-like data.
- Cross-domain seed flows should document ordering assumptions in service names or tests.

## ANTI-PATTERNS

- Do not put JUnit test helpers under this production package.
- Do not expose seed endpoints broadly without checking `SecurityPathConfig` and profile conditions.
- Do not create invalid domain objects to make scenario setup faster.
- Do not run cleanup by broad table truncation from this package.
