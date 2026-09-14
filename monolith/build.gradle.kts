// 아직 모듈로 빼내지 못한 나머지 전부. 실행 클래스와 src/main/resources 도 여기 있다.
// 공통 빌드 설정은 루트의 subprojects 블록에서 받는다.

plugins {
    `java-test-fixtures`
}

// main 의 implementation 의존은 testFixtures 컴파일에 전달되지 않는다.
// testFixtures 는 main 컴포넌트만 api 로 받으므로, 지원 코드가 직접 쓰는 라이브러리는 다시 선언한다.
// 목록은 src/testFixtures/java 의 실제 import 를 집계해서 정했다.
// 전부 api 다. IntegrationTestSupport 의 protected 필드와 TestMetricsAutoConfiguration 의
// public 반환 타입으로 노출돼 있어, implementation 으로 두면 상속한 모듈이 그 멤버를 쓰는 순간
// "cannot access" 로 컴파일이 깨진다.
dependencies {
    testFixturesApi("org.springframework.boot:spring-boot-starter-test")
    testFixturesApi("org.springframework.boot:spring-boot-testcontainers")
    testFixturesApi("org.springframework.boot:spring-boot-starter-data-jpa")
    testFixturesApi("org.springframework.boot:spring-boot-starter-security")
    testFixturesApi("org.testcontainers:testcontainers")
    testFixturesApi("org.testcontainers:junit-jupiter")
    testFixturesApi("org.testcontainers:postgresql")
    testFixturesApi("com.navercorp.fixturemonkey:fixture-monkey-starter:${libs.versions.fixture.monkey.get()}")

    // 외부 시스템 모킹(@MockitoBean)과 테스트 메트릭 구성에 쓴다.
    testFixturesApi("org.springframework.boot:spring-boot-starter-mail")
    testFixturesApi("com.google.firebase:firebase-admin:${libs.versions.firebase.admin.get()}")
    testFixturesApi("io.micrometer:micrometer-core")
}
