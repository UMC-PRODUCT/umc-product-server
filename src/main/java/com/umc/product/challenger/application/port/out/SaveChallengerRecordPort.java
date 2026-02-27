package com.umc.product.challenger.application.port.out;

import com.umc.product.challenger.domain.ChallengerRecord;
import java.util.List;

public interface SaveChallengerRecordPort {

    /**
     * 챌린저 기록 저장
     */
    ChallengerRecord save(ChallengerRecord record);

    List<ChallengerRecord> saveAll(List<ChallengerRecord> records);


    /**
     * 챌린저 기록 삭제
     */
    void delete(ChallengerRecord record);
}
