# Analytics Admin Dashboard Performance Improvement Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 운영진 대시보드 MVP 구현 이후 analytics 집계 API의 인덱스, 쿼리 실행 계획, 회귀 성능을 별도 작업으로 검증하고 개선한다.

**Architecture:** 기능 구현 계획과 분리된 후속 성능 작업이다. API 계약, DTO, 권한 스코프는 `2026-05-13-analytics-admin-dashboard-backend.md`의 구현 결과를 그대로 사용하고, 이 문서는 쿼리 성능과 DB 인덱스만 다룬다.

**Tech Stack:** Java 21, Spring Boot 3.5, JPA, QueryDSL, PostgreSQL, Flyway, JUnit 5, Testcontainers

---

## 전제

- 선행 문서: `docs/superpowers/plans/2026-05-13-analytics-admin-dashboard-backend.md`
- 선행 구현: analytics MVP API와 challenger 위험군 검색 확장이 완료되어 있어야 한다.
- 이 작업은 기능 완료 기준에 포함하지 않는다. 별도 PR 또는 별도 커밋으로 진행한다.

## 대상 API

- `GET /api/v1/admin/dashboard/summary`
- `GET /api/v1/admin/dashboard/action-queue`
- `GET /api/v1/admin/schools/summary`
- `GET /api/v1/challenger/search/offset?pointSumLte=...&sort=pointSum,asc`

## Files

- Create: `src/main/resources/db/migration/V20260513_02__add_admin_dashboard_analytics_indexes.sql`
- Modify: `src/main/java/com/umc/product/analytics/adapter/out/persistence/AdminDashboardAnalyticsQueryRepository.java`
- Modify: `src/main/java/com/umc/product/analytics/adapter/out/persistence/AdminSchoolAnalyticsQueryRepository.java`
- Modify: `src/main/java/com/umc/product/challenger/adapter/out/persistence/ChallengerQueryRepository.java`
- Test: `src/test/java/com/umc/product/analytics/adapter/out/persistence/AdminDashboardAnalyticsQueryRepositoryTest.java`
- Test: `src/test/java/com/umc/product/analytics/adapter/out/persistence/AdminSchoolAnalyticsQueryRepositoryTest.java`
- Test: `src/test/java/com/umc/product/challenger/adapter/out/persistence/ChallengerQueryRepositoryTest.java`

## Task 1: 기본 인덱스 추가

- [ ] **Step 1: Flyway migration 작성**

```sql
create index if not exists idx_challenger_gisu_status_part
    on challenger (gisu_id, status, part);

create index if not exists idx_challenger_member_gisu
    on challenger (member_id, gisu_id);

create index if not exists idx_member_school_created_at
    on member (school_id, created_at);

create index if not exists idx_challenger_point_challenger_created_at
    on challenger_point (challenger_id, created_at desc);

create index if not exists idx_challenger_role_gisu_role_org
    on challenger_role (gisu_id, role_type, organization_type, organization_id);

create index if not exists idx_project_gisu_status_chapter
    on project (gisu_id, status, chapter_id);

create index if not exists idx_project_application_status_submitted_at
    on project_application (status, submitted_at);

create index if not exists idx_project_matching_round_chapter_period
    on project_matching_round (chapter_id, starts_at, ends_at, decision_deadline);

create index if not exists idx_notice_notification_pending
    on notice (should_send_notification, notified_at);
```

- [ ] **Step 2: migration 적용 테스트 실행**

```bash
./gradlew test --tests "com.umc.product.analytics.adapter.out.persistence.AdminDashboardAnalyticsQueryRepositoryTest" --tests "com.umc.product.analytics.adapter.out.persistence.AdminSchoolAnalyticsQueryRepositoryTest"
```

Expected: Testcontainers PostgreSQL에서 migration 적용 후 테스트 PASS

## Task 2: QueryDSL 집계 쿼리 실행 계획 점검

- [ ] **Step 1: dashboard summary 쿼리의 조인 조건 확인**

확인 기준:

- `challenger.gisu_id`, `challenger.status` 조건이 가장 먼저 적용된다.
- `member.school_id` 필터는 스코프가 SCHOOL 또는 SCHOOL_PART일 때만 들어간다.
- `project.status`, `project.gisu_id`, `project.chapter_id` 조건이 index friendly하게 분리되어 있다.

- [ ] **Step 2: school summary 쿼리의 group by 범위 확인**

확인 기준:

- page 대상 학교 목록과 row 집계를 분리한다.
- 모든 학교를 집계한 뒤 application memory에서 paging하지 않는다.
- `riskThreshold`는 DB query의 `having` 또는 projection 계산에 포함한다.

- [ ] **Step 3: 위험군 challenger 검색의 pointSum 정렬 확인**

확인 기준:

- `pointSumLte`, `pointSumGte`가 전체 페이지 조회 이후 in-memory filter로 동작하지 않는다.
- `pointSum,asc` 정렬이 DB query에 반영된다.
- 최신 감점 조회는 현재 페이지의 challengerId set에 대해서만 1회 실행된다.

Run:

```bash
./gradlew test --tests "com.umc.product.challenger.adapter.out.persistence.ChallengerQueryRepositoryTest"
```

Expected: 위험군 필터/정렬/최신 감점 테스트 PASS

## Task 3: 성능 회귀 테스트 보강

- [ ] **Step 1: 대량 fixture 기반 repository 테스트 추가**

테스트 이름:

- `summary_대량_데이터에서도_스코프별_집계가_정상_동작한다`
- `schoolSummary_페이지_크기만큼만_결과를_반환한다`
- `challengerSearch_위험군_필터는_DB에서_적용된다`

- [ ] **Step 2: SQL 로그로 N+1 여부 확인**

확인 기준:

- dashboard summary 호출에서 school 수만큼 반복 쿼리가 발생하지 않는다.
- school summary 한 페이지 조회에서 challenger 수만큼 반복 쿼리가 발생하지 않는다.
- latest negative point 조회는 페이지당 1회 이하로 실행된다.

Run:

```bash
./gradlew test --tests "com.umc.product.analytics.adapter.out.persistence.*" --tests "com.umc.product.challenger.adapter.out.persistence.ChallengerQueryRepositoryTest"
```

Expected: 성능 회귀 테스트 PASS

## 검증 명령

```bash
./gradlew compileJava
./gradlew compileTestJava
./gradlew test --tests "com.umc.product.analytics.adapter.out.persistence.*" --tests "com.umc.product.challenger.adapter.out.persistence.ChallengerQueryRepositoryTest"
```

전체 회귀까지 확인할 때:

```bash
./gradlew test
```

## 완료 기준

- analytics MVP 기능 문서에 성능 작업이 포함되어 있지 않다.
- 신규 Flyway 인덱스 migration이 Testcontainers PostgreSQL에서 정상 적용된다.
- dashboard summary, action queue, school summary, 위험군 challenger 검색 repository 테스트가 통과한다.
- `./gradlew compileJava`가 통과한다.
- `./gradlew compileTestJava`가 통과한다.
- 대상 성능 회귀 테스트가 통과한다.
