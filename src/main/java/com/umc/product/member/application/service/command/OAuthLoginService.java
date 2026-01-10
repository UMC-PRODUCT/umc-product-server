package com.umc.product.member.application.service.command;

import com.umc.product.member.application.port.in.command.ProcessOAuthLoginCommand;
import com.umc.product.member.application.port.in.command.ProcessOAuthLoginUseCase;
import com.umc.product.member.application.port.out.LoadMemberOAuthPort;
import com.umc.product.member.application.port.out.SaveMemberPort;
import com.umc.product.member.domain.Member;
import com.umc.product.member.domain.MemberOAuth;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class OAuthLoginService implements ProcessOAuthLoginUseCase {

    private final LoadMemberOAuthPort loadMemberOAuthPort;
    private final SaveMemberPort saveMemberPort;

    @Override
    public Long processOAuthLogin(ProcessOAuthLoginCommand command) {
        log.info("Processing OAuth login: provider={}, providerId={}",
                command.provider(), command.providerId());

        // 1. 기존 OAuth 연동 조회
        return loadMemberOAuthPort
                .findByProviderAndProviderUserId(command.provider(), command.providerId())
                .map(MemberOAuth::getMemberId)  // 이미 가입된 회원
                // TODO: 임시 회원가입 하는 회원도 생각해야함
                .orElseGet(() -> registerOAuthMemberWithPendingState(command));  // 신규 회원 가입
    }

    private Long registerOAuthMemberWithPendingState(ProcessOAuthLoginCommand command) {
        log.info("Registering new member: email={}", command.email());

        // 1. Member 생성
        Member member = Member.builder()
                .name(command.name())
                .nickname(command.nickname() != null ? command.nickname() : generateNickname(command.email()))
                .email(command.email())
                .build();

        Member savedMember = saveMemberPort.save(member);

        // 2. MemberOAuth 연동 정보 저장
        MemberOAuth memberOAuth = MemberOAuth.builder()
                .memberId(savedMember.getId())
                .provider(command.provider())
                .providerId(command.providerId())
                .build();

        saveMemberPort.saveOAuth(memberOAuth);

        log.info("New member registered: memberId={}", savedMember.getId());
        return savedMember.getId();
    }

    private String generateNickname(String email) {
        // 이메일에서 @앞부분 + 랜덤 숫자
        String prefix = email.split("@")[0];
        return prefix + "_" + System.currentTimeMillis() % 10000;
    }
}
