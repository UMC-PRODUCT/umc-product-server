package com.umc.product.organization.adapter.in.web;

import com.umc.product.global.response.PageResponse;
import com.umc.product.organization.adapter.in.web.dto.request.SchoolListRequest;
import com.umc.product.organization.adapter.in.web.dto.response.SchoolDetailResponse;
import com.umc.product.organization.adapter.in.web.dto.response.SchoolListItemResponse;
import com.umc.product.organization.adapter.in.web.dto.response.SchoolPageResponse;
import com.umc.product.organization.adapter.in.web.dto.response.UnassignedSchoolListResponse;
import com.umc.product.organization.application.port.in.query.GetSchoolUseCase;
import com.umc.product.organization.application.port.in.query.dto.SchoolInfo;
import com.umc.product.storage.application.port.in.query.GetFileUseCase;
import com.umc.product.storage.application.port.in.query.dto.FileInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/schools")
@RequiredArgsConstructor
public class AdminSchoolQueryController implements AdminSchoolQueryControllerApi {

    private final GetSchoolUseCase getSchoolUseCase;
    private final GetFileUseCase getFileUseCase;

    @Override
    @GetMapping
    public SchoolPageResponse getSchools(@ModelAttribute SchoolListRequest request, Pageable pageable) {
        // Swagger를 위한 SchoolPageResponse 매핑
        PageResponse<SchoolListItemResponse> pageResponse = PageResponse.of(
                getSchoolUseCase.getSchools(request.toCondition(), pageable),
                SchoolListItemResponse::of
        );
        return SchoolPageResponse.from(pageResponse);
    }

    @Override
    @GetMapping("/{schoolId}")
    public SchoolDetailResponse getSchoolDetail(@PathVariable Long schoolId) {
        SchoolInfo schoolInfo = getSchoolUseCase.getSchoolDetail(schoolId);
        FileInfo fileInfo = getFileUseCase.getById(schoolInfo.logoImageId());
        return SchoolDetailResponse.of(schoolInfo, fileInfo.fileLink());
    }

    @Override
    @GetMapping("/unassigned")
    public UnassignedSchoolListResponse getUnassignedSchools(@RequestParam Long gisuId) {
        return UnassignedSchoolListResponse.from(getSchoolUseCase.getUnassignedSchools(gisuId));
    }
}

