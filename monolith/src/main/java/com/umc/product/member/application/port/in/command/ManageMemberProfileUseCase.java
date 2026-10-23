package com.umc.product.member.application.port.in.command;

import com.umc.product.member.application.port.in.command.dto.UpsertMemberProfileCommand;

public interface ManageMemberProfileUseCase {
    void upsert(UpsertMemberProfileCommand command);

    void delete(Long memberId);
}
