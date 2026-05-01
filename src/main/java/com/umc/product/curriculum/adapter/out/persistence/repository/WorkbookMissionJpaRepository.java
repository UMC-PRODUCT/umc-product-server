package com.umc.product.curriculum.adapter.out.persistence.repository;

import com.umc.product.curriculum.domain.WorkbookMission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WorkbookMissionJpaRepository extends JpaRepository<WorkbookMission, Long> {

    List<WorkbookMission> findByOriginalWorkbookId(Long originalWorkbookId);
}
