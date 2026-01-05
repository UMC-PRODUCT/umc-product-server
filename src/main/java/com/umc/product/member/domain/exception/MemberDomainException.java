package com.umc.product.member.domain.exception;

import com.umc.product.global.exception.BusinessException;
import com.umc.product.global.exception.constant.Domain;

public class MemberDomainException extends BusinessException {
    public MemberDomainException(MemberErrorCode memberErrorCode) {
        super(Domain.MEMBER, memberErrorCode);
    }


}
