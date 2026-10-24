package com.umc.product.project.application.port.in.query;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.umc.product.project.application.port.in.query.dto.ProjectInfo;

public interface SearchDraftProjectUseCase {

    /** 로그인한 작성자의 초안을 생성 시각·ID 내림차순으로 조회한다. */
    Page<ProjectInfo> searchDrafts(Long creatorMemberId, Long gisuId, Pageable pageable);
}
