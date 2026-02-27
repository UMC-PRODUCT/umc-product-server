package com.umc.product.notice.adapter.in.web.dto.request;

import com.umc.product.notice.application.port.in.query.dto.GetNoticeStatusQuery;
import com.umc.product.notice.domain.enums.NoticeReadStatus;
import com.umc.product.notice.domain.enums.NoticeReadStatusFilterType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.List;

@Schema(description = "공지 읽음 현황 조회 요청 (커서 기반 페이징)")
public record GetNoticeStatusRequest(
    @Schema(description = "페이징 커서 ID. 첫 페이지 요청 시 null. "
        + "status=READ일 때는 마지막 NoticeRead의 ID, status=UNREAD일 때는 마지막 Challenger의 ID를 넘겨주세요",
        nullable = true)
    Long cursorId,

    @Schema(description = "조직 필터 타입. ALL=전체, CHAPTER=지부별 필터, SCHOOL=학교별 필터. "
        + "CHAPTER/SCHOOL 선택 시 organizationIds에 해당 ID 목록을 함께 전달해야 합니다",
        example = "ALL")
    @NotNull(message = "필터 타입은 비어 있을 수 없습니다.")
    NoticeReadStatusFilterType filterType,

    @Schema(description = "필터링할 조직(지부/학교) ID 목록. filterType이 ALL이면 null 또는 빈 배열. "
        + "CHAPTER이면 지부 ID 목록, SCHOOL이면 학교 ID 목록을 전달",
        example = "[3, 4]", nullable = true)
    List<Long> organizationIds,

    @Schema(description = "읽음 상태 필터. READ=읽은 사람 목록 조회, UNREAD=안 읽은 사람 목록 조회",
        example = "UNREAD")
    @NotNull(message = "읽음 상태는 비어 있을 수 없습니다.")
    NoticeReadStatus status
) {
    public GetNoticeStatusQuery toQuery(Long noticeId) {
        return new GetNoticeStatusQuery(cursorId, noticeId, filterType, organizationIds, status);
    }
}
