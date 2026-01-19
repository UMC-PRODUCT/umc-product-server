package com.umc.product.challenger.application.port.out;


import com.umc.product.curriculum.domain.ChallengerMission;

public interface SaveChallengerMissionPort {

    ChallengerMission save(ChallengerMission challengerMission);

    void delete(ChallengerMission challengerMission);
}
