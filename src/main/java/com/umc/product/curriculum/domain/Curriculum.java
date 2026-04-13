package com.umc.product.curriculum.domain;

import com.umc.product.common.BaseEntity;
import com.umc.product.common.domain.enums.ChallengerPart;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.flywaydb.core.internal.util.StringUtils;

/**
 * Curriculum과 WeeklyCurriculum는 하나의 Aggregate
 * Curriculum이 WeeklyCurriculum을 관리하는 형태로 설계 예정
 */
@Entity
// TODO: gisuId, part 조합에 대한 unique 제약조건 추가 flyway까지 추가해야 되는데 일단 보류
//@Table(name = "curriculum",
//    uniqueConstraints = @UniqueConstraint(
//        name = "uk_curriculum_gisu_id_part",
//        columnNames = {"gisu_id", "part"}
//    )
//)
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

    @Column(nullable = false)
    private String title;

    @Builder(access = AccessLevel.PRIVATE)
    private Curriculum(Long gisuId, ChallengerPart part, String title) {
        this.gisuId = gisuId;
        this.part = part;
        this.title = title;
    }

    public static Curriculum create(Long gisuId, ChallengerPart part, String title) {
        return Curriculum.builder().gisuId(gisuId).part(part).title(title).build();
    }

    public void updateTitle(String title) {
        if (StringUtils.hasText(title)) {
            this.title = title;
        }
    }

    // TODO: updatePart가 필요할수도

}
