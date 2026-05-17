package com.umc.product.test.application.port.in.command.dto;

import java.util.List;

/**
 * Community 시딩 결과. ADR-017 참조.
 *
 * @param gisuId            작성자 풀로 사용한 기수
 * @param createdPostIds    생성된 Post ID 목록
 * @param createdCommentIds 생성된 Comment ID 목록
 * @param createdTrophyIds  생성된 Trophy ID 목록
 * @param postFailed        Post 생성 단계 실패 수
 * @param commentFailed     Comment 생성 단계 실패 수
 * @param trophyFailed      Trophy 생성 단계 실패 수
 * @param skipped           챌린저 풀이 비어있는 등의 이유로 전체 스킵된 경우 true
 * @param reason            스킵/부분 실패 사유 (skipped=false 이고 실패 없으면 null)
 */
public record SeedCommunityResult(
    Long gisuId,
    List<Long> createdPostIds,
    List<Long> createdCommentIds,
    List<Long> createdTrophyIds,
    int postFailed,
    int commentFailed,
    int trophyFailed,
    boolean skipped,
    String reason
) {

    public static SeedCommunityResult skipped(Long gisuId, String reason) {
        return new SeedCommunityResult(gisuId, List.of(), List.of(), List.of(), 0, 0, 0, true, reason);
    }
}
