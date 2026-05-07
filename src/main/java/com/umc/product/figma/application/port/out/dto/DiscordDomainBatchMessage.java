package com.umc.product.figma.application.port.out.dto;

import java.time.Instant;
import java.util.List;

/**
 * 한 사이클(또는 from~to 시간창) 안의 댓글을 도메인 단위로 묶어 발송하는 메시지.
 * <p>
 * 한 도메인 = 한 webhook = 한 메시지(embeds 다중)로 합쳐 발송하기 위한 DTO 다. 같은 도메인의 댓글이 25건을 넘으면 어댑터가 embed 를 추가로 분할하고 (Discord embed 는
 * fields 25개 / 메시지 당 embed 10개 / 전체 6000자 제한이 있음), 그래도 초과하면 추가 메시지로 페이지네이션한다.
 *
 * @param webhookUrl     도메인의 Discord webhook URL
 * @param domainKey      라우팅 도메인 식별자 (embed title / footer 에 노출)
 * @param mentionRenders 미리 렌더된 멘션 문자열 (예: ["<@&123>", "<@456>"]) — 알림 발생을 위해 메시지의 외부 content 영역에 출력된다.
 * @param windowFrom     사이클의 시작 시각 (footer 에 노출)
 * @param windowTo       사이클의 종료 시각 (footer 에 노출)
 * @param comments       이 도메인으로 묶인 댓글 목록 (다른 파일의 댓글이 섞일 수 있음)
 */
public record DiscordDomainBatchMessage(
    String webhookUrl,
    String domainKey,
    List<String> mentionRenders,
    Instant windowFrom,
    Instant windowTo,
    List<CommentEntry> comments
) {
    public record CommentEntry(
        String fileDisplayName,
        String pageName,
        String authorName,
        String message,
        String commentLink,
        Instant createdAt
    ) {
    }
}
