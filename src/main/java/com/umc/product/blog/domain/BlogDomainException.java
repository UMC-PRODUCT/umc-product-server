package com.umc.product.blog.domain;

import com.umc.product.global.exception.BusinessException;
import com.umc.product.global.exception.constant.Domain;

public class BlogDomainException extends BusinessException {

    public BlogDomainException(BlogErrorCode errorCode) {
        super(Domain.BLOG, errorCode);
    }

    public BlogDomainException(BlogErrorCode errorCode, String message) {
        super(Domain.BLOG, errorCode, message);
    }
}
