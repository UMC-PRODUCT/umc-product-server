package com.umc.product.organization.adapter.in.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.umc.product.global.security.annotation.Public;
import com.umc.product.organization.adapter.in.web.dto.response.school.SchoolDetailResponse;
import com.umc.product.organization.adapter.in.web.dto.response.school.SchoolNameListResponse;
import com.umc.product.organization.adapter.in.web.swagger.SchoolQueryControllerApi;
import com.umc.product.organization.application.port.in.query.GetSchoolUseCase;
import com.umc.product.organization.application.port.in.query.dto.school.SchoolDetailInfo;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/schools")
@RequiredArgsConstructor
public class SchoolQueryController implements SchoolQueryControllerApi {

    private final GetSchoolUseCase getSchoolUseCase;

    @Public
    @Override
    @GetMapping("/all")
    public SchoolNameListResponse getAllSchools() {
        return SchoolNameListResponse.from(getSchoolUseCase.getAllSchoolNames());
    }

    @Public
    @Override
    @GetMapping("/{schoolId}")
    public SchoolDetailResponse getSchoolDetail(@PathVariable Long schoolId) {
        SchoolDetailInfo schoolDetailInfo = getSchoolUseCase.getSchoolDetail(schoolId);
        return SchoolDetailResponse.of(schoolDetailInfo);
    }

}
