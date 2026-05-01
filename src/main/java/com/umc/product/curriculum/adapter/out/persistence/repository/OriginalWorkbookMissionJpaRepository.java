package com.umc.product.curriculum.adapter.out.persistence.repository;

import com.umc.product.curriculum.domain.OriginalWorkbookMission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OriginalWorkbookMissionJpaRepository extends JpaRepository<OriginalWorkbookMission, Long> {

    List<OriginalWorkbookMission> findByOriginalWorkbookIdIn(List<Long> originalWorkbookIds);
}
