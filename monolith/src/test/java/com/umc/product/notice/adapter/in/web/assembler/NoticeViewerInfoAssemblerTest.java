package com.umc.product.notice.adapter.in.web.assembler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verifyNoInteractions;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.umc.product.authorization.application.port.in.query.GetChallengerRoleUseCase;
import com.umc.product.challenger.application.port.in.query.GetChallengerUseCase;
import com.umc.product.challenger.application.port.in.query.dto.ChallengerInfo;
import com.umc.product.member.application.port.in.query.GetMemberUseCase;
import com.umc.product.member.application.port.in.query.dto.MemberInfo;
import com.umc.product.notice.application.port.in.query.dto.NoticeViewerInfo;
import com.umc.product.notice.domain.enums.NoticeTab;
import com.umc.product.organization.application.port.in.query.GetChapterUseCase;

@ExtendWith(MockitoExtension.class)
@DisplayName("NoticeViewerInfoAssembler")
class NoticeViewerInfoAssemblerTest {

    private static final Long MEMBER_ID = 1L;
    private static final Long GISU_ID = 9L;

    @Mock
    GetChallengerUseCase getChallengerUseCase;

    @Mock
    GetChallengerRoleUseCase getChallengerRoleUseCase;

    @Mock
    GetMemberUseCase getMemberUseCase;

    @Mock
    GetChapterUseCase getChapterUseCase;

    @InjectMocks
    NoticeViewerInfoAssembler sut;

    @Test
    @DisplayName("system role SUPER_ADMIN은 challenger role 없이 최상위 공지 역할을 얻는다")
    void system_super_admin_has_central_member_notice_role() {
        given(getChallengerUseCase.findByMemberIdAndGisuId(MEMBER_ID, GISU_ID)).willReturn(Optional.empty());
        given(getChallengerRoleUseCase.isSuperAdmin(MEMBER_ID)).willReturn(true);
        given(getMemberUseCase.findAllByIds(java.util.Set.of(MEMBER_ID))).willReturn(Map.of(
            MEMBER_ID,
            MemberInfo.builder().id(MEMBER_ID).build()
        ));

        NoticeViewerInfo result = sut.toMemberIdAndGisuId(MEMBER_ID, GISU_ID);

        assertThat(result.viewerRole()).isEqualTo(NoticeTab.CENTRAL_MEMBER);
        verifyNoInteractions(getChapterUseCase);
    }

    @Test
    @DisplayName("수강 없는 운영진(part=null)은 memberParts에 null이 섞이지 않는다")
    void 운영진_null_파트_방어() {
        given(getChallengerUseCase.findByMemberIdAndGisuId(MEMBER_ID, GISU_ID)).willReturn(
            Optional.of(ChallengerInfo.builder()
                .memberId(MEMBER_ID)
                .gisuId(GISU_ID)
                .part(null)
                .build())
        );
        given(getChallengerRoleUseCase.getAllResponsiblePartByMemberIdAndGisuId(MEMBER_ID, GISU_ID))
            .willReturn(Set.of());
        given(getChallengerRoleUseCase.isSuperAdmin(MEMBER_ID)).willReturn(false);
        given(getChallengerRoleUseCase.findAllByMemberId(MEMBER_ID)).willReturn(List.of());
        given(getMemberUseCase.findAllByIds(Set.of(MEMBER_ID))).willReturn(Map.of(
            MEMBER_ID,
            MemberInfo.builder().id(MEMBER_ID).build()
        ));

        NoticeViewerInfo result = sut.toMemberIdAndGisuId(MEMBER_ID, GISU_ID);

        assertThat(result.memberParts()).doesNotContainNull().isEmpty();
    }
}
