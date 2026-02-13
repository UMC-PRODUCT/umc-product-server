package com.umc.product.notice.adapter.in.web;

import com.umc.product.global.constant.SwaggerTag.Constants;
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
import com.umc.product.notice.adapter.in.web.swagger.NoticeContentApi;
import com.umc.product.notice.application.port.in.command.ManageNoticeContentUseCase;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/v1/notices")
@RequiredArgsConstructor
@Tag(name = Constants.NOTICE)
public class NoticeContentController implements NoticeContentApi {

    private final ManageNoticeContentUseCase manageNoticeContentUseCase;

    // 공지사항에 이미지 추가
    @PostMapping("/{noticeId}/images")
    public ApiResponse<AddNoticeImagesResponse> addNoticeImages(
        @PathVariable("noticeId") Long noticeId,
        @RequestBody @Valid AddNoticeImagesRequest request) {

        List<Long> imageIds = manageNoticeContentUseCase.addImages(request.toCommand(), noticeId);

        return ApiResponse.onSuccess(new AddNoticeImagesResponse(imageIds));
    }

    // 공지사항에 링크 추가
    @PostMapping("/{noticeId}/links")
    public ApiResponse<AddNoticeLinksResponse> addNoticeLinks(
        @PathVariable("noticeId") Long noticeId,
        @RequestBody @Valid AddNoticeLinksRequest request) {

        List<Long> linkIds = manageNoticeContentUseCase.addLinks(request.toCommand(), noticeId);

        return ApiResponse.onSuccess(new AddNoticeLinksResponse(linkIds));
    }

    // 공지사항에 투표 추가
    @PostMapping("/{noticeId}/votes")
    public ApiResponse<AddNoticeVoteResponse> addNoticeVote(
        @PathVariable("noticeId") Long noticeId,
        @RequestBody @Valid AddNoticeVoteRequest request,
        @CurrentMember MemberPrincipal memberPrincipal) {

        var result = manageNoticeContentUseCase.addVote(
            request.toCommand(memberPrincipal.getMemberId()), noticeId);

        return ApiResponse.onSuccess(AddNoticeVoteResponse.from(result));
    }

    // 공지사항 이미지 전체 수정
    @PatchMapping("/{noticeId}/images")
    public void replaceNoticeImages(
        @PathVariable("noticeId") Long noticeId,
        @RequestBody @Valid ReplaceNoticeImagesRequest request) {

        manageNoticeContentUseCase.replaceImages(request.toCommand(), noticeId);
    }

    // 공지사항 링크 전체 수정
    @PatchMapping("/{noticeId}/links")
    public void replaceNoticeLinks(
        @PathVariable("noticeId") Long noticeId,
        @RequestBody @Valid ReplaceNoticeLinksRequest request) {

        manageNoticeContentUseCase.replaceLinks(request.toCommand(), noticeId);
    }

}
