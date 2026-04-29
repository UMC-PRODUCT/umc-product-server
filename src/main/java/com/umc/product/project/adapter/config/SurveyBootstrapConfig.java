package com.umc.product.project.adapter.config;

import com.umc.product.survey.application.port.in.command.ManageFormSectionUseCase;
import com.umc.product.survey.application.port.in.command.ManageFormUseCase;
import com.umc.product.survey.application.port.in.command.ManageQuestionOptionUseCase;
import com.umc.product.survey.application.port.in.command.ManageQuestionUseCase;
import com.umc.product.survey.application.port.in.command.dto.CreateDraftFormCommand;
import com.umc.product.survey.application.port.in.command.dto.CreateFormSectionCommand;
import com.umc.product.survey.application.port.in.command.dto.CreateQuestionCommand;
import com.umc.product.survey.application.port.in.command.dto.CreateQuestionOptionCommand;
import com.umc.product.survey.application.port.in.command.dto.DeleteFormCommand;
import com.umc.product.survey.application.port.in.command.dto.DeleteFormSectionCommand;
import com.umc.product.survey.application.port.in.command.dto.DeleteQuestionCommand;
import com.umc.product.survey.application.port.in.command.dto.DeleteQuestionOptionCommand;
import com.umc.product.survey.application.port.in.command.dto.PublishFormCommand;
import com.umc.product.survey.application.port.in.command.dto.ReorderFormSectionsCommand;
import com.umc.product.survey.application.port.in.command.dto.ReorderQuestionOptionsCommand;
import com.umc.product.survey.application.port.in.command.dto.ReorderQuestionsCommand;
import com.umc.product.survey.application.port.in.command.dto.UpdateFormCommand;
import com.umc.product.survey.application.port.in.command.dto.UpdateFormSectionCommand;
import com.umc.product.survey.application.port.in.command.dto.UpdateQuestionCommand;
import com.umc.product.survey.application.port.in.command.dto.UpdateQuestionOptionCommand;
import com.umc.product.survey.application.port.in.query.GetFormUseCase;
import com.umc.product.survey.application.port.in.query.dto.FormInfo;
import com.umc.product.survey.application.port.in.query.dto.FormWithStructureInfo;
import java.util.Optional;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Survey 도메인 5종 UseCase 의 임시 stub 빈 등록.
 * <p>
 * 본 PR2 는 Survey 인터페이스에만 의존하지만, Survey 구현체가 develop 에 머지되기 전엔
 * Spring 컨텍스트가 빈을 못 찾아 부팅이 실패한다. Survey 구현 PR 이 머지될 때까지
 * 부팅만 가능하도록 임시로 빈을 등록한다.
 * <p>
 * {@link ConditionalOnMissingBean} 덕분에 Survey 구현체가 등록되면 자동으로 진짜 빈이 우선되고
 * 본 stub 은 무시된다. Survey 구현 PR 머지 후 본 클래스 전체를 삭제하면 된다.
 *
 * <h2>호출 시 동작</h2>
 * stub 메서드를 실제로 호출하면 {@link UnsupportedOperationException} 을 던져 즉시 실패한다.
 * PROJECT-106 / 106-GET 엔드포인트는 호출 시 모두 Survey 메서드를 거치므로 동작 안 함 —
 * 부팅만 가능한 상태일 뿐이다.
 */
// TODO: Survey 구현 PR (ManageFormService / ManageFormSectionService 등) 머지되면 본 클래스 삭제
@Configuration
public class SurveyBootstrapConfig {

    private static final String NOT_IMPLEMENTED_MSG =
        "Survey 도메인 구현 미머지 상태입니다. 실제 호출 전에 Survey 구현 PR 을 먼저 머지해주세요.";

    @Bean
    @ConditionalOnMissingBean
    public ManageFormUseCase manageFormUseCaseStub() {
        return new ManageFormUseCase() {
            @Override
            public Long createDraft(CreateDraftFormCommand command) {
                throw new UnsupportedOperationException(NOT_IMPLEMENTED_MSG);
            }

            @Override
            public void updateForm(UpdateFormCommand command) {
                throw new UnsupportedOperationException(NOT_IMPLEMENTED_MSG);
            }

            @Override
            public void publishForm(PublishFormCommand command) {
                throw new UnsupportedOperationException(NOT_IMPLEMENTED_MSG);
            }

            @Override
            public void deleteForm(DeleteFormCommand command) {
                throw new UnsupportedOperationException(NOT_IMPLEMENTED_MSG);
            }
        };
    }

    @Bean
    @ConditionalOnMissingBean
    public ManageFormSectionUseCase manageFormSectionUseCaseStub() {
        return new ManageFormSectionUseCase() {
            @Override
            public Long createSection(CreateFormSectionCommand command) {
                throw new UnsupportedOperationException(NOT_IMPLEMENTED_MSG);
            }

            @Override
            public void updateSection(UpdateFormSectionCommand command) {
                throw new UnsupportedOperationException(NOT_IMPLEMENTED_MSG);
            }

            @Override
            public void deleteSection(DeleteFormSectionCommand command) {
                throw new UnsupportedOperationException(NOT_IMPLEMENTED_MSG);
            }

            @Override
            public void reorderSections(ReorderFormSectionsCommand command) {
                throw new UnsupportedOperationException(NOT_IMPLEMENTED_MSG);
            }
        };
    }

    @Bean
    @ConditionalOnMissingBean
    public ManageQuestionUseCase manageQuestionUseCaseStub() {
        return new ManageQuestionUseCase() {
            @Override
            public Long createQuestion(CreateQuestionCommand command) {
                throw new UnsupportedOperationException(NOT_IMPLEMENTED_MSG);
            }

            @Override
            public void updateQuestion(UpdateQuestionCommand command) {
                throw new UnsupportedOperationException(NOT_IMPLEMENTED_MSG);
            }

            @Override
            public void deleteQuestion(DeleteQuestionCommand command) {
                throw new UnsupportedOperationException(NOT_IMPLEMENTED_MSG);
            }

            @Override
            public void reorderQuestions(ReorderQuestionsCommand command) {
                throw new UnsupportedOperationException(NOT_IMPLEMENTED_MSG);
            }
        };
    }

    @Bean
    @ConditionalOnMissingBean
    public ManageQuestionOptionUseCase manageQuestionOptionUseCaseStub() {
        return new ManageQuestionOptionUseCase() {
            @Override
            public Long createOption(CreateQuestionOptionCommand command) {
                throw new UnsupportedOperationException(NOT_IMPLEMENTED_MSG);
            }

            @Override
            public void updateOption(UpdateQuestionOptionCommand command) {
                throw new UnsupportedOperationException(NOT_IMPLEMENTED_MSG);
            }

            @Override
            public void deleteOption(DeleteQuestionOptionCommand command) {
                throw new UnsupportedOperationException(NOT_IMPLEMENTED_MSG);
            }

            @Override
            public void reorderOptions(ReorderQuestionOptionsCommand command) {
                throw new UnsupportedOperationException(NOT_IMPLEMENTED_MSG);
            }
        };
    }

    @Bean
    @ConditionalOnMissingBean
    public GetFormUseCase getFormUseCaseStub() {
        return new GetFormUseCase() {
            @Override
            public Optional<FormInfo> findById(Long formId) {
                throw new UnsupportedOperationException(NOT_IMPLEMENTED_MSG);
            }

            @Override
            public FormInfo getById(Long formId) {
                throw new UnsupportedOperationException(NOT_IMPLEMENTED_MSG);
            }

            @Override
            public FormWithStructureInfo getFormWithStructure(Long formId) {
                throw new UnsupportedOperationException(NOT_IMPLEMENTED_MSG);
            }
        };
    }
}
