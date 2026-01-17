package com.umc.product.authentication.adapter.in.web;

import com.umc.product.authentication.adapter.in.web.dto.request.AddOAuthRequest;
import com.umc.product.authentication.application.port.in.query.dto.MemberOAuthInfo;
import com.umc.product.global.exception.NotImplementedException;
import com.umc.product.global.security.MemberPrincipal;
import com.umc.product.global.security.annotation.CurrentMember;
import io.swagger.v3.oas.annotations.Operation;
import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/member-oauth")
public class MemberOAuthController {
    @PostMapping
    @Operation(summary = "로그인용 OAuth 수단 추가")
    List<MemberOAuthInfo> addMemberOAuth(
            @CurrentMember MemberPrincipal memberPrincipal,
            @RequestBody AddOAuthRequest request) {
        throw new NotImplementedException();
    }

    @DeleteMapping("{memberOAuthId}")
    @Operation(summary = "로그인용 OAuth 수단 제거",
            description = """
                    현재는 memberOAuthId로 식별해서 제거 처리를 진행하나, 추후 OAuth측에 다시 로그인해서 제거하는 방식으로 변경될 수 있습니다.
                    """)
    List<MemberOAuthInfo> deleteMemberOAuth(
            @CurrentMember MemberPrincipal memberPrincipal,
            @PathVariable String memberOAuthId
    ) {
        throw new NotImplementedException();
    }

    @GetMapping("me")
    @Operation(summary = "현재 회원 계정과 연동된 OAuth 정보 조회")
    List<MemberOAuthInfo> getMyOAuthInfos() {
        throw new NotImplementedException();
    }
}
