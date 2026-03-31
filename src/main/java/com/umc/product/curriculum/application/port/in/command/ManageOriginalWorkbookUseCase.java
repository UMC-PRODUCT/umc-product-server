package com.umc.product.curriculum.application.port.in.command;

/**
 * OriginalWorkbook 관리 UseCase
 */
public interface ManageOriginalWorkbookUseCase {

    /**
     * 워크북 배포 (운영진 전용)
     * <p>
     * 배포된 워크북만 앱에서 조회됩니다.
     *
     * @param workbookId 원본 워크북 ID
     */
    void release(Long workbookId);
}
