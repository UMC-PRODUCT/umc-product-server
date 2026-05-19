# 부팅 및 Docker 빌드 최적화 Phase 1 실행 계획

## 목표

`docs/analysis/boot-and-build-optimization-report.md`의 Phase 1 항목 중 Low risk로 즉시 검증 가능한 변경을 먼저 적용한다. 부팅 시간과 Docker 이미지 빌드 시간을 줄이되, provider 전환이나 운영 인프라 아키텍처처럼 외부 결정이 필요한 항목은 코드 변경에서 분리한다.

## 범위

### 이번 PR에서 적용

1. Hikari 초기 커넥션 확보 비용 축소
   - `HIKARI_MIN_IDLE` 기본값을 `10`에서 `2`로 낮춘다.
   - `HIKARI_INITIALIZATION_FAIL_TIMEOUT` 기본값을 `-1`로 둔다.
   - 운영에서 시작 직후 burst 트래픽을 우선해야 하면 환경 변수로 즉시 원복 가능하다.

2. Hibernate JDBC metadata 접근 차단
   - PostGIS dialect를 명시한다.
   - `hibernate.boot.allow_jdbc_metadata_access=false`를 설정한다.
   - `hibernate.temp.use_jdbc_metadata_defaults=false`를 설정한다.

3. local profile lazy initialization
   - `application-local.yml`을 추가한다.
   - 기본 active profile이 `local`이므로 로컬 개발 부팅만 지연 초기화한다.
   - prod/dev/test 프로필에 전파하지 않는다.

4. Spring Boot layered jar 기반 Docker 이미지
   - `bootJar` layered archive 생성을 명시한다.
   - Dockerfile에서 Spring Boot `tools` jarmode로 layer를 추출한다.
   - `dependencies`, `spring-boot-loader`, `snapshot-dependencies`, `application`을 별도 Docker layer로 복사한다.

5. Runtime base image JDK에서 JRE로 전환
   - `amazoncorretto:21-alpine`에서 프로젝트의 개발용 Dockerfile이 이미 사용하는 `eclipse-temurin:21-jre-alpine`로 바꾼다.
   - runtime container에는 javac/jlink가 필요 없다는 전제다.

6. GitHub Actions Docker layer cache 활성화
   - `docker/build-push-action@v6`에 `cache-from: type=gha`, `cache-to: type=gha,mode=max`를 추가한다.
   - layered jar 이후 적용하므로 fat jar 단일 레이어 캐시보다 cache 효율이 높다.

### 이번 PR에서 보류

1. Spring AI multi starter 제거
   - 현재 adapter 클래스가 `OpenAiChatModel`, `VertexAiGeminiChatModel`, `GoogleGenAiChatModel`을 직접 import한다.
   - dependency를 단순 제거하면 compile classpath가 깨진다.
   - provider별 source set, module 분리, 또는 adapter 추상화 재설계가 필요하므로 Phase 1에서 바로 제거하지 않는다.

2. `linux/amd64` 단일 플랫폼 전환
   - 현재 CI는 `linux/amd64,linux/arm64`를 모두 빌드한다.
   - ECS/Fargate, on-premise dev, ARM host 사용 여부 확인 전 축소하면 배포 가능성을 깨뜨릴 수 있다.
   - 인프라 아키텍처 확인 후 별도 PR로 결정한다.

## 실행 순서

```text
report copy
  |
  v
plan review
  |
  v
application.yml tuning
  |
  v
application-local.yml lazy init
  |
  v
bootJar layered config
  |
  v
Dockerfile layer extraction + JRE
  |
  v
GHA docker cache
  |
  v
compile/build verification
```

## Eng Review

| 항목 | 판단 | 이유 | 결정 |
|---|---|---|---|
| Hikari 기본값 축소 | 적합 | 환경 변수로 되돌릴 수 있고 변경 범위가 작다. 첫 요청 burst 지연만 확인하면 된다. | 진행 |
| Hibernate metadata 차단 | 적합 | PostGIS dialect 존재를 로컬 Gradle cache의 `hibernate-spatial-6.6.39.Final.jar`에서 확인했다. | 진행 |
| local lazy init | 적합 | 운영 프로필이 아니라 로컬 DX에만 영향을 준다. | 진행 |
| layered jar Dockerfile | 적합 | Spring Boot 3.5 공식 Dockerfile 가이드의 `-Djarmode=tools ... extract --layers` 흐름과 일치한다. | 진행 |
| JRE 전환 | 적합 | runtime container에서 JDK toolchain은 쓰지 않는다. `amazoncorretto:21-alpine-jre` 태그는 존재하지 않아 기존 dev Dockerfile과 같은 Temurin JRE Alpine을 사용한다. | 진행 |
| GHA cache | 적합 | layered jar 이후 적용하면 캐시 폭발 위험이 낮다. GHA 10GB 한도는 추후 summary에서 관찰한다. | 진행 |
| Spring AI starter 제거 | 위험 | 현재 source가 provider별 model class에 직접 의존한다. 단순 dependency 제거는 compile 실패 가능성이 크다. | 보류 |
| 단일 플랫폼 전환 | 위험 | 운영/개발 배포 아키텍처 확인이 없다. | 보류 |

## 검증 계획

1. `./gradlew compileJava`
2. `./gradlew bootJar`
3. `docker build -f docker/app/dockerfile .`
4. 가능하면 `docker run --rm ...`으로 `java -jar application.jar` layout 기동 확인

## 잔여 리스크

- Docker daemon 또는 registry 접근이 없는 환경에서는 Dockerfile 검증이 로컬에서 제한될 수 있다.
- `application-local.yml` 도입으로 로컬에서는 일부 bean 설정 오류가 첫 호출 시점으로 미뤄질 수 있다.
- GHA cache 비용/효율은 실제 Actions 로그에서 cache hit/miss를 확인해야 한다.
