package com.umc.product.recruitment.adapter.in.web;

import com.umc.product.global.constant.SwaggerTag;
import com.umc.product.global.security.MemberPrincipal;
import com.umc.product.global.security.annotation.CurrentMember;
import com.umc.product.recruitment.adapter.in.web.dto.response.ApplicationListForAdminResponse;
import com.umc.product.recruitment.application.port.in.PartOption;
import com.umc.product.recruitment.application.port.in.query.GetApplicationListForAdminUseCase;
import com.umc.product.recruitment.application.port.in.query.dto.ApplicationListForAdminInfo;
import com.umc.product.recruitment.application.port.in.query.dto.GetApplicationListForAdminQuery;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/recruitments/{recruitmentId}")
@Tag(name = SwaggerTag.Constants.RECRUITMENT_ADMIN)
public class RecruitmentAdminApplicationController {

    private final GetApplicationListForAdminUseCase getApplicationListForAdminUseCase;

    @GetMapping("/applications")
    @Operation(
            summary = "(총괄) 지원자 관리 리스트 조회",
            description = """
                    지부/학교/파트/키워드 조건으로 지원자(지원서) 목록을 조회합니다.
                    서류/면접 평가 점수 및 최종 결과 상태를 함께 내려줍니다.
                    정렬은 지원 시간(지원서 제출 시각) 오름차순 기준입니다.
                    """
    )
    public ApplicationListForAdminResponse getAdminApplications(
            @PathVariable Long recruitmentId,
            @RequestParam(required = false) Long chapterId,
            @RequestParam(required = false) Long schoolId,
            @RequestParam(required = false, defaultValue = "ALL") PartOption part,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false, defaultValue = "1") int page,
            @RequestParam(required = false, defaultValue = "20") int limit,
            @CurrentMember MemberPrincipal memberPrincipal
    ) {
        ApplicationListForAdminInfo info = getApplicationListForAdminUseCase.get(
                new GetApplicationListForAdminQuery(
                        recruitmentId,
                        chapterId,
                        schoolId,
                        part,
                        keyword,
                        page,
                        limit,
                        memberPrincipal.getMemberId()
                )
        );
        return ApplicationListForAdminResponse.from(info);
    }
}
