package com.umc.product.notice.domain.exception;

import com.umc.product.global.exception.BusinessException;
import com.umc.product.global.exception.constant.Domain;

public class NoticeDomainException extends BusinessException {
    public NoticeDomainException(NoticeErrorCode noticeErrorCode) {
        super(Domain.NOTICE, noticeErrorCode);
    }
}
