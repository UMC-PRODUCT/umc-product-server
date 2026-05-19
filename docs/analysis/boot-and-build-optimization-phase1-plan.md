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
   - `application.yml` 안에 `local` profile document를 추가한다.
   - 기본 active profile이 `local`이므로 로컬 개발 부팅만 지연 초기화한다.
   - prod/dev/test 프로필에 전파하지 않는다.

4. Spring Boot layered jar 기반 Docker 이미지
   - `bootJar` layered archive 생성을 명시한다.
   - Dockerfile에서 Spring Boot `tools` jarmode로 layer를 추출한다.
   - `dependencies`, `spring-boot-loader`, `snapshot-dependencies`, `application`을 별도 Docker layer로 복사한다.

5. Runtime base image Corretto JDK에서 Temurin JRE로 전환
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
application.yml local profile lazy init
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

## 추가 판단: Corretto에서 Temurin/JRE 전환 영향

### 변경 범위

이번 변경은 GitHub Actions에서 컴파일/테스트에 쓰는 JDK 자체를 바꾸는 변경이 아니다. `.github/workflows/ci.yml`의 `actions/setup-java@v4`는 계속 `distribution: 'corretto'`를 사용한다. 바뀐 것은 운영 Docker 이미지의 런타임 베이스 이미지다.

| 구분 | 변경 전 | 변경 후 | 영향 범위 |
|---|---|---|---|
| CI compile/test JDK | Corretto 21 | Corretto 21 | 변경 없음 |
| Docker builder stage | 없음 | `eclipse-temurin:21-jre-alpine` | Spring Boot jar layer 추출 실행 |
| Docker runtime stage | `amazoncorretto:21-alpine` | `eclipse-temurin:21-jre-alpine` | 운영 컨테이너 Java 런타임 |
| Java major version | 21 | 21 | 변경 없음 |
| 패키징 | JDK 포함 이미지 | JRE 이미지 | JDK 도구 제거 |

따라서 이 변경은 "Java 21 런타임 배포판 변경 + JDK에서 JRE로 축소"에 가깝다. Eclipse Temurin은 Docker Official Image로 제공되는 OpenJDK 빌드이며 Java SE TCK-tested 런타임으로 문서화되어 있다. Amazon Corretto도 OpenJDK 기반 TCK 인증 배포판이다. 두 이미지 모두 Java 21 기준의 표준 JVM 동작을 제공하므로, 애플리케이션 코드가 Java 표준 API와 일반 Spring Boot 런타임에만 의존한다면 기능 영향은 낮다.

### 영향 없음으로 판단한 근거

- 애플리케이션 빌드와 테스트는 여전히 Corretto 21에서 수행되므로 compile classpath, annotation processing, QueryDSL Q클래스 생성에는 영향이 없다.
- 런타임 컨테이너에서 `javac`, `jlink`, `jcmd`, `jmap`, `ToolProvider.getSystemJavaCompiler`, attach API 같은 JDK 도구를 호출하는 코드나 스크립트가 확인되지 않았다.
- 기존 운영 이미지도 Alpine 기반이고 변경 후 이미지도 Alpine 기반이므로 libc 계열 전환(glibc ↔ musl) 리스크가 새로 생기지 않는다.
- `eclipse-temurin:21-jre-alpine` 공식 이미지 태그는 `amd64`, `arm64v8`를 지원하므로 현재 `linux/amd64,linux/arm64` 멀티 플랫폼 빌드 범위와 맞는다.
- Spring Boot `tools` jarmode layer 추출은 JDK 컴파일러가 아니라 `java` 런타임으로 실행되므로 JRE 이미지에서도 동작한다.

### 남는 리스크

- JDK 도구가 사라진다. 운영 컨테이너 안에서 `jcmd`, `jmap`, `jstack`, `jfr` CLI로 즉석 진단하는 운영 절차가 있다면 별도 디버그 이미지나 외부 수집 절차가 필요하다.
- `-XX:+HeapDumpOnOutOfMemoryError` 자체는 JRE에서도 동작하지만, 생성된 heap dump를 컨테이너 내부에서 바로 분석하는 도구는 없다.
- 보고서의 장기 후보인 CRaC는 Corretto/JDK 배포판 선택과 직접 관련될 수 있으므로, Phase 2 이후 CRaC를 실제 적용할 때는 Temurin JRE 유지 여부를 다시 결정해야 한다.
- 벤더별 JVM 패치와 기본 CA/JVM 파일 구성 차이는 0이 아니다. 배포 전 최소 1회 `docker run` smoke test와 dev/staging 기동 확인이 필요하다.

### 로컬 검증 메모

- `java -version` 로컬 런타임: Temurin 21.0.11.
- `java -Djarmode=tools -jar ... extract --layers`로 layer 추출을 확인했다.
- 추출 결과의 주요 크기: `dependencies/lib` 약 175MB, application jar 약 4MB, `snapshot-dependencies` 0MB.
- Dockerfile과 동일하게 `dependencies`와 `application` layer를 한 디렉터리에 배치한 뒤 `java -jar`를 실행했을 때 `UmcProductApplication` 기동 로그까지 도달했다. 이후 실패 원인은 layer/런타임 문제가 아니라 로컬 환경 변수 `MAIL_HOST` 미설정이었다.
- 현재 환경에서는 Docker socket 권한 문제로 실제 `docker build`는 아직 로컬 검증하지 못했다.

### 참고 문서

- [Eclipse Temurin Docker Official Image](https://hub.docker.com/_/eclipse-temurin)
- [Docker official-images eclipse-temurin tag manifest](https://github.com/docker-library/official-images/blob/master/library/eclipse-temurin)
- [Amazon Corretto 21 Docker image guide](https://docs.aws.amazon.com/corretto/latest/corretto-21-ug/docker-install.html)

## Docker build cache 적용 효과

### 적용 위치

`.github/workflows/ci.yml`의 `docker/build-push-action@v6`에 다음 옵션을 추가했다.

```yaml
cache-from: type=gha
cache-to: type=gha,mode=max
```

GitHub hosted runner에서 `docker/build-push-action`을 사용할 때 `type=gha` 캐시의 URL과 토큰은 액션이 자동으로 채운다. 현재 워크플로는 이미 `docker/setup-buildx-action@v3`를 사용하고 있어 BuildKit 기반 cache import/export 경로와 맞는다.

### 어떤 부분에 작용하는가

1. Gradle 빌드에는 직접 작용하지 않는다.
   - `compileJava`, `test`, `bootJar`는 Docker build 전에 실행된다.
   - 이 구간은 기존 `actions/setup-java@v4`의 `cache: gradle`이 담당한다.

2. Dockerfile stage와 image layer에 작용한다.
   - BuildKit이 이전 workflow run의 cache record를 `cache-from: type=gha`로 가져온다.
   - 빌드가 끝나면 다음 run에서 재사용할 cache record와 layer를 `cache-to: type=gha,mode=max`로 저장한다.
   - `mode=max`는 final image layer뿐 아니라 중간 stage cache까지 가능한 넓게 저장하므로, `builder` stage의 layer 추출 단계와 runtime stage의 COPY 단계도 캐시 후보가 된다.

3. Spring Boot layered jar와 결합해 큰 의존성 layer를 안정화한다.
   - 현재 추출 기준으로 `dependencies/lib`가 약 175MB이고 application jar가 약 4MB다.
   - 일반적인 애플리케이션 코드 변경에서는 의존성 layer 내용이 그대로이고 application layer만 바뀐다.
   - 이 경우 ECR push/pull 및 BuildKit cache export에서 큰 의존성 layer를 다시 전송하지 않는 효과가 난다.

4. 멀티 플랫폼 빌드에서 플랫폼별로 따로 작용한다.
   - 현재 `linux/amd64,linux/arm64`를 모두 빌드한다.
   - base image pull, `apk add curl`, 유저/권한 설정, layer COPY 결과는 플랫폼별 cache key를 가진다.
   - 특히 ARM64 빌드는 GitHub runner에서 QEMU 에뮬레이션 비용이 붙을 수 있어, cache hit 시 절감 체감이 더 크다.

### 기대값

정량 baseline이 아직 없으므로 아래 값은 현재 Dockerfile 구조와 layer 크기 기준의 기대 범위다.

| 시나리오 | cache hit 범위 | 기대 효과 |
|---|---|---|
| 최초 적용 직후 cold build | 거의 없음 | cache export 때문에 오히려 소폭 증가 가능 |
| 같은 커밋 재실행 또는 Docker 입력이 동일한 재시도 | base/apk/extract/final copy 대부분 hit | 기존 보고서의 **5~10분 단축** 기대가 가장 잘 맞는 구간 |
| 일반 소스 변경, 의존성 변경 없음 | base/apk/final dependency layer 재사용, application layer만 갱신 | **1~3분 단축** 기대. ECR push가 병목이면 더 커질 수 있음 |
| `build.gradle.kts` 또는 의존성 변경 | dependency layer invalidation | 해당 run 효과 제한적. 다음 run부터 다시 warm cache |
| Dockerfile 또는 base image 변경 | 관련 step 이후 invalidation | cache 효과 제한적. 변경 후 새 cache 형성 |

중요한 제한은 현재 Dockerfile이 host에서 만들어진 fat jar 전체를 `builder` stage에 `application.jar`로 복사한다는 점이다. 애플리케이션 코드만 바뀌어도 jar 파일 digest가 바뀌므로 `RUN java -Djarmode=tools ... extract` 단계는 다시 실행될 수 있다. 즉, 이번 cache 적용은 "Gradle 빌드 자체를 생략"하거나 "모든 소스 변경에서 layer 추출을 생략"하는 최적화가 아니라, Docker/BuildKit layer 재사용과 registry 전송량 감소를 통해 CI 후반부를 줄이는 최적화다.

### 관찰해야 할 지표

- `docker/build-push-action` 로그의 `importing cache manifest from gha`, `CACHED`, `exporting cache` 출력
- `docker buildx` step 전체 시간과 push 시간
- GitHub Actions cache 사용량 및 eviction 여부
- ECR에 실제 push된 layer 수와 크기
- 같은 커밋 재실행, 소스만 변경, 의존성 변경 케이스를 분리한 평균 시간

### 참고 문서

- [Docker Docs: GitHub Actions cache backend](https://docs.docker.com/build/cache/backends/gha/)
- [Docker Docs: Cache management with GitHub Actions](https://docs.docker.com/build/ci/github-actions/cache/)

## 검증 계획

1. `./gradlew compileJava`
2. `./gradlew bootJar`
3. `docker build -f docker/app/dockerfile .`
4. 가능하면 `docker run --rm ...`으로 `java -jar application.jar` layout 기동 확인

## 잔여 리스크

- Docker daemon 또는 registry 접근이 없는 환경에서는 Dockerfile 검증이 로컬에서 제한될 수 있다.
- `application.yml`의 `local` profile lazy initialization으로 로컬에서는 일부 bean 설정 오류가 첫 호출 시점으로 미뤄질 수 있다.
- GHA cache 비용/효율은 실제 Actions 로그에서 cache hit/miss를 확인해야 한다.
