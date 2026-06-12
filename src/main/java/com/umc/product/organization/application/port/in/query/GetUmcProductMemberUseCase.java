package com.umc.product.organization.application.port.in.query;

import com.umc.product.organization.application.port.in.query.dto.umcproduct.UmcProductMemberInfo;
import com.umc.product.organization.application.port.in.query.dto.umcproduct.UmcProductMemberSearchCondition;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface GetUmcProductMemberUseCase {

    UmcProductMemberInfo getById(Long umcProductMemberId);

    Page<UmcProductMemberInfo> search(UmcProductMemberSearchCondition condition, Pageable pageable);
}
