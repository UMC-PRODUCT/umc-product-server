package com.umc.product.notice.application.port.out;

/**
 * 같은 공지 투표에서 동일 응답자의 변경을 트랜잭션 종료까지 직렬화한다.
 */
public interface LockNoticeVoteResponsePort {

    void lockResponse(Long noticeId, Long respondentMemberId);
}
