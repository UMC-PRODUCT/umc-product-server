package com.umc.product.member.application.service;

import com.umc.product.global.exception.NotImplementedException;
import com.umc.product.member.application.port.in.command.ManageMemberUseCase;
import com.umc.product.member.application.port.in.command.dto.DeleteMemberCommand;
import com.umc.product.member.application.port.in.command.dto.RegisterMemberCommand;
import com.umc.product.member.application.port.in.command.dto.UpdateMemberCommand;
import com.umc.product.member.application.port.out.SaveMemberPort;
import com.umc.product.member.domain.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberService implements ManageMemberUseCase {

    private final SaveMemberPort saveMemberPort;

    @Override
    @Transactional
    public Long registerMember(RegisterMemberCommand command) {
        Member savedMember = saveMemberPort.save(
                command.toEntity()
        );

        return savedMember.getId();
    }

    @Override
    public void updateMember(UpdateMemberCommand command) {
        throw new NotImplementedException();
    }

    @Override
    public void deleteMember(DeleteMemberCommand command) {
        throw new NotImplementedException();
    }
}
