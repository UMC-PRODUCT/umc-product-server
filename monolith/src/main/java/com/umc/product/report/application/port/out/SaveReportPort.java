package com.umc.product.report.application.port.out;

import com.umc.product.report.domain.Report;

public interface SaveReportPort {
    Report save(Report report);
}
