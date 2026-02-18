package com.umc.product.community.application.port.out.trophy;

import com.umc.product.community.domain.Trophy;

public interface SaveTrophyPort {
    Trophy save(Trophy trophy);

    void delete(Trophy trophy);

    void deleteById(Long trophyId);
}
