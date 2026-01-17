package com.umc.product.community.application.port.out;

import com.umc.product.community.domain.Post;

public interface SavePostPort {
    Post save(Post post);

    void delete(Post post);

    void deleteById(Long id);
}
