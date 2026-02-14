package com.umc.product.notice.adapter.in.web.swagger;

import com.umc.product.global.response.ApiResponse;
import com.umc.product.global.security.MemberPrincipal;
import com.umc.product.global.security.annotation.CurrentMember;
import com.umc.product.notice.adapter.in.web.dto.request.AddNoticeImagesRequest;
import com.umc.product.notice.adapter.in.web.dto.request.AddNoticeLinksRequest;
import com.umc.product.notice.adapter.in.web.dto.request.AddNoticeVoteRequest;
import com.umc.product.notice.adapter.in.web.dto.request.ReplaceNoticeImagesRequest;
import com.umc.product.notice.adapter.in.web.dto.request.ReplaceNoticeLinksRequest;
import com.umc.product.notice.adapter.in.web.dto.response.command.AddNoticeImagesResponse;
import com.umc.product.notice.adapter.in.web.dto.response.command.AddNoticeLinksResponse;
import com.umc.product.notice.adapter.in.web.dto.response.command.AddNoticeVoteResponse;
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

        @RequestBody @Valid AddNoticeImagesRequest request,

        @CurrentMember MemberPrincipal memberPrincipal
    );

    @Operation(
        summary = "첫 공지 생성 시 공지사항 링크를 추가하는 API입니다. "
    )
    ApiResponse<AddNoticeLinksResponse> addNoticeLinks(
        @Parameter(description = "공지사항 ID", required = true, example = "1")
        @PathVariable Long noticeId,

        @RequestBody @Valid AddNoticeLinksRequest request,

        @CurrentMember MemberPrincipal memberPrincipal
    );

    @Operation(
        summary = "공지사항 투표 추가",
        description = "공지사항에 투표를 1개 생성하여 연결합니다. 투표 생성과 공지 연결이 한 번에 처리됩니다."
    )
    ApiResponse<AddNoticeVoteResponse> addNoticeVote(
        @Parameter(description = "공지사항 ID", required = true, example = "1")
        @PathVariable Long noticeId,

        @RequestBody @Valid AddNoticeVoteRequest request,

        MemberPrincipal memberPrincipal
    );

    @Operation(
        summary = "공지사항 이미지 전체 수정",
        description = "요청받은 새 목록으로 교체합니다. 빈 배열([])을 보내면 모든 이미지가 삭제됩니다."
    )
    void replaceNoticeImages(
        @Parameter(description = "공지사항 ID", required = true, example = "1")
        @PathVariable Long noticeId,

        @RequestBody @Valid ReplaceNoticeImagesRequest request,

        @CurrentMember MemberPrincipal memberPrincipal
    );

    @Operation(
        summary = "공지사항 링크 전체 수정",
        description = "요청받은 새 목록으로 교체합니다. 빈 배열([])을 보내면 모든 링크가 삭제됩니다."
    )
    void replaceNoticeLinks(
        @Parameter(description = "공지사항 ID", required = true, example = "1")
        @PathVariable Long noticeId,

        @RequestBody @Valid ReplaceNoticeLinksRequest request,

        @CurrentMember MemberPrincipal memberPrincipal
    );

    @Operation(
        summary = "공지사항 투표 삭제",
        description = "공지사항 수정시 필요한 경우 해당 공지에 연결된 투표를 삭제합니다. 공지사항과 투표의 연결도 함께 제거됩니다."
    )
    void deleteNoticeVote(
        @Parameter(description = "공지사항 ID", required = true, example = "1")
        @PathVariable Long noticeId,

        MemberPrincipal memberPrincipal
    );

}
