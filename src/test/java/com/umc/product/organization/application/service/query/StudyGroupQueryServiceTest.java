package com.umc.product.organization.application.service.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.umc.product.authorization.application.port.in.query.GetChallengerRoleUseCase;
import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.common.domain.enums.ChallengerRoleType;
import com.umc.product.common.domain.enums.MemberStatus;
import com.umc.product.member.application.port.in.query.GetMemberUseCase;
import com.umc.product.member.application.port.in.query.dto.MemberInfo;
import com.umc.product.organization.application.port.in.query.GetGisuUseCase;
import com.umc.product.organization.application.port.in.query.dto.studygroup.StudyGroupInfo;
import com.umc.product.organization.application.port.in.query.dto.studygroup.StudyGroupViewScope;
import com.umc.product.organization.application.port.in.query.dto.studygroup.StudyGroupViewScope.AsPartLeader;
import com.umc.product.organization.application.port.in.query.dto.studygroup.StudyGroupViewScope.AsSchoolCore;
import com.umc.product.organization.application.port.out.query.LoadStudyGroupPort;
import com.umc.product.organization.application.port.service.query.StudyGroupQueryService;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class StudyGroupQueryServiceTest {

    @Mock
    GetGisuUseCase getGisuUseCase;
    @Mock
    GetMemberUseCase getMemberUseCase;
    @Mock
    GetChallengerRoleUseCase getChallengerRoleUseCase;
    @Mock
    LoadStudyGroupPort loadStudyGroupPort;

    @InjectMocks
    StudyGroupQueryService sut;

    @Test
    void getMyStudyGroups_회장은_AsSchoolCore_scope로_조회() {
        // given
        Long memberId = 1L;
        Long schoolId = 100L;
        Long gisuId = 10L;
        Set<Long> schoolMemberIds = Set.of(101L, 102L, 103L);

        given(getMemberUseCase.getById(memberId)).willReturn(memberInfo(memberId, schoolId));
        given(getGisuUseCase.getActiveGisuId()).willReturn(gisuId);
        given(getChallengerRoleUseCase.isSchoolCoreInGisu(memberId, gisuId, schoolId)).willReturn(true);
        given(getMemberUseCase.findAllIdsBySchoolId(schoolId)).willReturn(schoolMemberIds);
        given(getChallengerRoleUseCase.hasRoleTypeInGisu(memberId, gisuId, ChallengerRoleType.SCHOOL_PART_LEADER))
            .willReturn(false);

        StudyGroupInfo group1 = studyGroupInfo(1L, gisuId);
        StudyGroupInfo group2 = studyGroupInfo(2L, gisuId);
        given(loadStudyGroupPort.findMyStudyGroups(any(), eq(gisuId), eq(null), anyInt()))
            .willReturn(List.of(group1, group2));

        // when
        List<StudyGroupInfo> result = sut.getMyStudyGroups(memberId, null, 20);

        // then
        assertThat(result).extracting(StudyGroupInfo::groupId).containsExactly(1L, 2L);

        List<StudyGroupViewScope> capturedScopes = captureScopes();
        assertThat(capturedScopes).hasSize(1);
        assertThat(capturedScopes.get(0)).isInstanceOfSatisfying(AsSchoolCore.class,
            scope -> assertThat(scope.schoolMemberIds()).isEqualTo(schoolMemberIds));
    }

    @Test
    void getMyStudyGroups_파트장은_AsPartLeader_scope로_조회() {
        // given
        Long memberId = 2L;
        Long schoolId = 200L;
        Long gisuId = 10L;

        given(getMemberUseCase.getById(memberId)).willReturn(memberInfo(memberId, schoolId));
        given(getGisuUseCase.getActiveGisuId()).willReturn(gisuId);
        given(getChallengerRoleUseCase.isSchoolCoreInGisu(memberId, gisuId, schoolId)).willReturn(false);
        given(getChallengerRoleUseCase.hasRoleTypeInGisu(memberId, gisuId, ChallengerRoleType.SCHOOL_PART_LEADER))
            .willReturn(true);

        given(loadStudyGroupPort.findMyStudyGroups(any(), eq(gisuId), eq(null), anyInt()))
            .willReturn(List.of(studyGroupInfo(1L, gisuId)));

        // when
        sut.getMyStudyGroups(memberId, null, 20);

        // then
        List<StudyGroupViewScope> capturedScopes = captureScopes();
        assertThat(capturedScopes).hasSize(1);
        assertThat(capturedScopes.get(0)).isInstanceOfSatisfying(AsPartLeader.class,
            scope -> assertThat(scope.memberId()).isEqualTo(memberId));
    }

    @Test
    void getMyStudyGroups_회장과_파트장_겸직시_두_scope_조립() {
        // given
        Long memberId = 3L;
        Long schoolId = 300L;
        Long gisuId = 10L;
        Set<Long> schoolMemberIds = Set.of(301L, 302L);

        given(getMemberUseCase.getById(memberId)).willReturn(memberInfo(memberId, schoolId));
        given(getGisuUseCase.getActiveGisuId()).willReturn(gisuId);
        given(getChallengerRoleUseCase.isSchoolCoreInGisu(memberId, gisuId, schoolId)).willReturn(true);
        given(getMemberUseCase.findAllIdsBySchoolId(schoolId)).willReturn(schoolMemberIds);
        given(getChallengerRoleUseCase.hasRoleTypeInGisu(memberId, gisuId, ChallengerRoleType.SCHOOL_PART_LEADER))
            .willReturn(true);

        given(loadStudyGroupPort.findMyStudyGroups(any(), eq(gisuId), eq(null), anyInt()))
            .willReturn(List.of(studyGroupInfo(1L, gisuId)));

        // when
        sut.getMyStudyGroups(memberId, null, 20);

        // then
        List<StudyGroupViewScope> capturedScopes = captureScopes();
        assertThat(capturedScopes).hasSize(2);
        assertThat(capturedScopes).hasAtLeastOneElementOfType(AsSchoolCore.class);
        assertThat(capturedScopes).hasAtLeastOneElementOfType(AsPartLeader.class);
    }

    @Test
    void getMyStudyGroups_권한이_없으면_port_호출없이_빈_리스트() {
        // given — 회장도 파트장도 아닌 일반 챌린저
        Long memberId = 4L;
        Long schoolId = 400L;
        Long gisuId = 10L;

        given(getMemberUseCase.getById(memberId)).willReturn(memberInfo(memberId, schoolId));
        given(getGisuUseCase.getActiveGisuId()).willReturn(gisuId);
        given(getChallengerRoleUseCase.isSchoolCoreInGisu(memberId, gisuId, schoolId)).willReturn(false);
        given(getChallengerRoleUseCase.hasRoleTypeInGisu(memberId, gisuId, ChallengerRoleType.SCHOOL_PART_LEADER))
            .willReturn(false);

        // when
        List<StudyGroupInfo> result = sut.getMyStudyGroups(memberId, null, 20);

        // then
        assertThat(result).isEmpty();
        verify(loadStudyGroupPort, never()).findMyStudyGroups(any(), any(), any(), anyInt());
    }

    @Test
    void getMyStudyGroups_회장이지만_학교_멤버가_없으면_AsSchoolCore_scope_제외() {
        // given — 회장 권한 있지만 학교 멤버 집합이 비어있어 AsSchoolCore가 EXISTS=false가 될 것이 자명. 비용 절감 위해 scope 자체를 만들지 않음.
        //         파트장 권한을 같이 부여해 결과적으로 AsPartLeader만 조립되는지 검증한다.
        Long memberId = 5L;
        Long schoolId = 500L;
        Long gisuId = 10L;

        given(getMemberUseCase.getById(memberId)).willReturn(memberInfo(memberId, schoolId));
        given(getGisuUseCase.getActiveGisuId()).willReturn(gisuId);
        given(getChallengerRoleUseCase.isSchoolCoreInGisu(memberId, gisuId, schoolId)).willReturn(true);
        given(getMemberUseCase.findAllIdsBySchoolId(schoolId)).willReturn(Set.of());
        given(getChallengerRoleUseCase.hasRoleTypeInGisu(memberId, gisuId, ChallengerRoleType.SCHOOL_PART_LEADER))
            .willReturn(true);
        given(loadStudyGroupPort.findMyStudyGroups(any(), eq(gisuId), eq(null), anyInt()))
            .willReturn(List.of(studyGroupInfo(1L, gisuId)));

        // when
        sut.getMyStudyGroups(memberId, null, 20);

        // then
        List<StudyGroupViewScope> capturedScopes = captureScopes();
        assertThat(capturedScopes).hasSize(1);
        assertThat(capturedScopes.get(0)).isInstanceOf(AsPartLeader.class);
    }

    @Test
    void getMyStudyGroups_컨트롤러_hasNext_판단을_위해_size_plus_1로_port에_전달() {
        // given
        Long memberId = 6L;
        Long schoolId = 600L;
        Long gisuId = 10L;
        int requestedSize = 20;

        given(getMemberUseCase.getById(memberId)).willReturn(memberInfo(memberId, schoolId));
        given(getGisuUseCase.getActiveGisuId()).willReturn(gisuId);
        given(getChallengerRoleUseCase.isSchoolCoreInGisu(memberId, gisuId, schoolId)).willReturn(false);
        given(getChallengerRoleUseCase.hasRoleTypeInGisu(memberId, gisuId, ChallengerRoleType.SCHOOL_PART_LEADER))
            .willReturn(true);
        given(loadStudyGroupPort.findMyStudyGroups(any(), any(), any(), anyInt()))
            .willReturn(List.of());

        // when
        sut.getMyStudyGroups(memberId, null, requestedSize);

        // then
        verify(loadStudyGroupPort).findMyStudyGroups(any(), eq(gisuId), eq(null), eq(requestedSize + 1));
    }

    // ========== Helper Methods ==========

    @SuppressWarnings("unchecked")
    private List<StudyGroupViewScope> captureScopes() {
        ArgumentCaptor<List<StudyGroupViewScope>> captor = ArgumentCaptor.forClass(List.class);
        verify(loadStudyGroupPort).findMyStudyGroups(captor.capture(), any(), any(), anyInt());
        return captor.getValue();
    }

    private MemberInfo memberInfo(Long memberId, Long schoolId) {
        return MemberInfo.builder()
            .id(memberId)
            .name("테스트")
            .nickname("테스트")
            .email("test@example.com")
            .schoolId(schoolId)
            .schoolName("테스트학교")
            .profileImageId(null)
            .profileImageLink(null)
            .status(MemberStatus.ACTIVE)
            .roles(List.of())
            .build();
    }

    private StudyGroupInfo studyGroupInfo(Long groupId, Long gisuId) {
        return StudyGroupInfo.create(
            groupId,
            "그룹" + groupId,
            gisuId,
            ChallengerPart.SPRINGBOOT,
            Instant.parse("2026-05-01T00:00:00Z"),
            List.of(),
            List.of()
        );
    }
}
