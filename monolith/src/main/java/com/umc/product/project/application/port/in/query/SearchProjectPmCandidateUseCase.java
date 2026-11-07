package com.umc.product.project.application.port.in.query;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.umc.product.project.application.port.in.query.dto.ProjectPmCandidateInfo;

public interface SearchProjectPmCandidateUseCase {
    Page<ProjectPmCandidateInfo> search(Long requesterMemberId, Long gisuId, String keyword, Pageable pageable);
}
