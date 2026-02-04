package com.umc.product.organization.adapter.in.web;

import com.umc.product.organization.adapter.in.web.dto.response.SchoolLinkResponse;
import com.umc.product.organization.application.port.in.query.GetSchoolUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/schools")
@RequiredArgsConstructor
public class SchoolQueryController implements SchoolQueryControllerApi {

    private final GetSchoolUseCase getSchoolUseCase;

    @Override
    @GetMapping("/link/{schoolId}")
    public SchoolLinkResponse getSchoolLink(@PathVariable Long schoolId) {
        return SchoolLinkResponse.of(getSchoolUseCase.getSchoolLink(schoolId));
    }
}
