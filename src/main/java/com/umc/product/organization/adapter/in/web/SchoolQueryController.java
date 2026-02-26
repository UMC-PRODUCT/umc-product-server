package com.umc.product.organization.adapter.in.web;

import com.umc.product.global.security.annotation.Public;
import com.umc.product.organization.adapter.in.web.dto.response.SchoolLinkResponse;
import com.umc.product.organization.adapter.in.web.swagger.SchoolQueryControllerApi;
import com.umc.product.organization.application.port.in.query.GetSchoolUseCase;
import com.umc.product.organization.application.port.in.query.dto.SchoolDetailInfo;
import java.util.List;
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

    @Public
    @Override
    @GetMapping("/link/{schoolId}")
    public SchoolLinkResponse getSchoolLink(@PathVariable Long schoolId) {
        return SchoolLinkResponse.of(getSchoolUseCase.getSchoolLink(schoolId));
    }

    @Public
    @GetMapping("/gisu/{gisuId}")
    public List<SchoolDetailInfo> getSchoolListsByGisu(@PathVariable Long gisuId) {
        return getSchoolUseCase.getSchoolListByGisuId(gisuId);
    }
}
