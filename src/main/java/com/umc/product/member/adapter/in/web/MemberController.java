package com.umc.product.member.adapter.in.web;

import com.umc.product.global.constant.SwaggerTag;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/member")
@RequiredArgsConstructor
@Tag(name = SwaggerTag.Constants.MEMBER)
public class MemberController {

    // 로그인은 OAuth를 통해서만 진행됨!!
}
