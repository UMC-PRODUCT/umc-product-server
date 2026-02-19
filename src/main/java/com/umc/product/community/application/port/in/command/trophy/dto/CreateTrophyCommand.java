package com.umc.product.community.application.port.in.command.trophy.dto;

public record CreateTrophyCommand(
    Long challengerId,
    Integer week,
    String title,
    String content,
    String url
) {
}
