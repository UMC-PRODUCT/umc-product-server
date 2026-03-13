package com.umc.product.curriculum.application.port.in.query;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.curriculum.application.port.in.query.dto.CurriculumInfo;

public interface GetCurriculumUseCase {

    CurriculumInfo getByActiveGisuAndPart(ChallengerPart part);
}
