package com.umc.product.community.application.port.in.command.post;

import com.umc.product.community.application.port.in.command.post.dto.PostInfo;
import com.umc.product.community.application.port.in.command.post.dto.UpdateLightningCommand;

public interface UpdateLightningUseCase {
    PostInfo updateLightning(UpdateLightningCommand command);
}
