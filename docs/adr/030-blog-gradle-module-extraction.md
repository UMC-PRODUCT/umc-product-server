# ADR 030. blog 도메인 Gradle 모듈 분리

- 상태: 채택
- 날짜: 2026-09-15
- 관련 이슈: #1335

## 배경

다른 도메인의 blog 직접 import 는 없었다. 다만 감사 정책 테스트에는 문자열 기반 리플렉션 의존이
있어 분리 과정에서 함께 정리했다.

직접 import 가 없다는 것은 관례일 뿐 강제되지 않았다. 누구든 `import com.umc.product.blog...` 를
추가해도 빌드는 통과했다.

멀티 모듈 전환의 첫 대상으로 blog 를 골랐다. 들어오는 의존이 없어 되돌리기 쉽고, 이후 도메인
분리에 재사용할 빌드 골격을 만들 수 있기 때문이다.

## 결정

세 모듈로 나눈다.

```
:app        실행 jar 를 만들고 조립 결과를 검증한다.  -> :monolith, :blog
:blog       blog 도메인                             -> :monolith
:monolith   아직 빼내지 못한 나머지 전부              -> (프로젝트 의존 없음)
```

`:monolith` 가 `:blog` 를 컴파일 타임에 볼 수 없다. 이것이 이번 작업의 결과물이다.

```
error: package com.umc.product.blog.domain does not exist
```

### 실행 클래스와 resources 는 `:monolith` 에 남긴다

`:app` 에 두는 것이 교과서적이지만 통합 테스트가 깨진다. 이 저장소는 `@SpringBootTest` 를
`classes` 없이 쓰고, Spring 은 테스트 패키지에서 위로 올라가며 `@SpringBootConfiguration` 을 찾는다.
실행 클래스가 `:app` 에 있으면 `:monolith` 테스트는 `:app` 을 의존할 수 없어(순환) 설정 클래스를
찾지 못한다.

`:app` 에 두는 것이 원천적으로 불가능하지는 않다. 모듈별 테스트 설정 클래스를 명시하면 된다.
기존 테스트 변경량을 줄이려고 `:monolith` 에 남겼다.

부수적으로 컴포넌트 스캔도 해결된다. `@SpringBootApplication` 이 `com.umc.product` 를 스캔하고
blog 가 패키지명을 유지하므로 jar 경계와 무관하게 스캔된다.

### `:blog` 가 `:monolith` 전체를 보는 것을 허용한다

좁은 계약 모듈을 만들려면 `MemberInfo.from(Member)` 처럼 계약 DTO 가 엔티티를 물고 있는 코드부터
정리해야 한다. blog 가 컴파일에 필요로 하는 타입의 관측 집합은 44개이고 그중 JPA 엔티티가 3개다.
프로덕션 로직을 바꾸지 않는 이번 범위에서는 비용이 크다.

대신 반대 방향은 테스트로 고정한다 (`BlogModuleBoundaryTest`). 허용 목록 19개를 벗어나는 직접
import 와, 다른 도메인의 `adapter.out` / `application.service` 참조를 막는다.

**이 테스트가 고정하는 것은 직접 import 표면이지 전이 의존이 아니다.** 같은 패키지 참조와 코드에
그대로 쓴 완전한 이름은 잡지 못한다. 실제로 `GetChallengerRoleUseCase` 가 같은 패키지의
`ListChallengerRoleUseCase` 를 상속하고 blog 가 이를 쓰는데, 허용 목록에는 나타나지 않는다.

### 조립 결과는 `:app` 에서 확인한다

모듈별 테스트는 자기 모듈만 본다. `:blog` 를 `app/build.gradle.kts` 의 의존에서 빼도 `:blog:test` 는
그대로 통과한다. 테스트는 전부 초록인데 배포된 서버에 blog 가 없는 상태가 된다.

`ApplicationAssemblyTest` 가 실제로 조립된 컨텍스트에서 모듈별 대표 빈과 blog 권한 평가기 등록을
확인한다. 의존을 빼고 돌려 이 테스트만 실패하는 것을 검증했다.

`:app` 에는 `package-info.java` 만 둔다. 소스가 하나도 없으면 IntelliJ 가 모듈을 만들지 않아
`.run/*.xml` 이 참조할 대상이 사라지고, IDE 실행 구성이 `:monolith` 를 가리키게 되어 blog 빈이 빠진
채로 서버가 뜬다.

### ArchUnit, Spring Modulith 는 쓰지 않는다

이번 목표는 Gradle 의존 방향과 직접 import 표면의 고정이다. 기존 `community/architecture/*` 방식
(소스 스캔)으로 검증하고, Modulith 도입은 별도로 검토한다.

## 결과

### 얻은 것

- `:monolith` 가 blog 를 참조하면 컴파일이 실패한다. 역검증으로 확인했다.
- 다음 도메인 분리에 그대로 쓸 빌드 골격이 생겼다. subprojects 규약, 모듈별 QueryDSL 생성,
  testFixtures, 경계 테스트.
- 테스트는 monolith 4204 + blog 63 + app 2 이고, 분리 전 4237 에서 줄어든 것이 없다.
  늘어난 32건은 blog 감사 정책 1, 경계 검사 3, 경계 판정 8, import 스캐너 18, 조립 검증 2 다.

### 다음 도메인은 같은 방법으로 바로 옮길 수 없다

**빌드 설정은 재사용하되, 이동 범위와 순서는 도메인별 의존 관계에 따라 결정한다.** blog 는 들어오는
직접 의존이 0이라 가능했던 첫 사례다. `682b19a42` 기준으로 남은 후보를 세어보면 상황이 다르다.

| 후보 | 도메인 밖 main 파일 | testFixtures 파일 |
|------|--------------------:|------------------:|
| blog | 0 | 0 |
| notice | 4 | 1 |
| curriculum | 4 | 1 |
| organization | 73 | 5 |
| member | 75 | 3 |

전체 의존 수가 아니라 **직접 import 가 있는 파일 수**다.

특히 `support/ControllerTestSupport.java` 는 notice, organization 컨트롤러와 storage 조회 UseCase 를
직접 참조한다. 이름은 공통 지원 코드지만 도메인 중립이 아니다.

따라서 다음 작업자는 이동 전에 이것부터 판단한다.

1. 남는 monolith 가 대상 도메인을 참조하는가
2. 이미 분리한 blog 가 대상 도메인을 참조하는가
3. 공통 fixture 와 웹 테스트 지원 코드도 함께 옮겨야 하는가
4. 계약 분리가 먼저인가, 분리 순서를 바꿔야 하는가

문자열 리플렉션 의존은 컴파일로 드러나지 않으므로 별도로 찾아야 한다 (아래 참고).

옮긴 뒤에는 `AGENTS.md` 의 모듈 등록 4곳을 확인한다. 그중 `ApplicationAssemblyTest` 에 대표 빈을
추가하는 것이 나머지 누락을 잡아주는 안전장치다.

### 얻지 못한 것

- **완성된 도메인 격리가 아니다.** `:blog` 는 `:monolith` 전체를 본다.
- **`:blog:test` 는 독립 slice 테스트가 아니다.** Gradle task 가 분리될 뿐 여전히 전체 Spring
  컨텍스트를 띄운다.
- **blog 분리 배포는 하지 않는다.** 이번엔 모듈만 나눴다.

## 분리 과정에서 드러난 것

컴파일로 검증되지 않는 결합이 실제로 있었다. 다음 도메인에서도 같은 종류를 확인해야 한다.

**문자열 리플렉션.** `AuditCoveragePolicyTest` 가 `Class.forName("com.umc.product.blog...")` 로
blog 클래스를 찾고 있었다. 컴파일은 통과하고 실행에서만 `ClassNotFoundException` 이 났다.
검사 뼈대를 `AuditPolicyAssert` 로 빼고 모듈별로 자기 spec 을 들도록 나눴다.

**`ratchetFrom` 이 가려온 포맷 위반.** spotless 가 변경 파일만 검사하므로, 이동으로 전 파일이 새
경로가 되자 545개가 한꺼번에 드러났다. checkstyle 은 전부 warning 이라 빌드를 막지 않지만
spotless 는 실패시키고 테스트의 선행 작업이다. 이동 커밋과 포맷 정리 커밋을 나눴다.

**소스 세트를 넘는 패키지 전용 접근은 동작한다.** `RepositoryRootTest` 가 `src/test` 에 남고
`RepositoryRoot.locate()` 가 `testFixtures` 로 갔지만 같은 패키지라 컴파일된다.

**testFixtures 의 `protected` 노출은 `api` 여야 한다.** `IntegrationTestSupport` 의
`protected JavaMailSender mailSender` 는 하위 클래스에게 공개 API 다. `implementation` 으로 두면
상속한 모듈이 그 필드를 쓰는 순간 `cannot access` 로 깨진다.

## 미룬 결정

- **Flyway 마이그레이션 소유권.** blog 테이블 DDL 은 `create_blog_cms.sql` 한 곳에 있지만
  `:monolith` resources 에 남겼다. 모듈별로 분산하면 중복 버전 검사 범위를 함께 설계해야 하고,
  blog 는 분리 배포 대상이 아니라 실익이 없다.
- **공유 커널 재설계.** `Domain.BLOG`, `ResourceType.BLOG_*` 가 `:monolith` 에 남아 있다.
  `global` 이 도메인을 20건 역참조하는 것도 그대로다.
- **계약 DTO 의 `from(Entity)` 제거.** 좁은 계약 모듈의 선결 조건이다.
- **`blog-api` / `blog-impl` 분리.** 직접 소비자가 없어 지금은 비용 대비 이득이 작다.
- **CODEOWNERS 의 blog 항목.** 원래 없었고 이번에 추가하지 않았다. 소유권 정리는 별도로 다룬다.
