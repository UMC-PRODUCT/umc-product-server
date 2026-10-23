package com.umc.product.community.application.port.out.report;

import com.umc.product.community.domain.Report;

public interface SaveReportPort {
    Report save(Report report);
}
