package com.umc.product.challenger.adapter.in.web;

import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.umc.product.challenger.adapter.in.web.dto.request.CheckChallengerReadmissionRequest;
import com.umc.product.challenger.application.port.in.query.CheckChallengerReadmissionUseCase;
import com.umc.product.challenger.application.port.in.query.dto.ChallengerReadmissionInfo;
import com.umc.product.global.security.MemberPrincipal;
import com.umc.product.global.security.annotation.CurrentMember;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/challenger/admin/deactivations")
@RequiredArgsConstructor
@Tag(name = "Challenger | 탈부 관리 Query")
public class ChallengerReadmissionController {

    private final CheckChallengerReadmissionUseCase checkChallengerReadmissionUseCase;

    @PostMapping("/readmission-check")
    @Operation(
        operationId = "CHALLENGER-108",
        summary = "지원자 이메일로 재가입 제한 확인",
        description = """
            활성 기수 교내 회장·부회장, 중앙운영진과 SUPER_ADMIN이 지원자 이메일로 제한 여부를 확인합니다.
            학교와 관계없이 최신 탈부 등록 건의 기수 번호, 상태, 한국 기준 등록 날짜를 반환합니다.
            탈부 등록 시각부터 1년 동안 restricted가 true이며 5년 안에 등록한 이력만 확인합니다.
            사유, 메모, 학교, 이메일은 반환하지 않습니다.
            기록이 없으면 hasHistory와 restricted는 false이고 mostRecent는 비어 있습니다.
            """
    )
    public ChallengerReadmissionInfo checkReadmission(
        @Valid @RequestBody CheckChallengerReadmissionRequest request,
        @CurrentMember MemberPrincipal memberPrincipal,
        HttpServletResponse response
    ) {
        response.setHeader(HttpHeaders.CACHE_CONTROL, CacheControl.noStore().getHeaderValue());
        return checkChallengerReadmissionUseCase.checkReadmission(memberPrincipal.getMemberId(), request.email());
    }
}
