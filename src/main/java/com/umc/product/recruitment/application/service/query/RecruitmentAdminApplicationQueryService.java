package com.umc.product.recruitment.application.service.query;

import com.umc.product.recruitment.application.port.in.query.GetApplicationListForAdminUseCase;
import com.umc.product.recruitment.application.port.in.query.dto.ApplicationListForAdminInfo;
import com.umc.product.recruitment.application.port.in.query.dto.GetApplicationListForAdminQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RecruitmentAdminApplicationQueryService implements GetApplicationListForAdminUseCase {

    @Override
    public ApplicationListForAdminInfo get(GetApplicationListForAdminQuery query) {
        // todo: 총괄 권한 검증 필요
        return null;
    }
}
