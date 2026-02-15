package com.umc.product.member.adapter.in.web;

import com.umc.product.challenger.adapter.in.web.dto.response.ChallengerInfoResponse;
import com.umc.product.challenger.application.port.in.query.GetChallengerUseCase;
import com.umc.product.member.adapter.in.web.dto.response.MemberInfoResponse;
import com.umc.product.member.application.port.in.query.GetMemberUseCase;
import com.umc.product.member.application.port.in.query.MemberInfo;
import com.umc.product.organization.application.port.in.query.GetGisuUseCase;
import com.umc.product.organization.application.port.in.query.dto.GisuInfo;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MemberInfoResponseAssembler {

    private final GetMemberUseCase getMemberUseCase;
    private final GetChallengerUseCase getChallengerUseCase;
    private final GetGisuUseCase getGisuUseCase;

    public MemberInfoResponse fromMemberId(Long memberId) {
        MemberInfo memberInfo = getMemberUseCase.getById(memberId);

        List<ChallengerInfoResponse> challengerInfoResponses =
            getChallengerUseCase.getMemberChallengerList(memberId)
                .stream()
                .map(info -> {
                    GisuInfo gisuInfo = getGisuUseCase.getById(info.gisuId());
                    return ChallengerInfoResponse.from(info, memberInfo, gisuInfo);
                })
                .toList();

        return MemberInfoResponse.from(memberInfo, challengerInfoResponses);
    }
}
