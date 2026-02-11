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
        description = "첫 공지 생성 시 공지사항 이미지를 추가하는 API입니다. 파일 업로드 API로 먼저 이미지를 업로드한 뒤, 받은 이미지 ID를 전달하세요."
    )
    ApiResponse<AddNoticeImagesResponse> addNoticeImages(
        @Parameter(description = "공지사항 ID", required = true, example = "1")
        @PathVariable Long noticeId,

        @RequestBody @Valid AddNoticeImagesRequest request
    );

    @Operation(
        summary = "첫 공지 생성 시 공지사항 링크를 추가하는 API입니다. "
    )
    ApiResponse<AddNoticeLinksResponse> addNoticeLinks(
        @Parameter(description = "공지사항 ID", required = true, example = "1")
        @PathVariable Long noticeId,

        @RequestBody @Valid AddNoticeLinksRequest request
    );

    @Operation(
        summary = "첫 공지 생성 시 공지사항 투표를 추가하는 API입니다.",
        description = "투표 생성 API로 먼저 투표를 생성한 뒤, 받은 투표 ID를 전달하세요."
    )
    ApiResponse<AddNoticeVotesResponse> addNoticeVotes(
        @Parameter(description = "공지사항 ID", required = true, example = "1")
        @PathVariable Long noticeId,

        @RequestBody @Valid AddNoticeVotesRequest request
    );

    @Operation(
        summary = "공지사항 이미지 전체 수정",
        description = "요청받은 새 목록으로 교체합니다. 빈 배열([])을 보내면 모든 이미지가 삭제됩니다."
    )
    ApiResponse<ReplaceNoticeImagesResponse> replaceNoticeImages(
        @Parameter(description = "공지사항 ID", required = true, example = "1")
        @PathVariable Long noticeId,

        @RequestBody @Valid ReplaceNoticeImagesRequest request
    );

    @Operation(
        summary = "공지사항 링크 전체 수정",
        description = "요청받은 새 목록으로 교체합니다. 빈 배열([])을 보내면 모든 링크가 삭제됩니다."
    )
    ApiResponse<ReplaceNoticeLinksResponse> replaceNoticeLinks(
        @Parameter(description = "공지사항 ID", required = true, example = "1")
        @PathVariable Long noticeId,

        @RequestBody @Valid ReplaceNoticeLinksRequest request
    );

    @Operation(
        summary = "공지사항 투표 전체 수정",
        description = "요청받은 새 목록으로 교체합니다. 빈 배열([])을 보내면 모든 투표가 삭제됩니다."
    )
    ApiResponse<ReplaceNoticeVotesResponse> replaceNoticeVotes(
        @Parameter(description = "공지사항 ID", required = true, example = "1")
        @PathVariable Long noticeId,

        @RequestBody @Valid ReplaceNoticeVotesRequest request
    );

}
