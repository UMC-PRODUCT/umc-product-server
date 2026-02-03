package com.umc.product.notice.adapter.in.web.dto.response.command;

import java.util.List;

public record ReplaceNoticeImagesResponse(
    List<String> imageIds
) {
}
