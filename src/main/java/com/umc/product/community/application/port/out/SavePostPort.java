package com.umc.product.community.application.port.out;

import com.umc.product.community.application.port.in.post.TogglePostLikeUseCase.LikeResult;
import com.umc.product.community.domain.Post;

public interface SavePostPort {
    Post save(Post post);

    Post save(Post post, Long authorChallengerId);  // CREATE용 (author 정보 포함)

    void delete(Post post);

    void deleteById(Long postId);

    LikeResult toggleLike(Long postId, Long challengerId);
}
