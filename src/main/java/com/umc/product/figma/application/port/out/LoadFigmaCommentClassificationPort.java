package com.umc.product.figma.application.port.out;

import java.util.Collection;
import java.util.Map;

/**
 * 영구 캐시(figma_comment_classification) 에서 commentId → domain_key 매핑을 조회한다.
 * 후보 외 응답이나 mock provider 결과는 저장되지 않으므로, 본 port 의 결과는 항상 valid 한 분류만 반환한다.
 */
public interface LoadFigmaCommentClassificationPort {

    /**
     * @return 입력 commentId 들 중 영구 캐시에 존재하는 것만의 (commentId → domainKey) 매핑.
     *         존재하지 않는 commentId 는 결과 Map 에 포함되지 않는다.
     */
    Map<String, String> findClassifications(Collection<String> commentIds);
}
