package com.umc.product.project.adapter.out.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.umc.product.project.application.port.out.LoadProjectMatchingChapterTransferPort;
import com.umc.product.project.application.port.out.SaveProjectMatchingChapterTransferPort;
import com.umc.product.project.domain.ProjectMatchingChapterTransfer;
import com.umc.product.project.domain.exception.ProjectDomainException;
import com.umc.product.project.domain.exception.ProjectErrorCode;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ProjectMatchingChapterTransferPersistenceAdapter
    implements LoadProjectMatchingChapterTransferPort, SaveProjectMatchingChapterTransferPort {

    private final ProjectMatchingChapterTransferJpaRepository jpaRepository;

    @Override
    public ProjectMatchingChapterTransfer getById(Long id) {
        return jpaRepository.findById(id)
            .orElseThrow(() -> new ProjectDomainException(ProjectErrorCode.CHAPTER_TRANSFER_NOT_FOUND));
    }

    @Override
    public Optional<ProjectMatchingChapterTransfer> findActiveByGisuIdAndMemberId(Long gisuId, Long memberId) {
        return jpaRepository.findByGisuIdAndMemberIdAndReleasedAtIsNull(gisuId, memberId);
    }

    @Override
    public List<ProjectMatchingChapterTransfer> listActiveByChapterId(Long chapterId) {
        return jpaRepository.findActiveByChapterId(chapterId);
    }

    @Override
    public ProjectMatchingChapterTransfer save(ProjectMatchingChapterTransfer transfer) {
        return jpaRepository.save(transfer);
    }

    @Override
    public List<ProjectMatchingChapterTransfer> saveAll(List<ProjectMatchingChapterTransfer> transfers) {
        return jpaRepository.saveAll(transfers);
    }
}
