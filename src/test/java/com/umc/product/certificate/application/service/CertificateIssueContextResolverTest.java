package com.umc.product.certificate.application.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import java.time.Instant;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.umc.product.certificate.application.port.in.command.dto.AdminIssueCertificateCommand;
import com.umc.product.certificate.application.port.in.command.dto.IssueCertificateCommand;
import com.umc.product.certificate.domain.CertificateType;
import com.umc.product.certificate.domain.exception.CertificateErrorCode;
import com.umc.product.certificate.domain.exception.CertificateException;
import com.umc.product.challenger.application.port.in.query.GetChallengerUseCase;
import com.umc.product.challenger.application.port.in.query.dto.ChallengerInfo;
import com.umc.product.common.domain.enums.ChallengerStatus;
import com.umc.product.member.application.port.in.query.GetMemberUseCase;
import com.umc.product.member.application.port.in.query.dto.MemberInfo;
import com.umc.product.organization.application.port.in.query.GetGisuUseCase;
import com.umc.product.organization.application.port.in.query.dto.gisu.GisuInfo;
import com.umc.product.project.application.port.in.query.GetProjectMemberUseCase;
import com.umc.product.project.application.port.in.query.dto.ProjectMemberInfo;
import com.umc.product.project.domain.enums.ProjectMemberStatus;

@ExtendWith(MockitoExtension.class)
class CertificateIssueContextResolverTest {

    @Mock
    GetMemberUseCase getMemberUseCase;

    @Mock
    GetChallengerUseCase getChallengerUseCase;

    @Mock
    GetGisuUseCase getGisuUseCase;

    @Mock
    GetProjectMemberUseCase getProjectMemberUseCase;

    @Test
    @DisplayName("상장은 셀프 발급을 허용하지 않는다")
    void 상장은_셀프_발급을_허용하지_않는다() {
        // given
        CertificateIssueContextResolver sut = sut();

        // when & then
        assertThatThrownBy(() -> sut.resolveSelf(IssueCertificateCommand.builder()
            .type(CertificateType.AWARD)
            .requesterMemberId(1L)
            .gisuId(7L)
            .build()))
            .isInstanceOf(CertificateException.class)
            .extracting("baseCode")
            .isEqualTo(CertificateErrorCode.CERTIFICATE_SELF_ISSUE_FORBIDDEN);
    }

    @Test
    @DisplayName("수료 상태가 아니면 수료증 발급 조건을 만족하지 않는다")
    void 수료_상태가_아니면_수료증_발급_조건을_만족하지_않는다() {
        // given
        given(getMemberUseCase.getById(1L)).willReturn(member());
        given(getGisuUseCase.getById(7L)).willReturn(gisu());
        given(getChallengerUseCase.findByMemberIdAndGisuId(1L, 7L)).willReturn(Optional.of(
            ChallengerInfo.builder()
                .challengerId(10L)
                .memberId(1L)
                .gisuId(7L)
                .challengerStatus(ChallengerStatus.ACTIVE)
                .build()
        ));
        CertificateIssueContextResolver sut = sut();

        // when & then
        assertThatThrownBy(() -> sut.resolveSelf(IssueCertificateCommand.builder()
            .type(CertificateType.COMPLETION)
            .requesterMemberId(1L)
            .gisuId(7L)
            .build()))
            .isInstanceOf(CertificateException.class)
            .extracting("baseCode")
            .isEqualTo(CertificateErrorCode.CERTIFICATE_ELIGIBILITY_NOT_MET);
    }

    @Test
    @DisplayName("프로젝트 멤버가 완료 상태가 아니면 참가 확인서 발급 조건을 만족하지 않는다")
    void 프로젝트_멤버가_완료_상태가_아니면_참가_확인서_발급_조건을_만족하지_않는다() {
        // given
        given(getMemberUseCase.getById(1L)).willReturn(member());
        given(getGisuUseCase.getById(7L)).willReturn(gisu());
        given(getProjectMemberUseCase.findByProjectIdAndMemberId(100L, 1L)).willReturn(Optional.of(
            ProjectMemberInfo.builder()
                .projectMemberId(200L)
                .projectId(100L)
                .projectGisuId(7L)
                .projectName("UMC 프로젝트")
                .memberId(1L)
                .status(ProjectMemberStatus.ACTIVE)
                .build()
        ));
        CertificateIssueContextResolver sut = sut();

        // when & then
        assertThatThrownBy(() -> sut.resolveSelf(IssueCertificateCommand.builder()
            .type(CertificateType.PROJECT_PARTICIPATION)
            .requesterMemberId(1L)
            .gisuId(7L)
            .projectId(100L)
            .build()))
            .isInstanceOf(CertificateException.class)
            .extracting("baseCode")
            .isEqualTo(CertificateErrorCode.CERTIFICATE_ELIGIBILITY_NOT_MET);
    }

    @Test
    @DisplayName("상장 제목이 없으면 운영진 상장 발급 조건을 만족하지 않는다")
    void 상장_제목이_없으면_운영진_상장_발급_조건을_만족하지_않는다() {
        // given
        given(getMemberUseCase.getById(1L)).willReturn(member());
        given(getGisuUseCase.getById(7L)).willReturn(gisu());
        CertificateIssueContextResolver sut = sut();

        // when & then
        assertThatThrownBy(() -> sut.resolveAdmin(AdminIssueCertificateCommand.builder()
            .type(CertificateType.AWARD)
            .requesterMemberId(99L)
            .recipientMemberId(1L)
            .gisuId(7L)
            .awardTitle(" ")
            .build()))
            .isInstanceOf(CertificateException.class)
            .extracting("baseCode")
            .isEqualTo(CertificateErrorCode.CERTIFICATE_ELIGIBILITY_NOT_MET);
    }

    private CertificateIssueContextResolver sut() {
        return new CertificateIssueContextResolver(
            getMemberUseCase,
            getChallengerUseCase,
            getGisuUseCase,
            getProjectMemberUseCase
        );
    }

    private MemberInfo member() {
        return MemberInfo.builder()
            .id(1L)
            .name("김유엠")
            .schoolName("유엠씨대학교")
            .build();
    }

    private GisuInfo gisu() {
        return new GisuInfo(7L, 7L, Instant.parse("2026-03-01T00:00:00Z"), Instant.parse("2026-08-31T00:00:00Z"), true);
    }

}
