package com.umc.product.community.application.port.in.command.post;

import com.umc.product.community.application.port.in.command.post.dto.UpdateLightningCommand;
import com.umc.product.community.application.port.in.query.dto.PostInfo;

public interface UpdateLightningUseCase {
    PostInfo updateLightning(UpdateLightningCommand command);
}
