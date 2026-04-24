package com.umc.product.organization.domain;

import com.umc.product.common.BaseEntity;
import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.organization.exception.OrganizationDomainException;
import com.umc.product.organization.exception.OrganizationErrorCode;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "study_group")
public class StudyGroup extends BaseEntity {

    @OneToMany(mappedBy = "studyGroup", cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<StudyGroupMember> studyGroupMembers = new ArrayList<>();

    @OneToMany(mappedBy = "studyGroup", cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<StudyGroupMentor> studyGroupMentor = new ArrayList<>();

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(nullable = false)
    private Long gisuId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ChallengerPart part;

    @Builder(access = AccessLevel.PRIVATE)
    private StudyGroup(String name, Long gisuId, ChallengerPart part) {
        validate(name, gisuId, part);
        this.name = name;
        this.gisuId = gisuId;
        this.part = part;
    }

    public static StudyGroup create(String name, Long gisuId, ChallengerPart part, Set<Long> mentorIds, Set<Long> memberIds) {
        if(mentorIds == null || mentorIds.isEmpty()) {
            throw new OrganizationDomainException(OrganizationErrorCode.STUDY_GROUP_MENTOR_REQUIRED);
        }
        if(memberIds == null || memberIds.isEmpty()) {
            throw new OrganizationDomainException(OrganizationErrorCode.STUDY_GROUP_MEMBER_REQUIRED);
        }

        StudyGroup group = StudyGroup.builder()
            .name(name)
            .gisuId(gisuId)
            .part(part)
            .build();

        mentorIds.forEach(group::addMentor);
        memberIds.forEach(group::addStudyGroupMember);

        return group;
    }

    public void addStudyGroupMember(Long memberId) {
        if (memberId == null) {
            throw new OrganizationDomainException(OrganizationErrorCode.STUDY_GROUP_MEMBER_REQUIRED);
        }
        boolean duplicate = hasMember(memberId);
        if (duplicate) {
            throw new OrganizationDomainException(OrganizationErrorCode.STUDY_GROUP_MEMBER_DUPLICATED);
        }
        this.studyGroupMembers.add(StudyGroupMember.create(this, memberId));
    }

    public boolean hasMember(Long memberId) {
        return studyGroupMembers.stream()
            .anyMatch(m -> m.getMemberId().equals(memberId));
    }

    private void addMentor(Long mentorId) {
        if(mentorId == null) {
            throw new OrganizationDomainException(OrganizationErrorCode.STUDY_GROUP_MENTOR_ID_REQUIRED);
        }
        this.studyGroupMentor.add(StudyGroupMentor.create(this, mentorId));
    }

    private static void validate(String name, Long gisuId, ChallengerPart part) {
        if (name == null || name.isBlank()) {
            throw new OrganizationDomainException(OrganizationErrorCode.STUDY_GROUP_NAME_REQUIRED);
        }
        if (gisuId == null) {
            throw new OrganizationDomainException(OrganizationErrorCode.GISU_REQUIRED);
        }
        if (part == null) {
            throw new OrganizationDomainException(OrganizationErrorCode.PART_REQUIRED);
        }
    }

    // ============ Domain Methods (Aggregate Root Pattern) ============

    public void updateName(String name) {
        if(StringUtils.hasText(name)) {
            this.name = name;
        }
    }

    public void updatePart(ChallengerPart challengerPart) {
        if(challengerPart != null) {
            this.part = challengerPart;
        }
    }

    public void validateMembersNotJoined(Set<Long> memberIds) {
        Set<Long> alreadyJoined = memberIds.stream()
            .filter(this::hasMember)
            .collect(Collectors.toSet());

        if (!alreadyJoined.isEmpty()) {
            throw new OrganizationDomainException(
                OrganizationErrorCode.STUDY_GROUP_MEMBER_DUPLICATED,
                "이미 소속된 멤버: " + alreadyJoined
            );
        }
    }

}
