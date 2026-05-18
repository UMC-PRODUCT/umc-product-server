package com.umc.product.support;

import static org.assertj.core.api.Assertions.assertThat;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CommonFixtureTest {

    @Test
    @DisplayName("FailoverIntrospector가 3가지 방식의 객체를 모두 정상적으로 생성한다")
    void generateAllTypesOfObjects() {
        // 1. Builder 방식 테스트
        BuilderObject builderObj = CommonFixture.MONKEY.giveMeOne(BuilderObject.class);
        assertThat(builderObj).isNotNull();
        assertThat(builderObj.getName()).isNotBlank(); // defaultNotNull(true) 덕분에 null이 아님

        System.out.println("Builder 생성 결과: name=" + builderObj.getName() + ", age=" + builderObj.getAge());

        // 2. Bean(Setter) 방식 테스트
        BeanObject beanObj = CommonFixture.MONKEY.giveMeOne(BeanObject.class);
        assertThat(beanObj).isNotNull();
        assertThat(beanObj.getTitle()).isNotBlank();

        System.out.println("Bean 생성 결과: title=" + beanObj.getTitle() + ", id=" + beanObj.getId());
    }

    // 테스트용 1: Builder로만 생성 가능한 객체
    @Getter
    @Builder
    public static class BuilderObject {
        private String name;
        private int age;
    }

    // 테스트용 2: 기본생성자 + Setter로만 생성 가능한 객체 (Bean)
    @Getter
    @Setter
    @NoArgsConstructor
    public static class BeanObject {
        private String title;
        private Long id;
    }
}
