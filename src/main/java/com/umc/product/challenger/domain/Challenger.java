package com.umc.product.challenger.domain;

import com.umc.product.challenger.domain.exception.ChallengerDomainException;
import com.umc.product.challenger.domain.exception.ChallengerErrorCode;
import com.umc.product.common.BaseEntity;
import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.common.domain.enums.ChallengerStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
    name = "challenger",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_challenger_member_id_gisu_id",
        columnNames = {"member_id", "gisu_id"}
    )
)
public class Challenger extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, name = "member_id")
    private Long memberId;

    @Enumerated(EnumType.STRING)
    @Column(name = "part")
    private ChallengerPart part;

    /**
     * 개발 파트 위에 부가로 수강하는 인프라 트랙 여부. 개발 파트(웹/모바일 프로덕트 엔지니어)에만 true가 될 수 있다.
     */
    @Column(nullable = false, name = "infra")
    private boolean infra;

    @Column(nullable = false, name = "gisu_id")
    private Long gisuId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "status")
    private ChallengerStatus status;

    @Column(name = "modification_reason")
    private String modificationReason;

    @Column(name = "modified_by")
    private Long modifiedBy;

    @Builder
    public Challenger(Long memberId, ChallengerPart part, boolean infra, Long gisuId) {
        if (part == null || !part.canBeAssignedToChallenger()) {
            throw new ChallengerDomainException(ChallengerErrorCode.CHALLENGER_PART_NOT_FOUND);
        }
        if (infra && !part.canHaveInfra()) {
            throw new ChallengerDomainException(ChallengerErrorCode.INVALID_CHALLENGER_LEARNING_TYPE);
        }
        this.memberId = memberId;
        this.part = part;
        this.infra = infra;
        this.gisuId = gisuId;
        this.status = ChallengerStatus.ACTIVE;
    }

    public Challenger(Long memberId, ChallengerPart part, Long gisuId) {
        this(memberId, part, false, gisuId);
    }

    /**
     * 수강 없이 기수에 소속된 챌린저를 생성한다. 운영진 권한은 별도 역할로 부여한다.
     */
    public static Challenger createWithoutEnrollment(Long memberId, Long gisuId) {
        Challenger challenger = new Challenger();
        challenger.memberId = memberId;
        challenger.gisuId = gisuId;
        challenger.status = ChallengerStatus.ACTIVE;
        challenger.infra = false;
        return challenger;
    }

    public void validateChallengerStatus() {
        if (this.status != ChallengerStatus.ACTIVE) {
            throw new ChallengerDomainException(ChallengerErrorCode.CHALLENGER_NOT_ACTIVE);
        }
    }

    /**
     * 챌린저의 파트를 변경합니다. 새 파트가 infra를 얹을 수 없는 파트면 infra는 해제됩니다.
     */
    public void changePart(ChallengerPart newPart) {
        validateChallengerStatus();
        if (newPart == null || !newPart.canBeAssignedToChallenger()) {
            throw new ChallengerDomainException(ChallengerErrorCode.CHALLENGER_PART_NOT_FOUND);
        }
        this.part = newPart;
        if (!newPart.canHaveInfra()) {
            this.infra = false;
        }
    }

    /**
     * 개발 파트 챌린저에게 인프라 부가 트랙을 활성화합니다.
     */
    public void enableInfra() {
        validateChallengerStatus();
        if (this.part == null || !this.part.canHaveInfra()) {
            throw new ChallengerDomainException(ChallengerErrorCode.INVALID_CHALLENGER_LEARNING_TYPE);
        }
        this.infra = true;
    }

    /**
     * 코드로 전달된 학습 정보를 기존 챌린저에 반영합니다. 파트는 단일이므로 아직 파트가 없을 때만 설정하고,
     * infra는 개발 파트일 때만 추가로 활성화합니다. 실제로 변경이 발생하면 {@code true}를 반환합니다.
     */
    public boolean applyLearning(ChallengerPart newPart, boolean newInfra) {
        boolean changed = false;
        if (newPart != null && this.part != null && this.part != newPart) {
            throw new ChallengerDomainException(ChallengerErrorCode.INVALID_CHALLENGER_LEARNING_TYPE,
                "챌린저는 하나의 파트에만 소속될 수 있습니다.");
        }
        if (newPart != null && this.part == null) {
            changePart(newPart);
            changed = true;
        }
        if (newInfra && !this.infra && this.part != null && this.part.canHaveInfra()) {
            enableInfra();
            changed = true;
        }
        return changed;
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
}
