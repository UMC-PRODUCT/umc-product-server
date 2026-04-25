package com.umc.product.organization.adapter.out.persistence;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.organization.domain.StudyGroup;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StudyGroupJpaRepository extends JpaRepository<StudyGroup, Long> {

    Optional<StudyGroup> findByName(String name);

    @Query("SELECT sg.id FROM StudyGroup sg WHERE sg.gisuId = :gisuId AND sg.part IN :parts")
    List<Long> findIdsByGisuIdAndPartIn(@Param("gisuId") Long gisuId, @Param("parts") Set<ChallengerPart> parts);
}
