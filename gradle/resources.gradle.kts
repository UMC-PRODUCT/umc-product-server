import org.gradle.language.jvm.tasks.ProcessResources

// 로컬 개발용 dotenv 는 배포 산출물에 넣지 않는다.
// :app 의 bootJar exclude 만으로는 부족하다. BOOT-INF/lib 안의 모듈 jar 는 바깥 패턴이 닿지 않아
// 각 모듈이 스스로 걸러야 한다.
tasks.named<ProcessResources>("processResources") {
    exclude(".env", ".env.*", "**/.env", "**/.env.*")
}
