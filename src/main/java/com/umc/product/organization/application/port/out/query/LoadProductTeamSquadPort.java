package com.umc.product.organization.application.port.out.query;

import com.umc.product.organization.domain.ProductTeamSquad;
import java.time.Instant;
import java.util.Collection;
import java.util.List;

public interface LoadProductTeamSquadPort {

    ProductTeamSquad getById(Long squadId);

    List<ProductTeamSquad> listAll(Boolean active);

    List<ProductTeamSquad> listOverlapping(Instant startAt, Instant endAt);

    List<ProductTeamSquad> listByIds(Collection<Long> ids);
}
