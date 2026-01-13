package com.umc.product.organization.adapter.in.web;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/schools")
@RequiredArgsConstructor
public class SchoolQueryController {

//    private final GetSchoolUseCase getSchoolUseCase;
//
//    @GetMapping
//    public PageResponse<SchoolListItemResponse> getSchoolList(@ModelAttribute SchoolListRequest request,
//                                                              Pageable pageable) {
//        return PageResponse.of(getSchoolUseCase.getList(request.toCondition(), pageable), SchoolListItemResponse::of);
//    }
//
//    @GetMapping("/{schoolId}")
//    public SchoolDetailResponse getSchoolDetail(@PathVariable Long schoolId) {
//
//        SchoolInfo schoolInfo = getSchoolUseCase.getSchoolDetail(schoolId);
//
//        return SchoolDetailResponse.of(schoolInfo);
//    }
}
