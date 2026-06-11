package com.umc.product.organization.application.service;

import com.umc.product.member.application.port.in.query.GetMemberUseCase;
import com.umc.product.member.application.port.in.query.dto.MemberInfo;
import com.umc.product.organization.application.port.in.query.GetProductTeamMemberUseCase;
import com.umc.product.organization.application.port.in.query.dto.productteam.ProductTeamFunctionalMembershipInfo;
import com.umc.product.organization.application.port.in.query.dto.productteam.ProductTeamFunctionalUnitInfo;
import com.umc.product.organization.application.port.in.query.dto.productteam.ProductTeamGenerationInfo;
import com.umc.product.organization.application.port.in.query.dto.productteam.ProductTeamMemberInfo;
import com.umc.product.organization.application.port.in.query.dto.productteam.ProductTeamMemberSearchCondition;
import com.umc.product.organization.application.port.in.query.dto.productteam.ProductTeamSquadInfo;
import com.umc.product.organization.application.port.in.query.dto.productteam.ProductTeamSquadParticipationInfo;
import com.umc.product.organization.application.port.out.query.LoadProductTeamFunctionalMembershipPort;
import com.umc.product.organization.application.port.out.query.LoadProductTeamFunctionalUnitPort;
import com.umc.product.organization.application.port.out.query.LoadProductTeamGenerationPort;
import com.umc.product.organization.application.port.out.query.LoadProductTeamMemberPort;
import com.umc.product.organization.application.port.out.query.LoadProductTeamSquadParticipantPort;
import com.umc.product.organization.application.port.out.query.LoadProductTeamSquadPort;
import com.umc.product.organization.domain.ProductTeamFunctionalMembership;
import com.umc.product.organization.domain.ProductTeamFunctionalUnit;
import com.umc.product.organization.domain.ProductTeamMember;
import com.umc.product.organization.domain.ProductTeamSquad;
import com.umc.product.organization.domain.ProductTeamSquadParticipant;
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
    private final LoadProductTeamFunctionalMembershipPort loadProductTeamFunctionalMembershipPort;
    private final LoadProductTeamSquadParticipantPort loadProductTeamSquadParticipantPort;
    private final LoadProductTeamGenerationPort loadProductTeamGenerationPort;
    private final LoadProductTeamFunctionalUnitPort loadProductTeamFunctionalUnitPort;
    private final LoadProductTeamSquadPort loadProductTeamSquadPort;
    private final GetMemberUseCase getMemberUseCase;
    private final GetFileUseCase getFileUseCase;

    @Override
    public ProductTeamMemberInfo getById(Long productTeamMemberId) {
        ProductTeamMember member = loadProductTeamMemberPort.getById(productTeamMemberId);
        List<ProductTeamFunctionalMembership> functionalMemberships = loadProductTeamFunctionalMembershipPort
            .listByProductTeamMemberId(productTeamMemberId);
        List<ProductTeamSquadParticipant> squadParticipations = loadProductTeamSquadParticipantPort
            .listByProductTeamMemberId(productTeamMemberId);
        MemberInfo memberInfo = getMemberUseCase.findById(member.getMemberId()).orElse(null);
        Map<String, String> productProfileLinks = resolveProductProfileLinks(List.of(member));

        return toInfo(
            member,
            memberInfo,
            functionalMemberships,
            squadParticipations,
            generationMapOf(functionalMemberships),
            functionalUnitMapOf(functionalMemberships),
            squadMapOf(squadParticipations),
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
        List<ProductTeamFunctionalMembership> functionalMemberships =
            loadProductTeamFunctionalMembershipPort.listByProductTeamMemberIds(ids, condition);
        List<ProductTeamSquadParticipant> squadParticipations =
            loadProductTeamSquadParticipantPort.listByProductTeamMemberIds(ids);
        if (condition != null && condition.squadId() != null) {
            squadParticipations = squadParticipations.stream()
                .filter(participation -> Objects.equals(participation.getSquad().getId(), condition.squadId()))
                .toList();
        }
        Map<Long, List<ProductTeamFunctionalMembership>> membershipsByMember = functionalMemberships.stream()
            .collect(Collectors.groupingBy(
                membership -> membership.getProductTeamMember().getId(),
                LinkedHashMap::new,
                Collectors.toList()
            ));
        Map<Long, List<ProductTeamSquadParticipant>> squadsByMember = squadParticipations.stream()
            .collect(Collectors.groupingBy(
                participation -> participation.getProductTeamMember().getId(),
                LinkedHashMap::new,
                Collectors.toList()
            ));
        Map<Long, ProductTeamGenerationInfo> generationMap = generationMapOf(functionalMemberships);
        Map<Long, ProductTeamFunctionalUnitInfo> functionalUnitMap = functionalUnitMapOf(functionalMemberships);
        Map<Long, ProductTeamSquadInfo> squadMap = squadMapOf(squadParticipations);
        Map<Long, MemberInfo> memberInfoMap = resolveMemberInfos(memberMap.values());
        Map<String, String> productProfileLinks = resolveProductProfileLinks(memberMap.values());

        List<ProductTeamMemberInfo> content = ids.stream()
            .map(memberMap::get)
            .filter(Objects::nonNull)
            .map(member -> toInfo(
                member,
                memberInfoMap.get(member.getMemberId()),
                membershipsByMember.getOrDefault(member.getId(), List.of()),
                squadsByMember.getOrDefault(member.getId(), List.of()),
                generationMap,
                functionalUnitMap,
                squadMap,
                productProfileLinks.get(member.getProfileImageId())
            ))
            .toList();

        return new PageImpl<>(content, pageable, idPage.getTotalElements());
    }

    private ProductTeamMemberInfo toInfo(
        ProductTeamMember member,
        MemberInfo memberInfo,
        List<ProductTeamFunctionalMembership> functionalMemberships,
        List<ProductTeamSquadParticipant> squadParticipations,
        Map<Long, ProductTeamGenerationInfo> generationMap,
        Map<Long, ProductTeamFunctionalUnitInfo> functionalUnitMap,
        Map<Long, ProductTeamSquadInfo> squadMap,
        String productTeamProfileImageUrl
    ) {
        List<ProductTeamFunctionalMembershipInfo> functionalMembershipInfos = functionalMemberships.stream()
            .sorted(functionalMembershipComparator())
            .map(membership -> ProductTeamFunctionalMembershipInfo.from(
                membership,
                generationMap.get(membership.getProductTeamGenerationId()),
                functionalUnitMap.get(membership.getFunctionalUnitId())
            ))
            .toList();
        List<ProductTeamSquadParticipationInfo> squadParticipationInfos = squadParticipations.stream()
            .sorted(squadParticipationComparator())
            .map(participation -> ProductTeamSquadParticipationInfo.from(
                participation,
                squadMap.get(participation.getSquad().getId())
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
            functionalMembershipInfos,
            squadParticipationInfos
        );
    }

    private Comparator<ProductTeamFunctionalMembership> functionalMembershipComparator() {
        return Comparator
            .comparing(ProductTeamFunctionalMembership::getProductTeamGenerationId, Comparator.reverseOrder())
            .thenComparing(ProductTeamFunctionalMembership::getFunctionalUnitId)
            .thenComparing(membership -> membership.getRole().getSortOrder(), Comparator.reverseOrder())
            .thenComparing(membership -> membership.getPosition().getSortOrder())
            .thenComparing(ProductTeamFunctionalMembership::getId, Comparator.nullsLast(Long::compareTo));
    }

    private Comparator<ProductTeamSquadParticipant> squadParticipationComparator() {
        return Comparator
            .comparing((ProductTeamSquadParticipant participant) -> participant.getSquad().getSortOrder())
            .thenComparing(participant -> participant.getRole().getSortOrder(), Comparator.reverseOrder())
            .thenComparing(participant -> participant.getPosition().getSortOrder())
            .thenComparing(ProductTeamSquadParticipant::getId, Comparator.nullsLast(Long::compareTo));
    }

    private Map<Long, ProductTeamGenerationInfo> generationMapOf(
        List<ProductTeamFunctionalMembership> functionalMemberships
    ) {
        Set<Long> generationIds = functionalMemberships.stream()
            .map(ProductTeamFunctionalMembership::getProductTeamGenerationId)
            .collect(Collectors.toSet());
        if (generationIds.isEmpty()) {
            return Map.of();
        }
        return loadProductTeamGenerationPort.listByIds(generationIds).stream()
            .map(ProductTeamGenerationInfo::from)
            .collect(Collectors.toMap(ProductTeamGenerationInfo::productTeamGenerationId, Function.identity()));
    }

    private Map<Long, ProductTeamFunctionalUnitInfo> functionalUnitMapOf(
        List<ProductTeamFunctionalMembership> functionalMemberships
    ) {
        Set<Long> functionalUnitIds = functionalMemberships.stream()
            .map(ProductTeamFunctionalMembership::getFunctionalUnitId)
            .collect(Collectors.toSet());
        if (functionalUnitIds.isEmpty()) {
            return Map.of();
        }
        return loadProductTeamFunctionalUnitPort.listByIds(functionalUnitIds).stream()
            .map(ProductTeamFunctionalUnitInfo::from)
            .collect(Collectors.toMap(ProductTeamFunctionalUnitInfo::functionalUnitId, Function.identity()));
    }

    private Map<Long, ProductTeamSquadInfo> squadMapOf(List<ProductTeamSquadParticipant> squadParticipations) {
        Set<Long> squadIds = squadParticipations.stream()
            .map(participation -> participation.getSquad().getId())
            .collect(Collectors.toSet());
        if (squadIds.isEmpty()) {
            return Map.of();
        }
        return loadProductTeamSquadPort.listByIds(squadIds).stream()
            .map(ProductTeamSquadInfo::from)
            .collect(Collectors.toMap(ProductTeamSquadInfo::squadId, Function.identity()));
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
