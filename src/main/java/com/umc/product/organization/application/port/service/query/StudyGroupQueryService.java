package com.umc.product.organization.application.port.service.query;

import com.umc.product.organization.application.port.in.query.GetSchoolAccessContextUseCase;
import com.umc.product.organization.application.port.in.query.GetStudyGroupUseCase;
import com.umc.product.organization.application.port.in.query.dto.PartSummaryInfo;
import com.umc.product.organization.application.port.in.query.dto.SchoolAccessContext;
import com.umc.product.organization.application.port.in.query.dto.SchoolStudyGroupInfo;
import com.umc.product.organization.application.port.in.query.dto.StudyGroupDetailInfo;
import com.umc.product.organization.application.port.in.query.dto.StudyGroupListInfo;
import com.umc.product.organization.application.port.in.query.dto.StudyGroupListQuery;
import com.umc.product.organization.application.port.in.query.dto.StudyGroupNameInfo;
import com.umc.product.organization.application.port.out.query.LoadStudyGroupPort;
import com.umc.product.storage.application.port.in.query.GetFileUseCase;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StudyGroupQueryService implements GetStudyGroupUseCase {

    private final LoadStudyGroupPort loadStudyGroupPort;
    private final GetSchoolAccessContextUseCase getSchoolAccessContextUseCase;
    private final GetFileUseCase getFileUseCase;

    @Deprecated
    @Override
    public List<SchoolStudyGroupInfo> getSchools() {
        return loadStudyGroupPort.findSchoolsWithStudyGroups();
    }

    @Deprecated
    @Override
    public PartSummaryInfo getParts(Long schoolId) {
        return loadStudyGroupPort.findPartSummary(schoolId);
    }

    @Override
    public List<StudyGroupListInfo.StudyGroupInfo> getMyStudyGroups(Long memberId, Long cursor, int size) {
        SchoolAccessContext context = getSchoolAccessContextUseCase.getContext(memberId);

        StudyGroupListQuery query = new StudyGroupListQuery(
                context.schoolId(), context.part(), cursor, size
        );

        return getStudyGroups(query);
    }

    @Override
    public List<StudyGroupListInfo.StudyGroupInfo> getStudyGroups(StudyGroupListQuery query) {
        List<StudyGroupListInfo.StudyGroupInfo> groups = loadStudyGroupPort.findStudyGroups(
                query.schoolId(), query.part(), query.cursor(), query.fetchSize());
        return resolveStudyGroupListUrls(groups);
    }

    @Override
    public List<StudyGroupNameInfo> getStudyGroupNames(Long memberId) {
        SchoolAccessContext context = getSchoolAccessContextUseCase.getContext(memberId);
        return loadStudyGroupPort.findStudyGroupNames(context.schoolId(), context.part());
    }

    @Override
    public StudyGroupDetailInfo getStudyGroupDetail(Long groupId) {
        StudyGroupDetailInfo detail = loadStudyGroupPort.findStudyGroupDetail(groupId);
        return resolveStudyGroupDetailUrls(detail);
    }

    private List<StudyGroupListInfo.StudyGroupInfo> resolveStudyGroupListUrls(
            List<StudyGroupListInfo.StudyGroupInfo> groups) {
        Set<String> imageIds = new LinkedHashSet<>();
        for (StudyGroupListInfo.StudyGroupInfo group : groups) {
            if (group.leader() != null && group.leader().profileImageUrl() != null) {
                imageIds.add(group.leader().profileImageUrl());
            }
            for (StudyGroupListInfo.StudyGroupInfo.MemberSummaryInfo member : group.members()) {
                if (member.profileImageUrl() != null) {
                    imageIds.add(member.profileImageUrl());
                }
            }
        }

        if (imageIds.isEmpty()) {
            return groups;
        }

        Map<String, String> urlMap = resolveProfileImageUrls(imageIds);

        return groups.stream()
                .map(group -> new StudyGroupListInfo.StudyGroupInfo(
                        group.groupId(),
                        group.name(),
                        group.memberCount(),
                        group.leader() == null ? null : new StudyGroupListInfo.StudyGroupInfo.LeaderInfo(
                                group.leader().challengerId(),
                                group.leader().name(),
                                urlMap.getOrDefault(group.leader().profileImageUrl(),
                                        group.leader().profileImageUrl())
                        ),
                        group.members().stream()
                                .map(m -> new StudyGroupListInfo.StudyGroupInfo.MemberSummaryInfo(
                                        m.challengerId(),
                                        m.name(),
                                        urlMap.getOrDefault(m.profileImageUrl(), m.profileImageUrl())
                                ))
                                .toList()
                ))
                .toList();
    }

    private StudyGroupDetailInfo resolveStudyGroupDetailUrls(StudyGroupDetailInfo detail) {
        Set<String> imageIds = new LinkedHashSet<>();
        if (detail.leader() != null && detail.leader().profileImageUrl() != null) {
            imageIds.add(detail.leader().profileImageUrl());
        }
        for (StudyGroupDetailInfo.MemberInfo member : detail.members()) {
            if (member.profileImageUrl() != null) {
                imageIds.add(member.profileImageUrl());
            }
        }

        if (imageIds.isEmpty()) {
            return detail;
        }

        Map<String, String> urlMap = resolveProfileImageUrls(imageIds);

        StudyGroupDetailInfo.MemberInfo resolvedLeader = detail.leader() == null ? null
                : new StudyGroupDetailInfo.MemberInfo(
                        detail.leader().challengerId(),
                        detail.leader().memberId(),
                        detail.leader().name(),
                        urlMap.getOrDefault(detail.leader().profileImageUrl(),
                                detail.leader().profileImageUrl())
                );

        List<StudyGroupDetailInfo.MemberInfo> resolvedMembers = detail.members().stream()
                .map(m -> new StudyGroupDetailInfo.MemberInfo(
                        m.challengerId(),
                        m.memberId(),
                        m.name(),
                        urlMap.getOrDefault(m.profileImageUrl(), m.profileImageUrl())
                ))
                .toList();

        return new StudyGroupDetailInfo(
                detail.groupId(),
                detail.name(),
                detail.part(),
                detail.schools(),
                detail.createdAt(),
                detail.memberCount(),
                resolvedLeader,
                resolvedMembers
        );
    }

    private Map<String, String> resolveProfileImageUrls(Set<String> profileImageIds) {
        Map<String, String> urlMap = new HashMap<>();
        for (String id : profileImageIds) {
            urlMap.put(id, getFileUseCase.getById(id).fileLink());
        }
        return urlMap;
    }
}
