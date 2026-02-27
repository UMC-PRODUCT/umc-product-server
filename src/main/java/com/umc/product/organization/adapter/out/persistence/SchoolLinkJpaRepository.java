package com.umc.product.organization.adapter.out.persistence;

import com.umc.product.organization.domain.SchoolLink;
import java.util.List;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

public interface SchoolLinkJpaRepository extends Repository<SchoolLink, Long> {

    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM SchoolLink sl WHERE sl.school.id IN :schoolIds")
    void deleteAllBySchoolIdIn(@Param("schoolIds") List<Long> schoolIds);
}
