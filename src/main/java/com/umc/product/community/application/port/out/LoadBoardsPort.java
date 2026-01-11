package com.umc.product.community.application.port.out;

import com.umc.product.community.application.port.in.post.Query.BoardsSearchQuery;
import com.umc.product.community.domain.Boards;
import com.umc.product.community.domain.Enum.Category;
import java.util.List;
import java.util.Optional;

public interface LoadBoardsPort {
    List<Boards> findAllByQuery(BoardsSearchQuery query);

    Optional<Boards> findById(Long id);

    List<Boards> findByCategory(Category category);

    List<Boards> findByRegion(String region);
}
