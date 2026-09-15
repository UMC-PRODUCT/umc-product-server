package com.umc.product.community.application.port.in.query;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.umc.product.community.adapter.in.web.dto.response.PostResponse;

public interface SearchPostUseCase {

    Page<PostResponse> search(String keyword, Pageable pageable);
}
