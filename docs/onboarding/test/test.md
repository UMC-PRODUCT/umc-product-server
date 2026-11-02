# Test Seed 테스트 케이스

- 테스트 파일: 9개
- 테스트 케이스: 44개
- 분류 기준: `Controller`, `UseCase`, `Repository`, `E2E`, `Scheduler`, `Domain`, `External Adapter`, `Support`

| 카테고리 | 케이스 수 |
|---|---:|
| Controller / Inbound Adapter | 7 |
| UseCase / Application Service | 36 |
| Repository / Outbound Persistence | 1 |

## Controller / Inbound Adapter

### SeedControllerBeanRegistrationTest
- 위치: `monolith/src/test/java/com/umc/product/test/adapter/in/web/SeedControllerBeanRegistrationTest.java`

| 라인 | 테스트 케이스 | 입력/조건 | 기대 결과 |
|---:|---|---|---|
| [34](../../../monolith/src/test/java/com/umc/product/test/adapter/in/web/SeedControllerBeanRegistrationTest.java#L34) | 모든 시딩 빈은 @Profile(!prod) + @ConditionalOnProperty(app.seed.enabled=true) 가드를 가진다 | 파라미터 @ValueSource classes = { SeedController.class, MemberSeedService.class, MemberSystemRoleSeedService.class, MemberSystemRoleSeedPersistenceAdapter.class, ChallengerSeedService.class, ProjectScenarioSeedService.class, ProjectSeedDataCleanupService.class, ProjectApplicationSeedService.class, CurriculumSeedService.class, NoticeSeedService.class, DummyMemberFactory.class, DummyCurriculumFactory.class, DummyNoticeFactory.class, SeedProperties.class } | 성공: 검증 assertThat(profile); .isNotNull(); assertThat(profile.value()); .contains("!prod"); |

### ProjectScenarioSeedIntegrationTest
- 위치: `monolith/src/test/java/com/umc/product/test/adapter/in/web/ProjectScenarioSeedIntegrationTest.java`

| 라인 | 테스트 케이스 | 입력/조건 | 기대 결과 |
|---:|---|---|---|
| [67](../../../monolith/src/test/java/com/umc/product/test/adapter/in/web/ProjectScenarioSeedIntegrationTest.java#L67) | 총괄단이 공개 시나리오를 시딩하면 지부 TO 상한 안에서 TO를 걸고 프로젝트를 공개한다 | PLAN 4 · Design 3 · Web PE 5 · Mobile PE 4, 총괄단, IN_PROGRESS 3건 | 성공: 3건 모두 IN_PROGRESS, 응답 partQuotas 와 저장된 TO 일치, 웹·모바일 PE 동시 TO 없음, 지부 파트별 TO 합계 ≤ 파트 인원 |
| [104](../../../monolith/src/test/java/com/umc/product/test/adapter/in/web/ProjectScenarioSeedIntegrationTest.java#L104) | 지부에 남은 TO가 없으면 프로젝트를 만들지 않고 QUOTA 실패로 보고한다 | PLAN 3 · Mobile PE 3, 지부장, IN_PROGRESS 3건 | 성공: 1건 생성(Mobile PE 3), 2건 failedStep=QUOTA · projectId 없음, 프로젝트 1건만 저장 |
| [127](../../../monolith/src/test/java/com/umc/product/test/adapter/in/web/ProjectScenarioSeedIntegrationTest.java#L127) | PO를 지정하지 않으면 운영진이 관리하는 지부의 PLAN 챌린저만 PO로 고른다 | 자기 지부 PLAN 2, 다른 지부 PLAN 3, 지부장, IN_PROGRESS 2건 | 성공: 2건 모두 자기 지부, PO 는 자기 지부 PLAN |
| [151](../../../monolith/src/test/java/com/umc/product/test/adapter/in/web/ProjectScenarioSeedIntegrationTest.java#L151) | 운영진이 관리하지 않는 지부의 PO를 지정하면 프로젝트를 만들지 않고 QUOTA 실패로 보고한다 | 다른 지부 PLAN 을 productOwnerMemberIds 로 지정, 지부장 | 실패: failedStep=QUOTA · projectId 없음, 프로젝트 0건 |
| [175](../../../monolith/src/test/java/com/umc/product/test/adapter/in/web/ProjectScenarioSeedIntegrationTest.java#L175) | adminMemberId 없이 공개 시나리오를 요청하면 400으로 거부하고 프로젝트를 만들지 않는다 | IN_PROGRESS, adminMemberId 없음 | 실패: 400, 프로젝트 0건 |
| [192](../../../monolith/src/test/java/com/umc/product/test/adapter/in/web/ProjectScenarioSeedIntegrationTest.java#L192) | 승인 대기 시나리오는 PO가 직접 제출해 승인 대기 상태로 남고 TO를 걸지 않는다 | PLAN 1, PENDING_REVIEW 1건 | 성공: PENDING_REVIEW, partQuotas 빈 목록, 저장된 TO 없음 |

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

### ChapterQuotaBudgetTest
- 위치: `monolith/src/test/java/com/umc/product/test/application/service/ChapterQuotaBudgetTest.java`

| 라인 | 테스트 케이스 | 입력/조건 | 기대 결과 |
|---:|---|---|---|
| [31](../../../monolith/src/test/java/com/umc/product/test/application/service/ChapterQuotaBudgetTest.java#L31) | 여유가 바닥날 때까지 배정해도 파트별 TO 합계는 여유 인원을 넘지 않는다 | seed 50개, PLAN 100 · Design 3 · Web PE 7 · Mobile PE 5 | 성공: 파트별 배정 합계 ≤ 여유 인원 |
| [54](../../../monolith/src/test/java/com/umc/product/test/application/service/ChapterQuotaBudgetTest.java#L54) | 한 프로젝트에 웹과 모바일 프로덕트 엔지니어 TO를 함께 걸지 않는다 | Web PE · Mobile PE 여유 1000, 200회 배정 | 성공: 한 배정에 두 파트가 함께 없음, 두 파트 모두 한 번 이상 선택 |
| [78](../../../monolith/src/test/java/com/umc/product/test/application/service/ChapterQuotaBudgetTest.java#L78) | 여유 인원이 0 이하인 파트와 TO를 바꿀 수 없는 파트는 고르지 않는다 | Design 여유 음수, Web PE 여유 0, Spring Boot 잠금 | 성공: Mobile PE 만 배정 |
| [101](../../../monolith/src/test/java/com/umc/product/test/application/service/ChapterQuotaBudgetTest.java#L101) | 파트 여유가 TO 범위 최솟값보다 작으면 남은 여유만큼만 TO를 건다 | Mobile PE 여유 2 | 성공: Mobile PE TO 2 |
| [116](../../../monolith/src/test/java/com/umc/product/test/application/service/ChapterQuotaBudgetTest.java#L116) | 프로젝트마다 PLAN 1자리를 쓰고 PLAN 여유가 없으면 더 배정하지 않는다 | PLAN 여유 2 | 성공: 두 번 배정 후 세 번째는 Exhausted |
| [136](../../../monolith/src/test/java/com/umc/product/test/application/service/ChapterQuotaBudgetTest.java#L136) | TO를 걸 파트가 하나도 없으면 PLAN 여유가 있어도 배정하지 않는다 | PLAN 5, 다른 파트 여유 0 | 실패: Exhausted |

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
| [57](../../../monolith/src/test/java/com/umc/product/test/application/service/MemberSeedServiceTest.java#L57) | force=false 이고 회원 수가 임계값 초과면 시딩을 스킵한다 | 호출 seed(new SeedMembersCommand(5, false)) | 실패: 검증 assertThat(result.skipped()).isTrue(); assertThat(result.reason()).contains("threshold"); |
| [72](../../../monolith/src/test/java/com/umc/product/test/application/service/MemberSeedServiceTest.java#L72) | force=true 면 임계값 체크를 무시하고 시딩한다 | 호출 seed(new SeedMembersCommand(2, true)) | 성공: 검증 assertThat(result.skipped()).isFalse(); assertThat(result.registered()).isEqualTo(2); |
| [88](../../../monolith/src/test/java/com/umc/product/test/application/service/MemberSeedServiceTest.java#L88) | 정상 시딩 시 email batchRegister 가 1번 호출된다 | 호출 seed(new SeedMembersCommand(5, false)) | 성공: 정상 시딩 시 email batchRegister 가 1번 호출된다 |
| [103](../../../monolith/src/test/java/com/umc/product/test/application/service/MemberSeedServiceTest.java#L103) | email batchRegister 실패 시 등록 수는 0으로 반환된다 | 호출 seed(new SeedMembersCommand(3, false)) | 실패: 검증 assertThat(result.registered()).isZero(); |
| [120](../../../monolith/src/test/java/com/umc/product/test/application/service/MemberSeedServiceTest.java#L120) | count=0 이면 register 가 호출되지 않는다 | 호출 seed(new SeedMembersCommand(0, false)) | 성공: 검증 assertThat(result.skipped()).isFalse(); assertThat(result.registered()).isZero(); |
| [135](../../../monolith/src/test/java/com/umc/product/test/application/service/MemberSeedServiceTest.java#L135) | email 시퀀스는 현재 회원 수 + 1 부터 시작한다 | 호출 seed(new SeedMembersCommand(2, true)) | 성공: email 시퀀스는 현재 회원 수 + 1 부터 시작한다 |

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
