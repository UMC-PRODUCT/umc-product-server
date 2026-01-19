package com.umc.product.organization.adapter.in.web;

import com.umc.product.global.security.annotation.Public;
import com.umc.product.organization.adapter.in.web.dto.request.CreateSchoolRequest;
import com.umc.product.organization.adapter.in.web.dto.request.DeleteSchoolsRequest;
import com.umc.product.organization.adapter.in.web.dto.request.UpdateSchoolRequest;
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
@RequestMapping("/api/v1/admin/schools")
@RequiredArgsConstructor
public class SchoolController implements SchoolControllerApi {

    private final ManageSchoolUseCase manageSchoolUseCase;

    @Public
    @Override
    @PostMapping
    public void createSchool(@RequestBody @Valid CreateSchoolRequest request) {
        manageSchoolUseCase.register(request.toCommand());
    }

    @Public
    @Override
    @PatchMapping("/{schoolId}")
    public void updateSchool(@PathVariable Long schoolId, @RequestBody @Valid UpdateSchoolRequest request) {
        manageSchoolUseCase.updateSchool(schoolId, request.toCommand());
    }

    @Public
    @Override
    @DeleteMapping
    public void deleteSchools(@RequestBody @Valid DeleteSchoolsRequest request) {
        manageSchoolUseCase.deleteSchools(request.schoolIds());
    }
}

