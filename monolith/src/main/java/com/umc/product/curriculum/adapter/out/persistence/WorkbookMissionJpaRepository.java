package com.umc.product.curriculum.adapter.out.persistence;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.umc.product.curriculum.domain.WorkbookMission;

public interface WorkbookMissionJpaRepository extends JpaRepository<WorkbookMission, Long> {

    List<WorkbookMission> findByOriginalWorkbookId(Long originalWorkbookId);
}
