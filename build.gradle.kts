plugins {
    java
    id("org.springframework.boot") version "3.5.9"
    id("io.spring.dependency-management") version "1.1.7"
    id("org.asciidoctor.jvm.convert") version "4.0.5"
}

group = "com.umc"
version = "0.0.1"
description = "umc-product"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

// Lombok이 compile time에만 포함되도록 설정
configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
    create("asciidoctorExt")
}

repositories {
    mavenCentral()
}

// 의존성 버전 관리
val springDocVersion = "2.8.14"
val queryDslVersion = "5.0.0"
val jwtVersion = "0.12.5"
val awsVersion = "2.40.12"

// REST DOCS
val snippetsDir = file("build/generated-snippets")
val docsDir = "docs/asciidoc"

// QueryDSL Q클래스 생성 경로 설정
val querydslDir = layout.buildDirectory.dir("generated/querydsl").get().asFile

sourceSets {
    main {
        java {
            srcDirs(querydslDir)
        }
    }
}

dependencies {
    // --- Spring Boot Starters (버전 생략: Boot가 관리) ---
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    //    implementation("org.springframework.boot:spring-boot-starter-websocket") // 필요한 경우 그 때 추가
    implementation("org.springframework.boot:spring-boot-starter-aop")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-client")  // OAuth2 Client
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")

    // JWT
    implementation("io.jsonwebtoken:jjwt-api:${jwtVersion}")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:${jwtVersion}")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:${jwtVersion}")

    // --- Encryption  ---
    implementation("org.bouncycastle:bcprov-jdk18on:1.78.1")

    // --- QueryDSL ---
    // Jakarta 분류가 필요하므로 버전 명시가 안전할 수 있음
    implementation("com.querydsl:querydsl-jpa:${queryDslVersion}:jakarta")
    annotationProcessor("com.querydsl:querydsl-apt:${queryDslVersion}:jakarta")
    annotationProcessor("jakarta.annotation:jakarta.annotation-api")

    // APT가 jakarta 클래스를 로딩할 수 있게 명시
    annotationProcessor("jakarta.persistence:jakarta.persistence-api")

    // --- Database ---
    implementation("org.flywaydb:flyway-core")
    implementation("org.flywaydb:flyway-database-postgresql")
    runtimeOnly("org.postgresql:postgresql") // 버전은 Boot가 관리

    // --- Spatial / Location ---
    // JTS (위치 데이터용)
    implementation("org.locationtech.jts:jts-core:1.19.0")
    // Hibernate Spatial (JPA에서 Point 타입 사용)
    implementation("org.hibernate.orm:hibernate-spatial")

    // --- OpenAPI / Swagger ---
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:${springDocVersion}")

    // --- Utils ---
    // 서버 시작 시 자동으로 Dokcer Compose 실행
    developmentOnly("org.springframework.boot:spring-boot-docker-compose")
    // 다들 잘 아는 그 lombok
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
    // SQL 출력용 P6Spy
    implementation("com.github.gavlyukovskiy:p6spy-spring-boot-starter:1.10.0")

    // --- AWS & Cloud (필요 시 주석 해제) ---
    implementation(platform("software.amazon.awssdk:bom:${awsVersion}"))
    implementation("software.amazon.awssdk:s3")

    // --- Email ---
    implementation("org.springframework.boot:spring-boot-starter-mail")
    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")

    // --- Metrics ---
    implementation("io.micrometer:micrometer-registry-prometheus")

    // --- Tracing ---
    implementation("io.micrometer:micrometer-observation") // 관측 기능: metrics + tracing
    implementation("io.micrometer:micrometer-tracing-bridge-otel") // OpenTelemetry 연동
    implementation("io.opentelemetry:opentelemetry-exporter-otlp") // OTLP Exporter
    implementation("io.micrometer:context-propagation") // 비동기 작업에서 context를 잃어버리지 않도록 함


    // Firebase Admin SDK
    implementation("com.google.firebase:firebase-admin:9.2.0")

    // --- Test ---
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation("org.springframework.boot:spring-boot-testcontainers")
    testImplementation("org.testcontainers:testcontainers")
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.testcontainers:postgresql")
    implementation("org.hibernate.orm:hibernate-spatial")

    // --- Spring REST Docs ---
    "asciidoctorExt"("org.springframework.restdocs:spring-restdocs-asciidoctor")
    testImplementation("org.springframework.restdocs:spring-restdocs-mockmvc")
}

tasks.withType<JavaCompile>().configureEach {
    options.generatedSourceOutputDirectory.set(querydslDir)
}

tasks.clean {
    doFirst {
        println("=".repeat(50))
        println("[clean] gradle clean을 시작합니다.")
        println("=".repeat(50))
    }

    doLast {
        querydslDir.deleteRecursively()
        println("[clean] QueryDSL 생성 디렉토리를 삭제하였습니다.")

        println("[clean] gradle clean이 완료되었습니다.")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.test {
    outputs.dir(snippetsDir)
}

tasks.asciidoctor { // asciidoctor task 설정
    doFirst { // 기존에 존재하는 파일들을 지우고 시작함
        println("=".repeat(50))
        println("[asciidoctor] docs/static 디렉토리를 삭제합니다.")
        println("=".repeat(50))
        delete(file(docsDir))
    }

    configurations("asciidoctorExt") // asciidoctorExt 설정 추가
    baseDirFollowsSourceDir() // .adoc 파일에서 다른 .adoc을 include하여 사용하는 경우에 대한 경로 문제 해결
    setSourceDir(file(docsDir)) // AsciiDoc 소스 경로 지정 (repo custom)
    inputs.dir(snippetsDir) // 테스트를 통해서 생성된 snippets를 입력으로 사용
    dependsOn(tasks.test) // test task에 의존하도록 설정

    attributes(mapOf("snippets" to snippetsDir)) // .adoc 파일 안에서 {snippets} 변수 사용 가능

    // index.adoc 파일만 변환 대상으로 설정
    sources {
        include("**/index.adoc")
    }
}

val copyDocument = tasks.register<Copy>("copyDocument") { // REST DOCS 복사하는 task
    dependsOn(tasks.asciidoctor) // asciidoctor task에 의존하도록 설정

    from(file("build/docs/asciidoc")) // 기본 생성된 경로
    into(file("docs/static")) // 파일을 복사할 경로

    doLast {
        println("[copyDocument] AsciiDoc 문서를 docs/static에 복사하였습니니다.")
    }
}

tasks.build {
    dependsOn(tasks.clean) // 빌드 전 clean 수행
    dependsOn(copyDocument) // clean 수행 후에, AsciiDoc 문서 제작

    doLast {
        println("[build] gradle build가 완료되었습니다.")
    }
}

tasks.bootJar {
    dependsOn(copyDocument) // bootJar 실행 시 AsciiDoc이 생성하도록 함
}
