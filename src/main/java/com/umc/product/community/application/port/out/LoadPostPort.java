package com.umc.product.community.application.port.out;

import com.umc.product.community.application.port.in.post.query.PostSearchQuery;
import com.umc.product.community.domain.Post;
import com.umc.product.community.domain.enums.Category;
import java.util.List;
import java.util.Optional;

public interface LoadPostPort {
    List<Post> findAllByQuery(PostSearchQuery query);

    Optional<Post> findById(Long postId);

    List<Post> findByCategory(Category category);

    List<Post> findByRegion(String region);
}
