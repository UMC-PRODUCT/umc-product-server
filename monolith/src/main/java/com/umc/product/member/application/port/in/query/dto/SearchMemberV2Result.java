package com.umc.product.member.application.port.in.query.dto;

import org.springframework.data.domain.Page;

public record SearchMemberV2Result(Page<SearchMemberItemV2Info> page) {
}
