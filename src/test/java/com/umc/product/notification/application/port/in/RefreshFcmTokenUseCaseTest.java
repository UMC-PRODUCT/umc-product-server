package com.umc.product.notification.application.port.in;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import com.umc.product.member.application.port.out.SaveMemberPort;
import com.umc.product.member.domain.Member;
import com.umc.product.notification.adapter.in.web.dto.request.FcmRegistrationRequest;
import com.umc.product.notification.application.port.out.LoadFcmPort;
import com.umc.product.notification.application.port.out.SaveFcmPort;
import com.umc.product.notification.domain.FcmToken;
import com.umc.product.support.UseCaseTestSupport;
import java.util.Collections;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class RefreshFcmTokenUseCaseTest extends UseCaseTestSupport {

    @Autowired
    private RefreshFcmTokenUseCase refreshFcmTokenUseCase;

    @Autowired
    private SaveMemberPort saveMemberPort;

    @Autowired
    private SaveFcmPort saveFcmPort;

    @Autowired
    private LoadFcmPort loadFcmPort;

    @Nested
    class refreshTokenAndSubscriptions {

        @Test
        void 토큰이_없는_회원이_처음_등록하면_새_토큰이_저장된다() {
            // given
            Member member = saveMemberPort.save(Member.builder()
                .name("테스트")
                .nickname("test")
                .email("test@umc.com")
                .build());

            given(getChallengerUseCase.getMemberChallengerList(member.getId()))
                .willReturn(Collections.emptyList());

            // when
            refreshFcmTokenUseCase.refreshTokenAndSubscriptions(
                member.getId(), new FcmRegistrationRequest("new-fcm-token"));

            // then
            FcmToken saved = loadFcmPort.findByMemberId(member.getId());
            assertThat(saved).isNotNull();
            assertThat(saved.getFcmToken()).isEqualTo("new-fcm-token");
        }

        @Test
        void 기존_토큰이_있으면_새_토큰으로_업데이트된다() {
            // given
            Member member = saveMemberPort.save(Member.builder()
                .name("테스트")
                .nickname("test")
                .email("test2@umc.com")
                .build());
            saveFcmPort.save(FcmToken.createFCMToken(member, "old-fcm-token"));

            given(getChallengerUseCase.getMemberChallengerList(member.getId()))
                .willReturn(Collections.emptyList());

            // when
            refreshFcmTokenUseCase.refreshTokenAndSubscriptions(
                member.getId(), new FcmRegistrationRequest("updated-fcm-token"));

            // then
            FcmToken updated = loadFcmPort.findByMemberId(member.getId());
            assertThat(updated.getFcmToken()).isEqualTo("updated-fcm-token");
        }
    }
}
