package com.umc.product.challenger.adapter.in.web;

import com.umc.product.challenger.adapter.in.web.dto.request.AddChallengerRecordToMemberRequest;
import com.umc.product.challenger.adapter.in.web.dto.request.CreateChallengerRecordRequest;
import com.umc.product.global.exception.NotImplementedException;
import com.umc.product.global.security.MemberPrincipal;
import com.umc.product.global.security.annotation.CurrentMember;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/challenger-record")
@RequiredArgsConstructor
@Tag(name = "Challenger | 이전 기수 챌린저 기록 추가", description = "챌린저 관련 API")
public class ChallengerRecordController {

    // 코드를 이용해서 Member에 챌린저 기록을 추가하는 API
    @PostMapping("member")
    public void addChallengerRecordToMember(
        @CurrentMember MemberPrincipal memberPrincipal,
        @RequestBody AddChallengerRecordToMemberRequest request) {
        // TODO: 챌린저 기록 추가 로직 구현

        throw new NotImplementedException();
    }

    // 코드를 생성하는 API
    @PostMapping
    public Long createChallengerRecord(
        @RequestBody CreateChallengerRecordRequest request
    ) {
        // TODO: SUPER_ADMIN 만 가능하도록 권한 설정

        throw new NotImplementedException();
    }

}
