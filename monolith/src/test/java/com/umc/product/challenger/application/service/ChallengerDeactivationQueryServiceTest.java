package com.umc.product.challenger.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.umc.product.challenger.application.port.out.LoadChallengerDeactivationPort;

@ExtendWith(MockitoExtension.class)
@DisplayName("ChallengerDeactivationQueryService")
class ChallengerDeactivationQueryServiceTest {

    @Mock
    LoadChallengerDeactivationPort loadChallengerDeactivationPort;

    @InjectMocks
    ChallengerDeactivationQueryService sut;

    @Test
    @DisplayName("탈부 근거에 연결된 이미지인지 확인한다")
    void checkImageInUse() {
        given(loadChallengerDeactivationPort.existsByImageId("image-id")).willReturn(true);

        assertThat(sut.isImageInUse("image-id")).isTrue();
    }

    @Test
    @DisplayName("이미지 ID가 없으면 저장소를 조회하지 않는다")
    void doNotQueryNullImageId() {
        assertThat(sut.isImageInUse(null)).isFalse();

        then(loadChallengerDeactivationPort).shouldHaveNoInteractions();
    }
}
