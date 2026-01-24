package com.umc.product.storage.application.port.out;

import com.umc.product.storage.domain.FileMetadata;

/**
 * 파일 메타데이터 저장 Port
 */
public interface SaveFileMetadataPort {

    /**
     * 파일 메타데이터를 저장합니다.
     *
     * @param fileMetadata 파일 메타데이터
     * @return 저장된 파일 메타데이터
     */
    FileMetadata save(FileMetadata fileMetadata);

    /**
     * 파일 메타데이터를 삭제합니다.
     *
     * @param fileId 파일 고유 ID
     */
    void deleteByFileId(String fileId);
}
