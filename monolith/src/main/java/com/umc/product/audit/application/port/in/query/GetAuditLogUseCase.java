package com.umc.product.audit.application.port.in.query;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.umc.product.audit.application.port.in.query.dto.AuditLogInfo;
import com.umc.product.audit.application.port.in.query.dto.SearchAuditLogQuery;

public interface GetAuditLogUseCase {
    Page<AuditLogInfo> search(SearchAuditLogQuery query, Pageable pageable);
}
