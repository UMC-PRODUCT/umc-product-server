package com.umc.product.community.application.port.in.command.report;

import com.umc.product.community.application.port.in.command.report.dto.ReportCommunityMemberCommand;
import com.umc.product.community.application.port.in.query.report.dto.CommunityMemberReportReceiptInfo;

public interface ReportCommunityMemberUseCase {

    CommunityMemberReportReceiptInfo report(ReportCommunityMemberCommand command);
}
