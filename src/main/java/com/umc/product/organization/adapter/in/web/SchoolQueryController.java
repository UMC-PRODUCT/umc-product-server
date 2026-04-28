package com.umc.product.organization.adapter.in.web;

import com.umc.product.global.security.annotation.Public;
import com.umc.product.organization.adapter.in.web.dto.response.school.SchoolDetailResponse;
import com.umc.product.organization.adapter.in.web.dto.response.school.SchoolLinkResponse;
import com.umc.product.organization.adapter.in.web.dto.response.school.SchoolNameListResponse;
import com.umc.product.organization.adapter.in.web.dto.response.school.UnassignedSchoolListResponse;
import com.umc.product.organization.adapter.in.web.swagger.SchoolQueryControllerApi;
import com.umc.product.organization.application.port.in.query.GetSchoolUseCase;
import com.umc.product.organization.application.port.in.query.dto.school.SchoolDetailInfo;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/schools")
@RequiredArgsConstructor
public class SchoolQueryController implements SchoolQueryControllerApi {

    private final GetSchoolUseCase getSchoolUseCase;

    @Public
    @Override
    @Deprecated(since = "v2.0.0", forRemoval = true)
    @GetMapping("/link/{schoolId}")
    public SchoolLinkResponse getSchoolLink(@PathVariable Long schoolId) {
        return SchoolLinkResponse.of(getSchoolUseCase.getSchoolLink(schoolId));
    }

    @Public
    @GetMapping("/gisu/{gisuId}")
    public List<SchoolDetailInfo> getSchoolListsByGisu(@PathVariable Long gisuId) {
        return getSchoolUseCase.getSchoolListByGisuId(gisuId);
    }

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

    @Override
    @Deprecated(since = "v2.0.0", forRemoval = true)
    @GetMapping("/unassigned")
    public UnassignedSchoolListResponse getUnassignedSchools(@RequestParam Long gisuId) {
        return UnassignedSchoolListResponse.from(getSchoolUseCase.getUnassignedSchools(gisuId));
    }
}
