package com.umc.product.authentication.application.service;

import com.umc.product.member.application.port.in.command.ManageMemberCredentialUseCase;
import com.umc.product.member.application.port.in.command.dto.ChangeMemberPasswordCommand;
import com.umc.product.member.application.port.in.query.dto.MemberCredentialInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * 비밀번호 해시 정책 변경 시 점진적으로 재해싱(Rehash)을 수행하는 서비스.
 * <p>
 * 로그인 트랜잭션과 분리하기 위해 REQUIRES_NEW 전파 속성을 사용한다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CredentialRehashService {

    private final PasswordEncoder passwordEncoder;
    private final ManageMemberCredentialUseCase manageMemberCredentialUseCase;

    /**
     * 해시 정책이 변경되었는지 확인하고 필요시 최신 정책으로 업데이트한다.
     * <p>
     * 별도의 트랜잭션에서 실행되므로, 업데이트 실패가 로그인 전체 결과에 영향을 주지 않는다.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void rehashIfNeeded(MemberCredentialInfo credential, String rawPassword) {
        if (passwordEncoder.upgradeEncoding(credential.passwordHash())) {
            String upgraded = passwordEncoder.encode(rawPassword);
            try {
                manageMemberCredentialUseCase.changePassword(
                    ChangeMemberPasswordCommand.of(credential.memberId(), upgraded)
                );
                log.info("[ID/PW 로그인] 비밀번호 해시 정책 갱신 완료: memberId={}", credential.memberId());
            } catch (Exception e) {
                // rehash 실패는 로그인 자체에는 영향을 주지 않으므로 로그만 남기고 삼킨다.
                log.warn("[ID/PW 로그인] 비밀번호 해시 정책 갱신 실패: memberId={}", credential.memberId(), e);
            }
        }
    }
}
