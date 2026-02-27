package com.umc.product.member.application.port.out;

import com.umc.product.challenger.domain.Challenger;
import com.umc.product.member.application.port.in.query.dto.SearchMemberQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SearchMemberPort {

    Page<Challenger> search(SearchMemberQuery query, Pageable pageable);
}
