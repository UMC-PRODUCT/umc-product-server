# PRD — UMC PRODUCT Backend

## 서비스 개요

UMC PRODUCT는 UMC(University MakerS Club) 동아리의 활동 관리를 위한 백엔드 API 서버다. 운영진이 기수별 커리큘럼을 설계·배포하고, 챌린저(부원)가 워크북 미션을 수행하며 피드백을 받는 흐름을 지원한다.

---

## 사용자 유형

| 역할 | 설명 |
|------|------|
| **관리자 (Admin)** | 커리큘럼 생성·수정, 워크북 배포, 챌린저 관리, 피드백 작성 |
| **챌린저 (Challenger)** | 워크북 열람, 미션 제출, 진행 현황 조회 |
| **멤버 (Member)** | 로그인·프로필 등 공통 계정 기능 |

---

## 핵심 도메인 및 기능

### `curriculum` — 커리큘럼

| 엔티티 | 설명 |
|--------|------|
| `Curriculum` | 기수(gisuId) + 파트(part)로 식별되는 커리큘럼 |
| `WeeklyCurriculum` | 주차별 커리큘럼 (weekNo, 기간, 부록 여부) |
| `OriginalWorkbook` | 주차에 속하는 원본 워크북 (DRAFT → READY → RELEASED 상태) |
| `OriginalWorkbookMission` | 워크북 내 개별 미션 (링크/파일/텍스트 등 타입) |
| `ChallengerWorkbook` | 챌린저에게 배포된 워크북 인스턴스 |
| `MissionSubmission` | 미션별 제출물 |
| `MissionFeedback` | 제출물에 달리는 피드백 (PASS/FAIL) |

**핵심 규칙:**
- 워크북 상태는 단방향 전이만 허용 (DRAFT → READY → RELEASED, 역방향 불가)
- 챌린저 워크북 배포는 originalWorkbook 단위, 멤버당 1개 보장 (UNIQUE 제약)
- 스케줄러가 READY 상태이며 시작일이 지난 워크북을 자동 RELEASED 전환

### `challenger` — 챌린저

| 기능 | 설명 |
|------|------|
| 챌린저 등록 | 멤버를 특정 기수·파트의 챌린저로 등록 |
| 졸업 처리 | 챌린저 상태를 ACTIVE → GRADUATED 전환 |
| 출석 관리 | 기수별 출석 기록 |

### `member` — 멤버

- 소셜 로그인 기반 가입·인증
- JWT Access/Refresh 토큰 발급

### `organization` — 조직

- 기수(Gisu) 및 파트(Part) 관리
- 스터디 그룹(StudyGroup) 구성

### `community` / `notice` / `survey` / `schedule` 등

- 게시판, 공지사항, 설문, 일정 관리 보조 도메인

---

## 비기능 요구사항

| 항목 | 목표 |
|------|------|
| 응답 시간 | 조회 API P99 < 300ms |
| 트랜잭션 | 하나의 트랜잭션 = 하나의 Aggregate 수정 |
| N+1 방지 | IN 쿼리 배치 로딩 or Fetch Join 강제 |
| 보안 | JWT 인증, Casbin 기반 권한 관리 |
| 관측성 | Prometheus Metrics + OpenTelemetry Tracing |
| 문서화 | Spring REST Docs (AsciiDoc) 자동 생성 |