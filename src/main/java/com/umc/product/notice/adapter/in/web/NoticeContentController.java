package com.umc.product.notice.adapter.in.web;

import com.umc.product.common.dto.ChallengerContext;
import com.umc.product.global.constant.SwaggerTag.Constants;
import com.umc.product.notice.adapter.in.web.dto.request.AddNoticeImagesRequest;
import com.umc.product.notice.adapter.in.web.dto.request.AddNoticeLinksRequest;
import com.umc.product.notice.adapter.in.web.dto.request.AddNoticeVoteRequest;
import com.umc.product.notice.application.port.in.command.ManageNoticeContentUseCase;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/v1/notices")
@RequiredArgsConstructor
@Tag(name = Constants.NOTICE)
public class NoticeContentController {

    private final ManageNoticeContentUseCase manageNoticeContentUseCase;

    @PostMapping("/images")
    public void addNoticeImages(@RequestBody @Valid AddNoticeImagesRequest request, ChallengerContext context) {
        manageNoticeContentUseCase.addImages(request.toCommand(), context);
    }

    @PostMapping("/links")
    public void addNoticeLinks(@RequestBody @Valid AddNoticeLinksRequest request, ChallengerContext context) {
        manageNoticeContentUseCase.addLinks(request.toCommand(), context);
    }

    @PostMapping("/votes")
    public void addNoticeVotes(@RequestBody @Valid AddNoticeVoteRequest request, ChallengerContext context) {
        manageNoticeContentUseCase.addVote(request.toCommand(), context);
    }

}
