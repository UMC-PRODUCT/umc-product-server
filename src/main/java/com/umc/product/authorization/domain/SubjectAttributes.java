package com.umc.product.authorization.domain;

import com.umc.product.common.domain.enums.ChallengerPart;
import java.util.List;
import lombok.Builder;

/**
 * ABAC 중 Subject 속성을 나타냅니다.
 * <p>
 * "누가" 리소스에 접근하는지를 나타냅니다.
 */
@Builder
public record SubjectAttributes(
    Long memberId,
    Long schoolId,
    // 지부, 기수, 역할은 기수에 따라 달라짐
    List<GisuChallengerInfo> gisuChallengerInfos,
    List<RoleAttribute> roleAttributes

    // Environment Attributes
    // 선택 사항, 고려해볼만한 사항들을 추가할 수는 있을 것 같음
) {
    @Builder
    public record GisuChallengerInfo(
        Long gisuId,
        Long chapterId,
        ChallengerPart part,
        Long challengerId
    ) {
    }
}
