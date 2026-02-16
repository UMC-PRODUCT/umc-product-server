package com.umc.product.community.adapter.in.web;

import com.umc.product.challenger.application.port.in.query.GetChallengerUseCase;
import com.umc.product.challenger.application.port.in.query.dto.ChallengerInfoWithStatus;
import com.umc.product.community.application.port.in.report.ReportCommentCommand;
import com.umc.product.community.application.port.in.report.ReportCommentUseCase;
import com.umc.product.community.application.port.in.report.ReportPostCommand;
import com.umc.product.community.application.port.in.report.ReportPostUseCase;
import com.umc.product.global.security.MemberPrincipal;
import com.umc.product.global.security.annotation.CurrentMember;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Community | 신고", description = "게시글/댓글 신고 API")
public class ReportController {

    private final ReportPostUseCase reportPostUseCase;
    private final ReportCommentUseCase reportCommentUseCase;
    private final GetChallengerUseCase getChallengerUseCase;

    @PostMapping("/posts/{postId}/reports")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "게시글 신고", description = "게시글을 신고합니다. 중복 신고는 불가능합니다.")
    public void reportPost(
            @PathVariable Long postId,
            @CurrentMember MemberPrincipal memberPrincipal
    ) {
        Long memberId = memberPrincipal.getMemberId();
        ChallengerInfoWithStatus challenger = getChallengerUseCase.getLatestActiveChallengerByMemberId(memberId);

        ReportPostCommand command = new ReportPostCommand(postId, challenger.challengerId());
        reportPostUseCase.report(command);
    }

    @PostMapping("/comments/{commentId}/reports")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "댓글 신고", description = "댓글을 신고합니다. 중복 신고는 불가능합니다.")
    public void reportComment(
            @PathVariable Long commentId,
            @CurrentMember MemberPrincipal memberPrincipal
    ) {
        Long memberId = memberPrincipal.getMemberId();
        ChallengerInfoWithStatus challenger = getChallengerUseCase.getLatestActiveChallengerByMemberId(memberId);

        ReportCommentCommand command = new ReportCommentCommand(commentId, challenger.challengerId());
        reportCommentUseCase.report(command);
    }
}
