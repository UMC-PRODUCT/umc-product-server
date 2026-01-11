package com.umc.product.notice.application.port.in.command;

public record RemoveNoticeLinkCommand(
        Long noticeLinkId,
        Long requesterId
) {
}
