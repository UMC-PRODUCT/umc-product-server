import com.diffplug.gradle.spotless.SpotlessExtension
import org.gradle.api.GradleException
import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
    // 루트에는 소스가 없다. base 는 check/build 같은 lifecycle task 를 제공한다.
    // documentation-catalog 스크립트가 루트 check 에 의존을 거는데, 이게 없으면 구성 단계에서 실패한다.
    base
    alias(libs.plugins.spring.boot) apply false
    alias(libs.plugins.spring.dependency.management) apply false
    alias(libs.plugins.spotless)
}

allprojects {
    group = "com.umc"
    version = "2.0.0"
}

description = "UMC PRODUCT API by Server Team"

subprojects {
    apply(plugin = "java")
    apply(plugin = "checkstyle")
    apply(plugin = "org.springframework.boot")
    apply(plugin = "io.spring.dependency-management")
    apply(plugin = "com.diffplug.spotless")

    extensions.configure<JavaPluginExtension>("java") {
        toolchain {
            languageVersion = JavaLanguageVersion.of(21)
        }
    }

    repositories {
        mavenCentral()
    }

    // 실행 가능 jar 는 :app 만 만든다. 나머지는 라이브러리 jar 를 낸다.
    // Spring Boot 는 bootJar 와 공존할 때 일반 jar 에 -plain 을 붙이는데, bootJar 를 끈 모듈에는 불필요하다.
    if (name != "app") {
        tasks.named<BootJar>("bootJar") { enabled = false }
        tasks.named<Jar>("jar") {
            enabled = true
            archiveClassifier.set("")
        }
    }

    // apply(from = ...) 스크립트는 plugins {} 로 올린 플러그인 타입을 못 본다. 클래스로더 스코프가 달라서다.
    // spotless 설정이 여기 있는 이유다. 나머지 스크립트는 Gradle 코어 API 만 써서 분리할 수 있다.
    extensions.configure<SpotlessExtension>("spotless") {
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
    }

    apply(from = rootProject.file("gradle/dependencies.gradle.kts"))
    apply(from = rootProject.file("gradle/querydsl.gradle.kts"))
    apply(from = rootProject.file("gradle/quality.gradle.kts"))
    apply(from = rootProject.file("gradle/testing.gradle.kts"))
    apply(from = rootProject.file("gradle/resources.gradle.kts"))
}

// Flyway 마이그레이션은 :monolith 한 곳에 있다. 검사도 한 번만 돌리고, 각 모듈의 test 가 이 task 에 의존한다.
// 프로젝트 의존은 상대 프로젝트의 검증 task 를 자동으로 끌어오지 않기 때문이다.
val checkDuplicateFlywayMigrationVersions by tasks.registering {
    group = "verification"
    description = "Fails when two Flyway versioned migrations share the same version."

    val migrationFiles = fileTree("monolith/src/main/resources/db/migration") {
        include("V*__*.sql")
    }
    inputs.files(migrationFiles)

    doLast {
        val versionPattern = Regex("""^V(.+)__.+\.sql$""")
        val duplicatedVersions = migrationFiles.files
            .groupBy { migrationFile ->
                versionPattern.matchEntire(migrationFile.name)?.groupValues?.get(1)
                    ?: throw GradleException("Invalid Flyway migration filename: ${migrationFile.name}")
            }
            .filterValues { files -> files.size > 1 }

        if (duplicatedVersions.isNotEmpty()) {
            val details = duplicatedVersions.entries
                .sortedBy { it.key }
                .joinToString(System.lineSeparator()) { (version, files) ->
                    val paths = files
                        .sortedBy { it.name }
                        .joinToString(", ") { it.relativeTo(rootDir).path }
                    "  - $version: $paths"
                }

            throw GradleException("Duplicate Flyway migration versions found:${System.lineSeparator()}$details")
        }
    }
}

// :check 와 :build 를 직접 실행해도 모든 하위 모듈을 검증하고 빌드한다.
// 경로 없는 check 는 Gradle 이 알아서 하위까지 고르지만, 루트 경로를 붙이면 루트 것만 돈다.
tasks.named("check") {
    dependsOn(subprojects.map { "${it.path}:check" })
}

tasks.named("build") {
    dependsOn(subprojects.map { "${it.path}:build" })
}

// 루트는 저장소 공통 자산을, 위 subprojects 블록은 각 모듈의 Java 소스를 포맷 검사한다.
spotless {
    format("misc") {
        target(
            "*.gradle.kts",
            "*/build.gradle.kts",
            "gradle/**/*.gradle.kts",
            "gradle/**/*.toml",
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

apply(from = "gradle/documentation-catalog.gradle.kts")
