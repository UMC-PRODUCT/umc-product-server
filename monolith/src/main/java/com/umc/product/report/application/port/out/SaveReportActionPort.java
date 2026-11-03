package com.umc.product.report.application.port.out;

import com.umc.product.report.domain.ReportAction;

public interface SaveReportActionPort {

    ReportAction save(ReportAction action);
}
