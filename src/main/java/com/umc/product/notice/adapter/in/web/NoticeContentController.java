package com.umc.product.notice.adapter.in.web;

import com.umc.product.global.constant.SwaggerTag.Constants;
import com.umc.product.global.response.ApiResponse;
import com.umc.product.notice.adapter.in.web.dto.request.AddNoticeImagesRequest;
import com.umc.product.notice.adapter.in.web.dto.request.AddNoticeLinksRequest;
import com.umc.product.notice.adapter.in.web.dto.request.AddNoticeVotesRequest;
import com.umc.product.notice.adapter.in.web.dto.response.AddNoticeImagesResponse;
import com.umc.product.notice.adapter.in.web.dto.response.AddNoticeLinksResponse;
import com.umc.product.notice.adapter.in.web.dto.response.AddNoticeVotesResponse;
import com.umc.product.notice.adapter.in.web.swagger.NoticeContentApi;
import com.umc.product.notice.application.port.in.command.ManageNoticeContentUseCase;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
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

    @PostMapping("/{noticeId}/images")
    public ApiResponse<AddNoticeImagesResponse> addNoticeImages(@PathVariable Long noticeId, @RequestBody @Valid AddNoticeImagesRequest request) {
        List<Long> imageIds = manageNoticeContentUseCase.addImages(request.toCommand(), noticeId);
        return ApiResponse.onSuccess(new AddNoticeImagesResponse(imageIds));
    }

    @PostMapping("/{noticeId}/links")
    public ApiResponse<AddNoticeLinksResponse> addNoticeLinks(@PathVariable Long noticeId, @RequestBody @Valid AddNoticeLinksRequest request) {
        List<Long> linkIds = manageNoticeContentUseCase.addLinks(request.toCommand(), noticeId);
        return ApiResponse.onSuccess(new AddNoticeLinksResponse(linkIds));
    }

    @PostMapping("/{noticeId}/votes")
    public ApiResponse<AddNoticeVotesResponse> addNoticeVotes(@PathVariable Long noticeId, @RequestBody @Valid AddNoticeVotesRequest request) {
        List<Long> voteIds = manageNoticeContentUseCase.addVotes(request.toCommand(), noticeId);
        return ApiResponse.onSuccess(new AddNoticeVotesResponse(voteIds));
    }

}
