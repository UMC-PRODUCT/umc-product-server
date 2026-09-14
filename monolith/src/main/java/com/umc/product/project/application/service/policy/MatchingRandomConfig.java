package com.umc.product.project.application.service.policy;

import java.util.Random;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 자동 선발 알고리즘에 주입되는 {@link Random} 인스턴스 빈 등록.
 * <p>
 * 테스트에서는 시드를 고정한 {@code Random(seed)} 를 직접 주입하여 결정성을 확보한다.
 */
@Configuration
public class MatchingRandomConfig {

    @Bean
    public Random matchingRandom() {
        return new Random();
    }
}
