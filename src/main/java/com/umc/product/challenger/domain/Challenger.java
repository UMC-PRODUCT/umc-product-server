package com.umc.product.challenger.domain;

import com.umc.product.challenger.domain.enums.ChallengerPart;
import com.umc.product.challenger.domain.enums.ChallengerStatus;
import com.umc.product.challenger.domain.exception.ChallengerDomainException;
import com.umc.product.challenger.domain.exception.ChallengerErrorCode;
import com.umc.product.common.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
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

    @Column(nullable = false, name = "part")
    private ChallengerPart part;

    @Column(nullable = false, name = "gisu_id")
    private Long gisuId;

    @Column(nullable = false, name = "status")
    private ChallengerStatus status;

    @OneToMany(
            mappedBy = "challenger",
            fetch = FetchType.LAZY,           // 명시
            cascade = CascadeType.ALL,        // 명시
            orphanRemoval = true              // 명시
    )
    private List<ChallengerPoint> challengerPoint = new ArrayList<>();

    public Challenger(Long memberId, ChallengerPart part, Long gisuId) {
        this.memberId = memberId;
        this.part = part;
        this.gisuId = gisuId;
        this.status = ChallengerStatus.ACTIVE;
    }

    public void validateChallengerStatus() {
        if (this.status != ChallengerStatus.ACTIVE) {
            throw new ChallengerDomainException(ChallengerErrorCode.CHALLENGER_NOT_ACTIVE)
        }
    }
}
