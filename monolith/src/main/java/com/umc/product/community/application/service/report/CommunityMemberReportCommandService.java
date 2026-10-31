package com.umc.product.community.application.service.report;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.umc.product.community.application.port.in.command.report.ReportCommunityMemberUseCase;
import com.umc.product.community.application.port.in.command.report.dto.ReportCommunityMemberCommand;
import com.umc.product.community.application.port.in.query.report.dto.CommunityMemberReportReceiptInfo;
import com.umc.product.member.application.port.in.query.GetMemberUseCase;
import com.umc.product.member.application.port.in.query.dto.MemberInfo;
import com.umc.product.report.application.port.in.command.SubmitReportUseCase;
import com.umc.product.report.application.port.in.command.dto.SubmitReportCommand;
import com.umc.product.report.application.port.in.command.dto.SubmitReportResult;
import com.umc.product.report.domain.exception.ReportDomainException;
import com.umc.product.report.domain.exception.ReportErrorCode;

import lombok.RequiredArgsConstructor;

/**
 * 사용자 신고를 접수합니다. 대상 회원이 있는지 확인하고, 신고 당시 닉네임을 스냅샷으로 남겨 report 도메인에 넘깁니다.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class CommunityMemberReportCommandService implements ReportCommunityMemberUseCase {

    private final GetMemberUseCase getMemberUseCase;
    private final SubmitReportUseCase submitReportUseCase;

    @Override
    public CommunityMemberReportReceiptInfo report(ReportCommunityMemberCommand command) {
        if (command.targetMemberId().equals(command.requesterMemberId())) {
            throw new ReportDomainException(ReportErrorCode.REPORT_SELF_REPORT);
        }
        MemberInfo target = getMemberUseCase.getById(command.targetMemberId());

        SubmitReportResult submitted = submitReportUseCase.submit(SubmitReportCommand.user(
            command.requesterMemberId(),
            command.targetMemberId(),
            command.reason(),
            command.detail(),
            // 사용자 신고는 내용 대신 신고 당시 닉네임을 위치(context)에 남깁니다.
            new SubmitReportCommand.TargetSnapshot(command.targetMemberId(), null, target.nickname())
        ));
        return CommunityMemberReportReceiptInfo.from(submitted, command.targetMemberId(), command.reason());
    }
}
