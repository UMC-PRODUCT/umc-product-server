package com.umc.product.organization.adapter.out.persistence;

import com.umc.product.organization.application.port.in.query.dto.SchoolDetailInfo;
import com.umc.product.organization.application.port.in.query.dto.SchoolNameInfo;
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

    @Query("SELECT new com.umc.product.organization.application.port.in.query.dto.SchoolNameInfo(s.id, s.name) FROM School s ORDER BY s.name ASC")
    List<SchoolNameInfo> findAllNameInfoOrderByNameAsc();

    @Query("SELECT new com.umc.product.organization.application.port.in.query.dto.SchoolDetailInfo$SchoolLinkItem(sl.title, sl.type, sl.url) FROM SchoolLink sl WHERE sl.school.id = :schoolId")
    List<SchoolDetailInfo.SchoolLinkItem> findLinksBySchoolId(@Param("schoolId") Long schoolId);

    List<School> findAllByIdIn(List<Long> ids);

    @Query("SELECT s FROM School s " +
            "WHERE s.id NOT IN (" +
            "    SELECT cs.school.id FROM ChapterSchool cs " +
            "    WHERE cs.chapter.gisu.id = :gisuId" +
            ")")
    List<School> findUnassignedByGisuId(@Param("gisuId") Long gisuId);

    boolean existsById(Long schoolId);
}
