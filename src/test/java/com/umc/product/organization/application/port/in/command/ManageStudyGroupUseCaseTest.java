package com.umc.product.organization.application.port.in.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.umc.product.challenger.domain.Challenger;
import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.global.exception.BusinessException;
import com.umc.product.member.domain.Member;
import com.umc.product.organization.application.port.in.command.dto.CreateStudyGroupCommand;
import com.umc.product.organization.application.port.in.command.dto.UpdateStudyGroupCommand;
import com.umc.product.organization.application.port.out.command.ManageGisuPort;
import com.umc.product.organization.application.port.out.command.ManageStudyGroupPort;
import com.umc.product.organization.application.port.out.query.LoadStudyGroupPort;
import com.umc.product.organization.domain.Gisu;
import com.umc.product.organization.domain.StudyGroup;
import com.umc.product.organization.domain.StudyGroupMember;
import com.umc.product.support.TestChallengerRepository;
import com.umc.product.support.TestMemberRepository;
import com.umc.product.support.UseCaseTestSupport;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

@Transactional
class ManageStudyGroupUseCaseTest extends UseCaseTestSupport {

    @Autowired
    private ManageStudyGroupUseCase manageStudyGroupUseCase;

    @Autowired
    private ManageGisuPort manageGisuPort;

    @Autowired
    private ManageStudyGroupPort manageStudyGroupPort;

    @Autowired
    private LoadStudyGroupPort loadStudyGroupPort;

    @Autowired
    private TestMemberRepository memberRepository;

    @Autowired
    private TestChallengerRepository challengerRepository;

    @Test
    void 스터디_그룹을_생성한다() {
        // given
        Gisu gisu = manageGisuPort.save(createActiveGisu(8L));
        Challenger leader = createAndSaveChallenger("리더", ChallengerPart.WEB, gisu.getId());
        Challenger member1 = createAndSaveChallenger("멤버1", ChallengerPart.WEB, gisu.getId());
        Challenger member2 = createAndSaveChallenger("멤버2", ChallengerPart.WEB, gisu.getId());
        Challenger member3 = createAndSaveChallenger("멤버3", ChallengerPart.WEB, gisu.getId());

        CreateStudyGroupCommand command = new CreateStudyGroupCommand(
                "React A팀",
                ChallengerPart.WEB,
                leader.getId(),
                Set.of(member1.getId(), member2.getId(), member3.getId())
        );

        // when
        manageStudyGroupUseCase.create(command);

        // then
        StudyGroup result = loadStudyGroupPort.findByName("React A팀");
        assertThat(result.getName()).isEqualTo("React A팀");
        assertThat(result.getPart()).isEqualTo(ChallengerPart.WEB);
        assertThat(getLeaderId(result)).isEqualTo(leader.getId());
        assertThat(getMemberIds(result)).containsExactlyInAnyOrder(member1.getId(), member2.getId(), member3.getId());
    }

    @Test
    void 스터디원_없이_스터디_그룹을_생성한다() {
        // given
        Gisu gisu = manageGisuPort.save(createActiveGisu(8L));
        Challenger leader = createAndSaveChallenger("리더", ChallengerPart.SPRINGBOOT, gisu.getId());

        CreateStudyGroupCommand command = new CreateStudyGroupCommand(
                "Spring B팀",
                ChallengerPart.SPRINGBOOT,
                leader.getId(),
                Set.of()
        );

        // when
        manageStudyGroupUseCase.create(command);

        // then
        StudyGroup result = loadStudyGroupPort.findByName("Spring B팀");
        assertThat(result.getName()).isEqualTo("Spring B팀");
        assertThat(getLeaderId(result)).isEqualTo(leader.getId());
        assertThat(getMemberIds(result)).isEmpty();
    }

    @Test
    void 스터디_그룹_이름을_수정한다() {
        // given
        Gisu gisu = manageGisuPort.save(createActiveGisu(8L));
        Challenger leader = createAndSaveChallenger("리더", ChallengerPart.WEB, gisu.getId());
        StudyGroup studyGroup = createAndSaveStudyGroup(gisu, "Original Name", ChallengerPart.WEB, leader.getId());

        UpdateStudyGroupCommand command = new UpdateStudyGroupCommand(
                studyGroup.getId(),
                "New Name",
                leader.getId(),
                Set.of()
        );

        // when
        manageStudyGroupUseCase.update(command);

        // then
        StudyGroup result = loadStudyGroupPort.findById(studyGroup.getId());
        assertThat(result.getName()).isEqualTo("New Name");
    }

    @Test
    void 스터디_그룹_리더를_변경한다() {
        // given
        Gisu gisu = manageGisuPort.save(createActiveGisu(8L));
        Challenger oldLeader = createAndSaveChallenger("기존리더", ChallengerPart.WEB, gisu.getId());
        Challenger newLeader = createAndSaveChallenger("새리더", ChallengerPart.WEB, gisu.getId());
        Challenger member1 = createAndSaveChallenger("멤버1", ChallengerPart.WEB, gisu.getId());
        Challenger member2 = createAndSaveChallenger("멤버2", ChallengerPart.WEB, gisu.getId());
        StudyGroup studyGroup = createAndSaveStudyGroup(gisu, "React A팀", ChallengerPart.WEB, oldLeader.getId());

        UpdateStudyGroupCommand command = new UpdateStudyGroupCommand(
                studyGroup.getId(),
                "React A팀",
                newLeader.getId(),
                Set.of(member1.getId(), member2.getId())
        );

        // when
        manageStudyGroupUseCase.update(command);

        // then
        StudyGroup result = loadStudyGroupPort.findById(studyGroup.getId());
        assertThat(getLeaderId(result)).isEqualTo(newLeader.getId());
        assertThat(getMemberIds(result)).containsExactlyInAnyOrder(member1.getId(), member2.getId());
    }

    @Test
    void 스터디_그룹_멤버를_전체_교체한다() {
        // given
        Gisu gisu = manageGisuPort.save(createActiveGisu(8L));
        Challenger leader = createAndSaveChallenger("리더", ChallengerPart.WEB, gisu.getId());
        Challenger oldMember1 = createAndSaveChallenger("기존멤버1", ChallengerPart.WEB, gisu.getId());
        Challenger oldMember2 = createAndSaveChallenger("기존멤버2", ChallengerPart.WEB, gisu.getId());
        Challenger newMember1 = createAndSaveChallenger("새멤버1", ChallengerPart.WEB, gisu.getId());
        Challenger newMember2 = createAndSaveChallenger("새멤버2", ChallengerPart.WEB, gisu.getId());
        Challenger newMember3 = createAndSaveChallenger("새멤버3", ChallengerPart.WEB, gisu.getId());

        StudyGroup studyGroup = createAndSaveStudyGroup(gisu, "React A팀", ChallengerPart.WEB, leader.getId());
        studyGroup.addMember(oldMember1.getId());
        studyGroup.addMember(oldMember2.getId());
        manageStudyGroupPort.save(studyGroup);

        UpdateStudyGroupCommand command = new UpdateStudyGroupCommand(
                studyGroup.getId(),
                "React A팀",
                leader.getId(),
                Set.of(newMember1.getId(), newMember2.getId(), newMember3.getId())
        );

        // when
        manageStudyGroupUseCase.update(command);

        // then
        StudyGroup result = loadStudyGroupPort.findById(studyGroup.getId());
        assertThat(getMemberIds(result)).containsExactlyInAnyOrder(newMember1.getId(), newMember2.getId(), newMember3.getId());
        assertThat(getMemberIds(result)).doesNotContain(oldMember1.getId(), oldMember2.getId());
    }

    @Test
    void 스터디_그룹을_삭제한다() {
        // given
        Gisu gisu = manageGisuPort.save(createActiveGisu(8L));
        StudyGroup studyGroup = createAndSaveStudyGroup(gisu, "React A팀", ChallengerPart.WEB, 101L);
        Long groupId = studyGroup.getId();

        // when
        manageStudyGroupUseCase.delete(groupId);

        // then
        assertThatThrownBy(() -> loadStudyGroupPort.findById(groupId))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void 존재하지_않는_스터디_그룹을_수정하면_예외가_발생한다() {
        // given
        UpdateStudyGroupCommand command = new UpdateStudyGroupCommand(
                999L,
                "New Name",
                101L,
                Set.of()
        );

        // when & then
        assertThatThrownBy(() -> manageStudyGroupUseCase.update(command))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void 존재하지_않는_스터디_그룹을_삭제하면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> manageStudyGroupUseCase.delete(999L))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void 멤버_ID에_중복이_있으면_중복은_무시된다() {
        // given
        Gisu gisu = manageGisuPort.save(createActiveGisu(8L));
        Challenger leader = createAndSaveChallenger("리더", ChallengerPart.WEB, gisu.getId());
        Challenger member1 = createAndSaveChallenger("멤버1", ChallengerPart.WEB, gisu.getId());
        Challenger member2 = createAndSaveChallenger("멤버2", ChallengerPart.WEB, gisu.getId());

        CreateStudyGroupCommand command = new CreateStudyGroupCommand(
                "React A팀",
                ChallengerPart.WEB,
                leader.getId(),
                new HashSet<>(List.of(member1.getId(), member1.getId(), member2.getId()))  // member1 중복
        );

        // when
        manageStudyGroupUseCase.create(command);

        // then
        StudyGroup result = loadStudyGroupPort.findByName("React A팀");
        assertThat(getMemberIds(result)).containsExactlyInAnyOrder(member1.getId(), member2.getId());
    }

    @Test
    void 리더가_멤버에_포함되면_예외가_발생한다() {
        // given
        manageGisuPort.save(createActiveGisu(8L));

        CreateStudyGroupCommand command = new CreateStudyGroupCommand(
                "React A팀",
                ChallengerPart.WEB,
                101L,
                Set.of(101L, 102L, 103L)  // 101L이 리더이자 멤버
        );

        // when & then
        assertThatThrownBy(() -> manageStudyGroupUseCase.create(command))
                .isInstanceOf(BusinessException.class);
    }

    private Gisu createActiveGisu(Long generation) {
        return Gisu.builder()
                .generation(generation)
                .isActive(true)
                .startAt(Instant.parse("2024-03-01T00:00:00Z"))
                .endAt(Instant.parse("2024-08-31T23:59:59Z"))
                .build();
    }

    private StudyGroup createAndSaveStudyGroup(Gisu gisu, String name, ChallengerPart part, Long leaderId) {
        StudyGroup studyGroup = StudyGroup.create(name, gisu, part);
        studyGroup.addMember(leaderId, true);
        return manageStudyGroupPort.save(studyGroup);
    }

    private Challenger createAndSaveChallenger(String name, ChallengerPart part, Long gisuId) {
        Member member = memberRepository.save(createMember(name));
        return challengerRepository.save(new Challenger(member.getId(), part, gisuId));
    }

    private Member createMember(String name) {
        return Member.builder()
                .name(name)
                .nickname(name)
                .email(name + "@test.com")
                .build();
    }

    private Long getLeaderId(StudyGroup studyGroup) {
        return studyGroup.getLeader()
                .map(StudyGroupMember::getChallengerId)
                .orElse(null);
    }

    private List<Long> getMemberIds(StudyGroup studyGroup) {
        return studyGroup.getStudyGroupMembers().stream()
                .filter(member -> !member.isLeader())
                .map(StudyGroupMember::getChallengerId)
                .toList();
    }
}
