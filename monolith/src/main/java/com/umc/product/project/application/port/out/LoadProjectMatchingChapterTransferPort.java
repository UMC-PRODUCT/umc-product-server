package com.umc.product.project.application.port.out;

import java.util.List;
import java.util.Optional;

import com.umc.product.project.domain.ProjectMatchingChapterTransfer;

public interface LoadProjectMatchingChapterTransferPort {

    /**
     * 이동 기록을 조회합니다. 없으면 {@code CHAPTER_TRANSFER_NOT_FOUND}.
     */
    ProjectMatchingChapterTransfer getById(Long id);

    /**
     * 기수·회원의 활성(미해제) 이동을 조회합니다.
     */
    Optional<ProjectMatchingChapterTransfer> findActiveByGisuIdAndMemberId(Long gisuId, Long memberId);

    /**
     * 특정 지부로 들어오거나 특정 지부에서 나간 활성 이동을 모두 조회합니다. 지부는 기수에 종속되므로 기수 조건은 따로 받지 않습니다.
     */
    List<ProjectMatchingChapterTransfer> listActiveByChapterId(Long chapterId);
}
