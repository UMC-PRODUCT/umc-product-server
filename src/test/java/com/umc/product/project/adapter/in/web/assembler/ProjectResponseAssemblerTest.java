package com.umc.product.project.adapter.in.web.assembler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import com.umc.product.authorization.application.port.in.query.GetChallengerRoleUseCase;
import com.umc.product.member.application.port.in.query.GetMemberUseCase;
import com.umc.product.member.application.port.in.query.dto.MemberInfo;
import com.umc.product.project.adapter.in.web.dto.response.DraftProjectResponse;
import com.umc.product.project.adapter.in.web.dto.response.ProjectDetailResponse;
import com.umc.product.project.application.port.in.query.GetProjectUseCase;
import com.umc.product.project.application.port.in.query.SearchProjectUseCase;
import com.umc.product.project.application.port.in.query.dto.ProjectInfo;
import com.umc.product.project.application.port.out.LoadProjectApplicationFormPort;
import com.umc.product.project.domain.Project;
import com.umc.product.project.domain.ProjectApplicationForm;
import com.umc.product.project.domain.enums.ProjectStatus;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
