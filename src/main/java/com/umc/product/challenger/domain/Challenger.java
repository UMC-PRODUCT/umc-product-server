package com.umc.product.challenger.domain;

import com.umc.product.challenger.domain.enums.ChallengerPart;
import com.umc.product.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "challenger")
public class Challenger extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, name = "member_id")
    private Long memberId;

    @Enumerated
    @Column(nullable = false, name = "part")
    private ChallengerPart part;

    @Column(nullable = false, name = "gisu_id")
    private Long gisuId;

    public Challenger(Long memberId, ChallengerPart part, Long gisuId) {
        this.memberId = memberId;
        this.part = part;
        this.gisuId = gisuId;
    }

}
