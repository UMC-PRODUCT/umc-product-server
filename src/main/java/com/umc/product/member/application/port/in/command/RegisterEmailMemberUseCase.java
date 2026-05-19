package com.umc.product.member.application.port.in.command;

import com.umc.product.member.application.port.in.command.dto.EmailRegisterMemberCommand;
import java.util.List;

/**
 * 이메일 기반 회원가입 UseCase. ADR-017 흐름에서 사용한다.
 */
public interface RegisterEmailMemberUseCase {

    Long register(EmailRegisterMemberCommand command);

    /**
     * 여러 회원을 한 트랜잭션 안에서 등록한다. 도메인 검증과 자격증명 등록은 단건 {@link #register}
     * 와 동일하게 각 command 별로 수행되어 운영 흐름과 데이터 일관성이 동일하다. atomic batch 이며
     * 한 건 실패 시 전체 롤백된다.
     * <p>
     * 시딩처럼 N 건을 한 번에 등록하는 흐름에서 트랜잭션 commit 횟수와 동일 트랜잭션 안의 1차 캐시
     * 활용으로 단건 register × N 대비 round-trip 이 감소한다.
     *
     * @return 등록된 회원 ID 목록 (입력 순서 보존)
     */
    List<Long> batchRegister(List<EmailRegisterMemberCommand> commands);
}
