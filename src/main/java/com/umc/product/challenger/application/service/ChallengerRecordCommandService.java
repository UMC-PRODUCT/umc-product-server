package com.umc.product.challenger.application.service;

import com.umc.product.challenger.application.port.in.command.ManageChallengerRecordUseCase;
import com.umc.product.challenger.application.port.in.command.dto.CreateChallengerRecordCommand;
import com.umc.product.challenger.application.port.out.LoadChallengerRecordPort;
import com.umc.product.challenger.application.port.out.SaveChallengerRecordPort;
import com.umc.product.challenger.domain.ChallengerRecord;
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

    @Override
    public Long create(CreateChallengerRecordCommand command) {
        log.info("ChallengerRecord를 생성합니다. command={}", command.toString());
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
}
