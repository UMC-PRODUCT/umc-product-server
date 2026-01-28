package com.umc.product.organization.adapter.out.persistence;

import com.umc.product.organization.domain.ChapterSchool;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChapterSchoolJpaRepository extends JpaRepository<ChapterSchool, Long> {

    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM ChapterSchool cs WHERE cs.school.id IN :schoolIds")
    void deleteAllBySchoolIdIn(@Param("schoolIds") List<Long> schoolIds);

    @Query("SELECT cs FROM ChapterSchool cs " +
            "JOIN FETCH cs.chapter " +
            "JOIN FETCH cs.school " +
            "WHERE cs.chapter.gisu.id = :gisuId")
    List<ChapterSchool> findByGisuIdWithChapterAndSchool(@Param("gisuId") Long gisuId);
}
