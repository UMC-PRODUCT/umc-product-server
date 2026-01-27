package com.umc.product.curriculum.adapter.out.persistence;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.curriculum.domain.Curriculum;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CurriculumJpaRepository extends JpaRepository<Curriculum, Long> {

    @Query("SELECT c FROM Curriculum c left join fetch c.originalWorkbooks WHERE c.part = :part " +
            "AND c.gisuId = (SELECT g.id FROM Gisu g WHERE g.isActive = true)")
    Optional<Curriculum> findByActiveGisuAndPart(@Param("part") ChallengerPart part);

    @Query("SELECT c FROM Curriculum c left join fetch c.originalWorkbooks WHERE c.id = :id")
    Optional<Curriculum> findById(Long id);
}
