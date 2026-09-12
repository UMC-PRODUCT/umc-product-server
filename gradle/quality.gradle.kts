import java.io.File
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.plugins.quality.Checkstyle
import org.gradle.api.plugins.quality.CheckstyleExtension
import org.gradle.api.tasks.SourceSetContainer

val libsCatalog = extensions.getByType<VersionCatalogsExtension>().named("libs")
val checkstyleVersion = libsCatalog.findVersion("checkstyle").get().requiredVersion

extensions.configure<CheckstyleExtension>("checkstyle") {
    toolVersion = checkstyleVersion
    configDirectory.set(layout.projectDirectory.dir("config/checkstyle"))
    configProperties["suppressionFile"] =
        layout.projectDirectory.file("config/checkstyle/naver-checkstyle-suppressions.xml").asFile.absolutePath
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

fun sourceSetAllJava(sourceSetName: String) =
    extensions.getByType<SourceSetContainer>().named(sourceSetName).get().allJava

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
