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

// main 과 테스트의 생성 소스가 섞이지 않도록 출력 경로를 compileJava 에만 지정한다.
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
