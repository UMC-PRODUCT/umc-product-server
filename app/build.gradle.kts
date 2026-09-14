import org.springframework.boot.gradle.tasks.bundling.BootJar

// 소스가 없는 패키징 전용 모듈이다. 실행 클래스와 resources 는 :monolith 에 있다.
dependencies {
    implementation(project(":monolith"))
    implementation(project(":blog"))
}

springBoot {
    mainClass.set("com.umc.product.UmcProductApplication")
    buildInfo()
}

// bootJar 와 함께 활성화하면 Spring Boot 가 -plain.jar 를 같이 만든다.
// Dockerfile 의 COPY 가 두 파일을 잡아 실패하므로 끄고 파일명을 고정한다.
tasks.named<Jar>("jar") { enabled = false }

tasks.named<BootJar>("bootJar") {
    archiveFileName.set("app.jar")
    exclude(".env", ".env.*", "**/.env", "**/.env.*")

    layered {
        enabled.set(true)
    }
}
