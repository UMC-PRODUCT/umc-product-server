package com.umc.product.organization.adapter.in.web.v2;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.umc.product.global.security.annotation.Public;
import com.umc.product.organization.adapter.in.web.v2.dto.request.GisuOrganizationQueryRequest;
import com.umc.product.organization.adapter.in.web.v2.dto.response.GisuOrganizationV2Response;
import com.umc.product.organization.application.port.in.query.GetGisuOrganizationUseCase;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v2/gisu")
@RequiredArgsConstructor
@Tag(name = "Organization V2 | 기수 Query", description = "기수 기준 조직 정보를 조회합니다.")
public class GisuQueryV2Controller {

    private final GetGisuOrganizationUseCase getGisuOrganizationUseCase;

    @Public
    @GetMapping
    @Operation(operationId = "GISU-201", summary = "기수 조직 정보 조회", description = """
        `id`, `generation`, `active` 중 정확히 하나의 조회 기준으로 기수 조직 정보를 조회합니다.

        - `id`, `generation`은 반복 query param으로 여러 값을 전달할 수 있으며 중복 값은 제거됩니다.
        - `active=true`는 활성 기수 1개를 조회합니다. `active=false`는 유효하지 않은 조회 기준입니다.
        - `includeChapter=true`이면 기수 내 지부 정보를 포함합니다.
        - `includeSchool=true`이면 기수 내 학교 정보를 포함합니다.
        - `includeChapter=true&includeSchool=true`이면 지부 내 학교 정보와 기수 내 학교 정보를 모두 포함합니다.
        """)
    public GisuOrganizationV2Response getGisus(
        @ModelAttribute GisuOrganizationQueryRequest request
    ) {
        return GisuOrganizationV2Response.from(getGisuOrganizationUseCase.get(request.toQuery()));
    }
}
