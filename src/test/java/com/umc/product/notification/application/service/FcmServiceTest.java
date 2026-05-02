package com.umc.product.notification.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.umc.product.notification.adapter.in.web.dto.request.FcmRegistrationRequest;
import com.umc.product.notification.application.port.in.ManageFcmUseCase;
import com.umc.product.notification.application.port.out.LoadFcmPort;
import com.umc.product.notification.domain.FcmToken;
import com.umc.product.support.UseCaseTestSupport;
import com.umc.product.support.fixture.FcmTokenFixture;
import com.umc.product.support.fixture.MemberFixture;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

@Transactional
class FcmServiceTest extends UseCaseTestSupport {

    @Autowired
    private ManageFcmUseCase manageFcmUseCase;

    @Autowired
    private LoadFcmPort loadFcmPort;

    @Autowired
    private MemberFixture memberFixture;

    @Autowired
    private FcmTokenFixture fcmTokenFixture;

    @Test
    void 신규_토큰_등록_시_FCM_토큰이_활성_상태로_저장된다() {
        // given
        Long memberId = memberFixture.normalMember("테스터").getId();
        FcmRegistrationRequest request = new FcmRegistrationRequest("new-token");

        // when
        manageFcmUseCase.registerFcmToken(memberId, request);

        // then
        List<FcmToken> tokens = loadFcmPort.findAllActiveByMemberId(memberId);
        assertThat(tokens).hasSize(1);
        assertThat(tokens.get(0).getFcmToken()).isEqualTo("new-token");
        assertThat(tokens.get(0).isActive()).isTrue();
    }

    @Test
    void 동일_토큰_재등록_시_INSERT_없이_활성화만_된다() {
        // given
        Long memberId = memberFixture.normalMember("테스터").getId();
        FcmToken existing = fcmTokenFixture.FCM_토큰(memberId, "existing-token");
        existing.deactivate();

        // when
        manageFcmUseCase.registerFcmToken(memberId, new FcmRegistrationRequest("existing-token"));

        // then - 새 레코드 추가 없이 기존 토큰이 활성화됨
        List<FcmToken> tokens = loadFcmPort.findAllActiveByMemberId(memberId);
        assertThat(tokens).hasSize(1);
        assertThat(tokens.get(0).getFcmToken()).isEqualTo("existing-token");
        assertThat(tokens.get(0).isActive()).isTrue();
    }

    @Test
    void 새_기기_토큰_등록_시_기존_토큰과_함께_저장된다() {
        // given
        Long memberId = memberFixture.normalMember("테스터").getId();
        fcmTokenFixture.FCM_토큰(memberId, "old-token");

        // when
        manageFcmUseCase.registerFcmToken(memberId, new FcmRegistrationRequest("new-device-token"));

        // then - 기존 토큰 유지 + 새 토큰 추가
        List<FcmToken> tokens = loadFcmPort.findAllActiveByMemberId(memberId);
        assertThat(tokens).hasSize(2);
        assertThat(tokens).extracting(FcmToken::getFcmToken)
            .containsExactlyInAnyOrder("old-token", "new-device-token");
    }
}
