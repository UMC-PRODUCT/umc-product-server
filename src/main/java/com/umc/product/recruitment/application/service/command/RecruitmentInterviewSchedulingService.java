package com.umc.product.recruitment.application.service.command;

import com.umc.product.recruitment.application.port.in.command.CreateInterviewAssignmentUseCase;
import com.umc.product.recruitment.application.port.in.command.DeleteInterviewAssignmentUseCase;
import com.umc.product.recruitment.application.port.in.command.dto.CreateInterviewAssignmentCommand;
import com.umc.product.recruitment.application.port.in.command.dto.CreateInterviewAssignmentResult;
import com.umc.product.recruitment.application.port.in.command.dto.DeleteInterviewAssignmentCommand;
import com.umc.product.recruitment.application.port.in.query.dto.DeleteInterviewAssignmentResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RecruitmentInterviewSchedulingService implements CreateInterviewAssignmentUseCase,
    DeleteInterviewAssignmentUseCase {

    @Override
    public CreateInterviewAssignmentResult create(CreateInterviewAssignmentCommand command) {
        // todo: 운영진 권한 검증 필요
        // 할당 시 interview assignment 생성 로직 필요
        return null;
    }

    @Override
    public DeleteInterviewAssignmentResult delete(DeleteInterviewAssignmentCommand command) {
        // todo: 운영진 권한 검증 필요
        // 할당 해제 시 interview assignment 삭제 로직 필요
        return null;
    }

}
