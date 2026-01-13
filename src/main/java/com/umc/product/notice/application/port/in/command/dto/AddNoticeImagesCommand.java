package com.umc.product.notice.application.port.in.command.dto;

import com.umc.product.notice.domain.NoticeImage;
import java.util.List;

public record AddNoticeImagesCommand(
        Long noticeId,
        List<Long> imageIds
) {
}
