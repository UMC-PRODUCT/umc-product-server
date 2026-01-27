package com.umc.product.recruitment.adapter.out;

import com.umc.product.recruitment.domain.ApplicationPartPreference;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApplicationPartPreferenceJpaRepository extends JpaRepository<ApplicationPartPreference, Long> {

    List<ApplicationPartPreference> findAllByApplicationIdOrderByPriorityAsc(Long applicationId);
}
