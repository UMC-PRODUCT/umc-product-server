package com.umc.product.recruitment.application.port.in.query;

import com.umc.product.recruitment.application.port.in.command.dto.UpdateDocumentStatusCommand;
import com.umc.product.recruitment.application.port.in.command.dto.UpdateDocumentStatusInfo;

public interface UpdateDocumentStatusUseCase {
    UpdateDocumentStatusInfo update(UpdateDocumentStatusCommand command);
}
