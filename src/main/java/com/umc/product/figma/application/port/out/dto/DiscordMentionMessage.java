package com.umc.product.figma.application.port.out.dto;

import java.time.Instant;
import java.util.List;

/**
 * Discord 멘션 메시지 발송 요청.
 *
 * @param webhookUrl       대상 채널의 webhook URL
 * @param mentionRenders   미리 렌더링된 mention 문자열 목록 (예: ["<@&123>", "<@456>"]).
 *                         메시지의 content 영역에 들어가 알림이 발생한다.
 * @param fileDisplayName  Figma 파일 노출명 (embed title 에 활용)
 * @param domainKey        분류된 라우팅 도메인 키 (embed field 에 활용, fallback/unmatched 가능)
 * @param pageName         댓글이 달린 페이지 이름 (옵션, embed field)
 * @param authorName       댓글 작성자 표시명
 * @param message          댓글 본문
 * @param commentLink      Figma 댓글 deeplink
 * @param createdAt        댓글 생성 시각 (embed timestamp)
 */
public record DiscordMentionMessage(
    String webhookUrl,
    List<String> mentionRenders,
    String fileDisplayName,
    String domainKey,
    String pageName,
    String authorName,
    String message,
    String commentLink,
    Instant createdAt
) {
}
