package com.umc.product.infra.application.port.in.command;

import com.umc.product.infra.application.port.in.command.dto.CreateFileUploadUrlCommand;

public interface ManageFileUploadUseCase {
    void getFileUploadUrl(CreateFileUploadUrlCommand command);

    void completeFileUpload(String fileKey);

    void getFileUrl(String fileKey);
}
