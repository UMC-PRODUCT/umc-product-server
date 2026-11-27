package com.umc.product.inquiry.application.port.in.query;

import java.util.List;

import com.umc.product.inquiry.application.port.in.query.dto.InquiryTeamInfo;

public interface ListInquiryTeamsUseCase {

    /** 받는 팀 7개를 정해진 순서로 돌려주고, 맨 뒤에 "어느 팀인지 모르겠어요"를 붙인다. */
    List<InquiryTeamInfo> listTeams();
}
