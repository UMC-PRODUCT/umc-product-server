package com.umc.product.community.application.service.command;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.umc.product.audit.application.port.in.annotation.Audited;
import com.umc.product.audit.domain.AuditAction;
import com.umc.product.community.application.port.in.command.report.ReportCommentUseCase;
import com.umc.product.community.application.port.in.command.report.ReportPostUseCase;
import com.umc.product.community.application.port.in.command.report.dto.ReportCommentCommand;
import com.umc.product.community.application.port.in.command.report.dto.ReportPostCommand;
import com.umc.product.community.application.port.out.comment.LoadCommentPort;
import com.umc.product.community.application.port.out.post.LoadPostPort;
import com.umc.product.community.domain.exception.CommunityDomainException;
import com.umc.product.community.domain.exception.CommunityErrorCode;
import com.umc.product.global.exception.constant.Domain;
import com.umc.product.report.application.port.in.command.SubmitReportUseCase;
import com.umc.product.report.application.port.in.command.dto.SubmitReportCommand;
import com.umc.product.report.domain.enums.ReportTargetType;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class ReportCommandService implements ReportPostUseCase, ReportCommentUseCase {

    private final LoadPostPort loadPostPort;
    private final LoadCommentPort loadCommentPort;
    private final SubmitReportUseCase submitReportUseCase;

    @Audited(
        domain = Domain.COMMUNITY,
        action = AuditAction.SUBMIT,
        targetType = "PostReport",
        targetId = "#command.postId()",
        description = "'커뮤니티 게시글 신고를 제출했습니다.'"
    )
    @Override
    public void report(ReportPostCommand command) {
        // 게시글 존재 확인
        loadPostPort.findById(command.postId())
            .orElseThrow(() -> new CommunityDomainException(CommunityErrorCode.POST_NOT_FOUND));

        submitReportUseCase.submit(
            SubmitReportCommand.of(command.reporterId(), ReportTargetType.POST, command.postId())
        );
    }

    @Audited(
        domain = Domain.COMMUNITY,
        action = AuditAction.SUBMIT,
        targetType = "CommentReport",
        targetId = "#command.commentId()",
        description = "'커뮤니티 댓글 신고를 제출했습니다.'"
    )
    @Override
    public void report(ReportCommentCommand command) {
        // 댓글 존재 확인
        loadCommentPort.findById(command.commentId())
            .orElseThrow(() -> new CommunityDomainException(CommunityErrorCode.COMMENT_NOT_FOUND));

        submitReportUseCase.submit(
            SubmitReportCommand.of(command.reporterId(), ReportTargetType.COMMENT, command.commentId())
        );
    }
}
