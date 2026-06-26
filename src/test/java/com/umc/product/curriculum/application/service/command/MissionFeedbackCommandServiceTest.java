package com.umc.product.curriculum.application.service.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.umc.product.curriculum.application.port.in.command.dto.workbook.mission.CreateMissionFeedbackCommand;
import com.umc.product.curriculum.application.port.in.command.dto.workbook.mission.DeleteMissionFeedbackCommand;
import com.umc.product.curriculum.application.port.in.command.dto.workbook.mission.EditMissionFeedbackCommand;
import com.umc.product.curriculum.application.port.out.LoadMissionFeedbackPort;
import com.umc.product.curriculum.application.port.out.LoadMissionSubmissionPort;
import com.umc.product.curriculum.application.port.out.SaveMissionFeedbackPort;
import com.umc.product.curriculum.domain.MissionFeedback;
import com.umc.product.curriculum.domain.MissionSubmission;
import com.umc.product.curriculum.domain.enums.FeedbackResult;
import com.umc.product.curriculum.domain.exception.CurriculumDomainException;
import com.umc.product.curriculum.domain.exception.CurriculumErrorCode;

@ExtendWith(MockitoExtension.class)
@DisplayName("미션 피드백 Command Service")
class MissionFeedbackCommandServiceTest {

    private static final Long MISSION_SUBMISSION_ID = 10L;
    private static final Long MISSION_FEEDBACK_ID = 20L;
    private static final Long REVIEWER_MEMBER_ID = 30L;

    @Mock
    LoadMissionSubmissionPort loadMissionSubmissionPort;

    @Mock
    LoadMissionFeedbackPort loadMissionFeedbackPort;

    @Mock
    SaveMissionFeedbackPort saveMissionFeedbackPort;

    @InjectMocks
    MissionFeedbackCommandService sut;

    @Nested
    @DisplayName("미션 피드백 작성")
    class Create {

        @Test
        @DisplayName("운영진은 미션 제출물에 피드백을 작성할 수 있다")
        void createMissionFeedbackSuccess() {
            // given
            MissionSubmission submission = mock(MissionSubmission.class);
            given(loadMissionSubmissionPort.getById(MISSION_SUBMISSION_ID)).willReturn(submission);
            given(saveMissionFeedbackPort.save(any(MissionFeedback.class))).willAnswer(invocation -> {
                MissionFeedback feedback = invocation.getArgument(0);
                ReflectionTestUtils.setField(feedback, "id", MISSION_FEEDBACK_ID);
                return feedback;
            });

            CreateMissionFeedbackCommand command = CreateMissionFeedbackCommand.builder()
                .missionSubmissionId(MISSION_SUBMISSION_ID)
                .reviewerMemberId(REVIEWER_MEMBER_ID)
                .content("피드백")
                .result(FeedbackResult.PASS)
                .build();

            // when
            Long result = sut.create(command);

            // then
            assertThat(result).isEqualTo(MISSION_FEEDBACK_ID);

            ArgumentCaptor<MissionFeedback> captor = ArgumentCaptor.forClass(MissionFeedback.class);
            then(saveMissionFeedbackPort).should().save(captor.capture());
            assertThat(captor.getValue().getMissionSubmission()).isSameAs(submission);
            assertThat(captor.getValue().getReviewerMemberId()).isEqualTo(REVIEWER_MEMBER_ID);
            assertThat(captor.getValue().getContent()).isEqualTo("피드백");
            assertThat(captor.getValue().getFeedbackResult()).isEqualTo(FeedbackResult.PASS);
        }
    }

    @Nested
    @DisplayName("미션 피드백 수정")
    class Edit {

        @Test
        @DisplayName("작성자 본인은 피드백 내용을 수정할 수 있다")
        void editMissionFeedbackByReviewerSuccess() {
            // given
            MissionFeedback feedback = feedback(REVIEWER_MEMBER_ID, "기존 피드백");
            given(loadMissionFeedbackPort.getById(MISSION_FEEDBACK_ID)).willReturn(feedback);

            EditMissionFeedbackCommand command = EditMissionFeedbackCommand.builder()
                .missionFeedbackId(MISSION_FEEDBACK_ID)
                .reviewerMemberId(REVIEWER_MEMBER_ID)
                .content("수정된 피드백")
                .build();

            // when
            sut.edit(command);

            // then
            assertThat(feedback.getContent()).isEqualTo("수정된 피드백");
            then(saveMissionFeedbackPort).should().save(feedback);
        }

        @Test
        @DisplayName("작성자가 아니면 피드백 내용을 수정할 수 없다")
        void editMissionFeedbackByNonReviewerFails() {
            // given
            MissionFeedback feedback = feedback(999L, "기존 피드백");
            given(loadMissionFeedbackPort.getById(MISSION_FEEDBACK_ID)).willReturn(feedback);

            EditMissionFeedbackCommand command = EditMissionFeedbackCommand.builder()
                .missionFeedbackId(MISSION_FEEDBACK_ID)
                .reviewerMemberId(REVIEWER_MEMBER_ID)
                .content("수정된 피드백")
                .build();

            // when & then
            assertThatThrownBy(() -> sut.edit(command))
                .isInstanceOf(CurriculumDomainException.class)
                .extracting("baseCode")
                .isEqualTo(CurriculumErrorCode.WORKBOOK_ACCESS_DENIED);

            then(saveMissionFeedbackPort).should(never()).save(any());
        }
    }

    @Nested
    @DisplayName("미션 피드백 삭제")
    class Delete {

        @Test
        @DisplayName("작성자 본인은 피드백을 삭제할 수 있다")
        void deleteMissionFeedbackByReviewerSuccess() {
            // given
            MissionFeedback feedback = feedback(REVIEWER_MEMBER_ID, "피드백");
            given(loadMissionFeedbackPort.getById(MISSION_FEEDBACK_ID)).willReturn(feedback);

            DeleteMissionFeedbackCommand command = DeleteMissionFeedbackCommand.builder()
                .missionFeedbackId(MISSION_FEEDBACK_ID)
                .operatorMemberId(REVIEWER_MEMBER_ID)
                .build();

            // when
            sut.delete(command);

            // then
            then(saveMissionFeedbackPort).should().delete(feedback);
        }
    }

    private MissionFeedback feedback(Long reviewerMemberId, String content) {
        MissionFeedback feedback = MissionFeedback.create(
            mock(MissionSubmission.class),
            reviewerMemberId,
            content,
            FeedbackResult.FAIL
        );
        ReflectionTestUtils.setField(feedback, "id", MISSION_FEEDBACK_ID);
        return feedback;
    }
}
