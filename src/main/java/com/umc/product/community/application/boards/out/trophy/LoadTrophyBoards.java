package com.umc.product.community.application.boards.out.trophy;

import com.umc.product.community.application.boards.in.trophy.TrophySearchQuery;
import com.umc.product.community.domain.Trophy;
import java.util.List;

public interface LoadTrophyBoards {
    List<Trophy> findAllByQuery(TrophySearchQuery query);

    Trophy save(Trophy trophy);

    Trophy update(Trophy trophy);
}
