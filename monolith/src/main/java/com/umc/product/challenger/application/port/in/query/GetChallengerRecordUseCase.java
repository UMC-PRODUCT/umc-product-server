package com.umc.product.challenger.application.port.in.query;

import java.util.List;

import com.umc.product.challenger.application.port.in.query.dto.ChallengerRecordInfo;

public interface GetChallengerRecordUseCase {
    ChallengerRecordInfo getById(Long id);

    ChallengerRecordInfo getByCode(String code);

    List<ChallengerRecordInfo> getBySchoolId(Long schoolId);

    List<ChallengerRecordInfo> getByChapterId(Long chapterId);
}
