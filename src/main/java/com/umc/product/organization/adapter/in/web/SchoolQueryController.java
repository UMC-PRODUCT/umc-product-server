package com.umc.product.organization.adapter.in.web;

import com.umc.product.global.response.PageResponse;
import com.umc.product.organization.adapter.in.web.dto.request.SchoolListRequest;
import com.umc.product.organization.adapter.in.web.dto.response.SchoolDetailResponse;
import com.umc.product.organization.adapter.in.web.dto.response.SchoolListItemResponse;
import com.umc.product.organization.application.port.in.query.GetSchoolUseCase;
import com.umc.product.organization.application.port.in.query.dto.SchoolInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/schools")
@RequiredArgsConstructor
public class SchoolQueryController {

    private final GetSchoolUseCase getSchoolUseCase;

    @GetMapping
    public PageResponse<SchoolListItemResponse> getSchoolList(@ModelAttribute SchoolListRequest request,
                                                              Pageable pageable) {
        return PageResponse.of(getSchoolUseCase.getList(request.toCondition(), pageable), SchoolListItemResponse::of);
    }

    @GetMapping("/{schoolId}")
    public SchoolDetailResponse getSchoolDetail(@PathVariable Long schoolId) {

        SchoolInfo schoolInfo = getSchoolUseCase.getSchoolDetail(schoolId);

        return SchoolDetailResponse.of(schoolInfo);
    }
}
