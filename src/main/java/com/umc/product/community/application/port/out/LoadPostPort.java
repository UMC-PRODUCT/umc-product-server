package com.umc.product.community.application.port.out;

import com.umc.product.community.application.port.in.post.query.PostSearchQuery;
import com.umc.product.community.domain.Post;
import com.umc.product.community.domain.enums.Category;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface LoadPostPort {
    Page<Post> findAllByQuery(PostSearchQuery query, Pageable pageable);

    Optional<Post> findById(Long postId);

    List<Post> findByCategory(Category category);

    List<Post> findByRegion(String region);

    Page<PostSearchData> searchByKeyword(String keyword, Pageable pageable);
}
