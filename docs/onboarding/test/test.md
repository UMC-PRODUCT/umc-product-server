# Test Seed 테스트 케이스

- 테스트 파일: 7개
- 테스트 케이스: 28개
- 분류 기준: `Controller`, `UseCase`, `Repository`, `E2E`, `Scheduler`, `Domain`, `External Adapter`, `Support`

| 카테고리 | 케이스 수 |
|---|---:|
| Controller / Inbound Adapter | 1 |
| UseCase / Application Service | 26 |
| Repository / Outbound Persistence | 1 |

## Controller / Inbound Adapter

### SeedControllerBeanRegistrationTest
- 위치: `monolith/src/test/java/com/umc/product/test/adapter/in/web/SeedControllerBeanRegistrationTest.java`

| 라인 | 테스트 케이스 | 입력/조건 | 기대 결과 |
|---:|---|---|---|
| [38](../../../monolith/src/test/java/com/umc/product/test/adapter/in/web/SeedControllerBeanRegistrationTest.java#L38) | 모든 시딩 빈은 @Profile(!prod) + @ConditionalOnProperty(app.seed.enabled=true) 가드를 가진다 | 파라미터 @ValueSource classes = { SeedController, QaSeedController, MatchingRoundAdvanceController, MatchingRoundAdvanceService, MatchingRoundTimeRewritePersistenceAdapter, QaSeedOrchestrator, QaSeedMarkerPersistenceAdapter, ClasspathQaRosterAdapter, MemberSeedService, MemberSystemRoleSeedService, MemberSystemRoleSeedPersistenceAdapter, ChallengerSeedService, StartingLineSeedController, MatchingBotController, SeedProjectReadPersistenceAdapter, ProjectSeedDataCleanupService, CurriculumSeedService, NoticeSeedService, DummyMemberFactory, DummyCurriculumFactory, DummyNoticeFactory, SeedProperties } | 성공: 각 클래스에 @Profile("!prod")와 app.seed.enabled 조건이 있음 |

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

## Repository / Outbound Persistence

### ProjectSeedDataCleanupPersistenceAdapterTest
- 위치: `monolith/src/test/java/com/umc/product/test/adapter/out/persistence/ProjectSeedDataCleanupPersistenceAdapterTest.java`

| 라인 | 테스트 케이스 | 입력/조건 | 기대 결과 |
|---:|---|---|---|
| [41](../../../monolith/src/test/java/com/umc/product/test/adapter/out/persistence/ProjectSeedDataCleanupPersistenceAdapterTest.java#L41) | 대상 기수의 프로젝트 관련 데이터만 모두 삭제한다 | 호출 deleteByGisuId(targetGisu.getId()) | 성공: 검증 assertThat(result.deletedProjects()).isEqualTo(1); assertThat(result.deletedProjectMembers()).isEqualTo(1); assertThat(result.deletedProjectApplications()).isEqualTo(1); assertThat(result.deletedProjectApplicationForm... |
