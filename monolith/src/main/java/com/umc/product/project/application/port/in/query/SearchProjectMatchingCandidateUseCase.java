package com.umc.product.project.application.port.in.query;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.project.application.port.in.query.dto.ProjectMatchingCandidateInfo;

public interface SearchProjectMatchingCandidateUseCase {
    Page<ProjectMatchingCandidateInfo> search(Long projectId, Long requesterMemberId, ChallengerPart part,
        String keyword, Pageable pageable);
}
