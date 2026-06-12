package com.umc.product.organization.application.port.out.query;

import com.umc.product.organization.domain.UmcProductSquad;
import java.time.Instant;
import java.util.Collection;
import java.util.List;

public interface LoadUmcProductSquadPort {

    UmcProductSquad getById(Long squadId);

    List<UmcProductSquad> listAll(Boolean active);

    List<UmcProductSquad> listOverlapping(Instant startAt, Instant endAt);

    List<UmcProductSquad> listByIds(Collection<Long> ids);
}
