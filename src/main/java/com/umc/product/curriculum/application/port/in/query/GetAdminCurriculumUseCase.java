package com.umc.product.curriculum.application.port.in.query;

import com.umc.product.common.domain.enums.ChallengerPart;

public interface GetAdminCurriculumUseCase {

    AdminCurriculumInfo getByActiveGisuAndPart(ChallengerPart part);
}
