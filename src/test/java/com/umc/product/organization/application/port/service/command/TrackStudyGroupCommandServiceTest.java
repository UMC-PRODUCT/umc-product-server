package com.umc.product.organization.application.port.service.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.time.Instant;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.umc.product.challenger.application.port.in.query.GetChallengerUseCase;
import com.umc.product.challenger.application.port.in.query.dto.ChallengerBasicInfo;
import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.common.domain.enums.ChallengerStatus;
import com.umc.product.organization.application.port.in.command.dto.AddStudyMemberCommand;
import com.umc.product.organization.application.port.in.command.dto.CreateStudyGroupCommand;
import com.umc.product.organization.application.port.in.command.dto.UpdateStudyGroupCommand;
import com.umc.product.organization.application.port.out.command.SaveStudyGroupPort;
import com.umc.product.organization.application.port.out.query.LoadGisuPort;
import com.umc.product.organization.application.port.out.query.LoadStudyGroupPort;
import com.umc.product.organization.domain.Gisu;
import com.umc.product.organization.domain.StudyGroup;
import com.umc.product.organization.exception.OrganizationErrorCode;

@ExtendWith(MockitoExtension.class)
class TrackStudyGroupCommandServiceTest {

    @Mock private LoadStudyGroupPort loadStudyGroupPort;
    @Mock private LoadGisuPort loadGisuPort;
    @Mock private SaveStudyGroupPort saveStudyGroupPort;
    @Mock private GetChallengerUseCase getChallengerUseCase;
    @InjectMocks private StudyGroupCommandService service;

    @Test
    void 활성_챌린저는_자신의_파트_스터디에_참여할_수_있다() {
        given(loadGisuPort.getById(10L)).willReturn(gisu());
        given(getChallengerUseCase.listBasicByMemberIdsAndGisuId(Set.of(1L), 10L))
            .willReturn(List.of(challenger(ChallengerPart.WEB_PRODUCT_ENGINEER, false, ChallengerStatus.ACTIVE)));

        service.create(command(ChallengerPart.WEB_PRODUCT_ENGINEER));

        ArgumentCaptor<StudyGroup> captor = ArgumentCaptor.forClass(StudyGroup.class);
        verify(saveStudyGroupPort).save(captor.capture());
        assertThat(captor.getValue().getPart()).isEqualTo(ChallengerPart.WEB_PRODUCT_ENGINEER);
    }

    @Test
    void 인프라_선택_챌린저는_INFRA_스터디에_참여할_수_있다() {
        given(loadGisuPort.getById(10L)).willReturn(gisu());
        given(getChallengerUseCase.listBasicByMemberIdsAndGisuId(Set.of(1L), 10L))
            .willReturn(List.of(challenger(ChallengerPart.MOBILE_PRODUCT_ENGINEER, true, ChallengerStatus.ACTIVE)));

        service.create(command(ChallengerPart.INFRA));

        verify(saveStudyGroupPort).save(any());
    }

    @Test
    void 인프라를_선택하지_않은_챌린저는_INFRA_스터디에_참여할_수_없다() {
        given(loadGisuPort.getById(10L)).willReturn(gisu());
        given(getChallengerUseCase.listBasicByMemberIdsAndGisuId(Set.of(1L), 10L))
            .willReturn(List.of(challenger(ChallengerPart.WEB_PRODUCT_ENGINEER, false, ChallengerStatus.ACTIVE)));

        assertThatThrownBy(() -> service.create(command(ChallengerPart.INFRA)))
            .extracting("baseCode").isEqualTo(OrganizationErrorCode.STUDY_GROUP_TRACK_MEMBER_INVALID);
        verify(saveStudyGroupPort, never()).save(any());
    }

    @Test
    void 다른_파트_수강자는_스터디에_참여할_수_없다() {
        given(loadGisuPort.getById(10L)).willReturn(gisu());
        given(getChallengerUseCase.listBasicByMemberIdsAndGisuId(Set.of(1L), 10L))
            .willReturn(List.of(challenger(ChallengerPart.DESIGN, false, ChallengerStatus.ACTIVE)));

        assertThatThrownBy(() -> service.create(command(ChallengerPart.PLAN)))
            .extracting("baseCode").isEqualTo(OrganizationErrorCode.STUDY_GROUP_TRACK_MEMBER_INVALID);
    }

    @Test
    void 같은_기수와_파트의_다른_스터디에_속하면_생성을_거부한다() {
        given(loadGisuPort.getById(10L)).willReturn(gisu());
        given(getChallengerUseCase.listBasicByMemberIdsAndGisuId(Set.of(1L), 10L))
            .willReturn(List.of(challenger(ChallengerPart.PLAN, false, ChallengerStatus.ACTIVE)));
        given(loadStudyGroupPort.findConflictedMemberIds(10L, ChallengerPart.PLAN, Set.of(1L), null))
            .willReturn(Set.of(1L));

        assertThatThrownBy(() -> service.create(command(ChallengerPart.PLAN)))
            .extracting("baseCode").isEqualTo(OrganizationErrorCode.STUDY_GROUP_MEMBER_ALREADY_IN_PART_STUDY);
    }

    @Test
    void 최신_DB에_아직_소속되지_않은_수강자는_멤버추가할_수_있다() {
        StudyGroup group = group();
        given(loadStudyGroupPort.getEntityById(100L)).willReturn(group);
        given(getChallengerUseCase.listBasicByMemberIdsAndGisuId(Set.of(1L), 10L))
            .willReturn(List.of(challenger(ChallengerPart.PLAN, false, ChallengerStatus.ACTIVE)));
        service.addMember(AddStudyMemberCommand.of(100L, 1L));

        assertThat(group.hasMember(1L)).isTrue();
        verify(saveStudyGroupPort).save(group);
    }

    @Test
    void 스터디_파트를_변경할_때_기존_멤버의_수강여부를_검증한다() {
        StudyGroup group = group();
        given(loadStudyGroupPort.getEntityById(100L)).willReturn(group);
        given(getChallengerUseCase.listBasicByMemberIdsAndGisuId(Set.of(3L), 10L))
            .willReturn(List.of(challenger(3L, ChallengerPart.DESIGN, false, ChallengerStatus.ACTIVE)));

        service.update(new UpdateStudyGroupCommand(100L, null, ChallengerPart.DESIGN));

        assertThat(group.getPart()).isEqualTo(ChallengerPart.DESIGN);
        verify(saveStudyGroupPort).save(group);
    }

    private CreateStudyGroupCommand command(ChallengerPart part) {
        return new CreateStudyGroupCommand("파트 스터디", 10L, part, Set.of(2L), Set.of(1L));
    }

    private ChallengerBasicInfo challenger(ChallengerPart part, boolean infra, ChallengerStatus status) {
        return challenger(1L, part, infra, status);
    }

    private ChallengerBasicInfo challenger(
        Long memberId, ChallengerPart part, boolean infra, ChallengerStatus status
    ) {
        return new ChallengerBasicInfo(11L, memberId, 10L, part, infra, status);
    }

    private Gisu gisu() {
        Gisu gisu = Gisu.create(11L, Instant.parse("2026-09-01T00:00:00Z"),
            Instant.parse("2027-03-01T00:00:00Z"), false);
        ReflectionTestUtils.setField(gisu, "id", 10L);
        return gisu;
    }

    private StudyGroup group() {
        StudyGroup group = StudyGroup.create(
            "기획 스터디", 10L, ChallengerPart.PLAN, Set.of(3L), Set.of(2L));
        ReflectionTestUtils.setField(group, "id", 100L);
        return group;
    }
}
