package com.umc.product.community.application.service.command;

import com.umc.product.community.application.port.in.command.report.ReportCommentUseCase;
import com.umc.product.community.application.port.in.command.report.ReportPostUseCase;
import com.umc.product.community.application.port.in.command.report.dto.ReportCommentCommand;
import com.umc.product.community.application.port.in.command.report.dto.ReportPostCommand;
import com.umc.product.community.application.port.out.comment.LoadCommentPort;
import com.umc.product.community.application.port.out.post.LoadPostPort;
import com.umc.product.community.application.port.out.report.LoadReportPort;
import com.umc.product.community.application.port.out.report.SaveReportPort;
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
        // 게시글 존재 확인
        loadPostPort.findById(command.postId())
            .orElseThrow(() -> new BusinessException(Domain.COMMUNITY, CommunityErrorCode.POST_NOT_FOUND));

        // 중복 신고 확인 및 저장
        checkDuplicateAndSaveReport(command.reporterId(), ReportTargetType.POST, command.postId());
    }

    @Override
    public void report(ReportCommentCommand command) {
        // 댓글 존재 확인
        loadCommentPort.findById(command.commentId())
            .orElseThrow(() -> new BusinessException(Domain.COMMUNITY, CommunityErrorCode.COMMENT_NOT_FOUND));

        // 중복 신고 확인 및 저장
        checkDuplicateAndSaveReport(command.reporterId(), ReportTargetType.COMMENT, command.commentId());
    }

    /**
     * 중복 신고를 확인하고 신고를 생성합니다.
     *
     * @param reporterId 신고자 챌린저 ID
     * @param targetType 신고 대상 타입
     * @param targetId   신고 대상 ID
     * @throws BusinessException 이미 신고한 경우
     */
    private void checkDuplicateAndSaveReport(Long reporterId, ReportTargetType targetType, Long targetId) {
        // 중복 신고 확인
        if (loadReportPort.existsByReporterIdAndTargetTypeAndTargetId(reporterId, targetType, targetId)) {
            throw new BusinessException(Domain.COMMUNITY, CommunityErrorCode.REPORT_ALREADY_EXISTS);
        }

        // 신고 생성 및 저장
        Report report = Report.create(reporterId, targetType, targetId, null);
        saveReportPort.save(report);
    }
}
