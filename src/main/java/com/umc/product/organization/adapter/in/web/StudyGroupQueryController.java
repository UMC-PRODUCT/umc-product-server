package com.umc.product.organization.adapter.in.web;

import com.umc.product.organization.application.port.in.query.GetStudyGroupsUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/study-groups")
@RequiredArgsConstructor
public class StudyGroupQueryController {

    GetStudyGroupsUseCase getStudyGroupsUseCase;

//    @GetMapping()
//    public CursorResponse<StudyGroupListResponse> getStudyGroups(@RequestParam(required = false) Long cursor,
//                                                                 @RequestParam(required = false, defaultValue = "10") int size) {
//
//        getStudyGroupsUseCase.getStudyGroups();
//    }


}
