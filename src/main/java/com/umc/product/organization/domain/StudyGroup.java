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
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "study_group")
public class StudyGroup extends BaseEntity {

    @OneToMany(mappedBy = "studyGroup", cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<StudyGroupMember> studyGroupMembers = new ArrayList<>();

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

    public static StudyGroup create(String name, Long gisu_id, ChallengerPart part) {
        return StudyGroup.builder()
            .name(name)
            .gisu_id(gisu_id)
            .part(part)
            .build();
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

    /**
     * 현재 스터디장 조회
     *
     * @return 스터디장 멤버
     */
    public StudyGroupMember getLeader() {
        return studyGroupMembers.stream()
            .filter(StudyGroupMember::isLeader)
            .findFirst()
            .orElseThrow(() -> new OrganizationDomainException(OrganizationErrorCode.STUDY_GROUP_LEADER_REQUIRED));
    }

    // 동일한 기수에는 동일한 파트의 스터디 그룹 2개 이상 소속될 수 없다.
    // 스터디를 만들 때 파트장은 최소 1명 이상이여야 한다.
    // 스터디 그룹을 생성할 수 있는 권한은 파트장 직책을 가지고 있다면 가능하다.


}
