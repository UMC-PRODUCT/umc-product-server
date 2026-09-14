/**
 * 도메인별 영속화 Fixture 패키지.
 *
 * <h2>설계 원칙</h2>
 * <ul>
 *   <li><b>유효 기본값(valid defaults).</b> 도메인 규칙(필수값, 상태 전이)을 깨지 않는 값으로 생성한다.</li>
 *   <li><b>최소 인자.</b> 변동 가능한 식별자(예: 회원 ID, 기수 ID, 이름)만 호출 측에서 받고, 그 외는 기본값으로 채운다.</li>
 *   <li><b>의도 표현 메서드.</b> {@code 활성_기수 / 비활성_기수}, {@code 필수_약관 / 선택_약관} 처럼 시나리오 의도를 메서드 이름으로 드러낸다.</li>
 *   <li><b>ID 참조.</b> 다른 도메인 엔티티는 직접 보유하지 않고 ID 로만 참조한다(프로젝트 아키텍처 규칙).</li>
 *   <li><b>고정 시간.</b> {@code Instant.now()} 대신 고정 상수를 사용해 시간 의존성을 제거한다.</li>
 *   <li><b>영속화는 SavePort 로.</b> JPA Repository 를 직접 사용하지 않고 도메인의 OutPort 를 통해 저장한다.</li>
 * </ul>
 *
 * <h2>FixtureMonkey 사용 요약</h2>
 * <ul>
 *   <li><b>뼈대 잡기.</b> {@code giveMeOne} 또는 {@code giveMeBuilder} 로 시작한다.</li>
 *   <li><b>조작하기.</b> {@code set / setNull / setNotNull / size / minSize / maxSize / inner} 로 의도를 고정한다.</li>
 *   <li><b>결과 뽑기.</b> {@code sample / sampleList / sampleStream} 으로 객체를 생성한다.</li>
 *   <li><b>상세 가이드.</b> {@code docs/guides/monkey.md} 참고.</li>
 * </ul>
 *
 * <h2>금지 사항</h2>
 * <ul>
 *   <li>무작위 값({@code Faker}, {@code Random}) 사용으로 테스트의 결정성을 해치지 말 것.</li>
 *   <li>한 메서드에 두 개 이상의 도메인을 영속화하지 말 것. 시나리오 조합은 테스트 코드에서 한다.</li>
 *   <li>호출 측이 외부에서 ID 를 주입한 경우, 그 ID 가 실재하는 엔티티인지 검증하지 말 것
 *       (영속 책임 분리). 검증은 UseCase / 도메인 규칙의 몫이다.</li>
 * </ul>
 *
 * <h2>새로운 Fixture 추가 시</h2>
 * <ol>
 *   <li>도메인의 {@code Save{Domain}Port} 를 생성자 주입한다.</li>
 *   <li>{@code @Component} 를 부여해 Spring 컨텍스트에 등록한다.</li>
 *   <li>도메인의 정적 팩토리({@code create / of / from}) 또는 {@code Builder} 를 사용해 인스턴스를 만든다.
 *       엔티티 외부에서 {@code new} 를 직접 호출하지 않는다.</li>
 *   <li>의도가 다른 시나리오는 별도 메서드로 분리한다(빌더 chain 을 호출 측에 노출하지 않는다).</li>
 * </ol>
 */
package com.umc.product.support.fixture;
