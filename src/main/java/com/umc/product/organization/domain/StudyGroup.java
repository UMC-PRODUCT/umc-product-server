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
    private final List<StudyGroupOrganizer> studyGroupOrganizer = new ArrayList<>();

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(nullable = false)
    private Long gisu_id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ChallengerPart part;

    @Builder(access = AccessLevel.PRIVATE)
    private StudyGroup(String name, Long gisu_id, ChallengerPart part) {
        validate(name, gisu_id, part);
        this.name = name;
        this.gisu_id = gisu_id;
        this.part = part;
    }

    public static StudyGroup create(String name, Long gisu_id, ChallengerPart part, Set<Long> organizerIds, Set<Long> memberIds) {
        if(organizerIds == null || organizerIds.isEmpty()) {
            throw new OrganizationDomainException(OrganizationErrorCode.STUDY_GROUP_ORGANIZER_REQUIRED);
        }
        if(memberIds == null || memberIds.isEmpty()) {
            throw new OrganizationDomainException(OrganizationErrorCode.STUDY_GROUP_MEMBER_REQUIRED);
        }

        StudyGroup group = StudyGroup.builder()
            .name(name)
            .gisu_id(gisu_id)
            .part(part)
            .build();

        organizerIds.forEach(group::addOrganizer);
        memberIds.forEach(group::addStudyGroupMember);

        return group;
    }

    private void addStudyGroupMember(Long StudyGroupMemberId) {
        this.studyGroupMembers.add(StudyGroupMember.create(this, StudyGroupMemberId));
    }

    private void addOrganizer(Long organizerId) {
        if(organizerId == null) {
            throw new OrganizationDomainException(OrganizationErrorCode.STUDY_GROUP_ORGANIZER_ID_REQUIRED);
        }
        this.studyGroupOrganizer.add(StudyGroupOrganizer.create(this, organizerId));
    }

    private static void validate(String name, Long gisu_id, ChallengerPart part) {
        if (name == null || name.isBlank()) {
            throw new OrganizationDomainException(OrganizationErrorCode.STUDY_GROUP_NAME_REQUIRED);
        }
        if (gisu_id == null) {
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
        this.part = challengerPart;
    }

    // 동일한 기수에는 동일한 파트의 스터디 그룹 2개 이상 소속될 수 없다.
    // 스터디 그룹을 생성할 수 있는 권한은 파트장 직책을 가지고 있다면 가능하다.

}
