package com.umc.product.community.application.port.in.post;

import java.util.List;

public interface PostUseCase {
    PostResponse createPost(CreatePostCommand command);

    PostResponse createLightningPost(CreateLightningCommand command);

    List<PostResponse> getPosts(PostSearchQuery query);

    PostResponse getPost(Long postId);

    void deletePost(Long postId);

    PostResponse updatePost(Long postId, UpdatePostCommand command);
}
