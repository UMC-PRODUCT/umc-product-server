package com.umc.product.curriculum.adapter.out.persistence;

import com.umc.product.curriculum.domain.WorkbookMission;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkbookMissionJpaRepository extends JpaRepository<WorkbookMission, Long> {

    List<WorkbookMission> findByOriginalWorkbookId(Long originalWorkbookId);
}
