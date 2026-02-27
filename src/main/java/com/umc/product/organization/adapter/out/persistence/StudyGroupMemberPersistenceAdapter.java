package com.umc.product.organization.adapter.out.persistence;


import com.umc.product.organization.application.port.out.command.ManageStudyGroupMemberPort;
import com.umc.product.organization.application.port.out.query.LoadStudyGroupMemberPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StudyGroupMemberPersistenceAdapter implements ManageStudyGroupMemberPort, LoadStudyGroupMemberPort {
}
