package com.umc.product.member.application.port.in.query;

import com.umc.product.member.application.port.in.query.dto.SearchMemberQuery;
import com.umc.product.member.application.port.in.query.dto.SearchMemberResult;
import org.springframework.data.domain.Pageable;

public interface SearchMemberUseCase {

    SearchMemberResult searchBy(SearchMemberQuery query, Pageable pageable);
}
