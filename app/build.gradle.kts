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

// 배포 산출물은 app.jar 하나만 만들도록 일반 JAR 생성을 끈다.
tasks.named<Jar>("jar") { enabled = false }

tasks.named<BootJar>("bootJar") {
    archiveFileName.set("app.jar")
    exclude(".env", ".env.*", "**/.env", "**/.env.*")

    layered {
        enabled.set(true)
    }
}
