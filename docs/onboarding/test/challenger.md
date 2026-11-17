# Challenger 테스트 케이스

- 테스트 파일: 26개
- 테스트 케이스: 121개
- 분류 기준: `Controller`, `UseCase`, `Repository`, `E2E`, `Scheduler`, `Domain`, `External Adapter`, `Support`

| 카테고리 | 케이스 수 |
|---|---:|
| Controller / Inbound Adapter | 26 |
| UseCase / Application Service | 45 |
| Repository / Outbound Persistence | 7 |
| E2E / Integration | 9 |
| Scheduler | 7 |
| Domain | 27 |

## Controller / Inbound Adapter

### ChallengerCommandControllerTest
- 테스트 설명: ChallengerCommandController
- 위치: `monolith/src/test/java/com/umc/product/challenger/adapter/in/web/ChallengerCommandControllerTest.java`

| 라인 | 테스트 케이스 | 입력/조건 | 기대 결과 |
|---:|---|---|---|
| [61](../../../monolith/src/test/java/com/umc/product/challenger/adapter/in/web/ChallengerCommandControllerTest.java#L61) | ChallengerCommandController / 챌린저 생성 성공 시 생성된 챌린저 정보를 반환한다 | HTTP POST /api/v1/challenger; body {"memberId":1,"part":"SPRINGBOOT","gisuId":9} | 성공: HTTP 200 OK |
| [80](../../../monolith/src/test/java/com/umc/product/challenger/adapter/in/web/ChallengerCommandControllerTest.java#L80) | ChallengerCommandController / 챌린저 생성 요청의 part가 없으면 400 | HTTP POST /api/v1/challenger; body {"memberId":1,"gisuId":9} | 실패: HTTP 400 Bad Request |
| [93](../../../monolith/src/test/java/com/umc/product/challenger/adapter/in/web/ChallengerCommandControllerTest.java#L93) | ChallengerCommandController / 챌린저 batch 생성 요청 내부 항목의 part가 없으면 400 | HTTP POST /api/v1/challenger/batch; body [{"memberId":1,"gisuId":9}] | 실패: HTTP 400 Bad Request |
| [106](../../../monolith/src/test/java/com/umc/product/challenger/adapter/in/web/ChallengerCommandControllerTest.java#L106) | ChallengerCommandController / 파트 변경 요청의 newPart가 없으면 400 | HTTP PATCH /api/v1/challenger/{challengerId}/part; body {} | 실패: HTTP 400 Bad Request |

### ChallengerPointCommandControllerTest
- 테스트 설명: ChallengerPointCommandController
- 위치: `monolith/src/test/java/com/umc/product/challenger/adapter/in/web/ChallengerPointCommandControllerTest.java`

| 라인 | 테스트 케이스 | 입력/조건 | 기대 결과 |
|---:|---|---|---|
| [32](../../../monolith/src/test/java/com/umc/product/challenger/adapter/in/web/ChallengerPointCommandControllerTest.java#L32) | 상벌점 부여 성공 시 챌린저 정보를 반환한다 | HTTP POST /api/v1/challenger/{challengerId}/points; body {"pointType":"CUSTOM","pointValue":1,"description":"조정"} | 성공: HTTP 200 OK |
| [63](../../../monolith/src/test/java/com/umc/product/challenger/adapter/in/web/ChallengerPointCommandControllerTest.java#L63) | ChallengerPointCommandController / 상벌점 부여 요청의 pointType이 없으면 400 | HTTP POST /api/v1/challenger/{challengerId}/points; body {"pointValue":1,"description":"조정"} | 실패: HTTP 400 Bad Request |
| [76](../../../monolith/src/test/java/com/umc/product/challenger/adapter/in/web/ChallengerPointCommandControllerTest.java#L76) | ChallengerPointCommandController / 상벌점 설명을 수정한다 | HTTP PATCH /api/v1/challenger/points/{challengerPointId}; body {"newDescription":"수정"} | 성공: HTTP 200 OK |
| [87](../../../monolith/src/test/java/com/umc/product/challenger/adapter/in/web/ChallengerPointCommandControllerTest.java#L87) | ChallengerPointCommandController / 상벌점을 삭제한다 | HTTP DELETE /api/v1/challenger/points/{challengerPointId} | 성공: HTTP 200 OK |

### ChallengerQueryControllerTest
- 테스트 설명: ChallengerQueryController
- 위치: `monolith/src/test/java/com/umc/product/challenger/adapter/in/web/ChallengerQueryControllerTest.java`

| 라인 | 테스트 케이스 | 입력/조건 | 기대 결과 |
|---:|---|---|---|
| [24](../../../monolith/src/test/java/com/umc/product/challenger/adapter/in/web/ChallengerQueryControllerTest.java#L24) | 챌린저 단건 정보를 조회한다 | HTTP GET /api/v1/challenger/{challengerId} | 성공: HTTP 200 OK |

### ChallengerAdminQueryV2ControllerTest

- 위치: `monolith/src/test/java/com/umc/product/challenger/adapter/in/web/v2/ChallengerAdminQueryV2ControllerTest.java`

| 테스트 | 입력/조건 | 기대 결과 |
|---|---|---|
| 관리자용 챌린저 상세 정보를 조회한다 | HTTP GET /api/v2/challenger/admin/{challengerId} | 성공: HTTP 200 OK |

### ChallengerDeactivationCommandControllerTest

- 위치: `monolith/src/test/java/com/umc/product/challenger/adapter/in/web/ChallengerDeactivationCommandControllerTest.java`

| 테스트 | 입력/조건 | 기대 결과 |
|---|---|---|
| 현재 로그인한 운영진으로 탈부를 등록한다 | HTTP POST /api/v1/challenger/admin/{challengerId}/deactivations | 성공: HTTP 201 Created, 탈부 기록 ID 반환 |
| 사진이 5장을 초과하면 400을 반환한다 | imageIds 6개 | 실패: HTTP 400 Bad Request |
| 링크가 3개를 초과하면 400을 반환한다 | links 4개 | 실패: HTTP 400 Bad Request |
| 탈부 사유가 없으면 400을 반환한다 | reason 누락 | 실패: HTTP 400 Bad Request |
| 현재 로그인한 운영진으로 탈부 근거 자료를 추가한다 | HTTP POST /api/v1/challenger/admin/{challengerId}/deactivations/evidence | 성공: HTTP 201 Created |
| 추가할 사진이 5장을 초과하면 400을 반환한다 | imageIds 6개 | 실패: HTTP 400 Bad Request |

### ChallengerDeactivationQueryControllerTest

- 위치: `monolith/src/test/java/com/umc/product/challenger/adapter/in/web/ChallengerDeactivationQueryControllerTest.java`

| 테스트 | 입력/조건 | 기대 결과 |
|---|---|---|
| 자진 탈부와 타의 탈부 사유 목록을 조회한다 | HTTP GET /api/v1/challenger/admin/deactivations/reasons | 성공: HTTP 200 OK, 유형별 사유와 상세 입력 필수 여부 반환 |

### ChallengerRecordControllerTest
- 테스트 설명: ChallengerRecordController
- 위치: `monolith/src/test/java/com/umc/product/challenger/adapter/in/web/ChallengerRecordControllerTest.java`

| 라인 | 테스트 케이스 | 입력/조건 | 기대 결과 |
|---:|---|---|---|
| [60](../../../monolith/src/test/java/com/umc/product/challenger/adapter/in/web/ChallengerRecordControllerTest.java#L60) | ChallengerRecordController / 코드로 챌린저 기록을 조회한다 | HTTP GET /api/v1/challenger-record/code/{code} | 성공: HTTP 200 OK |
| [73](../../../monolith/src/test/java/com/umc/product/challenger/adapter/in/web/ChallengerRecordControllerTest.java#L73) | ChallengerRecordController / 회원 기록 추가 요청의 code가 blank이면 400 | HTTP POST /api/v1/challenger-record/member; body {"code":" "} | 실패: HTTP 400 Bad Request |
| [86](../../../monolith/src/test/java/com/umc/product/challenger/adapter/in/web/ChallengerRecordControllerTest.java#L86) | ChallengerRecordController / 기록 생성 요청의 gisuId가 없으면 400 | HTTP POST /api/v1/challenger-record; body {"chapterId":2,"schoolId":3,"part":"SPRINGBOOT","memberName":"홍길동"} | 실패: HTTP 400 Bad Request |
| [99](../../../monolith/src/test/java/com/umc/product/challenger/adapter/in/web/ChallengerRecordControllerTest.java#L99) | ChallengerRecordController / 기록 bulk 생성 요청 내부 항목의 gisuId가 없으면 400 | HTTP POST /api/v1/challenger-record/bulk; body [{"chapterId":2,"schoolId":3,"part":"SPRINGBOOT","memberName":"홍길동"}] | 실패: HTTP 400 Bad Request |

### ChallengerSearchControllerTest
- 테스트 설명: ChallengerSearchController
- 위치: `monolith/src/test/java/com/umc/product/challenger/adapter/in/web/ChallengerSearchControllerTest.java`

| 라인 | 테스트 케이스 | 입력/조건 | 기대 결과 |
|---:|---|---|---|
| [35](../../../monolith/src/test/java/com/umc/product/challenger/adapter/in/web/ChallengerSearchControllerTest.java#L35) | 커서 검색 size는 최대 50으로 제한된다 | HTTP GET /api/v1/challenger/search/cursor; param size="100" | 성공: HTTP 200 OK |
| [60](../../../monolith/src/test/java/com/umc/product/challenger/adapter/in/web/ChallengerSearchControllerTest.java#L60) | ChallengerSearchController / offset 검색 결과를 반환한다 | HTTP GET /api/v1/challenger/search/offset; param page="0"; param size="10" | 성공: HTTP 200 OK |

### ChallengerAdminSearchV2ControllerTest
- 테스트 설명: ChallengerAdminSearchV2Controller
- 위치: `monolith/src/test/java/com/umc/product/challenger/adapter/in/web/v2/ChallengerAdminSearchV2ControllerTest.java`

| 라인 | 테스트 케이스 | 입력/조건 | 기대 결과 |
|---:|---|---|---|
| [66](../../../monolith/src/test/java/com/umc/product/challenger/adapter/in/web/v2/ChallengerAdminSearchV2ControllerTest.java#L66) | 관리자용 챌린저 검색은 권한 범위가 적용된 검색을 호출하고 이메일을 마스킹한다 | HTTP GET /api/v2/challenger/admin/search; param gisuId="9"; param page="0"; param size="10" | 성공: HTTP 200 OK; 이메일 마스킹 |
| [96](../../../monolith/src/test/java/com/umc/product/challenger/adapter/in/web/v2/ChallengerAdminSearchV2ControllerTest.java#L96) | 관리자용 챌린저 검색의 기본 페이지 크기는 10이다 | HTTP GET /api/v2/challenger/admin/search | 성공: HTTP 200 OK; 기본 size 10 |
| [114](../../../monolith/src/test/java/com/umc/product/challenger/adapter/in/web/v2/ChallengerAdminSearchV2ControllerTest.java#L114) | 관리자용 챌린저 검색 권한이 없으면 403을 반환한다 | HTTP GET /api/v2/challenger/admin/search; param page="0"; param size="10" | 실패: HTTP 403 Forbidden; 에러코드 MEMBER-0015 |

### ChallengerSearchV2ControllerTest
- 테스트 설명: ChallengerSearchV2Controller
- 위치: `monolith/src/test/java/com/umc/product/challenger/adapter/in/web/v2/ChallengerSearchV2ControllerTest.java`

| 라인 | 테스트 케이스 | 입력/조건 | 기대 결과 |
|---:|---|---|---|
| [32](../../../monolith/src/test/java/com/umc/product/challenger/adapter/in/web/v2/ChallengerSearchV2ControllerTest.java#L32) | 챌린저 검색 v2 응답은 이메일을 마스킹한다 | HTTP GET /api/v2/challenger/search; param page="0"; param size="10" | 성공: HTTP 200 OK |

## UseCase / Application Service

### ChallengerActivityPeriodServiceTest
- 테스트 설명: ChallengerActivityPeriodService — 활동일 합산 로직
- 위치: `monolith/src/test/java/com/umc/product/challenger/application/service/ChallengerActivityPeriodServiceTest.java`

| 라인 | 테스트 케이스 | 입력/조건 | 기대 결과 |
|---:|---|---|---|
| [51](../../../monolith/src/test/java/com/umc/product/challenger/application/service/ChallengerActivityPeriodServiceTest.java#L51) | ChallengerActivityPeriodService — 활동일 합산 로직 / ACTIVE와 GRADUATED 챌린저의 기수만 합산한다 | 호출 calculateActivityPeriod(challengers, gisus) | 성공: 검증 assertThat(summary.perGisu()).hasSize(2); assertThat(summary.perGisu()); .containsExactlyInAnyOrder(10L, 11L); assertThat(summary.totalActivityDays()).isGreaterThan(0L); |
| [80](../../../monolith/src/test/java/com/umc/product/challenger/application/service/ChallengerActivityPeriodServiceTest.java#L80) | ChallengerActivityPeriodService — 활동일 합산 로직 / 챌린저가 없으면 빈 요약을 반환한다 | 호출 calculateActivityPeriod(List.of(), Map.of()) | 성공: 검증 assertThat(summary.totalActivityDays()).isZero(); assertThat(summary.perGisu()).isEmpty(); |
| [88](../../../monolith/src/test/java/com/umc/product/challenger/application/service/ChallengerActivityPeriodServiceTest.java#L88) | ChallengerActivityPeriodService — 활동일 합산 로직 / EXPELLED WITHDRAWN만 있으면 0일을 반환한다 | 호출 calculateActivityPeriod(challengers, gisus) | 성공: 검증 assertThat(summary.totalActivityDays()).isZero(); assertThat(summary.perGisu()).isEmpty(); |
| [109](../../../monolith/src/test/java/com/umc/product/challenger/application/service/ChallengerActivityPeriodServiceTest.java#L109) | 진행중인 기수는 now까지의 일수만 포함한다 | 호출 calculateActivityPeriod(challengers, gisus) | 성공: 검증 assertThat(summary.totalActivityDays()).isBetween(29L, 31L); |
| [128](../../../monolith/src/test/java/com/umc/product/challenger/application/service/ChallengerActivityPeriodServiceTest.java#L128) | 종료된 기수는 전체 기간을 합산한다 | 호출 calculateActivityPeriod(challengers, gisus) | 성공: 검증 assertThat(summary.totalActivityDays()).isEqualTo(ChronoUnit.DAYS.between(start, end)); |
| [146](../../../monolith/src/test/java/com/umc/product/challenger/application/service/ChallengerActivityPeriodServiceTest.java#L146) | getActivityPeriodByMemberId는 내부에서 챌린저와 기수를 조회한다 | 호출 getActivityPeriodByMemberId(100L) | 성공: 검증 assertThat(summary.totalActivityDays()).isEqualTo(ChronoUnit.DAYS.between(start, end)); |
| [162](../../../monolith/src/test/java/com/umc/product/challenger/application/service/ChallengerActivityPeriodServiceTest.java#L162) | getActivityPeriodByMemberId는 챌린저 0건이면 빈 요약을 반환한다 | 호출 getActivityPeriodByMemberId(100L) | 성공: 검증 assertThat(summary.totalActivityDays()).isZero(); assertThat(summary.perGisu()).isEmpty(); |

### ChallengerCommandServiceTest
- 테스트 설명: ChallengerCommandService
- 위치: `monolith/src/test/java/com/umc/product/challenger/application/service/ChallengerCommandServiceTest.java`

| 라인 | 테스트 케이스 | 입력/조건 | 기대 결과 |
|---:|---|---|---|
| [40](../../../monolith/src/test/java/com/umc/product/challenger/application/service/ChallengerCommandServiceTest.java#L40) | 동일 기수 챌린저가 없으면 생성한다 | CreateChallengerCommand {memberId=1L, part=ChallengerPart.SPRINGBOOT, gisuId=9L}; 호출 createChallenger(command) | 성공: 검증 assertThat(result).isEqualTo(100L); |
| [85](../../../monolith/src/test/java/com/umc/product/challenger/application/service/ChallengerCommandServiceTest.java#L85) | createChallenger / 동일 기수 챌린저가 있으면 생성하지 않는다 | CreateChallengerCommand {memberId=1L, part=ChallengerPart.SPRINGBOOT, gisuId=9L}; 호출 createChallenger(command)) | 실패: 예외 ChallengerDomainException; 에러코드 ChallengerErrorCode.CHALLENGER_ALREADY_EXISTS; 검증 .isEqualTo(ChallengerErrorCode.CHALLENGER_ALREADY_EXISTS); |
| [107](../../../monolith/src/test/java/com/umc/product/challenger/application/service/ChallengerCommandServiceTest.java#L107) | createChallenger / 변경할 파트와 상태가 모두 없으면 실패한다 | 조건 createChallenger / 변경할 파트와 상태가 모두 없으면 실패한다 | 실패: 예외 ChallengerDomainException; 에러코드 ChallengerErrorCode.BAD_CHALLENGER_UPDATE_REQUEST; 검증 .isEqualTo(ChallengerErrorCode.BAD_CHALLENGER_UPDATE_REQUEST); |
| [124](../../../monolith/src/test/java/com/umc/product/challenger/application/service/ChallengerCommandServiceTest.java#L124) | updateChallenger / 비활성 챌린저는 파트를 변경할 수 없다 | 조건 updateChallenger / 비활성 챌린저는 파트를 변경할 수 없다 | 실패: 예외 ChallengerDomainException; 에러코드 ChallengerErrorCode.CHALLENGER_NOT_ACTIVE; 검증 .isEqualTo(ChallengerErrorCode.CHALLENGER_NOT_ACTIVE); |
| [140](../../../monolith/src/test/java/com/umc/product/challenger/application/service/ChallengerCommandServiceTest.java#L140) | updateChallenger / deactivateChallenger는 EXPEL 타입을 EXPELLED 상태로 매핑한다 | 조건 updateChallenger / deactivateChallenger는 EXPEL 타입을 EXPELLED 상태로 매핑한다 | 성공: 검증 assertThat(challenger.getStatus()).isEqualTo(ChallengerStatus.EXPELLED); assertThat(challenger.getModifiedBy()).isEqualTo(99L); assertThat(challenger.getModificationReason()).isEqualTo("징계"); |
| [158](../../../monolith/src/test/java/com/umc/product/challenger/application/service/ChallengerCommandServiceTest.java#L158) | updateChallenger / 비활성 챌린저에게 상벌점을 부여할 수 없다 | GrantChallengerPointCommand {challengerId=1L, pointType=PointType.CUSTOM, pointValue=1, description="조정"}; 호출 grantChallengerPoint(GrantChallengerPointCommand.builder() | 실패: 예외 ChallengerDomainException; 에러코드 ChallengerErrorCode.CHALLENGER_NOT_ACTIVE; 검증 .isEqualTo(ChallengerErrorCode.CHALLENGER_NOT_ACTIVE); |
| [176](../../../monolith/src/test/java/com/umc/product/challenger/application/service/ChallengerCommandServiceTest.java#L176) | 상벌점 설명을 수정한다 | 호출 updateChallengerPoint(UpdateChallengerPointCommand.of(10L, "수정")) | 성공: 검증 assertThat(point.getDescription()).isEqualTo("수정"); |

### ChallengerDeactivationCommandServiceTest

- 위치: `monolith/src/test/java/com/umc/product/challenger/application/service/ChallengerDeactivationCommandServiceTest.java`

| 테스트 | 입력/조건 | 기대 결과 |
|---|---|---|
| 탈부 정보와 근거 자료를 저장하고 챌린저 상태를 변경한다 | 타의 탈부, 사진 2장, 링크 1개 | 성공: `EXPELLED` 변경 및 등록 이력 저장 |
| 이미 탈부 등록된 챌린저는 다시 등록할 수 없다 | 동일 challengerId의 탈부 기록 존재 | 실패: 중복 탈부 등록 예외 |
| 같은 근거 자료를 중복 등록할 수 없다 | 동일 imageId 2개 | 실패: 탈부 등록 요청 오류 |
| 탈부 근거 사진 카테고리가 아닌 파일은 등록할 수 없다 | 게시글 이미지 카테고리 파일 | 실패: 탈부 등록 요청 오류 |
| 탈부 사유가 없으면 챌린저 상태를 변경하지 않는다 | reason 누락 | 실패: 상태 미변경 |
| 근거 사진을 사용할 수 없으면 챌린저 상태를 변경하지 않는다 | Storage 파일 검증 실패 | 실패: 상태 미변경 |
| 기존 탈부 기록에 근거 자료와 진행 기록을 저장한다 | 사진과 링크 추가 | 성공: 근거 자료와 진행 기록 저장 |
| 사진 없이 링크만 추가할 수 있다 | imageIds 없음, links 존재 | 성공: 링크와 진행 기록 저장 |
| 근거 추가에서도 잘못된 파일 카테고리를 거부한다 | 게시글 이미지 카테고리 파일 | 실패: 탈부 등록 요청 오류 |
| 기존 자료와 합쳐 사진 상한을 초과할 수 없다 | 총 사진 수 5장 초과 | 실패: 탈부 등록 요청 오류 |
| 기존 자료와 합쳐 링크 상한을 초과할 수 없다 | 총 링크 수 3개 초과 | 실패: 탈부 등록 요청 오류 |
| 기존 사진을 다시 추가할 수 없다 | 동일 imageId 재등록 | 실패: 탈부 등록 요청 오류 |
| 기존 링크를 다시 추가할 수 없다 | 동일 링크 재등록 | 실패: 탈부 등록 요청 오류 |
| 사진과 링크가 모두 없으면 근거를 추가할 수 없다 | 빈 근거 추가 요청 | 실패: 탈부 등록 요청 오류 |

### ChallengerDeactivationDataDeletionServiceTest

- 위치: `monolith/src/test/java/com/umc/product/challenger/application/service/ChallengerDeactivationDataDeletionServiceTest.java`

| 테스트 | 입력/조건 | 기대 결과 |
|---|---|---|
| 외래 키 참조 순서에 맞춰 탈부 기록을 삭제한다 | 만료 탈부 기록 삭제 | 사진·링크·파일·진행 기록·탈부 기록 순서로 삭제 |

### ChallengerDeactivationQueryServiceTest

- 위치: `monolith/src/test/java/com/umc/product/challenger/application/service/ChallengerDeactivationQueryServiceTest.java`

| 테스트 | 입력/조건 | 기대 결과 |
|---|---|---|
| 탈부 근거에 연결된 이미지인지 확인한다 | 사용 중인 이미지 ID 조회 | 성공: 사용 여부 `true` 반환 |
| 이미지 ID가 없으면 저장소를 조회하지 않는다 | imageId가 `null` | 성공: `false` 반환, 저장소 미호출 |
| 자진 탈부와 타의 탈부 사유를 화면 표시 순서대로 조회한다 | 탈부 사유 목록 조회 | 성공: 유형별 사유와 상세 입력 필수 여부 반환 |

### ChallengerDeactivationRetentionServiceTest

- 위치: `monolith/src/test/java/com/umc/product/challenger/application/service/ChallengerDeactivationRetentionServiceTest.java`

| 테스트 | 입력/조건 | 기대 결과 |
|---|---|---|
| 등록일로부터 5년이 지난 탈부 기록을 삭제한다 | 보관 만료 대상 2건 | 성공: 두 기록 삭제 |
| 한 기록 삭제에 실패해도 다음 기록을 계속 처리한다 | 첫 기록 삭제 실패 | 다음 기록 처리 및 성공·실패 건수 집계 |
| 100건 단위로 다음 배치를 조회한다 | 첫 배치 100건, 두 번째 배치 2건 | 커서 기반 조회 및 전체 건수 합산 |

### ChallengerPermissionEvaluatorTest

- 위치: `monolith/src/test/java/com/umc/product/challenger/application/service/evaluator/ChallengerPermissionEvaluatorTest.java`

| 테스트 | 입력/조건 | 기대 결과 |
|---|---|---|
| SUPER_ADMIN은 기수와 학교에 관계없이 관리할 수 있다 | 전역 SUPER_ADMIN 권한 | 성공: 관리 허용 |
| 대상 기수 중앙 운영진은 관리할 수 있다 | 대상 기수 중앙 운영·교육국원 | 성공: 관리 허용 |
| 다른 기수 중앙 운영진은 관리할 수 없다 | 다른 기수 중앙 운영·교육국원 | 실패: 관리 거부 |
| 중앙 총괄은 다른 기수의 챌린저도 관리할 수 있다 | 중앙 총괄단 | 성공: 관리 허용 |

### ChallengerRecordCommandServiceTest
- 테스트 설명: ChallengerRecordCommandService
- 위치: `monolith/src/test/java/com/umc/product/challenger/application/service/ChallengerRecordCommandServiceTest.java`

| 라인 | 테스트 케이스 | 입력/조건 | 기대 결과 |
|---:|---|---|---|
| [40](../../../monolith/src/test/java/com/umc/product/challenger/application/service/ChallengerRecordCommandServiceTest.java#L40) | 학교가 요청 지부에 속하면 기록 코드를 생성한다 | 호출 create(command) | 성공: 검증 assertThat(result).isEqualTo(10L); |
| [86](../../../monolith/src/test/java/com/umc/product/challenger/application/service/ChallengerRecordCommandServiceTest.java#L86) | ChallengerRecordCommandService / 학교가 요청 지부에 속하지 않으면 기록 코드를 생성하지 않는다 | 호출 create(recordCommand())) | 실패: 예외 ChallengerDomainException; 에러코드 ChallengerErrorCode.INVALID_CHALLENGER_RECORD_CREATE_REQUEST; 검증 .isEqualTo(ChallengerErrorCode.INVALID_CHALLENGER_RECORD_CREATE_REQUEST); |
| [99](../../../monolith/src/test/java/com/umc/product/challenger/application/service/ChallengerRecordCommandServiceTest.java#L99) | ChallengerRecordCommandService / 일반 기록 코드를 소비하면 챌린저를 생성하고 코드를 사용 처리한다 | 호출 consumeCode(consumeCommand()) | 성공: 검증 assertThat(record.isUsed()).isTrue(); assertThat(record.getUsedMemberId()).isEqualTo(100L); |
| [114](../../../monolith/src/test/java/com/umc/product/challenger/application/service/ChallengerRecordCommandServiceTest.java#L114) | 이미 사용된 코드는 소비할 수 없다 | 호출 consumeCode(consumeCommand())) | 실패: 예외 ChallengerDomainException; 에러코드 ChallengerErrorCode.USED_CHALLENGER_RECORD_CODE; 검증 .isEqualTo(ChallengerErrorCode.USED_CHALLENGER_RECORD_CODE); |
| [130](../../../monolith/src/test/java/com/umc/product/challenger/application/service/ChallengerRecordCommandServiceTest.java#L130) | 일반 기록의 이름이 회원 정보와 다르면 챌린저를 생성하지 않는다 | 호출 consumeCode(consumeCommand())) | 실패: 예외 ChallengerDomainException; 에러코드 ChallengerErrorCode.INVALID_MEMBER_NAME_FOR_RECORD; 검증 .isEqualTo(ChallengerErrorCode.INVALID_MEMBER_NAME_FOR_RECORD); assertThat(record.isUsed()).isFalse(); |
| [146](../../../monolith/src/test/java/com/umc/product/challenger/application/service/ChallengerRecordCommandServiceTest.java#L146) | 일반 기록의 학교가 회원 정보와 다르면 챌린저를 생성하지 않는다 | 호출 consumeCode(consumeCommand())) | 실패: 예외 ChallengerDomainException; 에러코드 ChallengerErrorCode.INVALID_SCHOOL_FOR_RECORD; 검증 .isEqualTo(ChallengerErrorCode.INVALID_SCHOOL_FOR_RECORD); assertThat(record.isUsed()).isFalse(); |
| [162](../../../monolith/src/test/java/com/umc/product/challenger/application/service/ChallengerRecordCommandServiceTest.java#L162) | 같은 기수의 챌린저가 이미 있으면 일반 기록 코드를 소비하지 않는다 | 호출 consumeCode(consumeCommand())) | 실패: 예외 ChallengerDomainException; 에러코드 ChallengerErrorCode.CHALLENGER_ALREADY_EXISTS; 검증 .isEqualTo(ChallengerErrorCode.CHALLENGER_ALREADY_EXISTS); assertThat(record.isUsed()).isFalse(); |
| [180](../../../monolith/src/test/java/com/umc/product/challenger/application/service/ChallengerRecordCommandServiceTest.java#L180) | 운영진 기록 코드는 기존 챌린저에 역할을 부여하고 코드를 사용 처리한다 | 호출 consumeCode(consumeCommand()) | 성공: 검증 assertThat(record.isUsed()).isTrue(); |
| [199](../../../monolith/src/test/java/com/umc/product/challenger/application/service/ChallengerRecordCommandServiceTest.java#L199) | 운영진 기록 코드 소비 시 해당 기수 챌린저가 없으면 실패한다 | 호출 consumeCode(consumeCommand())) | 실패: 예외 ChallengerDomainException; 에러코드 ChallengerErrorCode.NO_CHALLENGER_IN_MEMBER_GISU; 검증 .isEqualTo(ChallengerErrorCode.NO_CHALLENGER_IN_MEMBER_GISU); assertThat(record.isUsed()).isFalse(); |

## Repository / Outbound Persistence

### ChallengerDeactivationPersistenceTest
- 테스트 설명: 탈부 관리 JPA 매핑
- 위치: `monolith/src/test/java/com/umc/product/challenger/adapter/out/persistence/ChallengerDeactivationPersistenceTest.java`

| 라인 | 테스트 케이스 | 입력/조건 | 기대 결과 |
|---:|---|---|---|
| [29](../../../monolith/src/test/java/com/umc/product/challenger/adapter/out/persistence/ChallengerDeactivationPersistenceTest.java#L29) | 탈부 기록과 진행 기록 및 근거 자료를 함께 저장한다 | 탈부 등록 History에 이미지와 링크를 연결해 저장 | 성공: History를 통해 탈부 기록과 작업자를 조회한다 |
| [71](../../../monolith/src/test/java/com/umc/product/challenger/adapter/out/persistence/ChallengerDeactivationPersistenceTest.java#L71) | 같은 챌린저의 탈부 기록은 한 번만 저장한다 | 동일 challengerId로 탈부 기록 2건 저장 | 실패: DB 유니크 제약 위반 |
| 1000자인 근거 링크를 저장한다 | 길이 1000인 URL | 성공: 전체 링크 보존 |
| 존재하지 않는 탈부 기록을 잠금 조회한다 | 존재하지 않는 challengerId | 실패: 탈부 기록 없음 오류 |
| 보관 만료 대상과 근거 자료를 조회하고 삭제한다 | 만료 대상 1건 | 성공: 외래 키 순서로 전체 삭제 |
| 보관 만료 시각·커서·조회 개수를 적용한다 | 경계 시각과 afterId, limit | 성공: 조건에 맞는 대상만 반환 |
| 다른 탈부 기록의 동일 사진 참조를 확인한다 | 같은 imageId를 두 기록이 참조 | 성공: 외부 참조 여부 반환 |

## E2E / Integration

### ChallengerDeactivationRegistrationIntegrationTest
- 위치: `monolith/src/test/java/com/umc/product/challenger/application/service/ChallengerDeactivationRegistrationIntegrationTest.java`

| 테스트 케이스 | 입력/조건 | 기대 결과 |
|---|---|---|
| 진행 기록 저장에 실패하면 챌린저 상태와 탈부 기록을 모두 롤백한다 | 탈부 등록 중 진행 기록 저장 실패 | 챌린저 상태와 탈부 기록 롤백 |
| 근거 링크 저장에 실패하면 근거 추가 진행 기록도 롤백한다 | 근거 추가 중 링크 저장 실패 | 진행 기록과 링크 롤백 |
| 동시에 근거 자료를 추가해도 비관적 락으로 링크 개수 제한을 지킨다 | 같은 탈부 기록에 링크를 동시에 추가 | 한 요청만 성공하고 최대 개수 유지 |

### ChallengerDeactivationDeletionIntegrationTest
- 위치: `monolith/src/test/java/com/umc/product/challenger/application/service/ChallengerDeactivationDeletionIntegrationTest.java`

| 테스트 케이스 | 입력/조건 | 기대 결과 |
|---|---|---|
| 자식 기록 삭제 중 실패하면 탈부 기록과 모든 근거 자료를 복구한다 | 진행 기록 삭제 실패 | 탈부 기록·진행 기록·근거 자료 롤백 |
| 스토리지 원본 삭제에 실패하면 탈부 기록과 근거 자료를 복구한다 | 첫 원본 삭제 실패 | DB 변경 롤백 |
| 일부 원본 삭제 후 롤백된 파일은 다른 탈부 기록에 다시 등록할 수 없다 | A 삭제 성공 후 B 삭제 실패, A 재등록 시도 | S3 원본이 없는 A 등록 거부 |

### ChallengerRecordControllerIntegrationTest
- 테스트 설명: ChallengerRecordController 통합 테스트
- 위치: `monolith/src/test/java/com/umc/product/challenger/adapter/in/web/ChallengerRecordControllerIntegrationTest.java`

| 라인 | 테스트 케이스 | 입력/조건 | 기대 결과 |
|---:|---|---|---|
| [75](../../../monolith/src/test/java/com/umc/product/challenger/adapter/in/web/ChallengerRecordControllerIntegrationTest.java#L75) | ChallengerRecordController 통합 테스트 / 일반 챌린저 기록 코드를 소비하면 챌린저가 생성되고 코드가 사용 처리된다 | HTTP POST /api/v1/challenger-record/member | 성공: HTTP 200 OK; 검증 assertThat(savedChallenger.getPart()).isEqualTo(ChallengerPart.SPRINGBOOT); assertThat(usedRecord.isUsed()).isTrue(); assertThat(usedRecord.getUsedMemberId()).isEqualTo(member.getId()); assertThat(usedRecord.getUsedAt... |
| [110](../../../monolith/src/test/java/com/umc/product/challenger/adapter/in/web/ChallengerRecordControllerIntegrationTest.java#L110) | 운영진 기록 코드를 소비하면 기존 챌린저에 역할이 부여되고 새 챌린저는 만들지 않는다 | HTTP POST /api/v1/challenger-record/member | 성공: HTTP 200 OK; 검증 assertThat(challengerJpaRepository.findByMemberId(member.getId())); .containsExactly(challenger.getId()); assertThat(challengerRoleJpaRepository.findByChallengerId(challenger.getId())); .hasSize(1) |
| [159](../../../monolith/src/test/java/com/umc/product/challenger/adapter/in/web/ChallengerRecordControllerIntegrationTest.java#L159) | 이미 사용된 챌린저 기록 코드는 재사용할 수 없다 | HTTP POST /api/v1/challenger-record/member | 실패: HTTP 200 OK; HTTP 400 Bad Request; 검증 assertThat(challengerJpaRepository.findByMemberId(member.getId())); .hasSize(1); .isEqualTo(ChallengerPart.ANDROID); assertThat(usedRecord.isUsed()).isTrue(); |

## Scheduler

### ChallengerDeactivationRetentionSchedulerTest

- 위치: `monolith/src/test/java/com/umc/product/challenger/adapter/in/scheduler/ChallengerDeactivationRetentionSchedulerTest.java`

| 테스트 | 입력/조건 | 기대 결과 |
|---|---|---|
| 보관 만료 기능이 비활성화되면 스케줄러 빈을 등록하지 않는다 | 활성 속성 `false` | 스케줄러 빈 미등록 |
| 보관 만료 기능이 활성화되면 스케줄러 빈을 등록한다 | 활성 속성 `true` | 스케줄러 빈 등록 |
| 스케줄러는 만료 삭제 작업을 전용 실행기에 제출한다 | 스케줄 실행 | 전용 executor에 작업 제출, scheduler thread에서 직접 실행하지 않음 |
| 이전 만료 삭제 작업이 실행 중이면 새 실행 요청을 건너뛴다 | executor가 실행 요청 거절 | 중복 만료 삭제 미실행 |
| 만료 기록을 모두 삭제하면 성공 지표를 기록한다 | 성공 3건, 실패 0건 | `success` 지표 기록 |
| 일부 삭제에 실패하면 부분 실패 지표를 기록한다 | 성공 2건, 실패 1건 | `partial_failure` 지표 기록 |
| 만료 처리 자체가 실패하면 실패 지표를 기록한다 | purge 예외 발생 | `failure` 지표 기록 및 예외 전파 |

## Domain

### ChallengerDeactivationTest
- 테스트 설명: ChallengerDeactivation 도메인
- 위치: `monolith/src/test/java/com/umc/product/challenger/domain/ChallengerDeactivationTest.java`

| 라인 | 테스트 케이스 | 입력/조건 | 기대 결과 |
|---:|---|---|---|
| [20](../../../monolith/src/test/java/com/umc/product/challenger/domain/ChallengerDeactivationTest.java#L20) | 탈부 사유에 대응하는 상태로 탈부 기록을 생성한다 | 모든 탈부 사유 enum | 성공: 사유의 상태와 저장 상태가 일치한다 |
| [43](../../../monolith/src/test/java/com/umc/product/challenger/domain/ChallengerDeactivationTest.java#L43) | 탈부 사유와 상태가 다르면 생성을 거절한다 | 자진 탈부 사유와 EXPELLED 상태 | 실패: 탈부 등록 요청 오류 |
| [58](../../../monolith/src/test/java/com/umc/product/challenger/domain/ChallengerDeactivationTest.java#L58) | 기타 사유는 상세 사유가 필수다 | 기타 사유와 공백 상세 사유 | 실패: 도메인 예외 |
| [71](../../../monolith/src/test/java/com/umc/product/challenger/domain/ChallengerDeactivationTest.java#L71) | 기타가 아닌 사유에는 상세 사유를 입력할 수 없다 | 정형 사유와 상세 사유 | 실패: 도메인 예외 |
| [84](../../../monolith/src/test/java/com/umc/product/challenger/domain/ChallengerDeactivationTest.java#L84) | 상세 사유는 100자를 초과할 수 없다 | 상세 사유 101자 | 실패: 도메인 예외 |
| [97](../../../monolith/src/test/java/com/umc/product/challenger/domain/ChallengerDeactivationTest.java#L97) | 메모는 100자를 초과할 수 없다 | 메모 101자 | 실패: 도메인 예외 |
| [110](../../../monolith/src/test/java/com/umc/product/challenger/domain/ChallengerDeactivationTest.java#L110) | 사진은 자신을 추가한 진행 기록과 연결된다 | 사진과 EVIDENCE_ADDED History | 성공: 작업자와 탈부 기록을 History로 조회한다 |
| [136](../../../monolith/src/test/java/com/umc/product/challenger/domain/ChallengerDeactivationTest.java#L136) | 탈부 등록 기록은 근거 자료가 없어도 생성할 수 있다 | REGISTERED, 사진 0장, 링크 0개 | 성공: 진행 기록을 생성한다 |
| [151](../../../monolith/src/test/java/com/umc/product/challenger/domain/ChallengerDeactivationTest.java#L151) | 근거 추가 기록에는 사진이나 링크가 하나 이상 필요하다 | EVIDENCE_ADDED, 사진 0장, 링크 0개 | 실패: 도메인 예외 |
| [163](../../../monolith/src/test/java/com/umc/product/challenger/domain/ChallengerDeactivationTest.java#L163) | 한 번의 진행 기록도 사진 5장과 링크 3개를 초과할 수 없다 | 사진 6장 또는 링크 4개 | 실패: 도메인 예외 |
| [183](../../../monolith/src/test/java/com/umc/product/challenger/domain/ChallengerDeactivationTest.java#L183) | 링크는 자신을 추가한 진행 기록과 연결된다 | 링크와 EVIDENCE_ADDED History | 성공: 작업자를 History로 조회한다 |
| 링크는 1000자까지 저장할 수 있다 | 1000자 링크 | 성공: 링크 생성 |
| 링크는 1000자를 초과할 수 없다 | 1001자 링크 | 실패: 도메인 예외 |

### ChallengerPointTest
- 테스트 설명: ChallengerPoint 도메인
- 위치: `monolith/src/test/java/com/umc/product/challenger/domain/ChallengerPointTest.java`

| 라인 | 테스트 케이스 | 입력/조건 | 기대 결과 |
|---:|---|---|---|
| [9](../../../monolith/src/test/java/com/umc/product/challenger/domain/ChallengerPointTest.java#L9) | pointValue가 없으면 PointType의 기본 점수를 사용한다 | 조건 pointValue가 없으면 PointType의 기본 점수를 사용한다 | 성공: 검증 assertThat(point.getPointValue()).isEqualTo(PointType.BEST_WORKBOOK.getValue()); |
| [26](../../../monolith/src/test/java/com/umc/product/challenger/domain/ChallengerPointTest.java#L26) | ChallengerPoint 도메인 / pointValue가 있으면 PointType보다 custom 점수를 우선한다 | 조건 ChallengerPoint 도메인 / pointValue가 있으면 PointType보다 custom 점수를 우선한다 | 성공: 검증 assertThat(point.getPointValue()).isEqualTo(-7.0); |
| [40](../../../monolith/src/test/java/com/umc/product/challenger/domain/ChallengerPointTest.java#L40) | ChallengerPoint 도메인 / 상벌점 설명을 수정한다 | 조건 ChallengerPoint 도메인 / 상벌점 설명을 수정한다 | 성공: 검증 assertThat(point.getDescription()).isEqualTo("새 설명"); |

### ChallengerRecordTest
- 테스트 설명: ChallengerRecord 도메인
- 위치: `monolith/src/test/java/com/umc/product/challenger/domain/ChallengerRecordTest.java`

| 라인 | 테스트 케이스 | 입력/조건 | 기대 결과 |
|---:|---|---|---|
| [12](../../../monolith/src/test/java/com/umc/product/challenger/domain/ChallengerRecordTest.java#L12) | 일반 챌린저 기록 코드를 생성한다 | 조건 일반 챌린저 기록 코드를 생성한다 | 성공: 검증 assertThat(record.getCode()).hasSize(6); assertThat(record.isUsed()).isFalse(); assertThat(record.isAdminRecord()).isFalse(); assertThat(record.getMemberName()).isEqualTo("홍길동"); |
| [26](../../../monolith/src/test/java/com/umc/product/challenger/domain/ChallengerRecordTest.java#L26) | ChallengerRecord 도메인 / 운영진 기록은 역할 타입과 조직 ID를 가진다 | 조건 ChallengerRecord 도메인 / 운영진 기록은 역할 타입과 조직 ID를 가진다 | 성공: 검증 assertThat(record.isAdminRecord()).isTrue(); assertThat(record.getChallengerRoleType()).isEqualTo(ChallengerRoleType.SCHOOL_PRESIDENT); assertThat(record.getOrganizationId()).isEqualTo(3L); |
| [39](../../../monolith/src/test/java/com/umc/product/challenger/domain/ChallengerRecordTest.java#L39) | ChallengerRecord 도메인 / 사용 처리 시 사용 회원과 시각을 기록한다 | 조건 ChallengerRecord 도메인 / 사용 처리 시 사용 회원과 시각을 기록한다 | 성공: 검증 assertThat(record.isUsed()).isTrue(); assertThat(record.getUsedMemberId()).isEqualTo(100L); assertThat(record.getUsedAt()).isNotNull(); |
| [51](../../../monolith/src/test/java/com/umc/product/challenger/domain/ChallengerRecordTest.java#L51) | ChallengerRecord 도메인 / 이미 사용된 코드는 다시 사용할 수 없다 | 조건 ChallengerRecord 도메인 / 이미 사용된 코드는 다시 사용할 수 없다 | 실패: 예외 ChallengerDomainException; 에러코드 ChallengerErrorCode.USED_CHALLENGER_RECORD_CODE; 검증 .isEqualTo(ChallengerErrorCode.USED_CHALLENGER_RECORD_CODE); |
| [63](../../../monolith/src/test/java/com/umc/product/challenger/domain/ChallengerRecordTest.java#L63) | ChallengerRecord 도메인 / 기록의 회원 이름과 학교가 요청자 정보와 일치해야 한다 | 조건 ChallengerRecord 도메인 / 기록의 회원 이름과 학교가 요청자 정보와 일치해야 한다 | 실패: 예외 ChallengerDomainException; 에러코드 ChallengerErrorCode.INVALID_MEMBER_NAME_FOR_RECORD, ChallengerErrorCode.INVALID_SCHOOL_FOR_RECORD; 검증 .isEqualTo(ChallengerErrorCode.INVALID_MEMBER_NAME_FOR_RECORD); .isEqualTo(ChallengerErrorCode.INVALID_SCHOOL_FOR_RECORD); |

### ChallengerTest
- 테스트 설명: Challenger 도메인
- 위치: `monolith/src/test/java/com/umc/product/challenger/domain/ChallengerTest.java`

| 라인 | 테스트 케이스 | 입력/조건 | 기대 결과 |
|---:|---|---|---|
| [27](../../../monolith/src/test/java/com/umc/product/challenger/domain/ChallengerTest.java#L27) | Challenger 도메인 / 챌린저 생성 시 기본적으로 활성화 상태이다 | 조건 Challenger 도메인 / 챌린저 생성 시 기본적으로 활성화 상태이다 | 성공: 검증 assertThat(challenger.getStatus()).isEqualTo(ChallengerStatus.ACTIVE); |
| [34](../../../monolith/src/test/java/com/umc/product/challenger/domain/ChallengerTest.java#L34) | Challenger 도메인 / ACTIVE 챌린저는 파트를 변경할 수 있다 | 조건 Challenger 도메인 / ACTIVE 챌린저는 파트를 변경할 수 있다 | 성공: 검증 assertThat(challenger.getPart()).isEqualTo(ChallengerPart.WEB); |
| [44](../../../monolith/src/test/java/com/umc/product/challenger/domain/ChallengerTest.java#L44) | changePart / ACTIVE 상태가 아니면 파트를 변경할 수 없다 | 조건 changePart / ACTIVE 상태가 아니면 파트를 변경할 수 없다 | 실패: 예외 ChallengerDomainException; 에러코드 ChallengerErrorCode.CHALLENGER_NOT_ACTIVE; 검증 .isEqualTo(ChallengerErrorCode.CHALLENGER_NOT_ACTIVE); |
| [58](../../../monolith/src/test/java/com/umc/product/challenger/domain/ChallengerTest.java#L58) | changePart / ACTIVE 챌린저는 상태 변경 사유와 수정자를 기록한다 | 조건 changePart / ACTIVE 챌린저는 상태 변경 사유와 수정자를 기록한다 | 성공: 검증 assertThat(challenger.getStatus()).isEqualTo(ChallengerStatus.EXPELLED); assertThat(challenger.getModifiedBy()).isEqualTo(99L); assertThat(challenger.getModificationReason()).isEqualTo("징계"); |
| [70](../../../monolith/src/test/java/com/umc/product/challenger/domain/ChallengerTest.java#L70) | changeStatus / ACTIVE 상태가 아니면 다시 상태를 변경할 수 없다 | 조건 changeStatus / ACTIVE 상태가 아니면 다시 상태를 변경할 수 없다 | 실패: 예외 ChallengerDomainException; 에러코드 ChallengerErrorCode.CHALLENGER_NOT_ACTIVE; 검증 .isEqualTo(ChallengerErrorCode.CHALLENGER_NOT_ACTIVE); |
| [82](../../../monolith/src/test/java/com/umc/product/challenger/domain/ChallengerTest.java#L82) | changeStatus / 상벌점 총합을 계산한다 | 조건 changeStatus / 상벌점 총합을 계산한다 | 성공: 검증 assertThat(challenger.getTotalPoints()).isEqualTo(-3.5); |
