package com.umc.product.challenger.adapter.in.web;

import java.nio.charset.StandardCharsets;

import org.springframework.http.CacheControl;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.umc.product.challenger.adapter.in.web.dto.request.ExportChallengerDeactivationRequest;
import com.umc.product.challenger.application.port.in.query.ExportChallengerDeactivationUseCase;
import com.umc.product.global.security.MemberPrincipal;
import com.umc.product.global.security.annotation.CurrentMember;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/challenger/admin/deactivations")
@Tag(name = "Challenger | 탈부 관리 Query", description = "관리자용 챌린저 탈부 조회 기능을 제공합니다.")
public class ChallengerDeactivationExportController {

    private final ExportChallengerDeactivationUseCase exportChallengerDeactivationUseCase;

    @PostMapping("/export")
    @Operation(
        operationId = "CHALLENGER-107",
        summary = "탈부 기록 CSV 내보내기",
        description = """
            실제 탈부 등록 기록을 최신순으로 최대 5만 건 내려받습니다. 조회 범위는 탈부 목록과 같습니다.
            - 전체 내보내기에는 한국 날짜 기준 시작일과 종료일이 필요하며 양 끝 날짜를 포함합니다.
            - 탈부 ID를 지정하면 선택 행과 조회 조건에 모두 맞는 기록만 내려받습니다. 최대 1,000개를 선택할 수 있습니다.
            - 이름·닉네임과 메모는 해당 열을 명시적으로 고를 때만 포함합니다.
            - 등록자 역할은 등록 시 보존하지 않아 빈 값이며, 탈부 날짜는 년·월·일을 내립니다.
            - 내보낼 기록이 없으면 열 제목만 담긴 CSV를 반환합니다.
            - 파일 인코딩은 UTF-8 BOM입니다.
            """,
        responses = @ApiResponse(
            responseCode = "200",
            content = @Content(mediaType = "text/csv", schema = @Schema(type = "string", format = "binary"))
        )
    )
    public ResponseEntity<byte[]> exportCsv(
        @Valid @RequestBody ExportChallengerDeactivationRequest request,
        @CurrentMember MemberPrincipal memberPrincipal
    ) {
        byte[] csv = exportChallengerDeactivationUseCase.exportCsv(memberPrincipal.getMemberId(), request.toQuery());
        return ResponseEntity.ok()
            .cacheControl(CacheControl.noStore())
            .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                .filename("challenger-deactivations.csv")
                .build().toString())
            .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
            .body(csv);
    }
}
