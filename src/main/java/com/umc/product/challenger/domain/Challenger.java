package com.umc.product.challenger.domain;

import com.umc.product.challenger.domain.exception.ChallengerDomainException;
import com.umc.product.challenger.domain.exception.ChallengerErrorCode;
import com.umc.product.common.BaseEntity;
import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.common.domain.enums.ChallengerStatus;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
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
@Table(name = "challenger")
public class Challenger extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, name = "member_id")
    private Long memberId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "part")
    private ChallengerPart part;

    @Column(nullable = false, name = "gisu_id")
    private Long gisuId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "status")
    private ChallengerStatus status;

    @Column(name = "modification_reason")
    private String modificationReason;

    @Column(name = "modified_by")
    private Long modifiedBy;

    @OneToMany(
            mappedBy = "challenger",
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<ChallengerPoint> challengerPoints = new ArrayList<>();


    @Builder
    public Challenger(Long memberId, ChallengerPart part, Long gisuId) {
        this.memberId = memberId;
        this.part = part;
        this.gisuId = gisuId;
        this.status = ChallengerStatus.ACTIVE;
    }

    public void validateChallengerStatus() {
        if (this.status != ChallengerStatus.ACTIVE) {
            throw new ChallengerDomainException(ChallengerErrorCode.CHALLENGER_NOT_ACTIVE);
        }
    }

    /**
     * 챌린저의 파트를 변경합니다.
     */
    public void changePart(ChallengerPart newPart) {
        validateChallengerStatus();
        this.part = newPart;
    }

    /**
     * 챌린저의 상태를 변경합니다.
     */
    public void changeStatus(ChallengerStatus newStatus, Long modifiedBy, String reason) {
        validateChallengerStatus();
        
        this.status = newStatus;
        this.modifiedBy = modifiedBy;
        this.modificationReason = reason;
    }

    /**
     * 챌린저에게 상벌점을 추가합니다.
     */
    public void addPoint(ChallengerPoint point) {
        this.challengerPoints.add(point);
    }
}
