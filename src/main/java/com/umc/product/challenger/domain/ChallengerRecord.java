package com.umc.product.challenger.domain;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.stream.IntStream;

import com.umc.product.challenger.domain.exception.ChallengerDomainException;
import com.umc.product.challenger.domain.exception.ChallengerErrorCode;
import com.umc.product.common.BaseEntity;
import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.common.domain.enums.ChallengerRoleType;
import com.umc.product.common.domain.enums.OrganizationType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "challenger_record")
public class ChallengerRecord extends BaseEntity {
    private static final SecureRandom CODE_RANDOM = new SecureRandom();

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 6)
    private String code;

    @Column(name = "member_name", length = 30)
    private String memberName;

    @Column(nullable = false, name = "created_member_id")
    private Long createdMemberId;

    @Column(nullable = false, name = "gisu_id")
    private Long gisuId;

    @Column(name = "chapter_id")
    private Long chapterId;

    @Column(nullable = false, name = "school_id")
    private Long schoolId;

    @Enumerated(EnumType.STRING)
    @Column(name = "part")
    private ChallengerPart part;

    @Column(nullable = false, name = "infra")
    private boolean infra;

    @Column(name = "challenger_role_type")
    @Enumerated(EnumType.STRING)
    private ChallengerRoleType challengerRoleType;

    @Column(name = "organization_id")
    private Long organizationId;

    @Column(nullable = false, name = "is_used")
    private boolean isUsed;

    @Column(name = "used_member_id")
    private Long usedMemberId;

    @Column(name = "used_at")
    private Instant usedAt;

    public static ChallengerRecord create(
        Long createdMemberId, Long gisuId, Long chapterId, Long schoolId,
        ChallengerPart part, String memberName
    ) {
        return create(createdMemberId, gisuId, chapterId, schoolId, part, false, memberName);
    }

    public static ChallengerRecord create(
        Long createdMemberId, Long gisuId, Long chapterId, Long schoolId,
        ChallengerPart part, boolean infra, String memberName
    ) {
        return createRecord(createdMemberId, gisuId, chapterId, schoolId, part, infra, memberName, null, null);
    }

    public static ChallengerRecord createAdmin(
        Long createdMemberId, Long gisuId, Long chapterId, Long schoolId,
        ChallengerPart part, String memberName,
        ChallengerRoleType challengerRoleType, Long organizationId
    ) {
        return createAdmin(createdMemberId, gisuId, chapterId, schoolId, part, false, memberName,
            challengerRoleType, organizationId);
    }

    public static ChallengerRecord createAdmin(
        Long createdMemberId, Long gisuId, Long chapterId, Long schoolId,
        ChallengerPart part, boolean infra, String memberName,
        ChallengerRoleType challengerRoleType, Long organizationId
    ) {
        return createRecord(createdMemberId, gisuId, chapterId, schoolId, part, infra, memberName,
            challengerRoleType, organizationId);
    }

    private static ChallengerRecord createRecord(
        Long createdMemberId, Long gisuId, Long chapterId, Long schoolId,
        ChallengerPart part, boolean infra, String memberName,
        ChallengerRoleType challengerRoleType, Long organizationId
    ) {
        if (part != null && !part.canBeAssignedToChallenger()) {
            throw new ChallengerDomainException(ChallengerErrorCode.INVALID_CHALLENGER_RECORD_CREATE_REQUEST);
        }
        if (infra && (part == null || !part.canHaveInfra())) {
            throw new ChallengerDomainException(ChallengerErrorCode.INVALID_CHALLENGER_RECORD_CREATE_REQUEST);
        }
        // 운영진 코드가 아닌 수강 코드는 파트가 필수다.
        if (challengerRoleType == null && part == null) {
            throw new ChallengerDomainException(ChallengerErrorCode.INVALID_CHALLENGER_RECORD_CREATE_REQUEST);
        }
        ChallengerRecord record = new ChallengerRecord();
        record.code = generateUniqueCode();
        record.createdMemberId = createdMemberId;
        record.gisuId = gisuId;
        record.chapterId = chapterId;
        record.schoolId = schoolId;
        record.memberName = memberName;
        record.part = part;
        record.infra = infra;
        record.challengerRoleType = challengerRoleType;
        record.organizationId = organizationId;
        if (chapterId == null && !record.canOmitChapter()) {
            throw new ChallengerDomainException(ChallengerErrorCode.INVALID_CHALLENGER_RECORD_CREATE_REQUEST,
                "수강하지 않는 중앙 운영진 코드만 지부를 생략할 수 있습니다.");
        }
        return record;
    }

    private static String generateUniqueCode() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        return IntStream.range(0, 6)
            .map(i -> chars.charAt(CODE_RANDOM.nextInt(chars.length())))
            .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
            .toString();
    }

    public boolean isAdminRecord() {
        return this.challengerRoleType != null;
    }

    public boolean canOmitChapter() {
        return isAdminRecord() && challengerRoleType.organizationType() == OrganizationType.CENTRAL
            && part == null;
    }

    /**
     * 코드의 학습 정보 유효성을 검증한다. 새 모델은 단일 part + infra이며 기수 학습 유형과 무관하다.
     */
    public void validateLearningSelection() {
        if (infra && (part == null || !part.canHaveInfra())) {
            throw new ChallengerDomainException(ChallengerErrorCode.INVALID_CHALLENGER_RECORD_CREATE_REQUEST);
        }
        if (!isAdminRecord() && part == null) {
            throw new ChallengerDomainException(ChallengerErrorCode.INVALID_CHALLENGER_RECORD_CREATE_REQUEST,
                "학습 파트를 선택해주세요.");
        }
    }

    public void markAsUsed(Long usedMemberId) {
        validateNotUsed();

        this.isUsed = true;
        this.usedMemberId = usedMemberId;
        this.usedAt = Instant.now();
    }

    public void validateNotUsed() {
        if (this.isUsed) {
            throw new ChallengerDomainException(ChallengerErrorCode.USED_CHALLENGER_RECORD_CODE);
        }
    }

    public void validateMember(String memberName, Long schoolId) {
        if (this.memberName == null || !this.memberName.equals(memberName)) {
            throw new ChallengerDomainException(ChallengerErrorCode.INVALID_MEMBER_NAME_FOR_RECORD);
        }

        if (!this.schoolId.equals(schoolId)) {
            throw new ChallengerDomainException(ChallengerErrorCode.INVALID_SCHOOL_FOR_RECORD);
        }
    }
}
