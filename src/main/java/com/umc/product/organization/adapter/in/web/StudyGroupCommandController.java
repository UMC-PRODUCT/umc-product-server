package com.umc.product.organization.adapter.in.web;

import com.umc.product.authorization.adapter.in.aspect.CheckAccess;
import com.umc.product.authorization.domain.PermissionType;
import com.umc.product.authorization.domain.ResourceType;
import com.umc.product.organization.adapter.in.web.dto.request.CreateStudyGroupRequest;
import com.umc.product.organization.adapter.in.web.dto.request.ReplaceStudyGroupMemberAndMentorRequest;
import com.umc.product.organization.adapter.in.web.dto.request.UpdateStudyGroupRequest;
import com.umc.product.organization.adapter.in.web.swagger.StudyGroupCommandControllerApi;
import com.umc.product.organization.application.port.in.command.ManageStudyGroupUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/study-groups")
@RequiredArgsConstructor
public class StudyGroupCommandController implements StudyGroupCommandControllerApi {

    private final ManageStudyGroupUseCase manageStudyGroupUseCase;

    @CheckAccess(
        resourceType = ResourceType.STUDY_GROUP,
        permission = PermissionType.WRITE,
        message = "스터디 그룹을 생성할 권한이 없습니다."
    )
    @Override
    @PostMapping
    public void create(@Valid @RequestBody CreateStudyGroupRequest request) {
        manageStudyGroupUseCase.create(request.toCommand());
    }

    @CheckAccess(
        resourceType = ResourceType.STUDY_GROUP,
        permission = PermissionType.EDIT,
        message = "해당 스터디 그룹을 수정할 권한이 없습니다."
    )
    @Override
    @PatchMapping("/{groupId}")
    public void update(
        @PathVariable Long groupId,
        @Valid @RequestBody UpdateStudyGroupRequest request
    ) {
        manageStudyGroupUseCase.update(request.toCommand(groupId));
    }

    @CheckAccess(
        resourceType = ResourceType.STUDY_GROUP,
        permission = PermissionType.EDIT,
        message = "해당 스터디 그룹 구성원을 수정할 권한이 없습니다."
    )
    @Override
    @PutMapping("/{groupId}/members-mentors")
    public void replaceStudyGroupMemberAndMentors(
        @PathVariable Long groupId,
        @Valid @RequestBody ReplaceStudyGroupMemberAndMentorRequest request
    ) {
        manageStudyGroupUseCase.replaceMemberAndMentors(request.toCommand(groupId));
    }

    @CheckAccess(
        resourceType = ResourceType.STUDY_GROUP,
        permission = PermissionType.DELETE,
        message = "해당 스터디 그룹을 삭제할 권한이 없습니다."
    )
    @Override
    @DeleteMapping("/{groupId}")
    public void delete(@PathVariable Long groupId) {
        manageStudyGroupUseCase.delete(groupId);
    }
}
