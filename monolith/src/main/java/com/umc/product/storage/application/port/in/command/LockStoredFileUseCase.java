package com.umc.product.storage.application.port.in.command;

import java.util.List;

import com.umc.product.storage.application.port.in.query.dto.FileMetadataInfo;

public interface LockStoredFileUseCase {

    List<FileMetadataInfo> lockUsableByIds(List<String> fileIds, Long memberId);
}
