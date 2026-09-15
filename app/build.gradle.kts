import org.springframework.boot.gradle.tasks.bundling.BootJar

// 배포 산출물을 조립하는 모듈이다. 실행 클래스와 resources 는 :monolith 에 있다.
// main 에는 package-info.java 만 두는데, 이유는 그 파일에 적어뒀다.
dependencies {
    implementation(project(":monolith"))
    implementation(project(":blog"))

    // 조립 결과를 검증하는 테스트만 둔다. 모듈별 테스트는 자기 모듈 것만 보므로
    // 배포 대상에서 모듈이 빠진 것은 여기서만 드러난다.
    testImplementation(testFixtures(project(":monolith")))
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
