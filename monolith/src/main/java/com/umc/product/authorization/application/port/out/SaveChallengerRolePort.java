package com.umc.product.authorization.application.port.out;

import java.util.List;

import com.umc.product.authorization.domain.ChallengerRole;

public interface SaveChallengerRolePort {

    ChallengerRole save(ChallengerRole challengerRole);

    List<ChallengerRole> saveAll(List<ChallengerRole> challengerRoles);

    void delete(ChallengerRole challengerRole);
}
