package com.umc.product.curriculum.adapter.out.persistence;

import com.umc.product.curriculum.domain.Curriculum;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CurriculumJpaRepository extends JpaRepository<Curriculum, Long> {

//    @Query("SELECT c FROM Curriculum c left join fetch c.originalWorkbooks WHERE c.part = :part AND c.gisuId = :gisuId")
//    Optional<Curriculum> findByGisuIdAndPart(@Param("gisuId") Long gisuId, @Param("part") ChallengerPart part);
//
//    @Query("SELECT c FROM Curriculum c left join fetch c.originalWorkbooks WHERE c.id = :id")
//    Optional<Curriculum> findById(Long id);
}
