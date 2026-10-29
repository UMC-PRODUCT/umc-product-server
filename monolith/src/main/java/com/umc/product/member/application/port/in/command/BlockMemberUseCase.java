package com.umc.product.member.application.port.in.command;

import com.umc.product.member.application.port.in.command.dto.MemberBlockCommand;
import com.umc.product.member.application.port.in.command.dto.MemberBlockInfo;

public interface BlockMemberUseCase {

    MemberBlockInfo block(MemberBlockCommand command);
}
