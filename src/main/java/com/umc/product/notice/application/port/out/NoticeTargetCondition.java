package com.umc.product.notice.application.port.out;

import com.umc.product.common.domain.enums.ChallengerPart;

/**
 * Notice target matching key derived from a viewer's (subject) attributes.
 * <p>
 * Used to filter visible notices in query layer.
 */
public record NoticeTargetCondition(
    Long gisuId,
    Long chapterId,
    ChallengerPart part
) {
}
