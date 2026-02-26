package com.umc.product.recruitment.application.port.in.query;

import com.umc.product.recruitment.application.port.in.query.dto.ExtensionBaseRecruitmentsInfo;

public interface GetExtensionBaseRecruitmentsUseCase {

    ExtensionBaseRecruitmentsInfo getRecruitmentsForExtensionBase(Long memberId);
}
