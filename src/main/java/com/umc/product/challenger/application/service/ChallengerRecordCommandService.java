package com.umc.product.challenger.application.service;

import com.umc.product.challenger.application.port.in.command.ManageChallengerRecordUseCase;
import com.umc.product.challenger.application.port.in.command.dto.CreateChallengerRecordCommand;
import com.umc.product.challenger.application.port.out.LoadChallengerRecordPort;
import com.umc.product.challenger.application.port.out.SaveChallengerRecordPort;
import com.umc.product.challenger.domain.ChallengerRecord;
import com.umc.product.challenger.domain.exception.ChallengerDomainException;
import com.umc.product.challenger.domain.exception.ChallengerErrorCode;
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

    private final GetChapterUseCase getChapterUseCase;

    @Override
    public Long create(CreateChallengerRecordCommand command) {
        log.info("ChallengerRecord를 생성합니다. command={}", command.toString());

        validateRecord(command.gisuId(), command.chapterId(), command.schoolId());

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

    private void validateRecord(Long gisuId, Long schoolId, Long chapterId) {
        ChapterInfo chapterInfo = getChapterUseCase.byGisuAndSchool(gisuId, schoolId);

        if (!chapterInfo.id().equals(chapterId)) {
            throw new ChallengerDomainException(ChallengerErrorCode.INVALID_CHALLENGER_RECORD_CREATE_REQUEST,
                "주어진 학교는 해당 기수에 해당 지부에 속하지 않았습니다.");
        }
    }
}
