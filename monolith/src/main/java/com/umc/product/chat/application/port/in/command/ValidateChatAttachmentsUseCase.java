package com.umc.product.chat.application.port.in.command;

import com.umc.product.chat.domain.MessageContentType;
import com.umc.product.storage.application.port.in.query.dto.FileMetadataInfo;

public interface ValidateChatAttachmentsUseCase {

    void validate(MessageContentType contentType, Iterable<FileMetadataInfo> files);
}
