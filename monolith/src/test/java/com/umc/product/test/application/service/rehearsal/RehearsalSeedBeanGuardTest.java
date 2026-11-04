package com.umc.product.test.application.service.rehearsal;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;

class RehearsalSeedBeanGuardTest {

    @ParameterizedTest(name = "{0} 는 시딩 가드 어노테이션을 모두 가진다")
    @ValueSource(classes = {
        RehearsalContextLoader.class,
        StartingLineSeedService.class,
        RoundSummaryQueryService.class,
        MatchingBotService.class,
        BotApplicationWriter.class
    })
    @DisplayName("매칭 리허설 빈은 @Profile(!prod) + @ConditionalOnProperty(app.seed.enabled=true) 가드를 가진다")
    void rehearsalBeansHaveSeedGuards(Class<?> beanClass) {
        assertThat(beanClass.getAnnotation(Profile.class).value()).contains("!prod");
        ConditionalOnProperty property = beanClass.getAnnotation(ConditionalOnProperty.class);
        assertThat(property.prefix()).isEqualTo("app.seed");
        assertThat(property.name()).contains("enabled");
        assertThat(property.havingValue()).isEqualTo("true");
    }
}
