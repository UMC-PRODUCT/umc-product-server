package com.umc.product.community.application.boards.in.boards;

import java.util.List;

public interface BoardsUseCase {
    BoardsResponse createPost(CreateBoardsCommand command);

    BoardsResponse createLightningPost(CreateLightningCommand command);

    List<BoardsResponse> getPosts(BoardsSearchQuery query);

    BoardsResponse getPost(Long postId);

    void deletePost(Long postId);

    BoardsResponse updatePost(Long postId, BoardsPostCommand command);
}
