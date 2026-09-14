package com.umc.product.blog.application.port.in.query.dto;

import java.util.List;

public record BlogSeoPathsInfo(
    List<BlogSeoPathInfo> paths
) {
}
