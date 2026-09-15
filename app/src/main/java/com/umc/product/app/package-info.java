/**
 * 배포 산출물을 조립하는 모듈.
 *
 * <p>실행 클래스와 {@code src/main/resources} 는 {@code :monolith} 에 있다. 통합 테스트가
 * {@code @SpringBootTest} 를 {@code classes} 없이 쓰기 때문이다. 실행 클래스를 여기로 옮기면
 * {@code :monolith} 테스트가 설정 클래스를 찾지 못한다. 자세한 내용은
 * {@code docs/adr/030-blog-gradle-module-extraction.md} 참고.
 *
 * <p>그래서 이 패키지에는 코드가 없다. 다만 소스 디렉터리 자체는 있어야 한다. IntelliJ 가 소스 없는
 * Gradle 프로젝트에는 모듈을 만들지 않아, {@code .run/*.xml} 이 참조할 대상이 사라진다. 그러면
 * IDE 실행 구성이 {@code :monolith} 를 가리키게 되고, blog 빈이 빠진 채로 서버가 뜬다.
 *
 * <p>배포 대상 모듈은 {@code app/build.gradle.kts} 의 의존으로 선언하고,
 * {@code ApplicationAssemblyTest} 가 실제 조립 결과를 확인한다.
 */
package com.umc.product.app;
