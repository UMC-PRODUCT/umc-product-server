package com.umc.product.audit.application.port.in.query;

import com.umc.product.audit.application.port.in.query.dto.AuditLogInfo;
import com.umc.product.audit.application.port.in.query.dto.SearchAuditLogQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface GetAuditLogUseCase {
    Page<AuditLogInfo> search(SearchAuditLogQuery query, Pageable pageable);
}
