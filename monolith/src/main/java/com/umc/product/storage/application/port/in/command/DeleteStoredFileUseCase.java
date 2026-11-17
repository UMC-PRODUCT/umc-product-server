package com.umc.product.storage.application.port.in.command;

public interface DeleteStoredFileUseCase {

    void deleteIfNotReferencedOutsideDeactivation(String fileId, Long deactivationId);
}
