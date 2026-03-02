package com.umc.product.challenger.application.service;

import com.umc.product.authorization.application.port.in.command.ManageChallengerRoleUseCase;
import com.umc.product.authorization.application.port.in.command.dto.CreateChallengerRoleCommand;
import com.umc.product.challenger.application.port.in.command.ManageChallengerRecordUseCase;
import com.umc.product.challenger.application.port.in.command.dto.ConsumeChallengerRecordCommand;
import com.umc.product.challenger.application.port.in.command.dto.CreateChallengerRecordCommand;
import com.umc.product.challenger.application.port.out.LoadChallengerPort;
import com.umc.product.challenger.application.port.out.LoadChallengerRecordPort;
import com.umc.product.challenger.application.port.out.SaveChallengerPort;
import com.umc.product.challenger.application.port.out.SaveChallengerRecordPort;
import com.umc.product.challenger.domain.Challenger;
import com.umc.product.challenger.domain.ChallengerRecord;
import com.umc.product.challenger.domain.exception.ChallengerDomainException;
import com.umc.product.challenger.domain.exception.ChallengerErrorCode;
import com.umc.product.member.application.port.in.query.GetMemberUseCase;
import com.umc.product.member.application.port.in.query.MemberInfo;
import com.umc.product.organization.application.port.in.query.GetChapterUseCase;
import com.umc.product.organization.application.port.in.query.dto.ChapterInfo;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChallengerRecordCommandService implements ManageChallengerRecordUseCase {

    private final SaveChallengerRecordPort saveChallengerRecordPort;
    private final LoadChallengerRecordPort loadChallengerRecordPort;

    private final SaveChallengerPort saveChallengerPort;
    private final LoadChallengerPort loadChallengerPort;

    private final GetChapterUseCase getChapterUseCase;
    private final GetMemberUseCase getMemberUseCase;
    private final ManageChallengerRoleUseCase manageChallengerRoleUseCase;

    @Override
    public Long create(CreateChallengerRecordCommand command) {
        log.info("ChallengerRecord를 생성합니다. command={}", command.toString());

        validateRecord(command.gisuId(), command.schoolId(), command.chapterId());

        return saveChallengerRecordPort.save(command.toEntity()).getId();
    }

    @Override
    public List<Long> createBulk(List<CreateChallengerRecordCommand> commands) {
        log.info("ChallengerRecord를 대량으로 생성합니다. commands={}",
            commands.stream().map(CreateChallengerRecordCommand::toString).toList());

        List<ChallengerRecord> records = commands.stream()
            .map(CreateChallengerRecordCommand::toEntity)
            .toList();

        return saveChallengerRecordPort.saveAll(records)
            .stream().map(ChallengerRecord::getId).toList();
    }

    @Override
    public void delete(Long id) {
        saveChallengerRecordPort.delete(loadChallengerRecordPort.getById(id));
    }

    @Override
    public void consumeCode(ConsumeChallengerRecordCommand command) {
        String code = command.code();
        Long memberId = command.targetMemberId();

        ChallengerRecord record = loadChallengerRecordPort.getByCode(code);
        record.validateNotUsed();

        // 운영진 기록, 즉 ChallengerRole 객체를 추가해야하는 상황이라면 Challenger를 생성하지 않음
        if (record.isAdminRecord()) {
            Long challengerId = loadChallengerPort.findByMemberIdAndGisuId(memberId, record.getGisuId())
                .orElseThrow(() -> new ChallengerDomainException(ChallengerErrorCode.NO_CHALLENGER_IN_MEMBER_GISU))
                .getId();

            manageChallengerRoleUseCase.createChallengerRole(
                CreateChallengerRoleCommand.builder()
                    .challengerId(challengerId)
                    .roleType(record.getChallengerRoleType())
                    .organizationId(record.getOrganizationId())
                    .responsiblePart(record.getPart())
                    .gisuId(record.getGisuId())
                    .build()
            );
        }

        // 챌린저 기록 추가하기
        else {
            MemberInfo memberInfo = getMemberUseCase.getMemberInfoById(memberId);
            record.validateMember(memberInfo.name(), memberInfo.schoolId());

            saveChallengerPort.save(
                Challenger.builder()
                    .memberId(memberId)
                    .part(record.getPart())
                    .gisuId(record.getGisuId())
                    .build()
            );
        }

        record.markAsUsed(memberId);
    }

    private void validateRecord(Long gisuId, Long schoolId, Long chapterId) {
        ChapterInfo chapterInfo = getChapterUseCase.byGisuAndSchool(gisuId, schoolId);

        if (!chapterInfo.id().equals(chapterId)) {
            log.debug("학교 schoolId={}가 해당 기수에 속한 지부는 {}, id={} 이지만 요청에서 제공된 chapterId={} 입니다.",
                schoolId, chapterInfo.name(), chapterInfo.id(), chapterId);

            throw new ChallengerDomainException(ChallengerErrorCode.INVALID_CHALLENGER_RECORD_CREATE_REQUEST,
                "주어진 학교는 해당 기수에 해당 지부에 속하지 않았습니다.");
        }
    }
}
