# Test Seed 테스트 케이스

- 테스트 파일: 27개
- 테스트 케이스: 95개
- 분류 기준: `Controller`, `UseCase`, `Repository`, `E2E`, `Scheduler`, `Domain`, `External Adapter`, `Support`

| 카테고리 | 케이스 수 |
|---|---:|
| Controller / Inbound Adapter | 4 |
| UseCase / Application Service | 38 |
| Repository / Outbound Persistence | 1 |
| E2E / Integration | 6 |
| Domain | 46 |

## Controller / Inbound Adapter

### QaSeedAutoRunnerTest
- 위치: `monolith/src/test/java/com/umc/product/test/adapter/in/runner/QaSeedAutoRunnerTest.java`

| 라인 | 테스트 케이스 | 입력/조건 | 기대 결과 |
|---:|---|---|---|
| [22](../../../monolith/src/test/java/com/umc/product/test/adapter/in/runner/QaSeedAutoRunnerTest.java#L22) | prod가 아니고 시딩과 QA 자동 실행이 모두 켜진 환경에서만 빈으로 등록된다 | QaSeedAutoRunner 클래스의 어노테이션 | 성공: @Profile에 !prod, @ConditionalOnProperty가 prefix app.seed, name enabled·qa-auto-run, havingValue true |
| [32](../../../monolith/src/test/java/com/umc/product/test/adapter/in/runner/QaSeedAutoRunnerTest.java#L32) | QA 시딩 전체를 실행하고 단계별 결과를 돌려준다 | SeedQaDataUseCase.seedAll()이 단계 결과 1개를 돌려줌 | 성공: run()이 그 결과를 그대로 돌려줌 |
| [48](../../../monolith/src/test/java/com/umc/product/test/adapter/in/runner/QaSeedAutoRunnerTest.java#L48) | QA 시딩이 예외를 던져도 기동을 깨지 않고 빈 결과를 돌려준다 | seedAll()이 IllegalStateException을 던짐 | 성공: 예외 없이 빈 목록을 돌려줌 |

### SeedControllerBeanRegistrationTest
- 위치: `monolith/src/test/java/com/umc/product/test/adapter/in/web/SeedControllerBeanRegistrationTest.java`

| 라인 | 테스트 케이스 | 입력/조건 | 기대 결과 |
|---:|---|---|---|
| [39](../../../monolith/src/test/java/com/umc/product/test/adapter/in/web/SeedControllerBeanRegistrationTest.java#L39) | 모든 시딩 빈은 @Profile(!prod) + @ConditionalOnProperty(app.seed.enabled=true) 가드를 가진다 | 파라미터 @ValueSource classes = { SeedController, QaSeedController, MatchingRoundAdvanceController, MatchingRoundAdvanceService, MatchingRoundTimeRewritePersistenceAdapter, QaSeedOrchestrator, QaSeedMarkerPersistenceAdapter, ClasspathQaRosterAdapter, QaGisuIdLoader, MemberSeedService, MemberSystemRoleSeedService, MemberSystemRoleSeedPersistenceAdapter, ChallengerSeedService, StartingLineSeedController, MatchingBotController, SeedProjectReadPersistenceAdapter, ProjectSeedDataCleanupService, CurriculumSeedService, NoticeSeedService, DummyMemberFactory, DummyCurriculumFactory, DummyNoticeFactory, SeedProperties } | 성공: 각 클래스에 @Profile("!prod")와 app.seed.enabled 조건이 있음 |

## UseCase / Application Service

### ChallengerSeedServiceTest
- 위치: `monolith/src/test/java/com/umc/product/test/application/service/ChallengerSeedServiceTest.java`

| 라인 | 테스트 케이스 | 입력/조건 | 기대 결과 |
|---:|---|---|---|
| [63](../../../monolith/src/test/java/com/umc/product/test/application/service/ChallengerSeedServiceTest.java#L63) | parts 가 null 이면 ADMIN 제외 모든 파트가 대상이 된다 | 호출 seed(new SeedChallengersCommand(gisuId, 1, null, null)) | 성공: 검증 assertThat(result.perCellSummary()).hasSize((int) expectedParts); assertThat(result.perCellSummary()) |
| [85](../../../monolith/src/test/java/com/umc/product/test/application/service/ChallengerSeedServiceTest.java#L85) | chapterIds 필터가 있으면 해당 Chapter 만 시딩한다 | 조건 chapterIds 필터가 있으면 해당 Chapter 만 시딩한다 | 성공: 검증 assertThat(result.perCellSummary()); .hasSize(1); assertThat(s.chapterId()).isEqualTo(2L); assertThat(s.schoolId()).isEqualTo(201L); |
| [114](../../../monolith/src/test/java/com/umc/product/test/application/service/ChallengerSeedServiceTest.java#L114) | 한 셀에서 createChallengerBulk 가 실패해도 다른 셀 시딩은 진행된다 | 조건 한 셀에서 createChallengerBulk 가 실패해도 다른 셀 시딩은 진행된다 | 실패: 검증 assertThat(result.perCellSummary()).hasSize(2); assertThat(result.totalCreated()).isEqualTo(1); |
| [141](../../../monolith/src/test/java/com/umc/product/test/application/service/ChallengerSeedServiceTest.java#L141) | 멤버 batch 생성 실패는 memberFailed 로 보고되고 챌린저 생성은 호출하지 않는다 | 조건 멤버 batch 생성 실패는 memberFailed 로 보고되고 챌린저 생성은 호출하지 않는다 | 실패: 검증 assertThat(result.perCellSummary()).hasSize(1); assertThat(cell.created()).isZero(); assertThat(cell.memberFailed()).isEqualTo(2); assertThat(cell.challengerFailed()).isZero(); |
| [167](../../../monolith/src/test/java/com/umc/product/test/application/service/ChallengerSeedServiceTest.java#L167) | gisuId 가 null 이면 활성 기수를 사용한다 | 조건 gisuId 가 null 이면 활성 기수를 사용한다 | 성공: 검증 assertThat(result.gisuId()).isEqualTo(10L); |

### CurriculumSeedServiceTest
- 위치: `monolith/src/test/java/com/umc/product/test/application/service/CurriculumSeedServiceTest.java`

| 라인 | 테스트 케이스 | 입력/조건 | 기대 결과 |
|---:|---|---|---|
| [67](../../../monolith/src/test/java/com/umc/product/test/application/service/CurriculumSeedServiceTest.java#L67) | 기본 파트 사용 시 ADMIN 제외 모든 파트에 대해 커리큘럼이 생성된다 | 호출 seed(new SeedCurriculumCommand(gisuId, 1, 0, null, null)) | 성공: 검증 assertThat(result.createdCurriculumIds()).hasSize((int) expectedPartCount); |
| [84](../../../monolith/src/test/java/com/umc/product/test/application/service/CurriculumSeedServiceTest.java#L84) | 주차 수만큼 WeeklyCurriculum 과 OriginalWorkbook 이 생성된다 | 조건 주차 수만큼 WeeklyCurriculum 과 OriginalWorkbook 이 생성된다 | 성공: 검증 assertThat(result.createdCurriculumIds()).hasSize(2); assertThat(result.createdWeeklyCurriculumIds()).hasSize(8); // 2 × 4; assertThat(result.createdOriginalWorkbookIds()).hasSize(8); |
| [102](../../../monolith/src/test/java/com/umc/product/test/application/service/CurriculumSeedServiceTest.java#L102) | missionsPerWorkbook 만큼 미션이 생성된다 | 조건 missionsPerWorkbook 만큼 미션이 생성된다 | 성공: 검증 assertThat(result.createdMissionIds()).hasSize(6); // 2 × 3 |
| [118](../../../monolith/src/test/java/com/umc/product/test/application/service/CurriculumSeedServiceTest.java#L118) | Curriculum 생성 실패는 다음 파트로 격리되어 다른 파트는 정상 시딩된다 | 조건 Curriculum 생성 실패는 다음 파트로 격리되어 다른 파트는 정상 시딩된다 | 실패: 검증 assertThat(result.curriculumFailed()).isEqualTo(1); assertThat(result.createdCurriculumIds()).hasSize(1); assertThat(result.createdWeeklyCurriculumIds()).hasSize(2); // 두 번째 파트의 2주차 |
| [145](../../../monolith/src/test/java/com/umc/product/test/application/service/CurriculumSeedServiceTest.java#L145) | releaseRequesterMemberId 가 지정되면 워크북을 RELEASED 로 전환한다 | 조건 releaseRequesterMemberId 가 지정되면 워크북을 RELEASED 로 전환한다 | 성공: 검증 assertThat(result.released()).isTrue(); |
| [162](../../../monolith/src/test/java/com/umc/product/test/application/service/CurriculumSeedServiceTest.java#L162) | WeeklyCurriculum 과 OriginalWorkbook 은 파트별 bulk UseCase 로 생성한다 | 조건 WeeklyCurriculum 과 OriginalWorkbook 은 파트별 bulk UseCase 로 생성한다 | 성공: 검증 assertThat(result.createdWeeklyCurriculumIds()).hasSize(6); assertThat(result.createdOriginalWorkbookIds()).hasSize(6); |
| [183](../../../monolith/src/test/java/com/umc/product/test/application/service/CurriculumSeedServiceTest.java#L183) | releaseRequesterMemberId 가 null 이면 워크북 상태 전환을 호출하지 않는다 | 조건 releaseRequesterMemberId 가 null 이면 워크북 상태 전환을 호출하지 않는다 | 성공: 검증 assertThat(result.released()).isFalse(); |
| [200](../../../monolith/src/test/java/com/umc/product/test/application/service/CurriculumSeedServiceTest.java#L200) | RELEASED 전환 실패 시 releaseFailed 에 카운트되고 released=false 로 반환한다 | 조건 RELEASED 전환 실패 시 releaseFailed 에 카운트되고 released=false 로 반환한다 | 실패: 검증 assertThat(result.released()).isFalse(); assertThat(result.releaseFailed()).isEqualTo(2); |
| [219](../../../monolith/src/test/java/com/umc/product/test/application/service/CurriculumSeedServiceTest.java#L219) | RELEASED 전환 시 모든 워크북 ID 와 요청자 멤버 ID 가 정확히 전달된다 | 호출 seed(new SeedCurriculumCommand(gisuId, 3, 0, List.of(ChallengerPart.WEB), 777L)) | 성공: 검증 assertThat(sent).hasSize(3); assertThat(sent).allSatisfy(cmd -> assertThat(cmd.requestedMemberId()).isEqualTo(777L)); |
| [238](../../../monolith/src/test/java/com/umc/product/test/application/service/CurriculumSeedServiceTest.java#L238) | gisuId 가 null 이면 활성 기수를 사용한다 | 조건 gisuId 가 null 이면 활성 기수를 사용한다 | 성공: 검증 assertThat(result.gisuId()).isEqualTo(10L); |
| [254](../../../monolith/src/test/java/com/umc/product/test/application/service/CurriculumSeedServiceTest.java#L254) | parts 에 ADMIN 이 포함되어도 ADMIN 은 시딩되지 않는다 | 조건 parts 에 ADMIN 이 포함되어도 ADMIN 은 시딩되지 않는다 | 성공: 검증 assertThat(result.createdCurriculumIds()).hasSize(1); |

### MemberSeedServiceTest
- 위치: `monolith/src/test/java/com/umc/product/test/application/service/MemberSeedServiceTest.java`

| 라인 | 테스트 케이스 | 입력/조건 | 기대 결과 |
|---:|---|---|---|
| [44](../../../monolith/src/test/java/com/umc/product/test/application/service/MemberSeedServiceTest.java#L44) | 비밀번호를 비워 보내면 기본 비밀번호와 활성 필수 약관 동의로 회원을 만든다 | 호출 create(CreateSeedMemberCommand.of(..., " ")) | 성공: 기본 비밀번호, 활성 필수 약관 동의, 요청한 schoolId 로 가입 |
| [59](../../../monolith/src/test/java/com/umc/product/test/application/service/MemberSeedServiceTest.java#L59) | 비밀번호를 보내면 그 비밀번호로 회원을 만든다 | 호출 create(CreateSeedMemberCommand.of(..., "Custom!Pass1")) | 성공: 보낸 비밀번호로 가입 |

### NoticeSeedServiceTest
- 위치: `monolith/src/test/java/com/umc/product/test/application/service/NoticeSeedServiceTest.java`

| 라인 | 테스트 케이스 | 입력/조건 | 기대 결과 |
|---:|---|---|---|
| [61](../../../monolith/src/test/java/com/umc/product/test/application/service/NoticeSeedServiceTest.java#L61) | 4 가지 scope 별로 요청 수량만큼 공지가 생성된다 | 조건 4 가지 scope 별로 요청 수량만큼 공지가 생성된다 | 성공: 검증 assertThat(result.totalCreated()).isEqualTo(15); assertThat(result.createdNoticeIds()).hasSize(15); assertThat(global.created()).isEqualTo(3); assertThat(chapter.created()).isEqualTo(4); |
| [90](../../../monolith/src/test/java/com/umc/product/test/application/service/NoticeSeedServiceTest.java#L90) | 권한 부족 등의 실패는 scope 단위 failed 카운트에 반영되고 다른 scope 는 진행된다 | 조건 권한 부족 등의 실패는 scope 단위 failed 카운트에 반영되고 다른 scope 는 진행된다 | 실패: 검증 assertThat(chapter.failed()).isEqualTo(2); // 2 chapters × 1; assertThat(chapter.created()).isZero(); assertThat(findScope(result, "GLOBAL").created()).isEqualTo(1); assertThat(findScope(result, "SCHOOL").created()).is... |
| [122](../../../monolith/src/test/java/com/umc/product/test/application/service/NoticeSeedServiceTest.java#L122) | 모든 count 가 0 이면 createNotice 가 호출되지 않는다 | 조건 모든 count 가 0 이면 createNotice 가 호출되지 않는다 | 성공: 검증 assertThat(result.totalCreated()).isZero(); |
| [139](../../../monolith/src/test/java/com/umc/product/test/application/service/NoticeSeedServiceTest.java#L139) | gisuId 가 null 이면 활성 기수를 사용한다 | 조건 gisuId 가 null 이면 활성 기수를 사용한다 | 성공: 검증 assertThat(result.gisuId()).isEqualTo(10L); |
| [156](../../../monolith/src/test/java/com/umc/product/test/application/service/NoticeSeedServiceTest.java#L156) | parts 가 null 이면 ADMIN 제외 모든 파트가 사용된다 | 조건 parts 가 null 이면 ADMIN 제외 모든 파트가 사용된다 | 성공: 검증 assertThat(partCaptor.getAllValues()).noneMatch(p -> p == ChallengerPart.ADMIN); |
| [177](../../../monolith/src/test/java/com/umc/product/test/application/service/NoticeSeedServiceTest.java#L177) | 제목·내용에 대상 범위 정보가 포함된 채로 생성된다 (DummyNoticeFactory 위임 확인) | 호출 seed(new SeedNoticeCommand(gisuId, 1L, 1, 1, 1, 1, List.of(ChallengerPart.WEB))) | 성공: 검증 assertThat(sent).anyMatch(c -> c.title().contains("[전체]")); assertThat(sent).anyMatch(c -> c.title().contains("[지부]")); assertThat(sent).anyMatch(c -> c.title().contains("[학교]")); assertThat(sent).anyMatch(c -> c.titl... |

### ProjectSeedDataCleanupServiceTest
- 위치: `monolith/src/test/java/com/umc/product/test/application/service/ProjectSeedDataCleanupServiceTest.java`

| 라인 | 테스트 케이스 | 입력/조건 | 기대 결과 |
|---:|---|---|---|
| [34](../../../monolith/src/test/java/com/umc/product/test/application/service/ProjectSeedDataCleanupServiceTest.java#L34) | gisuId가 지정되면 해당 기수 프로젝트 데이터를 삭제한다 | 호출 delete(DeleteSeedProjectDataCommand.of(9L)) | 성공: 검증 assertThat(result.gisuId()).isEqualTo(9L); assertThat(result.deletedProjects()).isEqualTo(2); assertThat(result.deletedProjectMembers()).isEqualTo(20); |
| [56](../../../monolith/src/test/java/com/umc/product/test/application/service/ProjectSeedDataCleanupServiceTest.java#L56) | gisuId가 null이면 활성 기수 프로젝트 데이터를 삭제한다 | 호출 delete(DeleteSeedProjectDataCommand.of(null)) | 성공: 검증 assertThat(result.gisuId()).isEqualTo(10L); assertThat(result.deletedProjects()).isEqualTo(1); |

### QaAccountsStepTest
- 위치: `monolith/src/test/java/com/umc/product/test/application/service/qa/QaAccountsStepTest.java`

| 라인 | 테스트 케이스 | 입력/조건 | 기대 결과 |
|---:|---|---|---|
| [50](../../../monolith/src/test/java/com/umc/product/test/application/service/qa/QaAccountsStepTest.java#L50) | superadmin 계정을 만들지 못하면 그 계정의 권한 부여만 건너뛰고 나머지 시스템 역할은 준다 | 계정 작성이 superadmin에서만 IllegalStateException, 다시 읽은 회원 ID에 superadmin 없음 | 부분 실패: 생성 수 = 계정 수 - 1 + 시스템 역할 19건 - 1, 실패 코드 IllegalStateException 1건·SYSTEM_ROLE_HOLDER_MISSING 1건, superadmin의 SUPER_ADMIN 부여는 호출하지 않음 |
| [78](../../../monolith/src/test/java/com/umc/product/test/application/service/qa/QaAccountsStepTest.java#L78) | 계정과 시스템 역할이 이미 모두 있으면 하나도 만들지 않고 전부 건너뛴 것으로 센다 | 계정 작성과 시스템 역할 부여가 모두 이미 있음(false) | 성공: 생성 0, 건너뜀 = 계정 수 + 시스템 역할 19건, 실패 없음 |

### QaGisuIdLoaderTest
- 위치: `monolith/src/test/java/com/umc/product/test/application/service/QaGisuIdLoaderTest.java`

| 라인 | 테스트 케이스 | 입력/조건 | 기대 결과 |
|---:|---|---|---|
| [27](../../../monolith/src/test/java/com/umc/product/test/application/service/QaGisuIdLoaderTest.java#L27) | 요청한 기수만 기수 번호별 ID로 돌려주고, 없는 기수는 빠진다 | 9·10·11기가 있을 때 10·11·12기를 요청 | 성공: 10기·11기 ID만 돌려줌 |
| [40](../../../monolith/src/test/java/com/umc/product/test/application/service/QaGisuIdLoaderTest.java#L40) | 요청한 기수의 행이 둘이면 어느 쪽도 고르지 않고 실패한다 | 11기 행이 두 개일 때 11기를 요청 | 실패: IllegalStateException, 메시지에 11기 |
| [52](../../../monolith/src/test/java/com/umc/product/test/application/service/QaGisuIdLoaderTest.java#L52) | 요청하지 않은 기수의 중복은 무시한다 | 10기가 두 개, 11기가 하나일 때 11기만 요청 | 성공: 11기 ID만 돌려줌 |
| [65](../../../monolith/src/test/java/com/umc/product/test/application/service/QaGisuIdLoaderTest.java#L65) | 한 기수를 찾을 때 그 기수가 없으면 빈 값을 돌려준다 | 10기만 있을 때 find(11) | 성공: 빈 Optional |
| [75](../../../monolith/src/test/java/com/umc/product/test/application/service/QaGisuIdLoaderTest.java#L75) | 한 기수를 찾을 때 그 기수의 행이 둘이면 실패한다 | 11기 행이 두 개일 때 find(11) | 실패: IllegalStateException, 메시지에 11기 |

### QaSeedBeanGuardTest
- 위치: `monolith/src/test/java/com/umc/product/test/application/service/qa/QaSeedBeanGuardTest.java`

| 라인 | 테스트 케이스 | 입력/조건 | 기대 결과 |
|---:|---|---|---|
| [13](../../../monolith/src/test/java/com/umc/product/test/application/service/qa/QaSeedBeanGuardTest.java#L13) | QA 시딩 단계 빈은 @Profile(!prod) + @ConditionalOnProperty(app.seed.enabled=true) 가드를 가진다 | 파라미터 @ValueSource classes = { QaDirectoryLoader, QaAccountWriter, QaAccountsStep, QaLearningStep, QaAttendanceStep, QaNoticesStep, QaPointsStep, QaChatStep, QaProjectTransfersStep } | 성공: 각 클래스에 @Profile("!prod")와 app.seed.enabled=true 조건이 있음 |

### QaSeedOrchestratorTest
- 위치: `monolith/src/test/java/com/umc/product/test/application/service/qa/QaSeedOrchestratorTest.java`

| 라인 | 테스트 케이스 | 입력/조건 | 기대 결과 |
|---:|---|---|---|
| [24](../../../monolith/src/test/java/com/umc/product/test/application/service/qa/QaSeedOrchestratorTest.java#L24) | 계정 단계가 실패하면 뒤 단계는 실행하지 않고 NOT_RUN으로 돌려준다 | ACCOUNTS 단계가 IllegalStateException을 던짐 | 실패: ACCOUNTS만 실행, 첫 결과 FAILED·errorCode IllegalStateException, 나머지 단계 NOT_RUN |
| [44](../../../monolith/src/test/java/com/umc/product/test/application/service/qa/QaSeedOrchestratorTest.java#L44) | 계정 외 단계가 실패해도 다음 단계는 실행하고, 만든 것이 없으면 UNCHANGED다 | ACCOUNTS는 1건 생성, LEARNING은 IllegalArgumentException, 나머지는 건너뜀만 있음 | 성공: 모든 단계 결과가 순서대로 나오고 APPLIED, FAILED 다음 나머지는 UNCHANGED |
| [68](../../../monolith/src/test/java/com/umc/product/test/application/service/qa/QaSeedOrchestratorTest.java#L68) | 단계 실행기가 빠지면 기동할 때 실패한다 | 단계 실행기 목록에서 마지막 하나를 빼고 생성 | 실패: IllegalStateException |

### RehearsalSeedBeanGuardTest
- 위치: `monolith/src/test/java/com/umc/product/test/application/service/rehearsal/RehearsalSeedBeanGuardTest.java`

| 라인 | 테스트 케이스 | 입력/조건 | 기대 결과 |
|---:|---|---|---|
| [13](../../../monolith/src/test/java/com/umc/product/test/application/service/rehearsal/RehearsalSeedBeanGuardTest.java#L13) | 매칭 리허설 빈은 @Profile(!prod) + @ConditionalOnProperty(app.seed.enabled=true) 가드를 가진다 | 파라미터 @ValueSource classes = { RehearsalContextLoader, StartingLineSeedService, RoundSummaryQueryService, RoundSnapshotReader, MatchingBotService, BotApplicationWriter } | 성공: 각 클래스에 @Profile("!prod")와 app.seed.enabled=true 조건이 있음 |

## Repository / Outbound Persistence

### ProjectSeedDataCleanupPersistenceAdapterTest
- 위치: `monolith/src/test/java/com/umc/product/test/adapter/out/persistence/ProjectSeedDataCleanupPersistenceAdapterTest.java`

| 라인 | 테스트 케이스 | 입력/조건 | 기대 결과 |
|---:|---|---|---|
| [41](../../../monolith/src/test/java/com/umc/product/test/adapter/out/persistence/ProjectSeedDataCleanupPersistenceAdapterTest.java#L41) | 대상 기수의 프로젝트 관련 데이터만 모두 삭제한다 | 호출 deleteByGisuId(targetGisu.getId()) | 성공: 검증 assertThat(result.deletedProjects()).isEqualTo(1); assertThat(result.deletedProjectMembers()).isEqualTo(1); assertThat(result.deletedProjectApplications()).isEqualTo(1); assertThat(result.deletedProjectApplicationForm... |

## E2E / Integration

### QaSeedIntegrationTest
- 위치: `monolith/src/test/java/com/umc/product/test/adapter/in/web/QaSeedIntegrationTest.java`

| 라인 | 테스트 케이스 | 입력/조건 | 기대 결과 |
|---:|---|---|---|
| [59](../../../monolith/src/test/java/com/umc/product/test/adapter/in/web/QaSeedIntegrationTest.java#L59) | 빈 DB에서 QA 시딩 전체를 부르면 계정 597개와 운영진·시스템 역할을 만들고, 다시 부르면 아무것도 바꾸지 않는다 | Testcontainers DB, POST /test/seed/qa 두 번 | 성공: 첫 호출에서 계정 597개, 11기 챌린저 역할 152개, SUPER_ADMIN 1명·UMC_PRODUCT 18명, 지부 이동 4건 생성. 두 번째 호출은 모두 건너뛰고 테이블 행 수가 같음 |

### MatchingRehearsalIntegrationTest
- 위치: `monolith/src/test/java/com/umc/product/test/adapter/in/web/MatchingRehearsalIntegrationTest.java`

| 라인 | 테스트 케이스 | 입력/조건 | 기대 결과 |
|---:|---|---|---|
| [59](../../../monolith/src/test/java/com/umc/product/test/adapter/in/web/MatchingRehearsalIntegrationTest.java#L59) | 빈 DB에서 QA 계정, 출발선, 쿼카 1차 개발 차수의 봇 지원·결정을 차례로 만들고, 각 단계를 다시 부르면 아무것도 바꾸지 않는다 | Testcontainers DB, QA 시딩 → 출발선 → 차수 시각을 SQL로 옮기며 봇 지원·결정 | 성공: 출발선 59개 적용, 지원 기간 밖 봇 호출은 409, null 제외 목록은 400, 봇 지원·결정 결과가 차수 요약과 맞고 재호출은 UNCHANGED |

### MatchingRehearsalFlowIntegrationTest
- 위치: `monolith/src/test/java/com/umc/product/test/adapter/in/web/MatchingRehearsalFlowIntegrationTest.java`

| 라인 | 테스트 케이스 | 입력/조건 | 기대 결과 |
|---:|---|---|---|
| [74](../../../monolith/src/test/java/com/umc/product/test/adapter/in/web/MatchingRehearsalFlowIntegrationTest.java#L74) | 빈 DB에서 QA 계정과 출발선을 만들고 쿼카 1차 개발을 발표까지 넘긴 뒤, 프로젝트를 지우고 출발선을 다시 만들어도 같은 모양이고 계정·지부 이동은 남는다 | Testcontainers DB, 차수 넘기기 API로 1차를 발표까지 진행한 뒤 DELETE /test/seed/projects와 출발선 재실행 | 성공: 1차 발표 뒤 2차 지원 시작 가능, 출발선을 다시 만들면 지부별 결과가 같고 계정 597개와 지부 이동은 유지 |

### MatchingRoundAdvanceIntegrationTest
- 위치: `monolith/src/test/java/com/umc/product/test/adapter/in/web/MatchingRoundAdvanceIntegrationTest.java`

| 라인 | 테스트 케이스 | 입력/조건 | 기대 결과 |
|---:|---|---|---|
| [71](../../../monolith/src/test/java/com/umc/product/test/adapter/in/web/MatchingRoundAdvanceIntegrationTest.java#L71) | 단계를 한 칸씩 넘기면 DB 시각을 지금으로 당기고 새 시각으로 알람을 다시 맞추며, 순서가 어긋나면 409로 거부한다 | 1·2·3차와 랜덤 차수를 사흘 뒤부터 등록, POST /test/seed/matching/rounds/{roundId}/advance | 성공: 넘긴 단계 시각이 지금이 되고 알람 4개를 다시 등록, 같은 단계 재호출은 ALREADY_REACHED. 실패: 단계 건너뛰기·앞 차수 미발표·랜덤 배정 전 조정은 409, 없는 차수는 404, 단계 없는 요청 본문은 400 |
| [122](../../../monolith/src/test/java/com/umc/product/test/adapter/in/web/MatchingRoundAdvanceIntegrationTest.java#L122) | 선발 마감으로 넘기면 실제 스케줄러가 1분 뒤 자동 선발하고, 발표로 넘기면 결과를 공개해 다음 차수를 시작할 수 있다 | 실제 스케줄러로 자동 선발과 발표를 기다림(최대 90초, 테스트 하나에 약 1분) | 성공: 자동 선발 뒤 발표로 넘기면 결과가 공개되고 2차 지원이 열림 |
| [148](../../../monolith/src/test/java/com/umc/product/test/adapter/in/web/MatchingRoundAdvanceIntegrationTest.java#L148) | 프로젝트 초기화는 지운 차수의 알람도 취소한다 | 차수 등록 뒤 DELETE /test/seed/projects | 성공: 차수 4개를 지우고 각 차수 알람이 취소됨 |

## Domain

### AdvancePlannerTest
- 위치: `monolith/src/test/java/com/umc/product/test/domain/rehearsal/AdvancePlannerTest.java`

| 라인 | 테스트 케이스 | 입력/조건 | 기대 결과 |
|---:|---|---|---|
| [40](../../../monolith/src/test/java/com/umc/product/test/domain/rehearsal/AdvancePlannerTest.java#L40) | 리허설 전체를 단계마다 넘기면 매번 도메인 시각 규칙을 지키고, 같은 단계를 다시 넘기면 이미 도달로 끝난다 | 파라미터 등록 시각 3가지(사흘 뒤, 오늘 몇 시간 안, 1차만 오늘) | 성공: 매 단계 결과가 ProjectMatchingRound.validateSchedule·validatePhaseSequence를 통과하고 모든 차수가 발표됨 |
| [51](../../../monolith/src/test/java/com/umc/product/test/domain/rehearsal/AdvancePlannerTest.java#L51) | 리허설 중 어느 상태에서 어느 차수·단계를 골라도 다시 쓰는 것은 다음 한 단계뿐이고, 그 결과는 도메인 시각 규칙을 지킨다 | 파라미터 등록 시각 3가지, 지나온 모든 상태 × 차수 × 단계 | 성공: 상태마다 Rewrite는 최대 1개이고 그 결과가 도메인 시각 규칙을 지킴 |
| [88](../../../monolith/src/test/java/com/umc/product/test/domain/rehearsal/AdvancePlannerTest.java#L88) | 1차 지원 시작은 시작을 지금으로, 내일보다 이른 뒤 단계와 뒤 차수 시각을 내일부터 한 시간 간격 슬롯으로 민다 | 오늘 몇 시간 안에 몰아 등록한 묶음에서 1차 APPLICATION_START | 성공: 1차 시작은 지금, 나머지 시각은 내일부터 한 시간 간격 |
| [108](../../../monolith/src/test/java/com/umc/product/test/domain/rehearsal/AdvancePlannerTest.java#L108) | QA가 내일 이후로 등록한 시각은 앞 시각과 간격이 맞으면 그대로 둔다 | 사흘 뒤부터 등록한 묶음에서 1차 APPLICATION_START | 성공: 1차 시작만 지금으로 바뀜 |
| [124](../../../monolith/src/test/java/com/umc/product/test/domain/rehearsal/AdvancePlannerTest.java#L124) | 내일 이후 시각이라도 민 앞 시각과 간격이 모자라면 앞 시각 한 시간 뒤로 민다 | 발표 시각이 내일 2분 뒤인 1차에서 APPLICATION_START | 성공: 발표가 민 선발 마감 한 시간 뒤로 이동 |
| [140](../../../monolith/src/test/java/com/umc/product/test/domain/rehearsal/AdvancePlannerTest.java#L140) | 발표로 넘기면 발표를 지금으로, 선발 마감을 5분 이상 앞으로 당긴다 | 자동 선발이 끝난 1차에서 RESULT_PUBLISH | 성공: 발표는 지금, 선발 마감은 5분 전, 지원 마감은 그 1초 전 |
| [161](../../../monolith/src/test/java/com/umc/product/test/domain/rehearsal/AdvancePlannerTest.java#L161) | 3차를 발표로 넘기면 랜덤 차수 발표 시각을 내일 이후로 둬 랜덤 배정보다 먼저 돌지 않게 한다 | 3차 RESULT_PUBLISH, 랜덤 차수가 오늘 등록됨 | 성공: 랜덤 차수 시각이 내일부터 슬롯으로 이동 |
| [184](../../../monolith/src/test/java/com/umc/product/test/domain/rehearsal/AdvancePlannerTest.java#L184) | 랜덤 조정 시작·종료·최종 공개는 각 시각을 지금으로 두고 조정 종료와 선발 마감을 같게 유지한다 | 랜덤 배정이 끝난 랜덤 차수에서 ADJUSTMENT_START → ADJUSTMENT_END → FINAL_PUBLISH | 성공: 각 시각이 지금이 되고 조정 종료 = 선발 마감 |
| [215](../../../monolith/src/test/java/com/umc/product/test/domain/rehearsal/AdvancePlannerTest.java#L215) | 앞 차수 발표 전 다음 차수 시작, 자동 선발 전 발표, 단계 건너뛰기, 랜덤 배정 전 조정 시작을 거부한다 | 순서가 어긋난 단계 요청 | 실패: PREVIOUS_ROUND_NOT_PUBLISHED, AUTO_DECISION_NOT_EXECUTED, STEP_OUT_OF_ORDER, RANDOM_NOT_ASSIGNED |
| [237](../../../monolith/src/test/java/com/umc/product/test/domain/rehearsal/AdvancePlannerTest.java#L237) | 이미 지난 단계를 다시 넘기면 시각을 바꾸지 않는다 | 같은 단계를 두 번 요청 | 성공: AlreadyReached |
| [248](../../../monolith/src/test/java/com/umc/product/test/domain/rehearsal/AdvancePlannerTest.java#L248) | 발표 시각이 없는 기존 차수가 섞인 묶음은 넘기지 않는다 | resultPublishAt이 null인 차수 | 실패: STEP_OUT_OF_ORDER |

### BotApplyPlannerTest
- 위치: `monolith/src/test/java/com/umc/product/test/domain/rehearsal/BotApplyPlannerTest.java`

| 라인 | 테스트 케이스 | 입력/조건 | 기대 결과 |
|---:|---|---|---|
| [37](../../../monolith/src/test/java/com/umc/product/test/domain/rehearsal/BotApplyPlannerTest.java#L37) | 팀 없는 사람 중 QA 몫과 제외 대상을 빼고 정확히 round(0.8 × 풀)명이 TO 남은 프로젝트에 지원한다 | 후보 21명(팀 있음 1명, QA 몫 1명, 제외 1명), TO 남은 프로젝트 1개, 지원 비율 0.8 | 성공: 풀 18명 중 14명이 TO 남은 프로젝트에 지원하고 나머지는 사유별로 건너뜀 |
| [56](../../../monolith/src/test/java/com/umc/product/test/domain/rehearsal/BotApplyPlannerTest.java#L56) | 계획대로 지원한 뒤의 스냅샷으로 다시 계획하면 새 지원이 없다 | 계획 실행 뒤 스냅샷으로 재계획 | 성공: 새 지원 0건 |
| [78](../../../monolith/src/test/java/com/umc/product/test/domain/rehearsal/BotApplyPlannerTest.java#L78) | 직전 차수에 떨어진 프로젝트는 고르지 않고, 이 차수 임시저장 지원서는 그 지원서를 이어 제출한다 | 직전 차수 불합격 이력과 이 차수 DRAFT 지원서 | 성공: 불합격 프로젝트 제외, DRAFT는 이어 제출 |
| [95](../../../monolith/src/test/java/com/umc/product/test/domain/rehearsal/BotApplyPlannerTest.java#L95) | 출발선 PO 59명의 인기도로 고르면 상위 20% 프로젝트가 지원의 절반 안팎을 받는다 | 출발선 PO 59명 인기도 가중치 | 성공: 상위 20% 프로젝트가 지원 절반 안팎을 받음 |

### BotDecisionPlannerTest
- 위치: `monolith/src/test/java/com/umc/product/test/domain/rehearsal/BotDecisionPlannerTest.java`

| 라인 | 테스트 케이스 | 입력/조건 | 기대 결과 |
|---:|---|---|---|
| [35](../../../monolith/src/test/java/com/umc/product/test/domain/rehearsal/BotDecisionPlannerTest.java#L35) | 1차 TO 6·지원 9면 올림(0.6 × 6) = 4명 합격, 남은 5명 중 2명 불합격, 3명은 미결정으로 두고 합격을 먼저 한다 | 1차, TO 6, 지원 9 | 성공: 합격 4, 불합격 2, 미결정 3, 합격 목록이 먼저 |
| [49](../../../monolith/src/test/java/com/umc/product/test/domain/rehearsal/BotDecisionPlannerTest.java#L49) | 목표는 최소 선발 인원 아래로 내려가지 않고, 이미 채운 인원을 빼며, 남은 TO와 지원자 수를 넘지 않는다 | TO·이미 채운 인원·지원자 수·비율 조합 5가지 | 성공: 최소 선발 인원 이상, 남은 TO·지원자 수 이하 |
| [59](../../../monolith/src/test/java/com/umc/product/test/domain/rehearsal/BotDecisionPlannerTest.java#L59) | QA 프로젝트와 제외 프로젝트는 결정하지 않고, 이미 결정된 지원서는 바꾸지 않으며, 다시 계획하면 결정이 없다 | QA 프로젝트, 제외 프로젝트, 이미 결정된 지원서가 섞인 스냅샷 | 성공: 해당 프로젝트·지원서는 건드리지 않고 재계획은 빈 결과 |
| [98](../../../monolith/src/test/java/com/umc/product/test/domain/rehearsal/BotDecisionPlannerTest.java#L98) | 지터는 PO마다 고정이고 0.8~1.2배 안에 있다 | PO 200명의 지터 계산 | 성공: 모두 0.8~1.2배 |

### RoundSummaryTest
- 위치: `monolith/src/test/java/com/umc/product/test/domain/rehearsal/RoundSummaryTest.java`

| 라인 | 테스트 케이스 | 입력/조건 | 기대 결과 |
|---:|---|---|---|
| [30](../../../monolith/src/test/java/com/umc/product/test/domain/rehearsal/RoundSummaryTest.java#L30) | 프로젝트·파트별로 제출한 지원을 봇·QA로 나누고 합격·불합격·미결정과 채움률을 센다 | 봇·QA 지원이 섞인 스냅샷 | 성공: 프로젝트·파트별 봇/QA 지원 수, 합격·불합격·미결정, 채움률 |
| [65](../../../monolith/src/test/java/com/umc/product/test/domain/rehearsal/RoundSummaryTest.java#L65) | 자동 선발 뒤 채움률은 지원서 합격 수 대신 이 차수가 선발한 팀원 수로 센다 | 자동 선발이 끝난 차수 | 성공: 채움률 분자가 이 차수 선발 팀원 수 |
| [84](../../../monolith/src/test/java/com/umc/product/test/domain/rehearsal/RoundSummaryTest.java#L84) | 1~3차 지금 단계는 지원 전, 지원 중, 결정 중, 자동 선발 대기 순서로 시각에서 나온다 | 시작 1초 전, 지원 마감, 지원 마감 1초 뒤, 선발 마감 1초 뒤 | 성공: BEFORE_START → APPLICATION_OPEN → DECISION_WINDOW → AWAITING_AUTO_DECISION |

### SeedHashTest
- 위치: `monolith/src/test/java/com/umc/product/test/domain/rehearsal/SeedHashTest.java`

| 라인 | 테스트 케이스 | 입력/조건 | 기대 결과 |
|---:|---|---|---|
| [18](../../../monolith/src/test/java/com/umc/product/test/domain/rehearsal/SeedHashTest.java#L18) | 같은 seed·목적·키면 같은 값이고, 값은 [0, 1)에 고르게 퍼진다 | 키 1만 개, 같은 입력과 목적만 다른 입력 | 성공: 값은 [0, 1)이고 평균 0.5±0.02, 같은 입력은 같은 값, 목적이 다르면 다른 값 |
| [33](../../../monolith/src/test/java/com/umc/product/test/domain/rehearsal/SeedHashTest.java#L33) | 기본 seed는 지부·유형·차수 번호로만 정해진다 | 지부·유형·차수 번호를 바꿔 계산 | 성공: 같은 입력은 같은 seed, 차수나 지부가 다르면 다른 seed |

### QaPersonasTest
- 위치: `monolith/src/test/java/com/umc/product/test/domain/roster/QaPersonasTest.java`

| 라인 | 테스트 케이스 | 입력/조건 | 기대 결과 |
|---:|---|---|---|
| [22](../../../monolith/src/test/java/com/umc/product/test/domain/roster/QaPersonasTest.java#L22) | 시나리오가 쓰는 계정은 모두 계정 계획 597개 안에 있다 | QaPersonas의 모든 static Persona 필드 | 성공: 각 페르소나 이메일이 계정 계획 이메일 목록에 있음 |
| [31](../../../monolith/src/test/java/com/umc/product/test/domain/roster/QaPersonasTest.java#L31) | 옛 별칭은 노션 로그인 문서와 같은 university.neordinary.com 명단 계정으로 옮긴다 | 중앙대 학교 회장·파트장·인프라 담당, 중앙 총괄·교육, superadmin 페르소나 | 성공: 각 페르소나가 정해진 명단 계정 이메일로 풀리고, 중앙대 12·27번은 인프라 |

### QaSystemRolesTest
- 위치: `monolith/src/test/java/com/umc/product/test/domain/roster/QaSystemRolesTest.java`

| 라인 | 테스트 케이스 | 입력/조건 | 기대 결과 |
|---:|---|---|---|
| [27](../../../monolith/src/test/java/com/umc/product/test/domain/roster/QaSystemRolesTest.java#L27) | UMC_PRODUCT는 명단 밖 4명과 챌린저 역할 조합별 명단 계정 13명, 10기 계정 1명이 갖는다 | 클래스패스 명단과 시스템 역할 표로 만든 계정 계획 | 성공: UMC_PRODUCT 보유자 18명(명단 밖 4명, 명단 계정 13명, cau_springboot), SUPER_ADMIN은 superadmin 1명 |
| [47](../../../monolith/src/test/java/com/umc/product/test/domain/roster/QaSystemRolesTest.java#L47) | 명단 밖 계정 4개는 챌린저 없이 중앙대학교 소속 플덕QA1~4로 계획에 들어간다 | 명단 밖 별칭 umcproduct01~04 | 성공: 표시 이름 플덕QA1~4 순서, 챌린저 등록 없음, 학교 중앙대학교 |
| [61](../../../monolith/src/test/java/com/umc/product/test/domain/roster/QaSystemRolesTest.java#L61) | 시스템 역할을 받는 별칭은 모두 계획된 계정 하나에만 대응한다 | 계획의 모든 시스템 역할 부여 | 성공: 각 부여 이메일이 계정 이메일 목록에 정확히 1번 나옴 |
| [70](../../../monolith/src/test/java/com/umc/product/test/domain/roster/QaSystemRolesTest.java#L70) | 계획에 없는 별칭에 시스템 역할을 주면 계획을 만들지 않는다 | 명단에 없는 별칭 duksung_g11_qa99에 UMC_PRODUCT 부여 | 실패: IllegalArgumentException, 메시지에 그 별칭 |
| [81](../../../monolith/src/test/java/com/umc/product/test/domain/roster/QaSystemRolesTest.java#L81) | 명단 밖 계정 별칭이 이미 있는 계정과 겹치면 계획을 만들지 않는다 | 명단 밖 계정 별칭을 기존 계정 cau_springboot로 지정 | 실패: IllegalArgumentException, 메시지에 cau_springboot |

### RehearsalCastTest
- 위치: `monolith/src/test/java/com/umc/product/test/domain/roster/RehearsalCastTest.java`

| 라인 | 테스트 케이스 | 입력/조건 | 기대 결과 |
|---:|---|---|---|
| [37](../../../monolith/src/test/java/com/umc/product/test/domain/roster/RehearsalCastTest.java#L37) | 명단 583행과 superadmin, 10기 9개, 명단 밖 4개를 합쳐 서로 다른 이메일의 계정 597개를 계획한다 | 클래스패스 명단과 시스템 역할 표 | 성공: 명단 583행, 계정 597개, 이메일 597개 모두 다름, 첫 계정은 가천 1번(가천QA1), superadmin·cau_springboot·중앙대 38번 포함 |
| [51](../../../monolith/src/test/java/com/umc/product/test/domain/roster/RehearsalCastTest.java#L51) | 학교별 파트 인원이 계획 문서의 인원표와 같다 | 지부가 있는 학교 23곳의 PLAN·디자인·Web PE·Mobile PE 인원 | 성공: 인원표와 같고, 파트가 없는 행은 34 + 15 = 49개 |
| [72](../../../monolith/src/test/java/com/umc/product/test/domain/roster/RehearsalCastTest.java#L72) | 지부별 PLAN은 캥거루 13, 쿼카 19, 수달 13, 아홀로틀 13, 티라노 11명이다 | 지부 5곳의 명단 순 PLAN | 성공: 지부 순서 캥거루·쿼카·수달·아홀로틀·티라노, PLAN 13·19·13·13·11명 |
| [80](../../../monolith/src/test/java/com/umc/product/test/domain/roster/RehearsalCastTest.java#L80) | 출발선은 지부마다 QA 프로젝트 2개와 무프로젝트 PLAN 2명을 두고 프로젝트 59개 중 49개를 시딩한다 | 지부별 출발선 계획 | 성공: 지부마다 QA PO 2명·무프로젝트 PLAN 2명, QA·시딩 PO 합계 59명 중 시딩 49명, 캥거루 QA PO는 가천 9·10번, 무프로젝트는 한성 13·26번 |
| [98](../../../monolith/src/test/java/com/umc/product/test/domain/roster/RehearsalCastTest.java#L98) | QA 몫 A·B·C는 지부마다 매칭 파트별 3명이고 지부 이동 4명은 들어가지 않는다 | 지부별 QA 몫 | 성공: 지부마다 9명, 지부 이동 4명 제외, 모두 일반 챌린저이고 매칭 지부가 그 지부, 라벨 A·B·C 세 번 반복, 캥거루·아홀로틀 앞 3명 고정 |
| [120](../../../monolith/src/test/java/com/umc/product/test/domain/roster/RehearsalCastTest.java#L120) | 지부가 없는 명지·상명·인천가톨릭은 매칭에서 빠지고 이동 4명은 매칭 지부를 옮긴다 | mju·smu·iccu 1번, 동양미래 4번, 숭실 31번, 지부별 매칭 인원 | 성공: 세 학교는 매칭 지부 없음, 동양미래 4번은 학교 지부 쿼카·매칭 지부 캥거루, 숭실 31번은 수달, 매칭 인원 합계 디자인 72·Web 284·Mobile 109, 캥거루 디자인 12 |
| [139](../../../monolith/src/test/java/com/umc/product/test/domain/roster/RehearsalCastTest.java#L139) | 중앙 총괄·부총괄과 지부장 5명이 명단 역할에서 나온다 | 명단 역할 컬럼 | 성공: 중앙 총괄 덕성 3번, 부총괄 가천 30번, 지부장 가천 2·숭실 1·중앙 1·홍익 1·동덕 1번, Web 학교 파트장 중앙 5번 |
| [153](../../../monolith/src/test/java/com/umc/product/test/domain/roster/RehearsalCastTest.java#L153) | 담당 파트가 없는 파트장, 끊긴 순번, 학교표에 없는 학교는 명단 파싱에서 거부한다 | 파트 없는 SCHOOL_PART_LEADER, 순번 1 다음 3, 학교표에 없는 snu, 인프라 표시한 DESIGN 행 | 실패: 네 경우 모두 IllegalArgumentException |

### QuotaApportionmentTest
- 위치: `monolith/src/test/java/com/umc/product/test/domain/startingline/QuotaApportionmentTest.java`

| 라인 | 테스트 케이스 | 입력/조건 | 기대 결과 |
|---:|---|---|---|
| [25](../../../monolith/src/test/java/com/umc/product/test/domain/startingline/QuotaApportionmentTest.java#L25) | 지부마다 파트별 TO 합계는 매칭 인원의 n/(n+4)를 내림한 값이고 프로젝트마다 개발 파트는 하나다 | 11기 QA 명단의 지부별 매칭 인원 | 성공: 파트별 TO 합계 = 내림(매칭 인원 × n/(n+4)), 프로젝트마다 Web·Mobile 중 하나 |
| [48](../../../monolith/src/test/java/com/umc/product/test/domain/startingline/QuotaApportionmentTest.java#L48) | 캥거루는 시딩 9개에 디자인 8, Web 37, Mobile 13을 나누고 QA 몫으로 4, 17, 6을 남긴다 | 캥거루 지부 | 성공: 시딩 TO 디자인 8·Web 37·Mobile 13, 남은 몫 4·17·6 |
| [63](../../../monolith/src/test/java/com/umc/product/test/domain/startingline/QuotaApportionmentTest.java#L63) | 같은 입력이면 같은 배분이 나온다 | 같은 입력 두 번 | 성공: 같은 배분 |
| [73](../../../monolith/src/test/java/com/umc/product/test/domain/startingline/QuotaApportionmentTest.java#L73) | 한 개발 파트 인원이 0이면 모든 프로젝트가 다른 개발 파트를 쓴다 | 한 개발 파트 매칭 인원 0 | 성공: 모든 프로젝트가 다른 개발 파트 |

### StartingLineStepTest
- 위치: `monolith/src/test/java/com/umc/product/test/domain/startingline/StartingLineStepTest.java`

| 라인 | 테스트 케이스 | 입력/조건 | 기대 결과 |
|---:|---|---|---|
| [18](../../../monolith/src/test/java/com/umc/product/test/domain/startingline/StartingLineStepTest.java#L18) | 프로젝트가 없으면 초안부터, 초안은 정보 → 지원 폼 → 제출 순서로 남은 단계부터 이어 간다 | 프로젝트 없음, 새 초안, 정보까지 채운 초안, 지원 폼까지 채운 초안 | 성공: CREATE_DRAFT → UPDATE_INFO → UPSERT_FORM → SUBMIT |
| [29](../../../monolith/src/test/java/com/umc/product/test/domain/startingline/StartingLineStepTest.java#L29) | QA 프로젝트는 승인 대기에서 멈추고, 시딩 프로젝트는 TO가 다를 때만 TO를 넣고 공개한다 | PENDING_REVIEW 프로젝트, 목표 TO 없음(QA)·있음(시딩) | 성공: QA는 다음 단계 없음, 시딩은 SET_QUOTAS, TO가 맞으면 PUBLISH |
| [42](../../../monolith/src/test/java/com/umc/product/test/domain/startingline/StartingLineStepTest.java#L42) | 이미 공개된 프로젝트는 QA·시딩 모두 건드리지 않는다 | IN_PROGRESS 프로젝트 | 성공: QA·시딩 모두 다음 단계 없음 |
