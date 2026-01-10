package com.umc.product.community.application.port.out;

import com.umc.product.community.domain.Boards;

public interface SaveBoardsPort {
    Boards save(Boards boards);

    void delete(Boards boards);

    void update(Boards boards);

    void deleteById(Long id);
}
