package com.umc.product.project.adapter.in.web.assembler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import com.umc.product.authorization.application.port.in.query.GetChallengerRoleUseCase;
import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.member.application.port.in.query.GetMemberUseCase;
import com.umc.product.member.application.port.in.query.dto.MemberInfo;
import com.umc.product.project.adapter.in.web.dto.common.MemberBrief;
import com.umc.product.project.adapter.in.web.dto.response.DraftProjectResponse;
import com.umc.product.project.adapter.in.web.dto.response.ProjectDetailResponse;
import com.umc.product.project.adapter.in.web.dto.response.ProjectMembersResponse;
import com.umc.product.project.application.port.in.query.GetProjectUseCase;
import com.umc.product.project.application.port.in.query.SearchProjectUseCase;
import com.umc.product.project.application.port.in.query.dto.ProjectInfo;
import com.umc.product.project.application.port.out.LoadProjectApplicationFormPort;
import com.umc.product.project.application.port.out.LoadProjectMemberPort;
import com.umc.product.project.domain.Project;
import com.umc.product.project.domain.ProjectApplicationForm;
import com.umc.product.project.domain.ProjectMember;
import com.umc.product.project.domain.enums.ProjectStatus;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class ProjectResponseAssemblerTest {

    @Mock
    GetProjectUseCase getProjectUseCase;
    @Mock
    SearchProjectUseCase searchProjectUseCase;
    @Mock
    GetMemberUseCase getMemberUseCase;
    @Mock
    GetChallengerRoleUseCase getChallengerRoleUseCase;
    @Mock
    LoadProjectApplicationFormPort loadProjectApplicationFormPort;
    @Mock
    LoadProjectMemberPort loadProjectMemberPort;

    @InjectMocks
    ProjectResponseAssembler sut;

    @Test
    void detailFor_폼이_있으면_applicationFormId가_채워진다() {
        ProjectInfo info = projectInfo(42L);
        given(getProjectUseCase.getById(42L)).willReturn(info);
        given(getMemberUseCase.findAllByIds(java.util.Set.of(99L)))
            .willReturn(Map.of(99L, memberInfo(99L)));

        Project project = project(42L);
        ProjectApplicationForm form = applicationForm(project, 100L, 500L);
        given(loadProjectApplicationFormPort.findByProjectId(42L)).willReturn(Optional.of(form));

        ProjectDetailResponse response = sut.detailFor(42L, 99L);

        assertThat(response.applicationFormId()).isEqualTo(100L);
    }

    @Test
    void detailFor_폼이_없으면_applicationFormId가_null() {
        ProjectInfo info = projectInfo(42L);
        given(getProjectUseCase.getById(42L)).willReturn(info);
        given(getMemberUseCase.findAllByIds(java.util.Set.of(99L)))
            .willReturn(Map.of(99L, memberInfo(99L)));
        given(loadProjectApplicationFormPort.findByProjectId(42L)).willReturn(Optional.empty());

        ProjectDetailResponse response = sut.detailFor(42L, 99L);

        assertThat(response.applicationFormId()).isNull();
    }

    @Test
    void draftFor_Draft가_있으면_applicationFormId_hydrate() {
        ProjectInfo info = projectInfo(42L);
        given(getProjectUseCase.findDraftByOwnerAndGisu(99L, 1L)).willReturn(Optional.of(info));
        given(getMemberUseCase.findAllByIds(java.util.Set.of(99L)))
            .willReturn(Map.of(99L, memberInfo(99L)));

        Project project = project(42L);
        ProjectApplicationForm form = applicationForm(project, 100L, 500L);
        given(loadProjectApplicationFormPort.findByProjectId(42L)).willReturn(Optional.of(form));

        DraftProjectResponse response = sut.draftFor(99L, 1L);

        assertThat(response.applicationFormId()).isEqualTo(100L);
    }

    @Test
    void draftFor_Draft가_없으면_null_반환() {
        given(getProjectUseCase.findDraftByOwnerAndGisu(99L, 1L)).willReturn(Optional.empty());

        DraftProjectResponse response = sut.draftFor(99L, 1L);

        assertThat(response).isNull();
    }

    @Test
    void membersFor_PM이_본인이면_실명_노출() {
        ProjectInfo info = projectInfo(42L);
        given(getProjectUseCase.getById(42L)).willReturn(info);
        given(loadProjectMemberPort.listByProjectId(42L)).willReturn(List.of(
            projectMember(99L, ChallengerPart.PLAN),
            projectMember(101L, ChallengerPart.PLAN),
            projectMember(102L, ChallengerPart.SPRINGBOOT)
        ));
        given(getMemberUseCase.findAllByIds(Set.of(99L, 101L, 102L))).willReturn(Map.of(
            99L, memberInfoOf(99L, "메인PM", "김메인"),
            101L, memberInfoOf(101L, "코PM가", "박가나"),
            102L, memberInfoOf(102L, "백엔드", "이다라")
        ));

        ProjectMembersResponse response = sut.membersFor(42L, 99L);

        assertThat(response.productOwner().memberId()).isEqualTo(99L);
        assertThat(response.productOwner().name()).isEqualTo("김메인");
        assertThat(response.coProductOwners()).hasSize(1);
        assertThat(response.coProductOwners().get(0).memberId()).isEqualTo(101L);
        assertThat(response.coProductOwners().get(0).name()).isEqualTo("박가나");
        assertThat(response.partGroups()).hasSize(1);
        assertThat(response.partGroups().get(0).part()).isEqualTo(ChallengerPart.SPRINGBOOT);
    }

    @Test
    void membersFor_외부인이면_실명_마스킹() {
        ProjectInfo info = projectInfo(42L);
        given(getProjectUseCase.getById(42L)).willReturn(info);
        given(loadProjectMemberPort.listByProjectId(42L)).willReturn(List.of(
            projectMember(99L, ChallengerPart.PLAN)
        ));
        given(getMemberUseCase.findAllByIds(Set.of(99L))).willReturn(Map.of(
            99L, memberInfoOf(99L, "메인PM", "김메인")
        ));
        given(getChallengerRoleUseCase.isCentralCoreInGisu(200L, 1L)).willReturn(false);

        ProjectMembersResponse response = sut.membersFor(42L, 200L);

        assertThat(response.productOwner().name()).isNull();
    }

    @Test
    void membersFor_보조PM_가나다순_정렬() {
        ProjectInfo info = projectInfo(42L);
        given(getProjectUseCase.getById(42L)).willReturn(info);
        given(loadProjectMemberPort.listByProjectId(42L)).willReturn(List.of(
            projectMember(101L, ChallengerPart.PLAN),
            projectMember(102L, ChallengerPart.PLAN),
            projectMember(103L, ChallengerPart.PLAN)
        ));
        given(getMemberUseCase.findAllByIds(Set.of(99L, 101L, 102L, 103L))).willReturn(Map.of(
            99L, memberInfoOf(99L, "메인", "김메인"),
            101L, memberInfoOf(101L, "다람쥐", "박다람"),
            102L, memberInfoOf(102L, "가람", "이가람"),
            103L, memberInfoOf(103L, "나무", "최나무")
        ));

        ProjectMembersResponse response = sut.membersFor(42L, 99L);

        List<String> nicknames = response.coProductOwners().stream().map(MemberBrief::nickname).toList();
        assertThat(nicknames).containsExactly("가람", "나무", "다람쥐");
    }

    private MemberInfo memberInfoOf(Long memberId, String nickname, String name) {
        return MemberInfo.builder()
            .id(memberId)
            .nickname(nickname)
            .name(name)
            .schoolName("학교")
            .build();
    }

    private ProjectMember projectMember(Long memberId, ChallengerPart part) {
        try {
            var c = ProjectMember.class.getDeclaredConstructor();
            c.setAccessible(true);
            ProjectMember pm = c.newInstance();
            ReflectionTestUtils.setField(pm, "memberId", memberId);
            ReflectionTestUtils.setField(pm, "part", part);
            return pm;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private ProjectInfo projectInfo(Long projectId) {
        return ProjectInfo.builder()
            .id(projectId)
            .status(ProjectStatus.DRAFT)
            .name("Triple")
            .description(null)
            .gisuId(1L)
            .chapterId(1L)
            .productOwnerMemberId(99L)
            .coProductOwnerMemberIds(List.of())
            .partQuotas(List.of())
            .build();
    }

    private MemberInfo memberInfo(Long memberId) {
        return MemberInfo.builder()
            .id(memberId)
            .nickname("이방토")
            .name("이예원")
            .schoolName("한양대 ERICA")
            .build();
    }

    private Project project(Long id) {
        Project p;
        try {
            var c = Project.class.getDeclaredConstructor();
            c.setAccessible(true);
            p = c.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        ReflectionTestUtils.setField(p, "id", id);
        return p;
    }

    private ProjectApplicationForm applicationForm(Project project, Long id, Long formId) {
        ProjectApplicationForm form = ProjectApplicationForm.create(project, formId);
        ReflectionTestUtils.setField(form, "id", id);
        return form;
    }
}
