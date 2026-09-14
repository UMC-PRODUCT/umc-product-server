package com.umc.product.authorization.application.port.out;

import com.umc.product.authorization.domain.ChallengerRole;
import java.util.List;

public interface SaveChallengerRolePort {

    ChallengerRole save(ChallengerRole challengerRole);

    List<ChallengerRole> saveAll(List<ChallengerRole> challengerRoles);

    void delete(ChallengerRole challengerRole);
}