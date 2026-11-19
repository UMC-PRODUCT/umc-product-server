package com.umc.product.challenger.application.port.out;

import java.util.List;

import com.umc.product.challenger.domain.ChallengerDeactivation;
import com.umc.product.challenger.domain.ChallengerDeactivationHistory;
import com.umc.product.challenger.domain.ChallengerDeactivationImage;
import com.umc.product.challenger.domain.ChallengerDeactivationLink;

public interface SaveChallengerDeactivationPort {

    ChallengerDeactivation save(ChallengerDeactivation deactivation);

    ChallengerDeactivationHistory saveHistory(ChallengerDeactivationHistory history);

    void saveImages(List<ChallengerDeactivationImage> images);

    void saveLinks(List<ChallengerDeactivationLink> links);
}
