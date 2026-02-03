package com.umc.product.notice.adapter.in.web.swagger;

import com.umc.product.global.response.ApiResponse;
import com.umc.product.notice.adapter.in.web.dto.request.AddNoticeImagesRequest;
import com.umc.product.notice.adapter.in.web.dto.request.AddNoticeLinksRequest;
import com.umc.product.notice.adapter.in.web.dto.request.AddNoticeVotesRequest;
import com.umc.product.notice.adapter.in.web.dto.request.ReplaceNoticeImagesRequest;
import com.umc.product.notice.adapter.in.web.dto.request.ReplaceNoticeLinksRequest;
import com.umc.product.notice.adapter.in.web.dto.request.ReplaceNoticeVotesRequest;
import com.umc.product.notice.adapter.in.web.dto.response.command.AddNoticeImagesResponse;
import com.umc.product.notice.adapter.in.web.dto.response.command.AddNoticeLinksResponse;
import com.umc.product.notice.adapter.in.web.dto.response.command.AddNoticeVotesResponse;
import com.umc.product.notice.adapter.in.web.dto.response.command.ReplaceNoticeImagesResponse;
import com.umc.product.notice.adapter.in.web.dto.response.command.ReplaceNoticeLinksResponse;
import com.umc.product.notice.adapter.in.web.dto.response.command.ReplaceNoticeVotesResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

public interface NoticeContentApi {

    @Operation(
        summary = "공지사항 이미지 추가",
        description = "공지사항에 이미지를 추가합니다."
    )
    ApiResponse<AddNoticeImagesResponse> addNoticeImages(
        @Parameter(description = "공지사항 ID", required = true)
        @PathVariable Long noticeId,

        @Parameter(description = "추가할 이미지 정보", required = true)
        @RequestBody @Valid AddNoticeImagesRequest request
    );

    @Operation(
        summary = "공지사항 링크 추가",
        description = "공지사항에 링크를 추가합니다."
    )
    ApiResponse<AddNoticeLinksResponse> addNoticeLinks(
        @Parameter(description = "공지사항 ID", required = true)
        @PathVariable Long noticeId,

        @Parameter(description = "추가할 링크 정보", required = true)
        @RequestBody @Valid AddNoticeLinksRequest request
    );

    @Operation(
        summary = "공지사항 투표 추가",
        description = "공지사항에 투표를 추가합니다."
    )
    ApiResponse<AddNoticeVotesResponse> addNoticeVotes(
        @Parameter(description = "공지사항 ID", required = true)
        @PathVariable Long noticeId,

        @Parameter(description = "추가할 투표 정보", required = true)
        @RequestBody @Valid AddNoticeVotesRequest request
    );

    @Operation(
        summary = "공지사항 이미지 수정",
        description = "공지사항 이미지를 전체 수정합니다."
    )
    ApiResponse<ReplaceNoticeImagesResponse> replaceNoticeImages(
        @Parameter(description = "공지사항 ID", required = true)
        @PathVariable Long noticeId,

        @Parameter(description = "수정할 이미지 정보", required = true)
        @RequestBody @Valid ReplaceNoticeImagesRequest request
    );

    @Operation(
        summary = "공지사항 링크 수정",
        description = "공지사항 링크를 전체 수정합니다."
    )
    ApiResponse<ReplaceNoticeLinksResponse> replaceNoticeLinks(
        @Parameter(description = "공지사항 ID", required = true)
        @PathVariable Long noticeId,

        @Parameter(description = "수정할 링크 정보", required = true)
        @RequestBody @Valid ReplaceNoticeLinksRequest request
    );

    @Operation(
        summary = "공지사항 투표 수정",
        description = "공지사항 투표를 전체 수정합니다."
    )
    ApiResponse<ReplaceNoticeVotesResponse> replaceNoticeVotes(
        @Parameter(description = "공지사항 ID", required = true)
        @PathVariable Long noticeId,

        @Parameter(description = "수정할 투표 정보", required = true)
        @RequestBody @Valid ReplaceNoticeVotesRequest request
    );

    @Operation(
        summary = "공지사항 콘텐츠 전체 삭제",
        description = "공지사항의 이미지/링크/투표를 모두 삭제합니다."
    )
    void removeNoticeContents(
        @Parameter(description = "공지사항 ID", required = true)
        @PathVariable Long noticeId
    );
}
