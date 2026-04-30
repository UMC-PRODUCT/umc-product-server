package com.umc.product.curriculum.adapter.out.persistence;

import com.umc.product.curriculum.domain.OriginalWorkbookMission;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OriginalWorkbookMissionJpaRepository extends JpaRepository<OriginalWorkbookMission, Long> {

    List<OriginalWorkbookMission> findByOriginalWorkbookId(Long originalWorkbookId);
}
