package com.umc.product.community.application.port.in.post.query;

import com.umc.product.community.application.port.in.PostInfo;
import java.util.List;

public interface GetPostListUseCase {
    List<PostInfo> getPostList(PostSearchQuery query);
}
