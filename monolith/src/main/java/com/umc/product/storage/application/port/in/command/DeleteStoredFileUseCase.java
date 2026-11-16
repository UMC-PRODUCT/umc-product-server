package com.umc.product.storage.application.port.in.command;

public interface DeleteStoredFileUseCase {

    void deleteIfExists(String fileId);
}
