package com.umc.product.member.application.port.in.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

import com.umc.product.authentication.application.port.in.command.OAuthAuthenticationUseCase;
import com.umc.product.common.domain.enums.OAuthProvider;
import com.umc.product.member.application.port.in.command.dto.RegisterMemberCommand;
import com.umc.product.member.application.port.in.command.dto.TermConsents;
import com.umc.product.member.application.port.in.command.dto.UpdateMemberCommand;
import com.umc.product.member.application.port.out.LoadMemberPort;
import com.umc.product.member.application.port.out.SaveMemberPort;
import com.umc.product.member.application.service.MemberService;
import com.umc.product.member.domain.Member;
import com.umc.product.member.domain.exception.MemberDomainException;
import com.umc.product.member.domain.exception.MemberErrorCode;
import com.umc.product.organization.application.port.out.query.LoadSchoolPort;
import com.umc.product.organization.exception.OrganizationDomainException;
import com.umc.product.organization.exception.OrganizationErrorCode;
import com.umc.product.storage.application.port.in.query.GetFileUseCase;
import com.umc.product.storage.application.port.out.LoadFileMetadataPort;
import com.umc.product.storage.domain.exception.StorageErrorCode;
import com.umc.product.storage.domain.exception.StorageException;
import com.umc.product.terms.application.port.in.command.ManageTermsAgreementUseCase;
import com.umc.product.terms.application.port.in.query.GetTermsUseCase;
import com.umc.product.terms.domain.exception.TermsDomainException;
import com.umc.product.terms.domain.exception.TermsErrorCode;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class ManageMemberUseCaseTest {

    @Mock
    LoadMemberPort loadMemberPort;

    @Mock
    SaveMemberPort saveMemberPort;

    @Mock
    LoadSchoolPort loadSchoolPort;

    @Mock
    LoadFileMetadataPort loadFileMetadataPort;

    @Mock
    GetFileUseCase getFileUseCase;

    @Mock
    OAuthAuthenticationUseCase oAuthAuthenticationUseCase;

    @Mock
    ManageTermsAgreementUseCase manageTermsAgreementUseCase;

    @Mock
    GetTermsUseCase getTermsUseCase;

    @InjectMocks
    MemberService memberService;

    @Test
    @DisplayName("일반적인 회원가입 성공")
    void 일반_회원가입_성공() {
        // given
        RegisterMemberCommand command = createCommand(1L, null, List.of(
            new TermConsents(1L, true),
            new TermConsents(2L, true)
        ));

        // 학교가 존재 검증 bypass
        given(loadSchoolPort.existsById(1L)).willReturn(true);
        // 필수 약관을 동의했는지 검증하는 부분 bypass
        given(getTermsUseCase.getRequiredTermIds()).willReturn(Set.of(1L, 2L));
        // member entity 저장 시 id를 1로 반환할 것
        given(saveMemberPort.save(any(Member.class))).willAnswer(invocation -> {
            Member member = invocation.getArgument(0);
            ReflectionTestUtils.setField(member, "id", 1L);
            return member;
        });

        // when
        Long memberId = memberService.registerMember(command);

        // then
        assertThat(memberId).isEqualTo(1L);
        then(saveMemberPort).should().save(any(Member.class));
        then(oAuthAuthenticationUseCase).should().linkOAuth(any());
        then(manageTermsAgreementUseCase).should(times(2)).createTermConsent(any());
    }

    @Test
    void 학교가_존재하지_않으면_예외() {
        // given
        RegisterMemberCommand command = createCommand(999L, null, List.of(
            new TermConsents(1L, true)
        ));

        given(loadSchoolPort.existsById(999L)).willReturn(false);

        // when & then
        assertThatThrownBy(() -> memberService.registerMember(command))
            .isInstanceOf(OrganizationDomainException.class)
            .extracting("code")
            .isEqualTo(OrganizationErrorCode.SCHOOL_NOT_FOUND);

        then(saveMemberPort).should(never()).save(any());
    }

    @Test
    @DisplayName("제공된 프로필 이미지가 존재하지 않으면 에러 발생")
    void 프로필_이미지가_존재하지_않으면_예외() {
        // given
        RegisterMemberCommand command = createCommand(1L, "profile_image_id", List.of(
            new TermConsents(1L, true)
        ));

        given(loadSchoolPort.existsById(1L)).willReturn(true);
        given(loadFileMetadataPort.existsByFileId("profile_image_id")).willReturn(false);

        // when & then
        assertThatThrownBy(() -> memberService.registerMember(command))
            .isInstanceOf(StorageException.class)
            .extracting("code")
            .isEqualTo(StorageErrorCode.FILE_NOT_FOUND);

        then(saveMemberPort).should(never()).save(any());
    }

    @Test
    void 프로필_이미지_ID가_null이면_검증_스킵() {
        // given
        RegisterMemberCommand command = createCommand(1L, null, List.of(
            new TermConsents(1L, true)
        ));

        given(loadSchoolPort.existsById(1L)).willReturn(true);
        given(getTermsUseCase.getRequiredTermIds()).willReturn(Set.of(1L));
        given(saveMemberPort.save(any(Member.class))).willAnswer(invocation -> {
            Member member = invocation.getArgument(0);
            ReflectionTestUtils.setField(member, "id", 1L);
            return member;
        });

        // when
        Long memberId = memberService.registerMember(command);

        // then
        assertThat(memberId).isEqualTo(1L);
        then(loadFileMetadataPort).should(never()).existsByFileId(any());
    }

    @Test
    void 필수_약관_미동의시_예외() {
        // given
        RegisterMemberCommand command = createCommand(1L, null, List.of(
            new TermConsents(1L, false),
            new TermConsents(2L, false)
        ));

        given(loadSchoolPort.existsById(1L)).willReturn(true);
        given(getTermsUseCase.getRequiredTermIds()).willReturn(Set.of(1L, 2L));

        // when & then
        assertThatThrownBy(() -> memberService.registerMember(command))
            .isInstanceOf(TermsDomainException.class)
            .extracting("code")
            .isEqualTo(TermsErrorCode.MANDATORY_TERMS_NOT_AGREED);

        then(saveMemberPort).should(never()).save(any());
    }

    @Test
    void 필수_약관_일부만_동의시_예외() {
        // given
        RegisterMemberCommand command = createCommand(1L, null, List.of(
            new TermConsents(1L, true),
            new TermConsents(2L, false)
        ));

        given(loadSchoolPort.existsById(1L)).willReturn(true);
        given(getTermsUseCase.getRequiredTermIds()).willReturn(Set.of(1L, 2L));

        // when & then
        assertThatThrownBy(() -> memberService.registerMember(command))
            .isInstanceOf(TermsDomainException.class)
            .extracting("code")
            .isEqualTo(TermsErrorCode.MANDATORY_TERMS_NOT_AGREED);

        then(saveMemberPort).should(never()).save(any());
    }

    @Test
    void 선택_약관만_미동의해도_성공() {
        // given
        RegisterMemberCommand command = createCommand(1L, null, List.of(
            new TermConsents(1L, true),   // 필수 약관 동의
            new TermConsents(2L, true),   // 필수 약관 동의
            new TermConsents(3L, false)   // 선택 약관 미동의
        ));

        given(loadSchoolPort.existsById(1L)).willReturn(true);
        given(getTermsUseCase.getRequiredTermIds()).willReturn(Set.of(1L, 2L));  // 1, 2만 필수
        given(saveMemberPort.save(any(Member.class))).willAnswer(invocation -> {
            Member member = invocation.getArgument(0);
            ReflectionTestUtils.setField(member, "id", 1L);
            return member;
        });

        // when
        Long memberId = memberService.registerMember(command);

        // then
        assertThat(memberId).isEqualTo(1L);
    }

    @Nested
    @DisplayName("updateMember")
    class UpdateMember {

        @Test
        void 프로필_수정_성공() {
            // given
            Member member = createMember(1L);
            given(loadMemberPort.findById(1L)).willReturn(Optional.of(member));
            given(getFileUseCase.existsById("new_image_id")).willReturn(true);

            UpdateMemberCommand command = UpdateMemberCommand.forProfileUpdate(1L, "new_image_id");

            // when
            memberService.updateMember(command);

            // then
            assertThat(member.getProfileImageId()).isEqualTo("new_image_id");
        }

        @Test
        void 존재하지_않는_회원이면_예외() {
            // given
            given(loadMemberPort.findById(999L)).willReturn(Optional.empty());

            UpdateMemberCommand command = UpdateMemberCommand.forProfileUpdate(999L, "new_image_id");

            // when & then
            assertThatThrownBy(() -> memberService.updateMember(command))
                .isInstanceOf(MemberDomainException.class)
                .extracting("code")
                .isEqualTo(MemberErrorCode.MEMBER_NOT_FOUND);
        }

        @Test
        void 존재하지_않는_프로필_이미지_ID면_예외() {
            // given
            Member member = createMember(1L);
            given(loadMemberPort.findById(1L)).willReturn(Optional.of(member));
            given(getFileUseCase.existsById("invalid_image_id")).willReturn(false);

            UpdateMemberCommand command = UpdateMemberCommand.forProfileUpdate(1L, "invalid_image_id");

            // when & then
            assertThatThrownBy(() -> memberService.updateMember(command))
                .isInstanceOf(StorageException.class)
                .extracting("code")
                .isEqualTo(StorageErrorCode.FILE_NOT_FOUND);
        }

        @Test
        void 프로필_이미지_ID가_null이면_검증_스킵() {
            // given
            Member member = createMember(1L);
            given(loadMemberPort.findById(1L)).willReturn(Optional.of(member));

            UpdateMemberCommand command = UpdateMemberCommand.forProfileUpdate(1L, null);

            // when
            memberService.updateMember(command);

            // then
            then(getFileUseCase).should(never()).existsById(any());
        }
    }

    // ── 헬퍼 메서드 ──

    private Member createMember(Long id) {
        Member member = Member.builder()
            .name("홍길동")
            .nickname("길동")
            .email("test@example.com")
            .schoolId(1L)
            .profileImageId("old_image_id")
            .build();
        ReflectionTestUtils.setField(member, "id", id);
        return member;
    }

    private RegisterMemberCommand createCommand(Long schoolId, String profileImageId, List<TermConsents> termConsents) {
        return RegisterMemberCommand.builder()
            .provider(OAuthProvider.KAKAO)
            .providerId("some_kakao_provider_id")
            .name("홍길동")
            .nickname("길동")
            .email("test@example.com")
            .schoolId(schoolId)
            .profileImageId(profileImageId)
            .termConsents(termConsents)
            .build();
    }
}
