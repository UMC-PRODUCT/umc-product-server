import org.gradle.api.tasks.Delete
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.compile.JavaCompile

val querydslDir = layout.buildDirectory.dir("generated/querydsl")

extensions.configure<SourceSetContainer>("sourceSets") {
    named("main") {
        java.srcDir(querydslDir)
        resources.exclude(".env.*", "**/.env.*")
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.compilerArgs.add("-Xlint:deprecation")
}

// 생성 위치는 main 컴파일에만 건다. 이 디렉터리가 main 소스 세트의 srcDir 이라,
// test/testFixtures 컴파일까지 여기에 쏟으면 다음 빌드에서 중복 클래스가 된다.
tasks.named<JavaCompile>("compileJava") {
    options.generatedSourceOutputDirectory.set(querydslDir)
}

tasks.named<Delete>("clean") {
    doFirst {
        println("=".repeat(50))
        println("[clean] gradle clean을 시작합니다.")
        println("=".repeat(50))
    }

    doLast {
        querydslDir.get().asFile.deleteRecursively()
        println("[clean] QueryDSL 생성 디렉토리를 삭제하였습니다.")
        println("[clean] gradle clean이 완료되었습니다.")
    }
}
