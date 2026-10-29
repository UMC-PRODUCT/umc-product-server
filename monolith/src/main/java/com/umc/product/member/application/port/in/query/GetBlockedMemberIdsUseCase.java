package com.umc.product.member.application.port.in.query;

import java.util.Map;
import java.util.Set;

/**
 * community 등 다른 도메인이 "이 회원이 차단한 상대" 여부만 조회할 때 쓰는 전용 포트.
 * 영속성 세부사항 대신 이 UseCase를 통해 의존하도록 해, member 내부 구조 변경이
 * 다른 도메인에 전파되지 않게 한다.
 */
public interface GetBlockedMemberIdsUseCase {

    Set<Long> getBlockedMemberIds(Long blockerMemberId);

    /**
     * blockerMemberId별 차단 대상 ID 집합을 한 번의 조회로 반환한다. 차단한 대상이 없는
     * blockerMemberId는 결과 Map에서 빠지므로, 호출부는 {@code getOrDefault(id, Set.of())}로
     * 조회해야 한다.
     */
    Map<Long, Set<Long>> getBlockedMemberIdsBatch(Set<Long> blockerMemberIds);
}
