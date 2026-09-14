package com.umc.product.blog.application.service;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.umc.product.blog.application.port.in.query.dto.BlogAuthorInfo;
import com.umc.product.member.application.port.in.query.GetMemberUseCase;
import com.umc.product.member.application.port.in.query.dto.MemberInfo;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class BlogAuthorAssembler {

    private final GetMemberUseCase getMemberUseCase;

    public Map<Long, BlogAuthorInfo> assemble(Set<Long> memberIds) {
        if (memberIds == null || memberIds.isEmpty()) {
            return Map.of();
        }
        Map<Long, MemberInfo> members = getMemberUseCase.findAllByIds(memberIds);
        return members.values().stream()
            .collect(Collectors.toMap(
                MemberInfo::id,
                member -> new BlogAuthorInfo(member.id(), member.name(), member.nickname(), member.profileImageLink())
            ));
    }
}
