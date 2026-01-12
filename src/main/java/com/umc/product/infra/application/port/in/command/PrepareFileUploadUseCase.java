package com.umc.product.infra.application.port.in.command;

import com.umc.product.infra.application.port.in.command.dto.FileUploadInfo;
import com.umc.product.infra.application.port.in.command.dto.PrepareFileUploadCommand;

/**
 * 파일 업로드 준비 UseCase
 *
 * <p>파일 업로드를 위한 URL과 필요한 정보를 제공합니다.
 * 구현 방식(presigned URL, signed URL, direct upload 등)에 독립적입니다.
 */
public interface PrepareFileUploadUseCase {
    /**
     * 파일 업로드를 준비하고 필요한 정보를 반환합니다.
     *
     * <p>구현체에 따라 presigned URL, direct upload endpoint,
     * 또는 기타 업로드 방식의 정보를 제공할 수 있습니다.
     *
     * @param command 파일 업로드 준비 정보
     * @return 파일 업로드에 필요한 정보
     */
    FileUploadInfo prepare(PrepareFileUploadCommand command);
}
