package com.umc.product.authorization.domain;

import java.util.List;
import java.util.stream.Collectors;
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
        // 기수에 대한 k
        List<Long> chapterIds,
        List<ChallengerRole> roles

        // Environment Attributes
        // 선택 사항, 고려해볼만한 사항들을 추가할 수는 있을 것 같음
) {

    @Override
    public String toString() {
        String roleTypes = roles != null
                ? roles.stream()
                .map(role -> role.getChallengerRoleType().name())
                .collect(Collectors.joining(", ", "[", "]"))
                : "[]";

        return "SubjectAttributes{" +
                "memberId=" + memberId +
                ", schoolId=" + schoolId +
                ", chapterIds=" + chapterIds +
                ", roles=" + roleTypes +
                '}';
    }
}
