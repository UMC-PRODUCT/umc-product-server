package com.umc.product.recruitment.application.service.command;

import com.umc.product.recruitment.application.port.in.command.CreateRecruitmentDraftFormResponseUseCase;
import com.umc.product.recruitment.application.port.in.command.CreateRecruitmentUseCase;
import com.umc.product.recruitment.application.port.in.command.DeleteRecruitmentFormResponseUseCase;
import com.umc.product.recruitment.application.port.in.command.DeleteRecruitmentUseCase;
import com.umc.product.recruitment.application.port.in.command.PublishRecruitmentUseCase;
import com.umc.product.recruitment.application.port.in.command.SubmitRecruitmentApplicationUseCase;
import com.umc.product.recruitment.application.port.in.command.UpdateRecruitmentDraftUseCase;
import com.umc.product.recruitment.application.port.in.command.UpsertRecruitmentFormQuestionsUseCase;
import com.umc.product.recruitment.application.port.in.command.UpsertRecruitmentFormResponseAnswersUseCase;
import com.umc.product.recruitment.application.port.in.command.dto.CreateOrGetDraftFormResponseInfo;
import com.umc.product.recruitment.application.port.in.command.dto.CreateOrGetRecruitmentDraftCommand;
import com.umc.product.recruitment.application.port.in.command.dto.CreateRecruitmentCommand;
import com.umc.product.recruitment.application.port.in.command.dto.CreateRecruitmentInfo;
import com.umc.product.recruitment.application.port.in.command.dto.DeleteRecruitmentCommand;
import com.umc.product.recruitment.application.port.in.command.dto.DeleteRecruitmentFormResponseCommand;
import com.umc.product.recruitment.application.port.in.command.dto.PublishRecruitmentCommand;
import com.umc.product.recruitment.application.port.in.command.dto.PublishRecruitmentInfo;
import com.umc.product.recruitment.application.port.in.command.dto.RecruitmentDraftInfo;
import com.umc.product.recruitment.application.port.in.command.dto.SubmitRecruitmentApplicationCommand;
import com.umc.product.recruitment.application.port.in.command.dto.SubmitRecruitmentApplicationInfo;
import com.umc.product.recruitment.application.port.in.command.dto.UpdateRecruitmentDraftCommand;
import com.umc.product.recruitment.application.port.in.command.dto.UpsertRecruitmentFormQuestionsCommand;
import com.umc.product.recruitment.application.port.in.command.dto.UpsertRecruitmentFormResponseAnswersCommand;
import com.umc.product.recruitment.application.port.in.command.dto.UpsertRecruitmentFormResponseAnswersInfo;
import com.umc.product.recruitment.application.port.in.query.dto.RecruitmentApplicationFormInfo;
import com.umc.product.recruitment.application.port.out.SaveRecruitmentPartPort;
import com.umc.product.recruitment.application.port.out.SaveRecruitmentPort;
import com.umc.product.recruitment.domain.Recruitment;
import com.umc.product.recruitment.domain.RecruitmentPart;
import com.umc.product.survey.application.port.out.SaveFormPort;
import com.umc.product.survey.domain.Form;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class RecruitmentService implements CreateRecruitmentDraftFormResponseUseCase,
        UpsertRecruitmentFormResponseAnswersUseCase,
        DeleteRecruitmentFormResponseUseCase,
        SubmitRecruitmentApplicationUseCase,
        CreateRecruitmentUseCase,
        DeleteRecruitmentUseCase,
        UpdateRecruitmentDraftUseCase,
        UpsertRecruitmentFormQuestionsUseCase,
        PublishRecruitmentUseCase {

    private final SaveFormPort saveFormPort;
    private final SaveRecruitmentPort saveRecruitmentPort;
    private final SaveRecruitmentPartPort saveRecruitmentPartPort;

    private Long resolveSchoolId() {
        return 1L;
    }

    private Long resolveActiveGisuId(Long schoolId) {
        return 1L;
    }

    @Override
    public CreateOrGetDraftFormResponseInfo createOrGet(CreateOrGetRecruitmentDraftCommand command) {
        return null;
    }

    @Override
    public UpsertRecruitmentFormResponseAnswersInfo upsert(
            UpsertRecruitmentFormResponseAnswersCommand command) {
        return null;
    }

    @Override
    public void delete(DeleteRecruitmentFormResponseCommand command) {
    }

    @Override
    public SubmitRecruitmentApplicationInfo submit(SubmitRecruitmentApplicationCommand command) {
        return null;
    }

    @Override
    public CreateRecruitmentInfo create(CreateRecruitmentCommand command) {

        Long schoolId = resolveSchoolId();
        Long gisuId = resolveActiveGisuId(schoolId);

        Form savedForm = saveFormPort.save(Form.createDraft(command.memberId()));

        Recruitment savedRecruitment = saveRecruitmentPort.save(
                Recruitment.createDraft(
                        schoolId,
                        gisuId,
                        savedForm.getId(),
                        command.recruitmentName()
                )
        );

        if (command.parts() != null && !command.parts().isEmpty()) {
            List<RecruitmentPart> recruitmentParts = command.parts().stream()
                    .distinct()
                    .map(part -> RecruitmentPart.createOpen(savedRecruitment.getId(), part))
                    .toList();

            saveRecruitmentPartPort.saveAll(recruitmentParts);
        }

        return CreateRecruitmentInfo.of(savedRecruitment.getId(), savedForm.getId());
    }

    @Override
    public void delete(DeleteRecruitmentCommand command) {

    }

    @Override
    public RecruitmentDraftInfo update(UpdateRecruitmentDraftCommand command) {
        return null;
    }

    @Override
    public RecruitmentApplicationFormInfo upsert(UpsertRecruitmentFormQuestionsCommand command) {
        return null;
    }

    @Override
    public PublishRecruitmentInfo publish(PublishRecruitmentCommand command) {
        return null;
    }
}
