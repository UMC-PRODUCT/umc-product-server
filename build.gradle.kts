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

val springDocVersion = "2.8.14"
val queryDslVersion = "5.0.0"
val jwtVersion = "0.12.5"
val awsVersion = "2.40.12"

// QueryDSL Q클래스 생성 경로 설정
val querydslDir = layout.buildDirectory.dir("generated/querydsl").get().asFile

sourceSets {
    main {
        java {
            srcDirs(querydslDir)
        }
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.generatedSourceOutputDirectory.set(querydslDir)
}

tasks.named("clean") {
    doLast {
        querydslDir.deleteRecursively()
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

    // --- Metrics ---
    implementation("io.micrometer:micrometer-registry-prometheus")

    // --- Tracing ---
    implementation("io.micrometer:micrometer-observation") // 관측 기능: metrics + tracing
    implementation("io.micrometer:micrometer-tracing-bridge-otel") // OpenTelemetry 연동
    implementation("io.opentelemetry:opentelemetry-exporter-otlp") // OTLP Exporter
    implementation("io.micrometer:context-propagation") // 비동기 작업에서 context를 잃어버리지 않도록 함


    // --- Test ---
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation("com.h2database:h2")
    testImplementation("org.testcontainers:testcontainers")
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.testcontainers:postgresql")

    // --- Spring REST Docs ---
    "asciidoctorExt"("org.springframework.restdocs:spring-restdocs-asciidoctor")
    testImplementation("org.springframework.restdocs:spring-restdocs-mockmvc")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

val snippetsDir = file("build/generated-snippets")

tasks.test {
    outputs.dir(snippetsDir)

}

tasks.asciidoctor {
    inputs.dir(snippetsDir)
    configurations("asciidoctorExt")

    attributes(mapOf("snippets" to snippetsDir)) // @kyeoungwoon 추가!

    sources {
        include("**/index.adoc")
    }

    baseDirFollowsSourceDir()
    dependsOn(tasks.test)
}

tasks.bootJar {
    dependsOn(tasks.asciidoctor)
    from(tasks.asciidoctor.get().outputDir) {
        into("static/docs")
    }
}
