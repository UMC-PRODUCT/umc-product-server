package com.umc.product.recruitment.adapter.out;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.recruitment.domain.ApplicationPartPreference;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ApplicationPartPreferenceJpaRepository extends JpaRepository<ApplicationPartPreference, Long> {

    List<ApplicationPartPreference> findAllByApplicationIdOrderByPriorityAsc(Long applicationId);

    List<ApplicationPartPreference> findByApplicationId(Long applicationId);

    @Query("""
                select (count(appPref) > 0)
                from ApplicationPartPreference appPref
                where appPref.application.id = :applicationId
                  and appPref.recruitmentPart.part = :part
                  and appPref.recruitmentPart.status = 'OPEN'
            """)
    boolean existsPreferredOpenPart(Long applicationId, ChallengerPart part);

}
