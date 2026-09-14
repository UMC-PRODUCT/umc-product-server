import java.io.File
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.plugins.quality.Checkstyle
import org.gradle.api.plugins.quality.CheckstyleExtension
import org.gradle.api.tasks.SourceSetContainer

// subprojects 블록에서 적용되는 시점에는 서브프로젝트의 카탈로그가 아직 없다. 루트를 거친다.
val libsCatalog = rootProject.extensions.getByType<VersionCatalogsExtension>().named("libs")
val checkstyleVersion = libsCatalog.findVersion("checkstyle").get().requiredVersion

extensions.configure<CheckstyleExtension>("checkstyle") {
    toolVersion = checkstyleVersion
    configDirectory.set(rootProject.layout.projectDirectory.dir("config/checkstyle"))
    configProperties["suppressionFile"] =
        rootProject.layout.projectDirectory
            .file("config/checkstyle/naver-checkstyle-suppressions.xml").asFile.absolutePath
    isIgnoreFailures = false
    maxErrors = 0
    maxWarnings = Int.MAX_VALUE
}

val lintBaseRef = providers.gradleProperty("lintBase").orElse("origin/develop")

// -PlintAll 은 변경 파일 필터를 끄고 소스 세트 전체를 검사한다.
// lintBase 로는 전체 검사를 대신할 수 없다. 그쪽은 어디까지나 diff 기준점만 바꾼다.
val lintAllEnabled = providers.gradleProperty("lintAll")
    .map { it.isBlank() || it.toBoolean() }
    .orElse(false)

// QueryDSL 생성 클래스는 생성 디렉터리가 소스 세트에 등록돼 있어 전체 검사에 딸려온다. 경로로 걸러낸다.
// 이름(Q*.java)으로 거르면 Question.java, QueryDslConfig.java 같은 실제 소스까지 조용히 빠진다.
val generatedSourceRoot = layout.buildDirectory.dir("generated").get().asFile

fun sourceSetAllJava(sourceSetName: String) =
    extensions.getByType<SourceSetContainer>().named(sourceSetName).get().allJava
        .filter { !it.startsWith(generatedSourceRoot) }

// git 은 저장소 루트 기준 경로를 출력하고, pathspec 도 실행 위치 기준으로 해석한다.
// 서브프로젝트에서 그냥 돌리면 경로가 어긋나 선택된 파일이 0건이 되고, 검사가 조용히 통과한다.
// workingDir 를 루트로 고정하고 :(top) 으로 pathspec 도 루트 기준으로 못박는다.
// 구분자는 반드시 invariantSeparatorsPath 여야 한다. Windows 의 역슬래시는 pathspec 에서 안 먹는다.
fun changedJavaFiles(vararg sourceRoots: String) = lintBaseRef.flatMap { baseRef ->
    val modulePrefix = projectDir.relativeTo(rootDir).invariantSeparatorsPath
    val scopedPathspecs = sourceRoots.map { sourceRoot ->
        if (modulePrefix.isEmpty()) ":(top)$sourceRoot" else ":(top)$modulePrefix/$sourceRoot"
    }

    val diffAgainstBaseProvider = providers.exec {
        workingDir = rootProject.projectDir
        commandLine(
            listOf("git", "diff", "--name-only", "--diff-filter=ACMR", "$baseRef...HEAD", "--") + scopedPathspecs
        )
        isIgnoreExitValue = true
    }.standardOutput.asText

    val localDiffProvider = providers.exec {
        workingDir = rootProject.projectDir
        commandLine(
            listOf("git", "diff", "--name-only", "--diff-filter=ACMR", "HEAD", "--") + scopedPathspecs
        )
        isIgnoreExitValue = true
    }.standardOutput.asText

    diffAgainstBaseProvider.zip(localDiffProvider) { baseOutput, localOutput ->
        (baseOutput.lines() + localOutput.lines())
            .asSequence()
            .map(String::trim)
            .filter { it.endsWith(".java") && it.isNotBlank() }
            .distinct()
            .map { rootProject.file(it) }
            .filter { it.exists() && it.startsWith(projectDir) }
            .toList()
    }
}

tasks.withType<Checkstyle>().configureEach {
    classpath = files()

    reports {
        xml.required.set(true)
        html.required.set(true)
    }
}

tasks.named<Checkstyle>("checkstyleMain") {
    if (lintAllEnabled.get()) {
        setSource(sourceSetAllJava("main"))
    } else {
        setSource(files(changedJavaFiles("src/main/java")))
    }
}

tasks.named<Checkstyle>("checkstyleTest") {
    if (lintAllEnabled.get()) {
        setSource(sourceSetAllJava("test"))
    } else {
        setSource(files(changedJavaFiles("src/test/java")))
    }
}

tasks.register("spotlessTest") {
    group = "verification"
    description = "Runs Spotless checks for Java test sources before executing tests."
    dependsOn(tasks.named("spotlessJavaCheck"))
}
