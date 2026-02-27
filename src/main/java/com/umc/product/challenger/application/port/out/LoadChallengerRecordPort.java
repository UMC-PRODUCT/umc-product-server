package com.umc.product.challenger.application.port.out;

import com.umc.product.challenger.domain.ChallengerRecord;
import java.util.Optional;

public interface LoadChallengerRecordPort {

    /**
     * ID로 챌린저 기록 조회
     */
    Optional<ChallengerRecord> findById(Long id);

    /**
     * ID로 챌린저 기록 조회 - 없으면 예외
     */
    ChallengerRecord getById(Long id);

    /**
     * 코드로 챌린저 기록 조회
     */
    Optional<ChallengerRecord> findByCode(String code);

    /**
     * 코드로 챌린저 기록 조회 - 없으면 예외
     */
    ChallengerRecord getByCode(String code);

    /**
     * 코드 존재 여부 확인
     */
    boolean existsByCode(String code);
}
