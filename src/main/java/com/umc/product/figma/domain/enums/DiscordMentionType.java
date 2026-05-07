package com.umc.product.figma.domain.enums;

/**
 * Discord 메시지에서 mention 을 그릴 형태.
 */
public enum DiscordMentionType {
    /** {@code <@&{id}>} — Discord role 멘션 */
    ROLE,
    /** {@code <@{id}>} — Discord 사용자 직접 멘션 */
    USER;

    public String render(String mentionId) {
        return switch (this) {
            case ROLE -> "<@&" + mentionId + ">";
            case USER -> "<@" + mentionId + ">";
        };
    }
}
