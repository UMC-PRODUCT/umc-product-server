package com.umc.product.organization.application.service;

import com.umc.product.member.application.port.in.query.GetMemberUseCase;
import com.umc.product.member.application.port.in.query.dto.MemberInfo;
import com.umc.product.organization.application.port.in.query.GetProductTeamMemberUseCase;
import com.umc.product.organization.application.port.in.query.dto.productteam.ProductTeamActivityInfo;
import com.umc.product.organization.application.port.in.query.dto.productteam.ProductTeamGenerationInfo;
import com.umc.product.organization.application.port.in.query.dto.productteam.ProductTeamMemberInfo;
import com.umc.product.organization.application.port.in.query.dto.productteam.ProductTeamMemberSearchCondition;
import com.umc.product.organization.application.port.out.query.LoadProductTeamGenerationPort;
import com.umc.product.organization.application.port.out.query.LoadProductTeamMemberPort;
import com.umc.product.organization.application.port.out.query.LoadProductTeamMembershipPort;
import com.umc.product.organization.domain.ProductTeamMember;
import com.umc.product.organization.domain.ProductTeamMembership;
import com.umc.product.storage.application.port.in.query.GetFileUseCase;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductTeamMemberQueryService implements GetProductTeamMemberUseCase {

    private final LoadProductTeamMemberPort loadProductTeamMemberPort;
    private final LoadProductTeamMembershipPort loadProductTeamMembershipPort;
    private final LoadProductTeamGenerationPort loadProductTeamGenerationPort;
    private final GetMemberUseCase getMemberUseCase;
    private final GetFileUseCase getFileUseCase;

    @Override
    public ProductTeamMemberInfo getById(Long productTeamMemberId) {
        ProductTeamMember member = loadProductTeamMemberPort.getById(productTeamMemberId);
        List<ProductTeamMembership> memberships = loadProductTeamMembershipPort
            .listByProductTeamMemberId(productTeamMemberId);
        Map<Long, ProductTeamGenerationInfo> generationMap = generationMapOf(memberships);
        MemberInfo memberInfo = getMemberUseCase.findById(member.getMemberId()).orElse(null);
        Map<String, String> productProfileLinks = resolveProductProfileLinks(List.of(member));

        return toInfo(
            member,
            memberInfo,
            memberships,
            generationMap,
            productProfileLinks.get(member.getProfileImageId())
        );
    }

    @Override
    public Page<ProductTeamMemberInfo> search(ProductTeamMemberSearchCondition condition, Pageable pageable) {
        Page<Long> idPage = loadProductTeamMemberPort.searchIds(condition, pageable);
        if (idPage.isEmpty()) {
            return Page.empty(pageable);
        }

        List<Long> ids = idPage.getContent();
        Map<Long, ProductTeamMember> memberMap = loadProductTeamMemberPort.listByIds(ids).stream()
            .collect(Collectors.toMap(ProductTeamMember::getId, Function.identity()));
        List<ProductTeamMembership> memberships = loadProductTeamMembershipPort.listByProductTeamMemberIds(ids, condition);
        Map<Long, List<ProductTeamMembership>> membershipsByMember = memberships.stream()
            .collect(Collectors.groupingBy(
                membership -> membership.getProductTeamMember().getId(),
                LinkedHashMap::new,
                Collectors.toList()
            ));
        Map<Long, ProductTeamGenerationInfo> generationMap = generationMapOf(memberships);
        Map<Long, MemberInfo> memberInfoMap = resolveMemberInfos(memberMap.values());
        Map<String, String> productProfileLinks = resolveProductProfileLinks(memberMap.values());

        List<ProductTeamMemberInfo> content = ids.stream()
            .map(memberMap::get)
            .filter(Objects::nonNull)
            .map(member -> toInfo(
                member,
                memberInfoMap.get(member.getMemberId()),
                membershipsByMember.getOrDefault(member.getId(), List.of()),
                generationMap,
                productProfileLinks.get(member.getProfileImageId())
            ))
            .toList();

        return new PageImpl<>(content, pageable, idPage.getTotalElements());
    }

    private ProductTeamMemberInfo toInfo(
        ProductTeamMember member,
        MemberInfo memberInfo,
        List<ProductTeamMembership> memberships,
        Map<Long, ProductTeamGenerationInfo> generationMap,
        String productTeamProfileImageUrl
    ) {
        List<ProductTeamActivityInfo> activities = memberships.stream()
            .sorted(activityComparator())
            .map(membership -> ProductTeamActivityInfo.from(
                membership,
                generationMap.get(membership.getProductTeamGenerationId())
            ))
            .toList();

        return new ProductTeamMemberInfo(
            member.getId(),
            member.getMemberId(),
            memberInfo == null ? null : memberInfo.name(),
            memberInfo == null ? null : memberInfo.nickname(),
            memberInfo == null ? null : memberInfo.schoolName(),
            memberInfo == null ? null : memberInfo.profileImageId(),
            memberInfo == null ? null : memberInfo.profileImageLink(),
            member.getIntroduction(),
            member.getProfileImageId(),
            productTeamProfileImageUrl,
            activities
        );
    }

    private Comparator<ProductTeamMembership> activityComparator() {
        return Comparator
            .comparing(ProductTeamMembership::getProductTeamGenerationId, Comparator.reverseOrder())
            .thenComparing(membership -> membership.getPart().getSortOrder())
            .thenComparing(membership -> membership.getRole().getSortOrder(), Comparator.reverseOrder())
            .thenComparing(membership -> membership.getPosition().getSortOrder())
            .thenComparing(ProductTeamMembership::getId, Comparator.nullsLast(Long::compareTo));
    }

    private Map<Long, ProductTeamGenerationInfo> generationMapOf(List<ProductTeamMembership> memberships) {
        Set<Long> generationIds = memberships.stream()
            .map(ProductTeamMembership::getProductTeamGenerationId)
            .collect(Collectors.toSet());
        if (generationIds.isEmpty()) {
            return Map.of();
        }
        return loadProductTeamGenerationPort.listByIds(generationIds).stream()
            .map(ProductTeamGenerationInfo::from)
            .collect(Collectors.toMap(ProductTeamGenerationInfo::productTeamGenerationId, Function.identity()));
    }

    private Map<Long, MemberInfo> resolveMemberInfos(Collection<ProductTeamMember> members) {
        Set<Long> memberIds = members.stream()
            .map(ProductTeamMember::getMemberId)
            .collect(Collectors.toSet());
        return getMemberUseCase.findAllByIds(memberIds);
    }

    private Map<String, String> resolveProductProfileLinks(Collection<ProductTeamMember> members) {
        List<String> imageIds = members.stream()
            .map(ProductTeamMember::getProfileImageId)
            .filter(Objects::nonNull)
            .distinct()
            .toList();
        if (imageIds.isEmpty()) {
            return Map.of();
        }
        return getFileUseCase.getFileLinks(new ArrayList<>(imageIds));
    }
}
