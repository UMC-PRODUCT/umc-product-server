import org.gradle.api.plugins.quality.Checkstyle

plugins {
    java
    id("org.springframework.boot") version "3.5.9"
    id("io.spring.dependency-management") version "1.1.7"
    id("org.asciidoctor.jvm.convert") version "4.0.5"
    id("com.diffplug.spotless") version "8.5.1"
    checkstyle
    jacoco
}

group = "com.umc"
version = "2.0.0"
description = "UMC PRODUCT API by Server Team"

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

// 의존성 버전
val springDocVersion = "2.8.17"
val queryDslVersion = "5.1.0"
val jwtVersion = "0.12.5"
val awsVersion = "2.40.12"
val springAiVersion = "1.1.5"
val otelVersion = "1.61.0"
val otelInstrumentationVersion = "2.27.0-alpha"

/*
 * OpenTelemetry 의존성 정리
 *
 * 장애 원인:
 * - opentelemetry-logback-appender-1.0:2.27.0-alpha 는
 *   LogRecordBuilder#setException(Throwable) API 를 호출한다.
 * - 이 API 는 opentelemetry-api:1.61.0 에 존재하지만, Spring Boot 기본 관리 버전인
 *   1.49.0 에는 없다.
 * - 런타임 classpath 가 1.49.0 으로 잡히면, 예외 로그를 내보내는 순간
 *   NoSuchMethodError 가 발생한다. Tomcat 이 요청 처리 예외를 error 로그로 남기려는
 *   시점에 다시 로깅 예외가 터져 response/exception handling 흐름까지 깨진다.
 *
 * 주요 의존성 역할:
 * - spring-boot-starter-actuator: actuator, metrics 자동 설정의 Spring Boot 진입점.
 * - micrometer-observation: Spring 관측 추상화. HTTP/DB/custom observation 을
 *   metrics/tracing 과 연결하는 기반.
 * - micrometer-tracing-bridge-otel: Micrometer Tracing 을 OpenTelemetry SDK 로 연결.
 * - opentelemetry-exporter-otlp: trace/log/metric 을 OTLP 로 Collector 에 전송.
 * - opentelemetry-logback-appender-1.0: Logback 이벤트를 OpenTelemetry Logs 로 변환.
 * - micrometer-registry-prometheus: /actuator/prometheus scrape 용 metrics registry.
 * - micrometer-registry-otlp: Micrometer metrics 를 OTLP endpoint 로 push.
 * - logstash-logback-encoder: stdout JSON 로그 포맷. OpenTelemetryAppender 는 여기서
 *   제공하는 structured arguments 도 capture 한다.
 * - context-propagation: 비동기 작업에서 trace/span context 손실을 줄인다.
 * - p6spy-spring-boot-starter, firebase-admin, google-cloud-storage/firestore 는
 *   transitive dependency 로 Spring Boot BOM 또는 OTel 관련 모듈을 추가 요청할 수 있어
 *   dependencyInsight 에 함께 나타난다.
 * - opentelemetry-bom: OTel API/SDK/exporter 모듈 버전을 같은 축으로 정렬한다.
 *
 * 런타임 흐름:
 * Tomcat 요청 처리 중 예외 발생 -> Logback error 로그 기록 -> OTEL appender 실행
 * -> Throwable 을 OTel LogRecord 로 매핑 -> setException(Throwable) 호출
 * -> 런타임 OTel API 버전이 낮으면 NoSuchMethodError 발생.
 *
 * 따라서 Spring Boot 의 opentelemetry.version 관리 속성과 명시 OTel BOM 을
 * appender 가 컴파일된 API 버전(1.61.0)으로 정렬한다.
 */
extra["opentelemetry.version"] = otelVersion

// REST DOCS
val snippetsDir = file("build/generated-snippets")
val asciiDocsSourceDir = "docs/asciidoc"
val asciiDocsDir = "docs/static"

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
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

    // Mock Data
    implementation("net.datafaker:datafaker:2.5.4")

    // JWT
    implementation("io.jsonwebtoken:jjwt-api:${jwtVersion}")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:${jwtVersion}")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:${jwtVersion}")

    // --- Encryption  ---
    implementation("org.bouncycastle:bcpkix-jdk18on:1.78.1")
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
    // 서버 시작 시 자동으로 Docker Compose 실행
    developmentOnly("org.springframework.boot:spring-boot-docker-compose")

    // 다들 잘 아는 그 lombok
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")

    // SQL 출력용 P6Spy
    implementation("com.github.gavlyukovskiy:p6spy-spring-boot-starter:1.10.0")

    // --- Cloud Service ---
    implementation(platform("software.amazon.awssdk:bom:${awsVersion}"))
    implementation("software.amazon.awssdk:s3")
    implementation("software.amazon.awssdk:cloudfront")  // CloudFront Signed URL
    implementation("software.amazon.awssdk:sesv2")       // AWS SES v2 (인증 이메일 발송)
    implementation("com.google.cloud:google-cloud-storage")

    // --- Email ---
    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")

    // BOM으로 버전 강제 정렬
    implementation(platform("com.google.protobuf:protobuf-bom:4.29.3"))
    implementation(platform("io.opentelemetry:opentelemetry-bom:${otelVersion}"))

    // --- Metrics ---
    implementation("io.micrometer:micrometer-registry-prometheus")
    implementation("io.micrometer:micrometer-registry-otlp")

    // --- Structured Logging (ADR-016) ---
    // dev/staging/prod 환경의 JSON 단일 라인 로그 encoder. local 은 텍스트 유지.
    implementation("net.logstash.logback:logstash-logback-encoder:7.4")

    // --- Tracing ---
    implementation("io.micrometer:micrometer-observation") // 관측 기능: metrics + tracing
    implementation("io.micrometer:micrometer-tracing-bridge-otel") // OpenTelemetry 연동
    implementation("io.opentelemetry:opentelemetry-exporter-otlp") // OTLP Exporter
    implementation("io.opentelemetry.instrumentation:opentelemetry-logback-appender-1.0:${otelInstrumentationVersion}")
    implementation("io.micrometer:context-propagation") // 비동기 작업에서 context를 잃어버리지 않도록 함

    // Firebase Admin SDK
    implementation("com.google.firebase:firebase-admin:9.7.1")

    // --- Spring AI (LLM provider 통합) ---
    implementation(platform("org.springframework.ai:spring-ai-bom:${springAiVersion}"))
    implementation("org.springframework.ai:spring-ai-starter-model-openai")
    implementation("org.springframework.ai:spring-ai-starter-model-vertex-ai-gemini")
    implementation("org.springframework.ai:spring-ai-starter-model-google-genai")

    // --- Cache ---
    implementation("org.springframework.boot:spring-boot-starter-cache")
    implementation("com.github.ben-manes.caffeine:caffeine")

    // --- Test ---
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation("org.springframework.boot:spring-boot-testcontainers")
    testImplementation("org.testcontainers:testcontainers")
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.testcontainers:postgresql")
    testImplementation("com.navercorp.fixturemonkey:fixture-monkey-starter:1.1.19") // Fixture 생성에 도움을 주는 친구
    testRuntimeOnly("jakarta.mail:jakarta.mail-api") // JavaMailSender MockitoBean 초기화에 필요

    testCompileOnly("org.projectlombok:lombok")
    testAnnotationProcessor("org.projectlombok:lombok")

    // --- Spring REST Docs ---
    "asciidoctorExt"("org.springframework.restdocs:spring-restdocs-asciidoctor")
    testImplementation("org.springframework.restdocs:spring-restdocs-mockmvc")
}

springBoot {
    buildInfo()
}

spotless {
    ratchetFrom("origin/develop")

    java {
        target("src/**/*.java")
        importOrder("\\#", "java", "javax", "org", "net", "com", "")
        removeUnusedImports()
        forbidWildcardImports()
        trimTrailingWhitespace()
        leadingTabsToSpaces(4)
        endWithNewline()
        formatAnnotations()
    }

    format("misc") {
        target(
            "*.gradle.kts",
            ".editorconfig",
            ".github/**/*.yml",
            ".github/**/*.yaml",
            "config/**/*.xml"
        )
        trimTrailingWhitespace()
        leadingTabsToSpaces(4)
        endWithNewline()
    }
}

checkstyle {
    toolVersion = "13.4.2"
    configDirectory.set(layout.projectDirectory.dir("config/checkstyle"))
    configProperties["suppressionFile"] =
        layout.projectDirectory.file("config/checkstyle/naver-checkstyle-suppressions.xml").asFile.absolutePath
    isIgnoreFailures = false
    maxErrors = 0
    maxWarnings = Int.MAX_VALUE
}

val lintBaseRef = providers.gradleProperty("lintBase").orElse("origin/develop")

fun changedJavaFiles(vararg sourceRoots: String) = lintBaseRef.flatMap { baseRef ->
    val diffAgainstBaseProvider = providers.exec {
        commandLine("git", "diff", "--name-only", "--diff-filter=ACMR", "$baseRef...HEAD", "--", *sourceRoots)
        isIgnoreExitValue = true
    }.standardOutput.asText

    val localDiffProvider = providers.exec {
        commandLine("git", "diff", "--name-only", "--diff-filter=ACMR", "HEAD", "--", *sourceRoots)
        isIgnoreExitValue = true
    }.standardOutput.asText

    diffAgainstBaseProvider.zip(localDiffProvider) { baseOutput, localOutput ->
        (baseOutput.lines() + localOutput.lines())
            .asSequence()
            .map(String::trim)
            .filter { it.endsWith(".java") && it.isNotBlank() }
            .distinct()
            .map(::file)
            .filter(File::exists)
            .toList()
    }
}

tasks.withType<Checkstyle>().configureEach {
    classpath = files()
    exclude("**/Q*.java")

    reports {
        xml.required.set(true)
        html.required.set(true)
    }
}

tasks.named<Checkstyle>("checkstyleMain") {
    setSource(files(changedJavaFiles("src/main/java")))
}

tasks.named<Checkstyle>("checkstyleTest") {
    setSource(files(changedJavaFiles("src/test/java")))
}

tasks.named<org.springframework.boot.gradle.tasks.bundling.BootJar>("bootJar") {
    layered {
        enabled.set(true)
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.compilerArgs.add("-Xlint:deprecation")
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
    doFirst { // 기존에 존재하는 파일들을 지우고 시작함
        println("=".repeat(50))
        println("[test] 테스트를 시작합니다.")
        println("=".repeat(50))
    }

    // 테스트가 터지면 이 곳에 ignoreFailures = true 를 넣으시면 됩니다.
    outputs.dir(snippetsDir)
    finalizedBy(tasks.jacocoTestReport)

    doLast {
        println("=".repeat(50))
        println("[test] 테스트가 완료되었습니다.")
        println("=".repeat(50))
    }
}

// codecov를 위한 Jacoco 설정
tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
}

tasks.register("generateRestDocsIndex") {
    dependsOn(tasks.test)

    doLast {
        val indexFile = file("docs/asciidoc/index.adoc")

        val content = buildString {
            appendLine("= UMC Product API Documentation")
            appendLine(":doctype: book")
            appendLine(":icons: font")
            appendLine(":source-highlighter: highlightjs")
            appendLine(":toc: left")
            appendLine(":toclevels: 2")
            appendLine(":sectlinks:")
            appendLine()
            appendLine("ifndef::snippets[]")
            appendLine(":snippets: ../../../build/generated-snippets")
            appendLine("endif::[]")
            appendLine()

            // 컨트롤러별로 그룹핑
            val controllerMap = mutableMapOf<String, MutableList<String>>()

            snippetsDir.listFiles()?.forEach { controllerDir ->
                if (controllerDir.isDirectory) {
                    val controllerName = controllerDir.name
                    controllerDir.listFiles()?.forEach { testDir ->
                        if (testDir.isDirectory) {
                            controllerMap
                                .getOrPut(controllerName) { mutableListOf() }
                                .add(testDir.name)
                        }
                    }
                }
            }

            // 정렬 후 출력
            controllerMap.keys.sorted().forEach { controller ->
                val displayName = controller
                    .replace("-controller-test", "")
                    .replace("-", " ")
                    .replaceFirstChar { it.uppercase() }

                appendLine("== $displayName")
                appendLine()

                controllerMap[controller]!!.sorted().forEach { testName ->
                    appendLine("=== $testName")

                    // 실제 존재하는 snippet 타입만 찾기
                    val testDir = file("${snippetsDir}/$controller/$testName")
                    val availableSnippets = listOf(
                        "http-request", "http-response",
                        "path-parameters", "query-parameters",
                        "request-fields", "response-fields"
                    ).filter { snippetType ->
                        File(testDir, "$snippetType.adoc").exists()
                    }

                    // 존재하는 snippet만으로 operation 매크로 생성
                    if (availableSnippets.isNotEmpty()) {
                        val snippetsParam = availableSnippets.joinToString(",")
                        appendLine("operation::$controller/$testName[snippets='$snippetsParam']")
                    } else {
                        // snippet 없으면 기본 operation (모든 snippet 포함 시도)
                        appendLine("operation::$controller/$testName[]")
                    }

                    appendLine()
                }
            }
        }

        indexFile.writeText(content)
        println("[generateRestDocsIndex] index.adoc 자동 생성 완료: ${indexFile.absolutePath}")
    }
}

tasks.asciidoctor { // asciidoctor task 설정
    dependsOn("generateRestDocsIndex")  // asciidoctor 전에 index 먼저 생성
    configurations("asciidoctorExt") // asciidoctorExt 설정 추가
    inputs.dir(snippetsDir) // 테스트를 통해서 생성된 snippets를 입력으로 사용

    baseDirFollowsSourceDir() // .adoc 파일에서 다른 .adoc을 include하여 사용하는 경우에 대한 경로 문제 해결
    setSourceDir(file(asciiDocsSourceDir)) // AsciiDoc 소스 경로 지정 (repo custom)

    // index.adoc 파일만 변환 대상으로 설정
    sources {
        include("**/index.adoc")
    }

    attributes(mapOf("snippets" to snippetsDir.toString())) // .adoc 파일 안에서 {snippets} 변수 사용 가능

    doFirst { // 기존에 존재하는 파일들을 지우고 시작함
        println("=".repeat(50))
        println("[asciidoctor] AsciiDoc 문서 생성을 시작합니다.")
        println("=".repeat(50))
    }

    doLast {
        println("=".repeat(50))
        println("[asciidoctor] AsciiDoc 문서 생성이 완료되었습니다.")
        println("=".repeat(50))
    }
}

val copyDocument = tasks.register<Copy>("copyDocument") { // REST DOCS 복사하는 task
    dependsOn(tasks.asciidoctor) // asciidoctor task에 의존하도록 설정

    doFirst {
        println("=".repeat(50))
        println("[asciidoctor] 생성된 Rest Docs를 복사합니다. 기존 문서들을 삭제됩니다.")
        delete(file(asciiDocsDir))
        println("=".repeat(50))
    }


    from(file("build/docs/asciidoc")) // 기본 생성된 경로
    into(file(asciiDocsDir)) // 파일을 복사할 경로

    doLast {
        println("=".repeat(50))
        println("[copyDocument] AsciiDoc 문서를 docs/static에 복사하였습니니다.")
        println("=".repeat(50))
    }
}

tasks.build {
    dependsOn(tasks.clean) // 빌드 전 clean 수행
    dependsOn(copyDocument) // clean 수행 후에, AsciiDoc 문서 제작

    doLast {
        println("[build] gradle build가 완료되었습니다.")
    }
}

// bootJar에는 document 생성을 제외하도록 함 (test가 실패하더라도 배포는 우선 되도록 하기 위함)
