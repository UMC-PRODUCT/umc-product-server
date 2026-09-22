package com.umc.product.challenger.domain;

import com.umc.product.challenger.domain.enums.PointType;
import com.umc.product.challenger.domain.exception.ChallengerDomainException;
import com.umc.product.challenger.domain.exception.ChallengerErrorCode;
import com.umc.product.common.BaseEntity;

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
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 챌린저의 상벌점 점수 입니다.
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "challenger_point")
public class ChallengerPoint extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "challenger_id", nullable = false)
    private Challenger challenger;

    @Column(nullable = false, name = "point")
    @Enumerated(EnumType.STRING)
    private PointType type;

    // CUSTOM의 직접 입력 점수이며, 과거 고정 유형에 저장된 개별 배점도 조회 호환을 위해 보존한다.
    @Column(name = "point_value")
    private Integer pointValue;

    @Column(length = 200)
    private String description;

    private ChallengerPoint(Challenger challenger, PointType type, Integer pointValue, String description) {
        validatePointValue(type, pointValue);
        this.challenger = challenger;
        this.type = type;
        this.pointValue = pointValue;
        this.description = description;
    }

    private static void validatePointValue(PointType type, Integer pointValue) {
        if (type == PointType.CUSTOM && pointValue == null) {
            throw new ChallengerDomainException(ChallengerErrorCode.CUSTOM_POINT_VALUE_REQUIRED);
        }
        // 기존 앱은 고정 유형에도 배점을 보내므로 기본값과 같은 입력은 허용한다.
        // 잘못된 배점을 막는 검증은 프론트 수정 이후에도 유지해야 한다.
        if (type != PointType.CUSTOM && pointValue != null
            && Double.compare(pointValue.doubleValue(), type.getValue()) != 0) {
            throw new ChallengerDomainException(ChallengerErrorCode.INVALID_POINT_VALUE);
        }
    }

    /**
     * 새로운 상벌점을 생성합니다.
     */
    public static ChallengerPoint create(Challenger challenger, PointType type, String description) {
        return new ChallengerPoint(challenger, type, null, description);
    }

    public static ChallengerPoint create(
        Challenger challenger, PointType type, Integer pointValue,
        String description
    ) {
        return new ChallengerPoint(challenger, type, pointValue, description);
    }

    public Long getChallengerId() {
        return challenger.getId();
    }

    /**
     * 상벌점의 설명을 수정합니다.
     */
    public void updateDescription(String newDescription) {
        this.description = newDescription;
    }

    public Double getPointValue() {
        // 과거 고정 유형의 pointValue가 기본 배점과 달라도 당시 점수와 총점을 보존해야 한다.
        // 이 우선 조회는 과거 실배점을 보존하는 데이터 이관이 완료되기 전에는 제거하지 않는다.
        // CUSTOM 점수에 필요한 pointValue 컬럼 자체는 제거 대상이 아니다.
        return pointValue != null
            ? pointValue
            : type.getValue();
    }
}
