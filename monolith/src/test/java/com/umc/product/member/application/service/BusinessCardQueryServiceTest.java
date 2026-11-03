package com.umc.product.member.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.common.domain.enums.MemberStatus;
import com.umc.product.common.domain.enums.OAuthProvider;
import com.umc.product.member.application.port.in.query.GetMemberUseCase;
import com.umc.product.member.application.port.in.query.dto.BusinessCardEditInfo;
import com.umc.product.member.application.port.in.query.dto.BusinessCardInfo;
import com.umc.product.member.application.port.in.query.dto.BusinessCardInfo.Contact;
import com.umc.product.member.application.port.in.query.dto.BusinessCardInfo.Link;
import com.umc.product.member.application.port.in.query.dto.MemberInfo;
import com.umc.product.member.application.port.out.LoadBusinessCardActivityPort;
import com.umc.product.member.application.port.out.LoadMemberOAuthProviderPort;
import com.umc.product.member.application.port.out.LoadMemberPort;
import com.umc.product.member.application.port.out.dto.BusinessCardActivity;
import com.umc.product.member.domain.LinkTypeAndLink;
import com.umc.product.member.domain.Member;
import com.umc.product.member.domain.MemberProfile;
import com.umc.product.member.domain.MemberProfileLinkType;

@ExtendWith(MockitoExtension.class)
@DisplayName("BusinessCardQueryService")
class BusinessCardQueryServiceTest {

    private static final Long MEMBER_ID = 1L;

    @Mock
    GetMemberUseCase getMemberUseCase;

    @Mock
    LoadMemberPort loadMemberPort;

    @Mock
    LoadBusinessCardActivityPort loadBusinessCardActivityPort;

    @Mock
    LoadMemberOAuthProviderPort loadMemberOAuthProviderPort;

    @InjectMocks
    BusinessCardQueryService sut;

    @Test
    @DisplayName("명함에는 기본 정보와 공개로 설정한 연락처·열 수 있는 링크만 담는다")
    void 명함에는_기본_정보와_공개로_설정한_연락처_열_수_있는_링크만_담는다() {
        // given
        MemberProfile profile = MemberProfile.fromLinks(List.of(
            new LinkTypeAndLink(MemberProfileLinkType.GITHUB, "https://github.com/umc"),
            new LinkTypeAndLink(MemberProfileLinkType.BLOG, "https://blog.example.com"),
            new LinkTypeAndLink(MemberProfileLinkType.PERSONAL, "umc.example.com")
        ));
        profile.updateContactEmail("contact@example.com");
        profile.changeVisibility(null, Map.of(MemberProfileLinkType.BLOG, false));
        givenMember(profile);
        given(loadBusinessCardActivityPort.listByMemberId(MEMBER_ID)).willReturn(List.of());

        // when
        BusinessCardInfo card = sut.getByMemberId(MEMBER_ID);

        // then
        assertThat(card.memberId()).isEqualTo(MEMBER_ID);
        assertThat(card.name()).isEqualTo("홍길동");
        assertThat(card.nickname()).isEqualTo("길동");
        assertThat(card.schoolName()).isEqualTo("UMC대학교");
        assertThat(card.profileImageLink()).isEqualTo("https://cdn.example.com/profile.png");
        assertThat(card.contact()).isEqualTo(new Contact("contact@example.com", true));
        assertThat(card.links()).containsExactly(new Link(MemberProfileLinkType.GITHUB, "https://github.com/umc", true));
    }

    @Test
    @DisplayName("명함에서 연락처를 비공개로 설정했거나 입력하지 않았으면 연락처를 담지 않는다")
    void 명함에서_연락처를_비공개로_설정했거나_입력하지_않았으면_연락처를_담지_않는다() {
        // given
        MemberProfile profile = MemberProfile.fromLinks(List.of());
        profile.updateContactEmail("contact@example.com");
        profile.changeVisibility(false, Map.of());
        givenMember(profile);
        given(loadBusinessCardActivityPort.listByMemberId(MEMBER_ID)).willReturn(List.of());

        // when & then
        assertThat(sut.getByMemberId(MEMBER_ID).contact()).isNull();
    }

    @Test
    @DisplayName("프로필이 없으면 명함의 연락처를 로그인 이메일로 채우지 않는다")
    void 프로필이_없으면_명함의_연락처를_로그인_이메일로_채우지_않는다() {
        // given
        givenMember(null);
        given(loadBusinessCardActivityPort.listByMemberId(MEMBER_ID)).willReturn(List.of());

        // when
        BusinessCardInfo card = sut.getByMemberId(MEMBER_ID);

        // then
        assertThat(card.contact()).isNull();
        assertThat(card.links()).isEmpty();
    }

    @Test
    @DisplayName("활동 이력 중 가장 최신 기수의 기수와 파트를 담는다")
    void 활동_이력_중_가장_최신_기수의_기수와_파트를_담는다() {
        // given
        givenMember(null);
        given(loadBusinessCardActivityPort.listByMemberId(MEMBER_ID)).willReturn(List.of(
            new BusinessCardActivity(7L, 7L, ChallengerPart.WEB),
            new BusinessCardActivity(99L, null, null),
            new BusinessCardActivity(8L, 8L, ChallengerPart.SPRINGBOOT)
        ));

        // when
        BusinessCardInfo card = sut.getByMemberId(MEMBER_ID);

        // then
        // 현재 활동 중이 아닌 지난 기수여도 가장 최신이면 표시하고, 기수를 알 수 없는 기록은 뒤로 미룬다.
        assertThat(card.activity()).isEqualTo(new BusinessCardInfo.Activity(8L, 8L, ChallengerPart.SPRINGBOOT));
    }

    @Test
    @DisplayName("활동 이력이 없으면 활동 이력을 비워 둔다")
    void 활동_이력이_없으면_활동_이력을_비워_둔다() {
        // given
        givenMember(null);
        given(loadBusinessCardActivityPort.listByMemberId(MEMBER_ID)).willReturn(List.of());

        // when & then
        assertThat(sut.getByMemberId(MEMBER_ID).activity()).isNull();
    }

    @Test
    @DisplayName("수정 화면은 비공개 항목과 입력하지 않은 링크까지 5개 타입을 모두 공개 여부와 함께 담는다")
    void 수정_화면은_비공개_항목과_입력하지_않은_링크까지_5개_타입을_모두_공개_여부와_함께_담는다() {
        // given
        MemberProfile profile = MemberProfile.fromLinks(List.of(
            new LinkTypeAndLink(MemberProfileLinkType.GITHUB, "https://github.com/umc"),
            new LinkTypeAndLink(MemberProfileLinkType.BLOG, "")
        ));
        profile.updateContactEmail("contact@example.com");
        profile.changeVisibility(false, Map.of(
            MemberProfileLinkType.GITHUB, false,
            MemberProfileLinkType.INSTAGRAM, false
        ));
        givenMember(profile);
        given(loadBusinessCardActivityPort.listByMemberId(MEMBER_ID)).willReturn(List.of(
            new BusinessCardActivity(9L, 9L, ChallengerPart.SPRINGBOOT)
        ));
        given(loadMemberOAuthProviderPort.listByMemberId(MEMBER_ID))
            .willReturn(List.of(OAuthProvider.GOOGLE, OAuthProvider.KAKAO));

        // when
        BusinessCardEditInfo edit = sut.getEditByMemberId(MEMBER_ID);

        // then
        assertThat(edit.name()).isEqualTo("홍길동");
        assertThat(edit.schoolName()).isEqualTo("UMC대학교");
        assertThat(edit.profileImageLink()).isEqualTo("https://cdn.example.com/profile.png");
        assertThat(edit.activity()).isEqualTo(new BusinessCardInfo.Activity(9L, 9L, ChallengerPart.SPRINGBOOT));
        assertThat(edit.oauthProviders()).containsExactly(OAuthProvider.GOOGLE, OAuthProvider.KAKAO);
        assertThat(edit.contact()).isEqualTo(new Contact("contact@example.com", false));
        assertThat(edit.links()).containsExactly(
            new Link(MemberProfileLinkType.LINKEDIN, null, true),
            new Link(MemberProfileLinkType.INSTAGRAM, null, false),
            new Link(MemberProfileLinkType.GITHUB, "https://github.com/umc", false),
            new Link(MemberProfileLinkType.BLOG, null, true),
            new Link(MemberProfileLinkType.PERSONAL, null, true)
        );
    }

    @Test
    @DisplayName("프로필이 없으면 수정 화면의 연락처는 비어 있는 공개, 링크는 5개 타입 모두 비어 있는 공개다")
    void 프로필이_없으면_수정_화면의_연락처는_비어_있는_공개_링크는_5개_타입_모두_비어_있는_공개다() {
        // given
        givenMember(null);
        given(loadBusinessCardActivityPort.listByMemberId(MEMBER_ID)).willReturn(List.of());
        given(loadMemberOAuthProviderPort.listByMemberId(MEMBER_ID)).willReturn(List.of());

        // when
        BusinessCardEditInfo edit = sut.getEditByMemberId(MEMBER_ID);

        // then
        assertThat(edit.activity()).isNull();
        assertThat(edit.contact()).isEqualTo(new Contact(null, true));
        assertThat(edit.links())
            .hasSize(MemberProfileLinkType.values().length)
            .allSatisfy(link -> {
                assertThat(link.link()).isNull();
                assertThat(link.isPublic()).isTrue();
            });
    }

    private void givenMember(MemberProfile profile) {
        given(getMemberUseCase.getById(MEMBER_ID)).willReturn(MemberInfo.builder()
            .id(MEMBER_ID)
            .name("홍길동")
            .nickname("길동")
            .email("gildong@example.com")
            .schoolId(10L)
            .schoolName("UMC대학교")
            .profileImageLink("https://cdn.example.com/profile.png")
            .status(MemberStatus.ACTIVE)
            .build());
        Member member = Member.create("홍길동", "길동", "gildong@example.com", 10L, null);
        if (profile != null) {
            member.assignProfile(profile);
        }
        given(loadMemberPort.findById(MEMBER_ID)).willReturn(Optional.of(member));
    }
}
