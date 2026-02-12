package com.umc.product.community.application.port.in.post;

import com.umc.product.community.application.port.in.PostInfo;
import com.umc.product.community.application.port.in.post.command.UpdateLightningCommand;

public interface UpdateLightningUseCase {
    PostInfo updateLightning(UpdateLightningCommand command);
}
