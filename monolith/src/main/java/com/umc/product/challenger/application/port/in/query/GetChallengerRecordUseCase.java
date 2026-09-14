package com.umc.product.challenger.application.port.in.query;

import com.umc.product.challenger.application.port.in.query.dto.ChallengerRecordInfo;
import java.util.List;

public interface GetChallengerRecordUseCase {
    ChallengerRecordInfo getById(Long id);

    ChallengerRecordInfo getByCode(String code);

    List<ChallengerRecordInfo> getBySchoolId(Long schoolId);

    List<ChallengerRecordInfo> getByChapterId(Long chapterId);
}
