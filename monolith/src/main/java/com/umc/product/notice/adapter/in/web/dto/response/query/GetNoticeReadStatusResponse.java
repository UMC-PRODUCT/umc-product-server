package com.umc.product.notice.adapter.in.web.dto.response.query;

import java.util.Objects;

import com.umc.product.notice.application.port.in.query.dto.NoticeReadStatusInfo;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 공지사항 수신 현황 확인 응답
 * <p>
 * Cursor 페이지네이션으로 응답함
 */
public record GetNoticeReadStatusResponse(
    Long challengerId,
    String name,
    String profileImageUrl,
    @Schema(description = "파트 코드. 수강 파트가 없으면 빈 문자열") String part,
    Long schoolId,
    String schoolName,
    Long chapterId,
    String chapterName
) {
    public static GetNoticeReadStatusResponse from(NoticeReadStatusInfo info) {
        return new GetNoticeReadStatusResponse(
            info.challengerId(),
            Objects.toString(info.name(), ""),
            info.profileImageUrl(),
            info.part() == null ? "" : info.part().name(),
            info.schoolId(),
            Objects.toString(info.schoolName(), ""),
            info.chapterId(),
            Objects.toString(info.chapterName(), "")
        );
    }
}
