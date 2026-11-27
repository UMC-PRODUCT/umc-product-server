package com.umc.product.member.application.port.in.query;

import java.util.Set;

/**
 * 재가입 제한 확인에 사용할 현재 및 탈퇴 계정의 ID를 조회합니다.
 */
public interface FindMemberIdsForReadmissionUseCase {

    Set<Long> findMemberIds(String email);
}
