package com.umc.product.curriculum.domain;

import com.umc.product.challenger.domain.enums.ChallengerPart;
import com.umc.product.common.BaseEntity;
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
@Table(name = "curriculum")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Curriculum extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long gisuId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ChallengerPart part;

    @OneToMany(mappedBy = "curriculum", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OriginalWorkbook> originalWorkbooks = new ArrayList<>();

    @Builder
    private Curriculum(Long gisuId, ChallengerPart part) {
        this.gisuId = gisuId;
        this.part = part;
    }
}
