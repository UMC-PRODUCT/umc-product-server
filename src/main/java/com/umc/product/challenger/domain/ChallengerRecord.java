package com.umc.product.challenger.domain;

import com.umc.product.challenger.domain.exception.ChallengerDomainException;
import com.umc.product.challenger.domain.exception.ChallengerErrorCode;
import com.umc.product.common.BaseEntity;
import com.umc.product.common.domain.enums.ChallengerPart;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "challenger_record")
public class ChallengerRecord extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 6)
    private String code;

    @Column(nullable = false, name = "created_member_id")
    private Long createdMemberId;

    @Column(nullable = false, name = "gisu_id")
    private Long gisuId;

    @Column(nullable = false, name = "chapter_id")
    private Long chapterId;

    @Column(nullable = false, name = "school_id")
    private Long schoolId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "part")
    private ChallengerPart part;

    @Column(nullable = false, name = "is_used")
    private boolean isUsed;

    @Column(name = "used_member_id")
    private Long usedMemberId;

    @Column(name = "used_at")
    private Instant usedAt;

    public static ChallengerRecord create(
        Long createdMemberId, Long gisuId, Long chapterId, Long schoolId,
        ChallengerPart part
    ) {
        ChallengerRecord record = new ChallengerRecord();

        record.code = generateUniqueCode();
        record.createdMemberId = createdMemberId;
        record.part = part;
        record.gisuId = gisuId;
        record.schoolId = schoolId;
        record.chapterId = chapterId;
        record.isUsed = false; // 생성 시에는 사용되지 않은 상태로 시작

        return record;
    }

    private static String generateUniqueCode() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        ThreadLocalRandom random = ThreadLocalRandom.current();

        return IntStream.range(0, 6)
            .map(i -> chars.charAt(random.nextInt(chars.length())))
            .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
            .toString();
    }

    public void markAsUsed(Long memberId) {
        validateNotUsed();

        this.isUsed = true;
        this.usedMemberId = memberId;
        this.usedAt = Instant.now();
    }

    public void validateNotUsed() {
        if (this.isUsed) {
            throw new ChallengerDomainException(ChallengerErrorCode.USED_CHALLENGER_RECORD_CODE);
        }
    }
}
