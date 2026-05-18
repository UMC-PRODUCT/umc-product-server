package com.umc.product.member.application.port.in.query;

import com.umc.product.member.application.port.in.query.dto.SearchMemberQuery;
import com.umc.product.member.application.port.in.query.dto.SearchMemberResult;
import com.umc.product.member.application.port.in.query.dto.SearchMemberV2Result;
import org.springframework.data.domain.Pageable;

public interface SearchMemberUseCase {

    SearchMemberResult searchBy(SearchMemberQuery query, Pageable pageable);

    /**
     * v2 검색: v1 필드에 더해 챌린저 상태와 현재 활성 기수 운영진 여부를 포함합니다.
     */
    SearchMemberV2Result searchByV2(SearchMemberQuery query, Pageable pageable);
}
