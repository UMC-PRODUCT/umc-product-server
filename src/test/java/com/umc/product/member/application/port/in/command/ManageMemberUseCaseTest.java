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
import com.umc.product.member.application.port.out.SaveMemberPort;
import com.umc.product.member.application.service.MemberService;
import com.umc.product.member.domain.Member;
import com.umc.product.organization.application.port.out.query.LoadSchoolPort;
import com.umc.product.organization.exception.OrganizationDomainException;
import com.umc.product.organization.exception.OrganizationErrorCode;
import com.umc.product.storage.application.port.out.LoadFileMetadataPort;
import com.umc.product.storage.domain.exception.StorageErrorCode;
import com.umc.product.storage.domain.exception.StorageException;
import com.umc.product.terms.application.port.in.command.ManageTermsAgreementUseCase;
import com.umc.product.terms.application.port.in.query.GetTermsUseCase;
import com.umc.product.terms.domain.exception.TermsDomainException;
import com.umc.product.terms.domain.exception.TermsErrorCode;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class ManageMemberUseCaseTest {

    @Mock
    SaveMemberPort saveMemberPort;

    @Mock
    LoadSchoolPort loadSchoolPort;

    @Mock
    LoadFileMetadataPort loadFileMetadataPort;

    @Mock
    OAuthAuthenticationUseCase oAuthAuthenticationUseCase;

    @Mock
    ManageTermsAgreementUseCase manageTermsAgreementUseCase;

    @Mock
    GetTermsUseCase getTermsUseCase;

    @InjectMocks
    MemberService sut;

    @Test
    void 회원가입_성공() {
        // given
        RegisterMemberCommand command = createCommand(1L, null, List.of(
                new TermConsents(1L, true),
                new TermConsents(2L, true)
        ));

        given(loadSchoolPort.existsById(1L)).willReturn(true);
        given(getTermsUseCase.getRequiredTermIds()).willReturn(Set.of(1L, 2L));
        given(saveMemberPort.save(any(Member.class))).willAnswer(invocation -> {
            Member member = invocation.getArgument(0);
            ReflectionTestUtils.setField(member, "id", 1L);
            return member;
        });

        // when
        Long memberId = sut.registerMember(command);

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
        assertThatThrownBy(() -> sut.registerMember(command))
                .isInstanceOf(OrganizationDomainException.class)
                .extracting("code")
                .isEqualTo(OrganizationErrorCode.SCHOOL_NOT_FOUND);

        then(saveMemberPort).should(never()).save(any());
    }

    @Test
    void 프로필_이미지가_존재하지_않으면_예외() {
        // given
        RegisterMemberCommand command = createCommand(1L, 999L, List.of(
                new TermConsents(1L, true)
        ));

        given(loadSchoolPort.existsById(1L)).willReturn(true);
        given(loadFileMetadataPort.existsById(999L)).willReturn(false);

        // when & then
        assertThatThrownBy(() -> sut.registerMember(command))
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
        Long memberId = sut.registerMember(command);

        // then
        assertThat(memberId).isEqualTo(1L);
        then(loadFileMetadataPort).should(never()).existsById(any());
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
        assertThatThrownBy(() -> sut.registerMember(command))
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
        assertThatThrownBy(() -> sut.registerMember(command))
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
        Long memberId = sut.registerMember(command);

        // then
        assertThat(memberId).isEqualTo(1L);
    }

    private RegisterMemberCommand createCommand(Long schoolId, Long profileImageId, List<TermConsents> termConsents) {
        return RegisterMemberCommand.builder()
                .provider(OAuthProvider.KAKAO)
                .providerId("kakao_12345")
                .name("홍길동")
                .nickname("길동이")
                .email("test@example.com")
                .schoolId(schoolId)
                .profileImageId(profileImageId)
                .termConsents(termConsents)
                .build();
    }
}
