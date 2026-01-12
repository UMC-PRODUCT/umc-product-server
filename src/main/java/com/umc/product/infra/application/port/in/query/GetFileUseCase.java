package com.umc.product.infra.application.port.in.query;

import com.umc.product.infra.application.port.in.query.dto.FileInfo;
import java.time.Duration;

/**
 * 파일 조회 UseCase
 */
public interface GetFileUseCase {
    /**
     * 파일 메타데이터를 조회합니다.
     *
     * @param fileId 파일 ID
     * @return 파일 정보
     */
    FileInfo getById(String fileId);

    /**
     * 파일 다운로드 URL을 생성합니다.
     *
     * <p>구현체에 따라 presigned URL, signed URL 등의 방식으로
     * 제한된 시간 동안 접근 가능한 URL을 반환합니다.
     *
     * @param fileId   파일 ID
     * @param validity URL 유효 기간
     * @return 다운로드 가능한 URL
     */
    String generateDownloadUrl(String fileId, Duration validity);

    /**
     * 파일이 존재하는지 확인합니다.
     *
     * @param fileId 파일 ID
     * @return 존재 여부
     */
    boolean existsById(String fileId);
}
