package com.umc.product.recruitment.domain.exception;

import com.umc.product.global.exception.BusinessException;
import com.umc.product.global.exception.constant.Domain;

public class RecruitmentDomainException extends BusinessException {
    public RecruitmentDomainException(RecruitmentErrorCode recruitmentErrorCode) {
        super(Domain.RECRUITMENT, recruitmentErrorCode);
    }


}
