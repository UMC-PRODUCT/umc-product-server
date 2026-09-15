package com.umc.product.challenger.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

import java.time.Instant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.umc.product.authorization.adapter.out.persistence.ChallengerRoleJpaRepository;
import com.umc.product.authorization.application.port.in.CheckPermissionUseCase;
import com.umc.product.authorization.application.port.in.command.ManageChallengerRoleUseCase;
import com.umc.product.authorization.application.port.in.command.dto.CreateChallengerRoleCommand;
import com.umc.product.challenger.adapter.out.persistence.ChallengerJpaRepository;
import com.umc.product.challenger.adapter.out.persistence.ChallengerRecordJpaRepository;
import com.umc.product.challenger.application.port.in.command.ManageChallengerRecordUseCase;
import com.umc.product.challenger.application.port.in.command.dto.ConsumeChallengerRecordCommand;
import com.umc.product.challenger.application.port.in.command.dto.CreateChallengerRecordCommand;
import com.umc.product.challenger.application.port.out.SaveChallengerPort;
import com.umc.product.challenger.domain.Challenger;
import com.umc.product.challenger.domain.ChallengerRecord;
import com.umc.product.challenger.domain.exception.ChallengerDomainException;
import com.umc.product.challenger.domain.exception.ChallengerErrorCode;
import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.common.domain.enums.ChallengerRoleType;
import com.umc.product.common.domain.enums.GisuLearningType;
import com.umc.product.member.application.port.out.SaveMemberPort;
import com.umc.product.member.domain.Member;
import com.umc.product.notification.application.port.in.SendWebhookAlarmUseCase;
import com.umc.product.organization.application.port.out.command.SaveGisuPort;
import com.umc.product.organization.domain.Chapter;
import com.umc.product.organization.domain.Gisu;
import com.umc.product.organization.domain.School;
import com.umc.product.support.IntegrationTestSupport;
import com.umc.product.support.fixture.ChapterFixture;
import com.umc.product.support.fixture.SchoolFixture;

@DisplayName("코드 한 번으로 기수 수강과 운영진 역할 등록")
class ChallengerRecordRegistrationIntegrationTest extends IntegrationTestSupport {

    @Autowired private ManageChallengerRecordUseCase records;
    @Autowired private SaveGisuPort saveGisuPort;
    @Autowired private ChapterFixture chapterFixture;
    @Autowired private SchoolFixture schoolFixture;
    @Autowired private SaveMemberPort saveMemberPort;
    @Autowired private SaveChallengerPort saveChallengerPort;
    @Autowired private ChallengerJpaRepository challengers;
    @Autowired private ChallengerRecordJpaRepository recordRepository;
    @Autowired private ChallengerRoleJpaRepository roles;
    @Autowired private ManageChallengerRoleUseCase manageRoles;
    @Autowired private CheckPermissionUseCase permissions;
    @MockitoBean private SendWebhookAlarmUseCase notifications;

    private Gisu gisu;
    private Chapter chapter;
    private School school;
    private Member member;

    @BeforeEach
    void 준비() {
        gisu = saveGisuPort.save(Gisu.create(9310L,
            Instant.parse("2026-09-01T00:00:00Z"), Instant.parse("2027-02-28T00:00:00Z"),
            false, GisuLearningType.TRACK));
        chapter = chapterFixture.지부(gisu, "통합등록지부");
        school = schoolFixture.지부에_소속된_학교("통합등록학교", chapter);
        member = saveMemberPort.save(Member.create("등록회원", "등록", "register@test.com", school.getId(), null));
    }

    @Test
    @DisplayName("신규 회원이 단일 파트와 인프라 선택 및 학교 회장 역할을 한 번에 등록한다")
    void 신규_수강과_역할_등록() {
        ChallengerRecord record = issue(ChallengerPart.WEB_PRODUCT_ENGINEER, true,
            ChallengerRoleType.SCHOOL_PRESIDENT);

        consume(record);

        Challenger challenger = membership();
        assertThat(challenger.getPart()).isEqualTo(ChallengerPart.WEB_PRODUCT_ENGINEER);
        assertThat(challenger.isInfra()).isTrue();
        assertThat(roles.findByChallengerId(challenger.getId())).singleElement().satisfies(role -> {
            assertThat(role.getChallengerRoleType()).isEqualTo(ChallengerRoleType.SCHOOL_PRESIDENT);
            assertThat(role.getOrganizationId()).isEqualTo(school.getId());
            assertThat(role.getGisuId()).isEqualTo(gisu.getId());
        });
        assertUsed(record);
    }

    @Test
    @DisplayName("수강 없는 학교 운영진은 파트 없이 역할만 등록한다")
    void 비수강_학교_운영진_등록() {
        ChallengerRecord record = issue(null, false, ChallengerRoleType.SCHOOL_VICE_PRESIDENT);

        consume(record);

        assertThat(membership().getPart()).isNull();
        assertThat(membership().isInfra()).isFalse();
        assertThat(roles.findByChallengerId(membership().getId())).hasSize(1);
        assertUsed(record);
    }

    @Test
    @DisplayName("지부 없는 중앙 운영진이 가입하고 중앙 권한을 조회한다")
    void 지부_없는_중앙_운영진_등록과_권한조회() {
        school = schoolFixture.학교("중앙만참여학교");
        member = saveMemberPort.save(Member.create("중앙회원", "중앙", "central@test.com", school.getId(), null));
        Long id = records.create(command(null, false, ChallengerRoleType.CENTRAL_OPERATING_TEAM_MEMBER)
            .chapterId(null).build());

        consume(recordRepository.findById(id).orElseThrow());

        var snapshot = permissions.loadSubject(member.getId()).toAuthoritySnapshot();
        assertThat(snapshot.isCentralMemberInGisu(gisu.getId())).isTrue();
        assertThat(snapshot.gisuChallengerInfos()).singleElement()
            .satisfies(info -> assertThat(info.chapterId()).isNull());
        assertThat(membership().getPart()).isNull();
    }

    @Test
    @DisplayName("기존 파트 수강생은 같은 파트 코드로 역할만 추가하고 기존 역할을 보존한다")
    void 기존_소속과_수강과_역할_보존() {
        Challenger existing = saveChallengerPort.save(Challenger.builder().memberId(member.getId())
            .gisuId(gisu.getId()).part(ChallengerPart.WEB_PRODUCT_ENGINEER).build());
        Long oldRole = manageRoles.createChallengerRole(CreateChallengerRoleCommand.builder()
            .challengerId(existing.getId()).gisuId(gisu.getId()).organizationId(chapter.getId())
            .roleType(ChallengerRoleType.CHAPTER_PRESIDENT).build());
        ChallengerRecord record = issue(ChallengerPart.WEB_PRODUCT_ENGINEER, false,
            ChallengerRoleType.SCHOOL_PRESIDENT);

        consume(record);

        assertThat(membership().getId()).isEqualTo(existing.getId());
        assertThat(membership().getPart()).isEqualTo(ChallengerPart.WEB_PRODUCT_ENGINEER);
        assertThat(roles.findByChallengerId(existing.getId())).hasSize(2)
            .anySatisfy(role -> assertThat(role.getId()).isEqualTo(oldRole));
        assertUsed(record);
    }

    @Test
    @DisplayName("기존 개발 파트 수강생은 코드로 인프라 선택을 추가할 수 있다")
    void 기존_개발_파트에_인프라_선택_추가() {
        saveChallengerPort.save(Challenger.builder().memberId(member.getId())
            .gisuId(gisu.getId()).part(ChallengerPart.WEB_PRODUCT_ENGINEER).build());
        ChallengerRecord record = issue(ChallengerPart.WEB_PRODUCT_ENGINEER, true, null);

        consume(record);

        assertThat(membership().getPart()).isEqualTo(ChallengerPart.WEB_PRODUCT_ENGINEER);
        assertThat(membership().isInfra()).isTrue();
        assertUsed(record);
    }

    @Test
    @DisplayName("기존 챌린저와 다른 파트의 코드는 등록하지 않는다")
    void 다른_파트_코드_거절() {
        saveChallengerPort.save(Challenger.builder().memberId(member.getId())
            .gisuId(gisu.getId()).part(ChallengerPart.WEB_PRODUCT_ENGINEER).build());
        ChallengerRecord record = issue(ChallengerPart.DESIGN, false, null);

        assertThatThrownBy(() -> consume(record)).isInstanceOf(ChallengerDomainException.class)
            .extracting("baseCode").isEqualTo(ChallengerErrorCode.INVALID_CHALLENGER_LEARNING_TYPE);

        assertThat(membership().getPart()).isEqualTo(ChallengerPart.WEB_PRODUCT_ENGINEER);
        assertThat(recordRepository.findById(record.getId()).orElseThrow().isUsed()).isFalse();
    }

    @Test
    @DisplayName("등록 중 실패하면 파트와 역할 및 코드 사용을 모두 롤백한다")
    void 등록_전체_롤백() {
        ChallengerRecord record = issue(ChallengerPart.DESIGN, false, ChallengerRoleType.SCHOOL_PRESIDENT);
        doThrow(new IllegalStateException("등록 후처리 실패")).when(notifications).sendBuffered(any());

        assertThatThrownBy(() -> consume(record)).isInstanceOf(IllegalStateException.class);

        assertThat(challengers.findByMemberId(member.getId())).isEmpty();
        assertThat(roles.count()).isZero();
        assertThat(recordRepository.findById(record.getId()).orElseThrow().isUsed()).isFalse();
    }

    private CreateChallengerRecordCommand.CreateChallengerRecordCommandBuilder command(
        ChallengerPart part, boolean infra, ChallengerRoleType role
    ) {
        return CreateChallengerRecordCommand.builder().creatorMemberId(member.getId())
            .gisuId(gisu.getId()).chapterId(chapter.getId()).schoolId(school.getId())
            .memberName(member.getName()).part(part).infra(infra).challengerRoleType(role);
    }

    private ChallengerRecord issue(ChallengerPart part, boolean infra, ChallengerRoleType role) {
        return recordRepository.findById(records.create(command(part, infra, role).build())).orElseThrow();
    }

    private void consume(ChallengerRecord record) {
        records.consumeCode(ConsumeChallengerRecordCommand.builder()
            .targetMemberId(member.getId()).code(record.getCode()).build());
    }

    private Challenger membership() {
        return challengers.findByMemberIdAndGisuId(member.getId(), gisu.getId()).orElseThrow();
    }

    private void assertUsed(ChallengerRecord record) {
        ChallengerRecord saved = recordRepository.findById(record.getId()).orElseThrow();
        assertThat(saved.isUsed()).isTrue();
        assertThat(saved.getUsedMemberId()).isEqualTo(member.getId());
    }
}
