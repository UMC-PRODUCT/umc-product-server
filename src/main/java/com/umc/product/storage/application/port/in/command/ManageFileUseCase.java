package com.umc.product.storage.application.port.in.command;

import com.umc.product.storage.application.port.in.command.dto.FileUploadInfo;
import com.umc.product.storage.application.port.in.command.dto.PrepareFileUploadCommand;

/**
 * 이미 업로드된 파일을 관리하는 경우를 담당합니다.
 * <p>
 * 파일 업로드 완료 처리, 삭제 등의 관리 작업을 수행합니다.
 */
public interface ManageFileUseCase {
    /**
     * 파일 업로드를 위한 URL을 생성합니다.
     * <p>
     * 사용하는 file uploader에 따라서 본 서버일 수도 있고, S3 Presigned URL일 수도 있고 합니다.
     *
     * @param command 파일 업로드 준비 정보
     * @return 업로드 URL 및 관련 정보
     */
    FileUploadInfo getFileUploadUrl(PrepareFileUploadCommand command);


    /**
     * 파일 업로드 완료를 확인하고 메타데이터를 업데이트합니다.
     *
     * @param fileId 파일 ID
     */
    void confirmUpload(String fileId);

    /**
     * 파일을 삭제합니다.
     * <p>
     * 메타데이터와 실제 스토리지의 파일을 모두 삭제합니다.
     *
     * @param fileId 파일 ID
     */
    void deleteFile(String fileId);
}
