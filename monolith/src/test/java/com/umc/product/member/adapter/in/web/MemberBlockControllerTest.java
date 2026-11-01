package com.umc.product.member.adapter.in.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.umc.product.global.config.JacksonConfig;
import com.umc.product.global.security.JwtTokenProvider;
import com.umc.product.global.security.MemberPrincipal;
import com.umc.product.member.application.port.in.command.BlockMemberUseCase;
import com.umc.product.member.application.port.in.command.UnblockMemberUseCase;
import com.umc.product.member.application.port.in.command.dto.MemberBlockCommand;
import com.umc.product.member.application.port.in.command.dto.MemberBlockInfo;
import com.umc.product.member.application.port.in.query.ListBlockedMembersUseCase;
import com.umc.product.member.application.port.in.query.dto.BlockedMemberInfo;

@WebMvcTest(controllers = MemberBlockController.class)
@Import(JacksonConfig.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("MemberBlockController")
class MemberBlockControllerTest {

    private static final Long REQUESTER_ID = 99L;

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    BlockMemberUseCase blockMemberUseCase;

    @MockitoBean
    UnblockMemberUseCase unblockMemberUseCase;

    @MockitoBean
    ListBlockedMembersUseCase listBlockedMembersUseCase;

    @BeforeEach
    void setUpSecurityContext() {
        MemberPrincipal principal = MemberPrincipal.builder()
            .memberId(REQUESTER_ID)
            .build();
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities())
        );
    }

    @Test
    @DisplayName("block은 CurrentMember를 blocker로 하여 차단을 요청하고 결과를 반환한다")
    void block_요청자를_blocker로_차단한다() throws Exception {
        given(blockMemberUseCase.block(any(MemberBlockCommand.class)))
            .willReturn(new MemberBlockInfo(REQUESTER_ID, 20L, true));

        mockMvc.perform(post("/api/v1/member/blocks/20"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result.memberId").value(20))
            .andExpect(jsonPath("$.result.isBlocked").value(true));

        then(blockMemberUseCase).should().block(new MemberBlockCommand(REQUESTER_ID, 20L));
    }

    @Test
    @DisplayName("unblock은 CurrentMember를 blocker로 하여 차단 해제를 요청하고 결과를 반환한다")
    void unblock_요청자를_blocker로_차단_해제한다() throws Exception {
        given(unblockMemberUseCase.unblock(any(MemberBlockCommand.class)))
            .willReturn(new MemberBlockInfo(REQUESTER_ID, 20L, false));

        mockMvc.perform(delete("/api/v1/member/blocks/20"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result.memberId").value(20))
            .andExpect(jsonPath("$.result.isBlocked").value(false));

        then(unblockMemberUseCase).should().unblock(new MemberBlockCommand(REQUESTER_ID, 20L));
    }

    @Test
    @DisplayName("listBlocked는 CurrentMember가 차단한 회원 목록을 반환한다")
    void listBlocked_차단한_회원_목록을_반환한다() throws Exception {
        Instant blockedAt = Instant.parse("2026-10-29T00:00:00Z");
        given(listBlockedMembersUseCase.listBlockedMembers(REQUESTER_ID))
            .willReturn(List.of(new BlockedMemberInfo(20L, "홍길동", blockedAt)));

        mockMvc.perform(get("/api/v1/member/blocks"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result.members[0].memberId").value(20))
            .andExpect(jsonPath("$.result.members[0].name").value("홍길동"));

        then(listBlockedMembersUseCase).should().listBlockedMembers(REQUESTER_ID);
    }
}
