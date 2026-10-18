package com.umc.product.project.application.port.out;

import java.util.List;

import com.umc.product.project.domain.ProjectMatchingChapterTransfer;

public interface SaveProjectMatchingChapterTransferPort {

    ProjectMatchingChapterTransfer save(ProjectMatchingChapterTransfer transfer);

    List<ProjectMatchingChapterTransfer> saveAll(List<ProjectMatchingChapterTransfer> transfers);
}
