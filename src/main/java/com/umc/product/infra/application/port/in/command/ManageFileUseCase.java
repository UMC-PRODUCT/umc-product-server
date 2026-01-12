package com.umc.product.infra.application.port.in.command;

/**
 * 파일 관리 UseCase
 *
 * <p>파일 업로드 완료 처리, 삭제 등의 관리 작업을 수행합니다.
 */
public interface ManageFileUseCase {
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
    void delete(String fileId);
}
