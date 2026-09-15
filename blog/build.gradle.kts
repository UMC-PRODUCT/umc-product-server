// blog 도메인. :monolith 가 blog 를 컴파일 타임에 못 보게 하는 것이 이 모듈의 존재 이유다.
//
// 반대 방향은 아직 좁히지 못했다. 좁은 계약 모듈을 만들려면 MemberInfo.from(Member) 처럼
// 계약 DTO 가 엔티티를 물고 있는 코드부터 정리해야 한다. 대신 BlogModuleBoundaryTest 로
// blog 가 쓰는 외부 타입을 고정한다.
dependencies {
    // monolith 를 blog 의 컴파일 API 로 노출하지 않는다. 런타임에는 전이 의존으로 포함된다.
    implementation(project(":monolith"))
    testImplementation(testFixtures(project(":monolith")))
}
