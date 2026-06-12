package com.umc.product.organization.application.port.in.command;

import com.umc.product.organization.application.port.in.command.dto.CreateUmcProductMemberCommand;
import com.umc.product.organization.application.port.in.command.dto.ReplaceUmcProductMemberFunctionalMembershipsCommand;
import com.umc.product.organization.application.port.in.command.dto.UpdateUmcProductMemberProfileCommand;

public interface ManageUmcProductMemberUseCase {

    Long create(CreateUmcProductMemberCommand command);

    void updateProfile(UpdateUmcProductMemberProfileCommand command);

    void replaceFunctionalMemberships(ReplaceUmcProductMemberFunctionalMembershipsCommand command);

    void delete(Long umcProductMemberId, Long requesterMemberId);
}
