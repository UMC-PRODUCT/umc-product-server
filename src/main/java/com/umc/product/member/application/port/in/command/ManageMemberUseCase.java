package com.umc.product.member.application.port.in.command;

public interface ManageMemberUseCase {
    Long register(RegisterMemberCommand command);

    /**
     * OAuth로 회원 정보를 작성한 사용자가 회원가입을 완료할 수 있도록 합니다.
     */
    Long completeRegister(CompleteRegisterMemberCommand command);
}
