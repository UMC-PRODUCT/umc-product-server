package com.umc.product.organization.domain;

import com.umc.product.common.BaseEntity;
import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.global.exception.BusinessException;
import com.umc.product.global.exception.constant.Domain;
import com.umc.product.organization.exception.OrganizationErrorCode;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "study_group")
public class StudyGroup extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gisu_id")
    private Gisu gisu;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ChallengerPart part;

    @OneToMany(mappedBy = "studyGroup", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StudyGroupMember> studyGroupMembers = new ArrayList<>();

    @Builder(access = AccessLevel.PRIVATE)
    private StudyGroup(String name, Gisu gisu, ChallengerPart part) {
        validate(name, gisu, part);
        this.name = name;
        this.gisu = gisu;
        this.part = part;
    }

    public static StudyGroup create(String name, Gisu gisu, ChallengerPart part) {
        return StudyGroup.builder()
            .name(name)
            .gisu(gisu)
            .part(part)
            .build();
    }

    private static void validate(String name, Gisu gisu, ChallengerPart part) {
        if (name == null || name.isBlank()) {
            throw new BusinessException(Domain.COMMON, OrganizationErrorCode.STUDY_GROUP_NAME_REQUIRED);
        }
        if (gisu == null) {
            throw new BusinessException(Domain.COMMON, OrganizationErrorCode.GISU_REQUIRED);
        }
        if (part == null) {
            throw new BusinessException(Domain.COMMON, OrganizationErrorCode.PART_REQUIRED);
        }
    }

    // ============ Domain Methods (Aggregate Root Pattern) ============

    /**
     * 스터디 그룹에 멤버 추가
     *
     * @param challengerId 추가할 챌린저 ID
     * @param isLeader     리더 여부
     * @throws BusinessException 이미 존재하는 멤버인 경우
     */
    public void addMember(Long challengerId, boolean isLeader) {
        validateMemberNotExists(challengerId);
        StudyGroupMember member = StudyGroupMember.create(this, challengerId, isLeader);
        studyGroupMembers.add(member);
    }

    /**
     * 스터디 그룹에 일반 멤버 추가
     *
     * @param challengerId 추가할 챌린저 ID
     * @throws BusinessException 이미 존재하는 멤버인 경우
     */
    public void addMember(Long challengerId) {
        addMember(challengerId, false);
    }

    /**
     * 스터디 그룹에서 멤버 제거
     *
     * @param challengerId 제거할 챌린저 ID
     * @throws BusinessException 멤버를 찾을 수 없는 경우
     */
    public void removeMember(Long challengerId) {
        StudyGroupMember member = findMemberByChallengerId(challengerId);
        studyGroupMembers.remove(member);
    }

    /**
     * 스터디 그룹 멤버 목록 전체 교체
     *
     * @param challengerIds 새로운 멤버 ID 목록
     */
    public void updateMembers(Set<Long> challengerIds) {
        studyGroupMembers.clear();
        if (challengerIds != null) {
            challengerIds.forEach(this::addMember);
        }
    }

    /**
     * 스터디 그룹 이름 변경
     *
     * @param newName 새로운 이름
     */
    public void updateName(String newName) {
        if (newName == null || newName.isBlank()) {
            throw new BusinessException(Domain.COMMON, OrganizationErrorCode.STUDY_GROUP_NAME_REQUIRED);
        }
        this.name = newName;
    }

    /**
     * 스터디 그룹 파트 변경
     *
     * @param newPart 새로운 파트
     */
    public void updatePart(ChallengerPart newPart) {
        if (newPart == null) {
            throw new BusinessException(Domain.COMMON, OrganizationErrorCode.PART_REQUIRED);
        }
        this.part = newPart;
    }

    /**
     * 리더 지정
     *
     * @param challengerId 리더로 지정할 챌린저 ID
     * @throws BusinessException 멤버를 찾을 수 없는 경우
     */
    public void assignLeader(Long challengerId) {
        // 기존 리더 해제
        getLeader().ifPresent(StudyGroupMember::removeAsLeader);
        // 새 리더 지정
        findMemberByChallengerId(challengerId).assignAsLeader();
    }

    /**
     * 현재 리더 조회
     *
     * @return 리더 멤버 (없으면 empty)
     */
    public Optional<StudyGroupMember> getLeader() {
        return studyGroupMembers.stream()
            .filter(StudyGroupMember::isLeader)
            .findFirst();
    }

    /**
     * 특정 챌린저가 이 그룹의 멤버인지 확인
     *
     * @param challengerId 확인할 챌린저 ID
     * @return 멤버 여부
     */
    public boolean hasMember(Long challengerId) {
        return studyGroupMembers.stream()
            .anyMatch(member -> member.getChallengerId().equals(challengerId));
    }

    /**
     * 멤버 수 조회
     *
     * @return 현재 멤버 수
     */
    public int getMemberCount() {
        return studyGroupMembers.size();
    }

    private void validateMemberNotExists(Long challengerId) {
        if (hasMember(challengerId)) {
            throw new BusinessException(Domain.COMMON, OrganizationErrorCode.STUDY_GROUP_MEMBER_ALREADY_EXISTS);
        }
    }

    private StudyGroupMember findMemberByChallengerId(Long challengerId) {
        return studyGroupMembers.stream()
            .filter(member -> member.getChallengerId().equals(challengerId))
            .findFirst()
            .orElseThrow(
                () -> new BusinessException(Domain.COMMON, OrganizationErrorCode.STUDY_GROUP_MEMBER_NOT_FOUND));
    }

}
