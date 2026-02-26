package com.umc.product.challenger.adapter.in.web;

import com.umc.product.challenger.adapter.in.web.dto.request.AddChallengerRecordToMemberRequest;
import com.umc.product.challenger.adapter.in.web.dto.request.CreateChallengerRecordRequest;
import com.umc.product.global.exception.NotImplementedException;
import com.umc.product.global.security.MemberPrincipal;
import com.umc.product.global.security.annotation.CurrentMember;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/challenger-record")
@RequiredArgsConstructor
@Tag(name = "Challenger | 챌린저 기록", description = "챌린저 기록 관련")
public class ChallengerRecordController {

    // 코드를 이용해서 Member에 챌린저 기록을 추가하는 API
    @Operation(summary = "6자리 코드를 이용해서 회원(계정)에 챌린저 기록 추가",
        description = """
            각 챌린저 활동 기록에 대해서 발급된 6자리 코드를 입력하여,
            현재 로그인한 계정에 챌린저 기록 및 권한을 추가하는 기능입니다.

            각 코드는 1회만 생성 가능하며, 어떤 계정에, 언제 사용되었는지 기록됩니다.
            """)
    @PostMapping("member")
    public void addChallengerRecordToMember(
        @CurrentMember MemberPrincipal memberPrincipal,
        @RequestBody AddChallengerRecordToMemberRequest request) {
        // TODO: 챌린저 기록 추가 로직 구현

        throw new NotImplementedException();
    }

    // 코드를 생성하는 API
    @Operation(summary = "[ADMIN] 과거 챌린저 기록을 위한 코드 생성 기능",
        description = """
            중앙운영사무국 총괄단만 사용 가능한 기능입니다. 9기 이전 기수의 챌린저 기록을 업로드하고,
            각 기록을 모든 회원이 추가할 수 있도록 6자리 코드를 생성하여 발급합니다.
            """)
    @PostMapping
    public Long createChallengerRecord(
        @RequestBody CreateChallengerRecordRequest request
    ) {
        // TODO: SUPER_ADMIN 만 가능하도록 권한 설정

        throw new NotImplementedException();
    }

}
