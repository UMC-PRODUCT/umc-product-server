package com.umc.product.storage.application.port.out;

import com.umc.product.storage.domain.FileMetadata;
import java.util.Optional;

/**
 * 파일 메타데이터 조회 Port
 */
public interface LoadFileMetadataPort {

    /**
     * 파일 ID로 메타데이터를 조회합니다.
     *
     * @param fileId 파일 고유 ID
     * @return 파일 메타데이터
     */
    Optional<FileMetadata> findByFileId(String fileId);

    /**
     * 파일 ID로 메타데이터 존재 여부를 확인합니다.
     *
     * @param fileId 파일 고유 ID
     * @return 존재 여부
     */
    boolean existsByFileId(String fileId);

    /**
     * pk ID로 메타데이터 존재 여부를 확인합니다.
     *
     * @param id 파일 pk ID
     * @return 존재 여부
     */
    boolean existsById(Long id);
}
