package com.umc.product.organization.adapter.in.web;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/schools")
@RequiredArgsConstructor
public class SchoolController {

//    private final ManageSchoolUseCase manageSchoolUseCase;
//
//    @PostMapping
//    public void createSchool(@RequestBody @Valid CreateSchoolRequest createSchoolRequest) {
//        manageSchoolUseCase.register(createSchoolRequest.toCommand());
//    }
//
//    @PatchMapping("/{schoolId}")
//    public void updateSchool(@PathVariable Long schoolId, @RequestBody @Valid UpdateSchoolRequest updateSchoolRequest) {
//        manageSchoolUseCase.updateSchool(updateSchoolRequest.toCommand());
//    }
//
//    @DeleteMapping("/{schoolId}")
//    public void deleteSchool(@PathVariable Long schoolId) {
//        manageSchoolUseCase.deleteSchool(schoolId);
//    }
//
//    @DeleteMapping
//    public void deleteSchools(@RequestBody @Valid DeleteSchoolsRequest request) {
//        manageSchoolUseCase.deleteSchools(request.schoolIds());
//    }
}

