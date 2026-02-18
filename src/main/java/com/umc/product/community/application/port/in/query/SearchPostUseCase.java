package com.umc.product.community.application.port.in.query;

import com.umc.product.community.application.port.in.query.dto.PostSearchResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SearchPostUseCase {

    Page<PostSearchResult> search(String keyword, Pageable pageable);
}
