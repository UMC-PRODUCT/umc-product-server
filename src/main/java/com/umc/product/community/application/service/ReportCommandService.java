package com.umc.product.community.application.service;

import com.umc.product.community.application.port.in.report.ReportCommentCommand;
import com.umc.product.community.application.port.in.report.ReportCommentUseCase;
import com.umc.product.community.application.port.in.report.ReportPostCommand;
import com.umc.product.community.application.port.in.report.ReportPostUseCase;
import com.umc.product.community.application.port.out.LoadCommentPort;
import com.umc.product.community.application.port.out.LoadPostPort;
import com.umc.product.community.application.port.out.LoadReportPort;
import com.umc.product.community.application.port.out.SaveReportPort;
import com.umc.product.community.domain.Report;
import com.umc.product.community.domain.enums.ReportTargetType;
import com.umc.product.community.domain.exception.CommunityErrorCode;
import com.umc.product.global.exception.BusinessException;
import com.umc.product.global.exception.constant.Domain;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ReportCommandService implements ReportPostUseCase, ReportCommentUseCase {

    private final LoadPostPort loadPostPort;
    private final LoadCommentPort loadCommentPort;
    private final LoadReportPort loadReportPort;
    private final SaveReportPort saveReportPort;

    @Override
    public void report(ReportPostCommand command) {
        // 1. 게시글 존재 확인
        loadPostPort.findById(command.postId())
                .orElseThrow(() -> new BusinessException(Domain.COMMUNITY, CommunityErrorCode.POST_NOT_FOUND));

        // 2. 중복 신고 확인
        if (loadReportPort.existsByReporterIdAndTargetTypeAndTargetId(
                command.reporterId(), ReportTargetType.POST, command.postId())) {
            throw new BusinessException(Domain.COMMUNITY, CommunityErrorCode.REPORT_ALREADY_EXISTS);
        }

        // 3. 신고 생성 (사유 없음)
        Report report = Report.createPostReport(command.reporterId(), command.postId(), null);
        saveReportPort.save(report);
    }

    @Override
    public void report(ReportCommentCommand command) {
        // 1. 댓글 존재 확인
        loadCommentPort.findById(command.commentId())
                .orElseThrow(() -> new BusinessException(Domain.COMMUNITY, CommunityErrorCode.COMMENT_NOT_FOUND));

        // 2. 중복 신고 확인
        if (loadReportPort.existsByReporterIdAndTargetTypeAndTargetId(
                command.reporterId(), ReportTargetType.COMMENT, command.commentId())) {
            throw new BusinessException(Domain.COMMUNITY, CommunityErrorCode.REPORT_ALREADY_EXISTS);
        }

        // 3. 신고 생성 (사유 없음)
        Report report = Report.createCommentReport(command.reporterId(), command.commentId(), null);
        saveReportPort.save(report);
    }
}
