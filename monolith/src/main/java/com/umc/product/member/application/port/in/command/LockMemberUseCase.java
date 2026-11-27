package com.umc.product.member.application.port.in.command;

import java.util.List;
import java.util.Set;

/**
 * 호출한 트랜잭션이 끝날 때까지 회원의 변경 요청을 직렬화한다.
 */
public interface LockMemberUseCase {

    void lockById(Long memberId);

    /** 이미 삭제된 회원에게는 잠금을 적용하지 않는다. */
    void lockByIdIfExists(Long memberId);

    /** 현재 트랜잭션 동안 회원 삭제를 막고, 실제로 잠근 회원의 ID만 반환한다. */
    List<Long> lockExistingIdsForShare(Set<Long> memberIds);
}
