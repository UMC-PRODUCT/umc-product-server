package com.umc.product.member.application.port.in.command;

import com.umc.product.member.application.port.in.command.dto.RegisterMemberCommand;

public interface ManageMemberUseCase {
    /**
     * 회원가입 (회원 생성) 입니다. OAuth를 이용해서 회원가입하는 경우만을 고려합니다.
     * <p>
     * register complete라고 보시면 됩니다.
     */
    Long registerMember(RegisterMemberCommand command);

    /**
     * 회원의 정보를 수정하거나, 상태를 변경하는 등의 업데이트 작업을 수행합니다.
     */
    void updateMember(UpdateMemberCommand command);

    /**
     * 회원을 Hard Delete 합니다.
     */
    void deleteMember(DeleteMemberCommand command);
}
