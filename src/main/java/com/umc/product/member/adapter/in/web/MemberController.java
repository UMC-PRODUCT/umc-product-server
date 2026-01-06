package com.umc.product.member.adapter.in.web;

import com.umc.product.global.constant.SwaggerTag;
import com.umc.product.global.security.MemberPrincipal;
import com.umc.product.global.security.annotation.CurrentMember;
import com.umc.product.member.adapter.in.web.dto.request.CompleteRegisterMemberRequest;
import com.umc.product.member.adapter.in.web.dto.response.MemberResponse;
import com.umc.product.member.application.port.in.command.ManageMemberUseCase;
import com.umc.product.member.application.port.in.query.GetMemberUseCase;
import com.umc.product.member.application.port.in.query.MemberInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/member")
@RequiredArgsConstructor
@Tag(name = SwaggerTag.Constants.MEMBER)
public class MemberController {

    private final ManageMemberUseCase manageMemberUseCase;
    private final GetMemberUseCase getMemberUseCase;

    // 로그인은 OAuth를 통해서만 진행됨!!

    @PostMapping("/register/complete")
    @Operation(summary = "회원가입 완료",
            description = """
                    ### OAuth 로그인 후 회원가입 완료 처리
                    - OAuth로 로그인한 사용자가 아직 회원가입이 완료되지 않은 상태(이름, 닉네임 등 사용자 정보를 설정하지 않은 상태)인 경우에 사용됩니다.
                    - 이름, 닉네임, 이메일, 학교명을 선택합니다.
                    - 작성 기준: 2026/01/06 하늘/박경운
                    """)
    public Long completeRegister(@Valid @RequestBody CompleteRegisterMemberRequest request,
                                 @CurrentMember MemberPrincipal currentMember) {

        Long userId = manageMemberUseCase.completeRegister(request.toCommand(currentMember.getMemberId()));
        return userId;
    }


    @GetMapping("/{memberId}")
    @Operation(summary = "사용자 상세 조회")
    public MemberResponse getUser(@PathVariable Long memberId) {
        MemberInfo memberInfo = getMemberUseCase.getById(memberId);
        return MemberResponse.from(memberInfo);
    }
}
