package com.umc.product.organization.application.port.in.command;

import com.umc.product.organization.application.port.in.command.dto.AddStudyMemberCommand;
import com.umc.product.organization.application.port.in.command.dto.AddStudyMentorCommand;
import com.umc.product.organization.application.port.in.command.dto.CreateStudyGroupCommand;
import com.umc.product.organization.application.port.in.command.dto.DeleteStudyMemberCommand;
import com.umc.product.organization.application.port.in.command.dto.DeleteStudyMentorCommand;
import com.umc.product.organization.application.port.in.command.dto.ReplaceStudyGroupMemberAndMentorCommand;
import com.umc.product.organization.application.port.in.command.dto.UpdateStudyGroupCommand;

public interface ManageStudyGroupUseCase {

    void create(CreateStudyGroupCommand command);

    void update(UpdateStudyGroupCommand command);

    void addMember(AddStudyMemberCommand command);

    void addMentor(AddStudyMentorCommand command);

    void deleteMember(DeleteStudyMemberCommand command);

    void deleteMentor(DeleteStudyMentorCommand command);

    void replaceMemberAndMentors(ReplaceStudyGroupMemberAndMentorCommand command);

    void delete(Long groupId);
}
