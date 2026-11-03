package com.umc.product.member.adapter.out.external.authentication;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.umc.product.authentication.application.port.in.query.GetMemberOAuthUseCase;
import com.umc.product.authentication.application.port.in.query.dto.MemberOAuthInfo;
import com.umc.product.common.domain.enums.OAuthProvider;

@ExtendWith(MockitoExtension.class)
@DisplayName("MemberOAuthProviderAdapter")
class MemberOAuthProviderAdapterTest {

    private static final Long MEMBER_ID = 1L;

    @Mock
    GetMemberOAuthUseCase getMemberOAuthUseCase;

    @InjectMocks
    MemberOAuthProviderAdapter sut;

    @Test
    @DisplayName("연동된 소셜 로그인 수단을 중복 없이 enum 순서로 반환한다")
    void 연동된_소셜_로그인_수단을_중복_없이_enum_순서로_반환한다() {
        // given
        given(getMemberOAuthUseCase.getOAuthList(MEMBER_ID)).willReturn(List.of(
            new MemberOAuthInfo(3L, MEMBER_ID, OAuthProvider.KAKAO),
            new MemberOAuthInfo(1L, MEMBER_ID, OAuthProvider.GOOGLE),
            new MemberOAuthInfo(2L, MEMBER_ID, OAuthProvider.KAKAO)
        ));

        // when
        List<OAuthProvider> providers = sut.listByMemberId(MEMBER_ID);

        // then
        assertThat(providers).containsExactly(OAuthProvider.GOOGLE, OAuthProvider.KAKAO);
    }
}
