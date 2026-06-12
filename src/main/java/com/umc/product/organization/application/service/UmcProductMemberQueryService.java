package com.umc.product.organization.application.service;

import com.umc.product.member.application.port.in.query.GetMemberUseCase;
import com.umc.product.member.application.port.in.query.dto.MemberInfo;
import com.umc.product.organization.application.port.in.query.GetUmcProductMemberUseCase;
import com.umc.product.organization.application.port.in.query.dto.umcproduct.UmcProductFunctionalMembershipInfo;
import com.umc.product.organization.application.port.in.query.dto.umcproduct.UmcProductFunctionalUnitInfo;
import com.umc.product.organization.application.port.in.query.dto.umcproduct.UmcProductGenerationInfo;
import com.umc.product.organization.application.port.in.query.dto.umcproduct.UmcProductMemberInfo;
import com.umc.product.organization.application.port.in.query.dto.umcproduct.UmcProductMemberSearchCondition;
import com.umc.product.organization.application.port.in.query.dto.umcproduct.UmcProductSquadInfo;
import com.umc.product.organization.application.port.in.query.dto.umcproduct.UmcProductSquadParticipationInfo;
import com.umc.product.organization.application.port.out.query.LoadUmcProductFunctionalMembershipPort;
import com.umc.product.organization.application.port.out.query.LoadUmcProductFunctionalUnitPort;
import com.umc.product.organization.application.port.out.query.LoadUmcProductGenerationPort;
import com.umc.product.organization.application.port.out.query.LoadUmcProductMemberPort;
import com.umc.product.organization.application.port.out.query.LoadUmcProductSquadParticipantPort;
import com.umc.product.organization.application.port.out.query.LoadUmcProductSquadPort;
import com.umc.product.organization.domain.UmcProductFunctionalMembership;
import com.umc.product.organization.domain.UmcProductFunctionalUnit;
import com.umc.product.organization.domain.UmcProductMember;
import com.umc.product.organization.domain.UmcProductSquad;
import com.umc.product.organization.domain.UmcProductSquadParticipant;
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
public class UmcProductMemberQueryService implements GetUmcProductMemberUseCase {

    private final LoadUmcProductMemberPort loadUmcProductMemberPort;
    private final LoadUmcProductFunctionalMembershipPort loadUmcProductFunctionalMembershipPort;
    private final LoadUmcProductSquadParticipantPort loadUmcProductSquadParticipantPort;
    private final LoadUmcProductGenerationPort loadUmcProductGenerationPort;
    private final LoadUmcProductFunctionalUnitPort loadUmcProductFunctionalUnitPort;
    private final LoadUmcProductSquadPort loadUmcProductSquadPort;
    private final GetMemberUseCase getMemberUseCase;
    private final GetFileUseCase getFileUseCase;

    @Override
    public UmcProductMemberInfo getById(Long umcProductMemberId) {
        UmcProductMember member = loadUmcProductMemberPort.getById(umcProductMemberId);
        List<UmcProductFunctionalMembership> functionalMemberships = loadUmcProductFunctionalMembershipPort
            .listByUmcProductMemberId(umcProductMemberId);
        List<UmcProductSquadParticipant> squadParticipations = loadUmcProductSquadParticipantPort
            .listByUmcProductMemberId(umcProductMemberId);
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
    public Page<UmcProductMemberInfo> search(UmcProductMemberSearchCondition condition, Pageable pageable) {
        Page<Long> idPage = loadUmcProductMemberPort.searchIds(condition, pageable);
        if (idPage.isEmpty()) {
            return Page.empty(pageable);
        }

        List<Long> ids = idPage.getContent();
        Map<Long, UmcProductMember> memberMap = loadUmcProductMemberPort.listByIds(ids).stream()
            .collect(Collectors.toMap(UmcProductMember::getId, Function.identity()));
        List<UmcProductFunctionalMembership> functionalMemberships =
            loadUmcProductFunctionalMembershipPort.listByUmcProductMemberIds(ids);
        List<UmcProductSquadParticipant> squadParticipations =
            loadUmcProductSquadParticipantPort.listByUmcProductMemberIds(ids);
        Map<Long, List<UmcProductFunctionalMembership>> membershipsByMember = functionalMemberships.stream()
            .collect(Collectors.groupingBy(
                membership -> membership.getUmcProductMember().getId(),
                LinkedHashMap::new,
                Collectors.toList()
            ));
        Map<Long, List<UmcProductSquadParticipant>> squadsByMember = squadParticipations.stream()
            .collect(Collectors.groupingBy(
                participation -> participation.getUmcProductMember().getId(),
                LinkedHashMap::new,
                Collectors.toList()
            ));
        Map<Long, UmcProductGenerationInfo> generationMap = generationMapOf(functionalMemberships);
        Map<Long, UmcProductFunctionalUnitInfo> functionalUnitMap = functionalUnitMapOf(functionalMemberships);
        Map<Long, UmcProductSquadInfo> squadMap = squadMapOf(squadParticipations);
        Map<Long, MemberInfo> memberInfoMap = resolveMemberInfos(memberMap.values());
        Map<String, String> productProfileLinks = resolveProductProfileLinks(memberMap.values());

        List<UmcProductMemberInfo> content = ids.stream()
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

    private UmcProductMemberInfo toInfo(
        UmcProductMember member,
        MemberInfo memberInfo,
        List<UmcProductFunctionalMembership> functionalMemberships,
        List<UmcProductSquadParticipant> squadParticipations,
        Map<Long, UmcProductGenerationInfo> generationMap,
        Map<Long, UmcProductFunctionalUnitInfo> functionalUnitMap,
        Map<Long, UmcProductSquadInfo> squadMap,
        String umcProductProfileImageUrl
    ) {
        List<UmcProductFunctionalMembershipInfo> functionalMembershipInfos = functionalMemberships.stream()
            .sorted(functionalMembershipComparator())
            .map(membership -> UmcProductFunctionalMembershipInfo.from(
                membership,
                generationMap.get(membership.getUmcProductGenerationId()),
                functionalUnitMap.get(membership.getFunctionalUnitId())
            ))
            .toList();
        List<UmcProductSquadParticipationInfo> squadParticipationInfos = squadParticipations.stream()
            .sorted(squadParticipationComparator())
            .map(participation -> UmcProductSquadParticipationInfo.from(
                participation,
                squadMap.get(participation.getSquad().getId())
            ))
            .toList();

        return new UmcProductMemberInfo(
            member.getId(),
            member.getMemberId(),
            memberInfo == null ? null : memberInfo.name(),
            memberInfo == null ? null : memberInfo.nickname(),
            memberInfo == null ? null : memberInfo.schoolName(),
            memberInfo == null ? null : memberInfo.profileImageId(),
            memberInfo == null ? null : memberInfo.profileImageLink(),
            member.getIntroduction(),
            member.getProfileImageId(),
            umcProductProfileImageUrl,
            functionalMembershipInfos,
            squadParticipationInfos
        );
    }

    private Comparator<UmcProductFunctionalMembership> functionalMembershipComparator() {
        return Comparator
            .comparing(UmcProductFunctionalMembership::getUmcProductGenerationId, Comparator.reverseOrder())
            .thenComparing(UmcProductFunctionalMembership::getFunctionalUnitId)
            .thenComparing(membership -> membership.getRole().getSortOrder(), Comparator.reverseOrder())
            .thenComparing(membership -> membership.getPosition().getSortOrder())
            .thenComparing(UmcProductFunctionalMembership::getId, Comparator.nullsLast(Long::compareTo));
    }

    private Comparator<UmcProductSquadParticipant> squadParticipationComparator() {
        return Comparator
            .comparing((UmcProductSquadParticipant participant) -> participant.getSquad().getSortOrder())
            .thenComparing(participant -> participant.getRole().getSortOrder(), Comparator.reverseOrder())
            .thenComparing(participant -> participant.getPosition().getSortOrder())
            .thenComparing(UmcProductSquadParticipant::getId, Comparator.nullsLast(Long::compareTo));
    }

    private Map<Long, UmcProductGenerationInfo> generationMapOf(
        List<UmcProductFunctionalMembership> functionalMemberships
    ) {
        Set<Long> generationIds = functionalMemberships.stream()
            .map(UmcProductFunctionalMembership::getUmcProductGenerationId)
            .collect(Collectors.toSet());
        if (generationIds.isEmpty()) {
            return Map.of();
        }
        return loadUmcProductGenerationPort.listByIds(generationIds).stream()
            .map(UmcProductGenerationInfo::from)
            .collect(Collectors.toMap(UmcProductGenerationInfo::umcProductGenerationId, Function.identity()));
    }

    private Map<Long, UmcProductFunctionalUnitInfo> functionalUnitMapOf(
        List<UmcProductFunctionalMembership> functionalMemberships
    ) {
        Set<Long> functionalUnitIds = functionalMemberships.stream()
            .map(UmcProductFunctionalMembership::getFunctionalUnitId)
            .collect(Collectors.toSet());
        if (functionalUnitIds.isEmpty()) {
            return Map.of();
        }
        return loadUmcProductFunctionalUnitPort.listByIds(functionalUnitIds).stream()
            .map(UmcProductFunctionalUnitInfo::from)
            .collect(Collectors.toMap(UmcProductFunctionalUnitInfo::functionalUnitId, Function.identity()));
    }

    private Map<Long, UmcProductSquadInfo> squadMapOf(List<UmcProductSquadParticipant> squadParticipations) {
        Set<Long> squadIds = squadParticipations.stream()
            .map(participation -> participation.getSquad().getId())
            .collect(Collectors.toSet());
        if (squadIds.isEmpty()) {
            return Map.of();
        }
        return loadUmcProductSquadPort.listByIds(squadIds).stream()
            .map(UmcProductSquadInfo::from)
            .collect(Collectors.toMap(UmcProductSquadInfo::squadId, Function.identity()));
    }

    private Map<Long, MemberInfo> resolveMemberInfos(Collection<UmcProductMember> members) {
        Set<Long> memberIds = members.stream()
            .map(UmcProductMember::getMemberId)
            .collect(Collectors.toSet());
        return getMemberUseCase.findAllByIds(memberIds);
    }

    private Map<String, String> resolveProductProfileLinks(Collection<UmcProductMember> members) {
        List<String> imageIds = members.stream()
            .map(UmcProductMember::getProfileImageId)
            .filter(Objects::nonNull)
            .distinct()
            .toList();
        if (imageIds.isEmpty()) {
            return Map.of();
        }
        return getFileUseCase.getFileLinks(new ArrayList<>(imageIds));
    }
}
