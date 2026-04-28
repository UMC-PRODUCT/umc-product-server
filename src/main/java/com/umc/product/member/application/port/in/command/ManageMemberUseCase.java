package com.umc.product.member.application.port.in.command;

import com.umc.product.member.application.port.in.command.dto.DeleteMemberCommand;
import com.umc.product.member.application.port.in.command.dto.UpdateMemberCommand;

public interface ManageMemberUseCase {

    /**
     * 회원의 정보를 수정하거나, 상태를 변경하는 등의 업데이트 작업을 수행합니다.
     */
    void updateMember(UpdateMemberCommand command);

    /**
     * 회원을 Hard Delete 합니다.
     */
    void deleteMember(DeleteMemberCommand command);
}
