package com.umc.product.organization.adapter.in.web;

import com.umc.product.authorization.adapter.in.aspect.CheckAccess;
import com.umc.product.authorization.domain.PermissionType;
import com.umc.product.authorization.domain.ResourceType;
import com.umc.product.organization.adapter.in.web.dto.request.AssignSchoolRequest;
import com.umc.product.organization.adapter.in.web.dto.request.CreateSchoolRequest;
import com.umc.product.organization.adapter.in.web.dto.request.DeleteSchoolsRequest;
import com.umc.product.organization.adapter.in.web.dto.request.UnassignSchoolRequest;
import com.umc.product.organization.adapter.in.web.dto.request.UpdateSchoolRequest;
import com.umc.product.organization.adapter.in.web.swagger.AdminSchoolControllerApi;
import com.umc.product.organization.application.port.in.command.ManageSchoolUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/schools")
@RequiredArgsConstructor
public class AdminSchoolController implements AdminSchoolControllerApi {

    private final ManageSchoolUseCase manageSchoolUseCase;

    @CheckAccess(resourceType = ResourceType.SCHOOL, permission = PermissionType.WRITE)
    @Override
    @PostMapping
    public void createSchool(@RequestBody @Valid CreateSchoolRequest request) {
        manageSchoolUseCase.register(request.toCommand());
    }

    @CheckAccess(resourceType = ResourceType.SCHOOL, permission = PermissionType.EDIT)
    @Override
    @PatchMapping("/{schoolId}")
    public void updateSchool(@PathVariable Long schoolId, @RequestBody @Valid UpdateSchoolRequest request) {
        manageSchoolUseCase.updateSchool(schoolId, request.toCommand());
    }

    @CheckAccess(resourceType = ResourceType.SCHOOL, permission = PermissionType.DELETE)
    @Override
    @DeleteMapping
    public void deleteSchools(@RequestBody @Valid DeleteSchoolsRequest request) {
        manageSchoolUseCase.deleteSchools(request.schoolIds());
    }

    @CheckAccess(resourceType = ResourceType.SCHOOL, permission = PermissionType.EDIT)
    @Override
    @PatchMapping("/{schoolId}/assign")
    public void assignToChapter(@PathVariable Long schoolId, @RequestBody @Valid AssignSchoolRequest request) {
        manageSchoolUseCase.assignToChapter(request.toCommand(schoolId));
    }

    @CheckAccess(resourceType = ResourceType.SCHOOL, permission = PermissionType.EDIT)
    @Override
    @PatchMapping("/{schoolId}/unassign")
    public void unassignFromChapter(@PathVariable Long schoolId, @RequestBody @Valid UnassignSchoolRequest request) {
        manageSchoolUseCase.unassignFromChapter(request.toCommand(schoolId));
    }
}
