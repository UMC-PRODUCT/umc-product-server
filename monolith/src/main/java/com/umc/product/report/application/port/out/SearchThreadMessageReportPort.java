package com.umc.product.report.application.port.out;

import com.umc.product.report.application.port.out.dto.ThreadMessageReportSearchQuery;
import com.umc.product.report.application.port.out.dto.ThreadMessageReportSearchResult;

public interface SearchThreadMessageReportPort {

    ThreadMessageReportSearchResult search(ThreadMessageReportSearchQuery query);
}
