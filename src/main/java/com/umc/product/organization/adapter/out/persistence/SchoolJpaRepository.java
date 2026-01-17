package com.umc.product.organization.adapter.out.persistence;

import com.umc.product.organization.domain.School;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

public interface SchoolJpaRepository extends Repository<School, Long> {

    School save(School school);

    @Modifying
    @Query("DELETE FROM School s WHERE s.id IN :ids")
    void deleteAllByIdIn(@Param("ids") List<Long> ids);

    Optional<School> findById(Long schoolId);

    @Query("SELECT DISTINCT s FROM School s " +
            "LEFT JOIN FETCH s.chapterSchools cs " +
            "LEFT JOIN FETCH cs.chapter c " +
            "LEFT JOIN FETCH c.gisu " +
            "WHERE s.id = :schoolId")
    Optional<School> findByIdWithDetails(@Param("schoolId") Long schoolId);
}
