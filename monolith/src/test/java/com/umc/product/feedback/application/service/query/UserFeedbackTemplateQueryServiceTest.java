package com.umc.product.feedback.application.service.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.umc.product.authorization.application.port.in.query.GetChallengerRoleUseCase;
import com.umc.product.challenger.application.port.in.query.GetChallengerUseCase;
import com.umc.product.challenger.application.port.in.query.dto.ChallengerInfo;
import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.feedback.application.port.in.query.dto.UserFeedbackTemplateInfo;
import com.umc.product.feedback.application.port.out.LoadUserFeedbackTemplatePort;
import com.umc.product.feedback.domain.UserFeedbackTemplate;
import com.umc.product.feedback.domain.enums.UserFeedbackContext;
import com.umc.product.feedback.domain.enums.UserFeedbackTargetType;
import com.umc.product.form.application.port.in.query.GetFormResponseUseCase;
import com.umc.product.form.application.port.in.query.GetFormUseCase;
import com.umc.product.form.application.port.in.query.dto.FormResponseInfo;
import com.umc.product.form.application.port.in.query.dto.FormWithStructureInfo;
import com.umc.product.form.domain.enums.FormResponseStatus;
import com.umc.product.organization.application.port.in.query.GetGisuUseCase;
import com.umc.product.organization.application.port.in.query.dto.gisu.GisuInfo;

@ExtendWith(MockitoExtension.class)
class UserFeedbackTemplateQueryServiceTest {

    private static final Long MEMBER_ID = 1L;
    private static final Long GISU_ID = 11L;
    private static final Long FORM_ID = 201L;

    @Mock
    LoadUserFeedbackTemplatePort loadUserFeedbackTemplatePort;
    @Mock
    GetGisuUseCase getGisuUseCase;
    @Mock
    GetChallengerUseCase getChallengerUseCase;
    @Mock
    GetChallengerRoleUseCase getChallengerRoleUseCase;
    @Mock
    GetFormUseCase getFormUseCase;
    @Mock
    GetFormResponseUseCase getFormResponseUseCase;

    @InjectMocks
    UserFeedbackTemplateQueryService sut;

    @ParameterizedTest
    @EnumSource(value = UserFeedbackContext.class, names = {"APPLICATION_SUBMITTED", "MATCHING_COMPLETED"})
    @DisplayName("해당 폼에 제출한 이력이 없으면 템플릿과 폼을 반환한다")
    void 미제출이면_폼을_반환한다(UserFeedbackContext context) {
        // Given
        givenTemplate(context, UserFeedbackTargetType.NEW_CHALLENGER);
        FormWithStructureInfo form = FormWithStructureInfo.builder()
            .formId(FORM_ID).title("사용자 조사").sections(List.of()).build();
        given(getFormUseCase.getFormWithStructure(FORM_ID)).willReturn(form);

        // When
        UserFeedbackTemplateInfo result = sut.findTemplate(MEMBER_ID, context).orElseThrow();

        // Then
        assertThat(result.form()).isSameAs(form);
        assertThat(result.templateId()).isEqualTo(101L);
        assertThat(result.context()).isEqualTo(context);
        then(getFormResponseUseCase).should().findSubmittedByFormIdAndRespondentMemberId(FORM_ID, MEMBER_ID);
        then(getFormResponseUseCase).shouldHaveNoMoreInteractions();
        then(loadUserFeedbackTemplatePort).should(never()).findByContextAndTargetType(
            context, UserFeedbackTargetType.CHALLENGER);
    }

    @ParameterizedTest
    @EnumSource(value = UserFeedbackContext.class, names = {"APPLICATION_SUBMITTED", "MATCHING_COMPLETED"})
    @DisplayName("해당 폼에 제출 완료한 사용자는 빈 결과를 반환하고 폼을 조회하지 않는다")
    void 제출했으면_폼을_반환하지_않는다(UserFeedbackContext context) {
        // Given
        givenTemplate(context, UserFeedbackTargetType.NEW_CHALLENGER);
        given(getFormResponseUseCase.findSubmittedByFormIdAndRespondentMemberId(FORM_ID, MEMBER_ID))
            .willReturn(Optional.of(FormResponseInfo.builder()
                .formId(FORM_ID).respondentMemberId(MEMBER_ID).status(FormResponseStatus.SUBMITTED).build()));

        // When
        Optional<UserFeedbackTemplateInfo> result = sut.findTemplate(MEMBER_ID, context);

        // Then
        assertThat(result).isEmpty();
        then(getFormUseCase).shouldHaveNoInteractions();
        then(loadUserFeedbackTemplatePort).should(never()).findByContextAndTargetType(
            context, UserFeedbackTargetType.CHALLENGER);
    }

    @ParameterizedTest
    @EnumSource(value = UserFeedbackTargetType.class, names = {"NEW_CHALLENGER", "EXPERIENCED_CHALLENGER", "ADMIN"})
    @DisplayName("대상별 전용 템플릿이 있으면 공통 템플릿을 조회하지 않는다")
    void 전용_템플릿을_우선_반환한다(UserFeedbackTargetType targetType) {
        // Given
        UserFeedbackContext context = targetType == UserFeedbackTargetType.ADMIN
            ? UserFeedbackContext.APPLICATION_MONITORING : UserFeedbackContext.APPLICATION_SUBMITTED;
        givenTemplate(context, targetType);
        given(getFormUseCase.getFormWithStructure(FORM_ID)).willReturn(FormWithStructureInfo.builder()
            .formId(FORM_ID).sections(List.of()).build());

        // When
        UserFeedbackTemplateInfo result = sut.findTemplate(MEMBER_ID, context).orElseThrow();

        // Then
        assertThat(result.targetType()).isEqualTo(targetType);
        then(loadUserFeedbackTemplatePort).should(never()).findByContextAndTargetType(
            context, UserFeedbackTargetType.CHALLENGER);
    }

    @Test
    @DisplayName("활성 기수가 없으면 템플릿과 제출 이력을 조회하지 않는다")
    void 활성_기수가_없으면_빈_결과를_반환한다() {
        // Given
        given(getGisuUseCase.findActiveGisu()).willReturn(Optional.empty());

        // When
        Optional<UserFeedbackTemplateInfo> result = sut.findTemplate(MEMBER_ID, UserFeedbackContext.APPLICATION_SUBMITTED);

        // Then
        assertThat(result).isEmpty();
        then(loadUserFeedbackTemplatePort).shouldHaveNoInteractions();
        then(getFormResponseUseCase).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("활성 템플릿이 없으면 제출 이력을 조회하지 않고 빈 결과를 반환한다")
    void 활성_템플릿이_없으면_빈_결과를_반환한다() {
        // Given
        givenRequester(UserFeedbackTargetType.NEW_CHALLENGER);

        // When
        Optional<UserFeedbackTemplateInfo> result = sut.findTemplate(MEMBER_ID, UserFeedbackContext.APPLICATION_SUBMITTED);

        // Then
        assertThat(result).isEmpty();
        then(getFormResponseUseCase).shouldHaveNoInteractions();
        then(getFormUseCase).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("현재 기수 챌린저가 아니면 빈 결과를 반환한다")
    void 대상_사용자가_아니면_빈_결과를_반환한다() {
        // Given
        given(getGisuUseCase.findActiveGisu()).willReturn(Optional.of(new GisuInfo(GISU_ID, 11L, null, null, true)));
        given(getChallengerUseCase.getAllByMemberId(MEMBER_ID)).willReturn(List.of());

        // When
        Optional<UserFeedbackTemplateInfo> result = sut.findTemplate(MEMBER_ID, UserFeedbackContext.APPLICATION_SUBMITTED);

        // Then
        assertThat(result).isEmpty();
        then(loadUserFeedbackTemplatePort).shouldHaveNoInteractions();
        then(getFormResponseUseCase).shouldHaveNoInteractions();
    }

    @ParameterizedTest
    @CsvSource({
        "APPLICATION_SUBMITTED, NEW_CHALLENGER",
        "APPLICATION_SUBMITTED, EXPERIENCED_CHALLENGER",
        "MATCHING_COMPLETED, NEW_CHALLENGER",
        "MATCHING_COMPLETED, EXPERIENCED_CHALLENGER"
    })
    @DisplayName("신규·기존 전용 템플릿이 없으면 CHALLENGER 공통 템플릿을 반환한다")
    void 전용_템플릿이_없으면_공통_템플릿을_반환한다(UserFeedbackContext context, UserFeedbackTargetType targetType) {
        // Given
        givenRequester(targetType);
        given(loadUserFeedbackTemplatePort.findByContextAndTargetType(context, targetType))
            .willReturn(Optional.empty());
        UserFeedbackTemplate common = UserFeedbackTemplate.create(context, UserFeedbackTargetType.CHALLENGER, FORM_ID);
        ReflectionTestUtils.setField(common, "id", 102L);
        given(loadUserFeedbackTemplatePort.findByContextAndTargetType(context, UserFeedbackTargetType.CHALLENGER))
            .willReturn(Optional.of(common));
        given(getFormUseCase.getFormWithStructure(FORM_ID)).willReturn(FormWithStructureInfo.builder()
            .formId(FORM_ID).sections(List.of()).build());

        // When
        UserFeedbackTemplateInfo result = sut.findTemplate(MEMBER_ID, context).orElseThrow();

        // Then
        assertThat(result.templateId()).isEqualTo(102L);
        assertThat(result.targetType()).isEqualTo(UserFeedbackTargetType.CHALLENGER);
        assertThat(result.form().formId()).isEqualTo(FORM_ID);
        then(loadUserFeedbackTemplatePort).should().findByContextAndTargetType(context, targetType);
        then(getFormResponseUseCase).should().findSubmittedByFormIdAndRespondentMemberId(FORM_ID, MEMBER_ID);
    }

    @ParameterizedTest
    @EnumSource(value = UserFeedbackTargetType.class, names = {"NEW_CHALLENGER", "EXPERIENCED_CHALLENGER"})
    @DisplayName("공통 폼에 이미 제출했으면 신규·기존 챌린저 모두 빈 결과를 반환한다")
    void 공통_폼에_제출했으면_빈_결과를_반환한다(UserFeedbackTargetType targetType) {
        // Given
        givenRequester(targetType);
        given(loadUserFeedbackTemplatePort.findByContextAndTargetType(UserFeedbackContext.APPLICATION_SUBMITTED, targetType))
            .willReturn(Optional.empty());
        given(loadUserFeedbackTemplatePort.findByContextAndTargetType(
            UserFeedbackContext.APPLICATION_SUBMITTED, UserFeedbackTargetType.CHALLENGER))
            .willReturn(Optional.of(UserFeedbackTemplate.create(
                UserFeedbackContext.APPLICATION_SUBMITTED, UserFeedbackTargetType.CHALLENGER, FORM_ID)));
        given(getFormResponseUseCase.findSubmittedByFormIdAndRespondentMemberId(FORM_ID, MEMBER_ID))
            .willReturn(Optional.of(FormResponseInfo.builder()
                .formId(FORM_ID).respondentMemberId(MEMBER_ID).status(FormResponseStatus.SUBMITTED).build()));

        // When
        Optional<UserFeedbackTemplateInfo> result = sut.findTemplate(MEMBER_ID, UserFeedbackContext.APPLICATION_SUBMITTED);

        // Then
        assertThat(result).isEmpty();
        then(getFormUseCase).shouldHaveNoInteractions();
    }

    @ParameterizedTest
    @EnumSource(value = UserFeedbackContext.class, names = "APPLICATION_MONITORING")
    @DisplayName("운영진 설문의 전용 템플릿이 없으면 공통 템플릿을 조회하지 않는다")
    void 운영진_설문은_공통_템플릿으로_대체하지_않는다(UserFeedbackContext context) {
        // Given
        givenRequester(UserFeedbackTargetType.ADMIN);

        // When
        Optional<UserFeedbackTemplateInfo> result = sut.findTemplate(MEMBER_ID, context);

        // Then
        assertThat(result).isEmpty();
        then(loadUserFeedbackTemplatePort).should().findByContextAndTargetType(context, UserFeedbackTargetType.ADMIN);
        then(loadUserFeedbackTemplatePort).shouldHaveNoMoreInteractions();
        then(getFormResponseUseCase).shouldHaveNoInteractions();
    }

    @ParameterizedTest
    @CsvSource({
        "APPLICATION_SUBMITTED, NEW_CHALLENGER",
        "APPLICATION_SUBMITTED, EXPERIENCED_CHALLENGER",
        "MATCHING_COMPLETED, NEW_CHALLENGER",
        "MATCHING_COMPLETED, EXPERIENCED_CHALLENGER"
    })
    @DisplayName("운영진이 현재 기수 챌린저를 겸하면 챌린저 설문과 운영진 설문을 각각 제공한다")
    void 운영진_챌린저_겸임자는_각_설문을_받는다(UserFeedbackContext context, UserFeedbackTargetType targetType) {
        // Given
        givenRequester(targetType);
        given(getChallengerRoleUseCase.isCentralMemberInGisu(MEMBER_ID, GISU_ID)).willReturn(true);
        given(loadUserFeedbackTemplatePort.findByContextAndTargetType(context, targetType))
            .willReturn(Optional.empty());
        given(loadUserFeedbackTemplatePort.findByContextAndTargetType(context, UserFeedbackTargetType.CHALLENGER))
            .willReturn(Optional.of(UserFeedbackTemplate.create(context, UserFeedbackTargetType.CHALLENGER, FORM_ID)));
        given(loadUserFeedbackTemplatePort.findByContextAndTargetType(
            UserFeedbackContext.APPLICATION_MONITORING, UserFeedbackTargetType.ADMIN))
            .willReturn(Optional.of(UserFeedbackTemplate.create(
                UserFeedbackContext.APPLICATION_MONITORING, UserFeedbackTargetType.ADMIN, 202L)));
        given(getFormUseCase.getFormWithStructure(FORM_ID)).willReturn(FormWithStructureInfo.builder()
            .formId(FORM_ID).sections(List.of()).build());
        given(getFormUseCase.getFormWithStructure(202L)).willReturn(FormWithStructureInfo.builder()
            .formId(202L).sections(List.of()).build());

        // When
        UserFeedbackTemplateInfo challengerSurvey = sut.findTemplate(MEMBER_ID, context).orElseThrow();
        UserFeedbackTemplateInfo adminSurvey = sut.findTemplate(MEMBER_ID, UserFeedbackContext.APPLICATION_MONITORING)
            .orElseThrow();

        // Then
        assertThat(challengerSurvey.context()).isEqualTo(context);
        assertThat(challengerSurvey.targetType()).isEqualTo(UserFeedbackTargetType.CHALLENGER);
        assertThat(challengerSurvey.form().formId()).isEqualTo(FORM_ID);
        assertThat(adminSurvey.targetType()).isEqualTo(UserFeedbackTargetType.ADMIN);
        assertThat(adminSurvey.form().formId()).isEqualTo(202L);
        then(loadUserFeedbackTemplatePort).should(never()).findByContextAndTargetType(context, UserFeedbackTargetType.ADMIN);
    }

    @Test
    @DisplayName("운영진이 아닌 챌린저에게는 운영진 설문을 제공하지 않는다")
    void 일반_챌린저에게_운영진_설문을_제공하지_않는다() {
        // Given
        given(getGisuUseCase.findActiveGisu()).willReturn(Optional.of(new GisuInfo(GISU_ID, 11L, null, null, true)));

        // When
        Optional<UserFeedbackTemplateInfo> result = sut.findTemplate(MEMBER_ID, UserFeedbackContext.APPLICATION_MONITORING);

        // Then
        assertThat(result).isEmpty();
        then(getChallengerRoleUseCase).should().isCentralMemberInGisu(MEMBER_ID, GISU_ID);
        then(getChallengerUseCase).shouldHaveNoInteractions();
        then(loadUserFeedbackTemplatePort).shouldHaveNoInteractions();
    }

    private void givenTemplate(UserFeedbackContext context, UserFeedbackTargetType targetType) {
        givenRequester(targetType);
        UserFeedbackTemplate template = UserFeedbackTemplate.create(context, targetType, FORM_ID);
        ReflectionTestUtils.setField(template, "id", 101L);
        given(loadUserFeedbackTemplatePort.findByContextAndTargetType(context, targetType))
            .willReturn(Optional.of(template));
    }

    private void givenRequester(UserFeedbackTargetType targetType) {
        given(getGisuUseCase.findActiveGisu()).willReturn(Optional.of(new GisuInfo(GISU_ID, 11L, null, null, true)));
        if (targetType == UserFeedbackTargetType.ADMIN) {
            given(getChallengerRoleUseCase.isCentralMemberInGisu(MEMBER_ID, GISU_ID)).willReturn(true);
            return;
        }
        ChallengerInfo current = ChallengerInfo.builder().gisuId(GISU_ID).part(ChallengerPart.SPRINGBOOT).build();
        List<ChallengerInfo> history = targetType == UserFeedbackTargetType.EXPERIENCED_CHALLENGER
            ? List.of(current, ChallengerInfo.builder().gisuId(9L).build()) : List.of(current);
        given(getChallengerUseCase.getAllByMemberId(MEMBER_ID)).willReturn(history);
    }
}
