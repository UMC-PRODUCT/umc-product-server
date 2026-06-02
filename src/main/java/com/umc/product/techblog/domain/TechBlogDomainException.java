package com.umc.product.techblog.domain;

import com.umc.product.global.exception.BusinessException;
import com.umc.product.global.exception.constant.Domain;

public class TechBlogDomainException extends BusinessException {

    public TechBlogDomainException(TechBlogErrorCode errorCode) {
        super(Domain.TECH_BLOG, errorCode);
    }

    public TechBlogDomainException(TechBlogErrorCode errorCode, String message) {
        super(Domain.TECH_BLOG, errorCode, message);
    }
}
