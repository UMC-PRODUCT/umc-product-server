package com.umc.product.figma.application.port.out;

/**
 * Figma 댓글 LLM 분류 결과를 영구 캐시에 보관한다. 동일 commentId 가 이미 존재하면 무시(idempotent).
 */
public interface SaveFigmaCommentClassificationPort {

    /**
     * @param commentId 댓글 식별자 (영구 캐시 키)
     * @param domainKey 매칭된 라우팅 도메인 키. 후보 외 응답이거나 LLM 호출 실패면 호출하지 않는다.
     * @param provider  분류한 LLM provider. mock 제외 (호출자가 사전에 거른다).
     */
    void save(String commentId, String domainKey, String provider);
}
