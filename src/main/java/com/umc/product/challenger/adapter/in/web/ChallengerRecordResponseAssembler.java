package com.umc.product.challenger.adapter.in.web;

import com.umc.product.challenger.adapter.in.web.dto.response.ChallengerRecordResponse;
import com.umc.product.challenger.application.port.in.query.GetChallengerRecordUseCase;
import com.umc.product.challenger.application.port.in.query.dto.ChallengerRecordInfo;
import com.umc.product.organization.application.port.in.query.GetGisuUseCase;
import com.umc.product.organization.application.port.in.query.GetSchoolUseCase;
import com.umc.product.organization.application.port.in.query.dto.GisuInfo;
import com.umc.product.organization.application.port.in.query.dto.SchoolDetailInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class ChallengerRecordResponseAssembler {

    private final GetGisuUseCase getGisuUseCase;
    private final GetSchoolUseCase getSchoolUseCase;
    private final GetChallengerRecordUseCase getChallengerRecordUseCase;

    public ChallengerRecordResponse from(String code) {

        ChallengerRecordInfo recordInfo = getChallengerRecordUseCase.getByCode(code);

        return infoToResponse(recordInfo);
    }

    public ChallengerRecordResponse from(Long id) {

        ChallengerRecordInfo recordInfo = getChallengerRecordUseCase.getById(id);

        return infoToResponse(recordInfo);
    }

    private ChallengerRecordResponse infoToResponse(ChallengerRecordInfo recordInfo) {
        GisuInfo gisuInfo = getGisuUseCase.getById(recordInfo.gisuId());
        SchoolDetailInfo schoolInfo = getSchoolUseCase.getSchoolDetail(recordInfo.schoolId());

        return ChallengerRecordResponse.builder()
            .code(recordInfo.code())
            .part(recordInfo.part())
            .gisuId(gisuInfo.gisuId())
            .gisu(gisuInfo.gisu())
            .schoolId(schoolInfo.schoolId())
            .schoolName(schoolInfo.schoolName())
            .chapterId(schoolInfo.chapterId())
            .chapterName(schoolInfo.chapterName())
            .memberName(recordInfo.memberName())
            .build();
    }
}
