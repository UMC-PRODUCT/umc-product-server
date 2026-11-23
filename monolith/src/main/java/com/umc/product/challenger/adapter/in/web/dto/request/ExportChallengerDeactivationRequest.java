package com.umc.product.challenger.adapter.in.web.dto.request;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.umc.product.challenger.application.port.in.query.dto.ExportChallengerDeactivationQuery;
import com.umc.product.challenger.application.port.in.query.dto.ExportChallengerDeactivationQuery.Column;
import com.umc.product.challenger.application.port.in.query.dto.ExportChallengerDeactivationQuery.Type;
import com.umc.product.challenger.application.port.in.query.dto.SearchChallengerDeactivationQuery;
import com.umc.product.challenger.domain.enums.ChallengerDeactivationListStatus;
import com.umc.product.challenger.domain.enums.ChallengerDeactivationReason;
import com.umc.product.common.domain.enums.ChallengerPart;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record ExportChallengerDeactivationRequest(
    @Positive @Schema(description = "조회 기수 ID. 생략하면 활성 기수") Long gisuId,
    @Positive Long chapterId,
    @Positive Long schoolId,
    ChallengerPart part,
    @Size(max = 100) String keyword,
    ChallengerDeactivationReason reason,
    @Schema(description = "복수 선택 가능. 생략하거나 비우면 모든 탈부 유형")
    Set<@NotNull Type> types,
    @Schema(description = "한국 날짜 기준 시작일(포함). 전체 내보내기에서 필수", example = "2026-09-01")
    LocalDate startDate,
    @Schema(description = "한국 날짜 기준 종료일(포함). 전체 내보내기에서 필수", example = "2026-11-30")
    LocalDate endDate,
    @Size(max = ExportChallengerDeactivationQuery.MAX_SELECTED_IDS) @Schema(description = "선택 행 탈부 ID. 비워 두면 전체 내보내기")
    List<@NotNull @Positive Long> deactivationIds,
    @Size(min = 1) @Schema(description = "포함할 열 묶음. 생략하면 소속, 사유, 날짜, 등록자 역할")
    Set<@NotNull Column> columns
) {
    public ExportChallengerDeactivationQuery toQuery() {
        return new ExportChallengerDeactivationQuery(
            new SearchChallengerDeactivationQuery(
                gisuId, chapterId, schoolId, part, keyword, reason, ChallengerDeactivationListStatus.REGISTERED
            ),
            startDate, endDate, deactivationIds == null ? Set.of() : new HashSet<>(deactivationIds), types, columns
        );
    }
}
