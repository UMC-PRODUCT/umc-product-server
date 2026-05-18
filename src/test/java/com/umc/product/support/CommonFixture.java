package com.umc.product.support;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.BeanArbitraryIntrospector;
import com.navercorp.fixturemonkey.api.introspector.BuilderArbitraryIntrospector;
import com.navercorp.fixturemonkey.api.introspector.FailoverIntrospector;
import com.navercorp.fixturemonkey.api.introspector.FieldReflectionArbitraryIntrospector;
import com.navercorp.fixturemonkey.api.jqwik.JavaTypeArbitraryGenerator;
import com.navercorp.fixturemonkey.api.jqwik.JqwikPlugin;
import com.navercorp.fixturemonkey.jakarta.validation.plugin.JakartaValidationPlugin;
import java.util.Arrays;
import net.jqwik.api.Arbitraries;
import net.jqwik.api.arbitraries.StringArbitrary;

public class CommonFixture {

    private static final CharSequence KOR_ENG_CHARS;
    /**
     * 테스트 코드를 작성하는 메서드에서 호출하여 사용할 수 있도록 static Field로 구성
     * <p>
     * <code>CommonFixture.fixtureMonkey</code> 와 같이 공통 설정에 대해서 사용하도록 합니다.
     */
    public static final FixtureMonkey MONKEY = FixtureMonkey.builder()
        // Validation을 읽어서 동작하도록 반영
        .plugin(new JakartaValidationPlugin())
        // 생성되는 문자를 한글 또는 영어로 한정 (나중에 빡세게 테스트할 때는 제거할 것)
        .plugin(new JqwikPlugin()
            .javaTypeArbitraryGenerator(new JavaTypeArbitraryGenerator() {
                @Override
                public StringArbitrary strings() {
                    // 💡 2. 기본 생성 문자열을 방금 만든 풀(KOR_ENG_CHARS) 안에서만 뽑도록 제한!
                    return Arbitraries.strings()
                        .withChars(KOR_ENG_CHARS);
                    // .ofMinLength(1)
                    // .ofMaxLength(10); // 길이 조절 시 사용할 것
                }
            })
        )
        .objectIntrospector(new FailoverIntrospector(
            Arrays.asList(
                /*
                FixtureMonkey는 3가지 방법을 지원함.
                1. Builder 생성
                    Builder 패턴으로 객체를 생성하는 방식
                2. Getter/Setter가 있는 Bean 객체
                    NoArgsConstructor로 빈 객체를 하나 만들고, Setter Method로 하나씩 값 넣는 방식
                3. 필드만 있는 객체
                    Reflection 방식을 사용함
                 */
                BuilderArbitraryIntrospector.INSTANCE,
                BeanArbitraryIntrospector.INSTANCE,
                FieldReflectionArbitraryIntrospector.INSTANCE
            )
        ))

        // FixtureMonkey의 대입값에 null 허용하지 않음.
        .defaultNotNull(true)
        .build();

    static {
        StringBuilder sb = new StringBuilder();
        // 영어 소문자 추가
        for (char c = 'a'; c <= 'z'; c++) {
            sb.append(c);
        }
        // 영어 대문자 추가
        for (char c = 'A'; c <= 'Z'; c++) {
            sb.append(c);
        }
        // 완성형 한글(가~힣) 11,172자 추가
        for (char c = '가'; c <= '힣'; c++) {
            sb.append(c);
        }

        KOR_ENG_CHARS = sb.toString();
    }
}
