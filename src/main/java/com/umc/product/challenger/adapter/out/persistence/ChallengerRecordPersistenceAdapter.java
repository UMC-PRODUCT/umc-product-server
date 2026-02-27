package com.umc.product.challenger.adapter.out.persistence;

import com.umc.product.challenger.application.port.out.LoadChallengerRecordPort;
import com.umc.product.challenger.application.port.out.SaveChallengerRecordPort;
import com.umc.product.challenger.domain.ChallengerRecord;
import com.umc.product.challenger.domain.exception.ChallengerDomainException;
import com.umc.product.challenger.domain.exception.ChallengerErrorCode;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ChallengerRecordPersistenceAdapter implements LoadChallengerRecordPort, SaveChallengerRecordPort {

    private final ChallengerRecordJpaRepository repository;

    @Override
    public Optional<ChallengerRecord> findById(Long id) {
        return repository.findById(id);
    }

    @Override
    public ChallengerRecord getById(Long id) {
        return repository.findById(id)
            .orElseThrow(() -> new ChallengerDomainException(ChallengerErrorCode.CHALLENGER_NOT_FOUND));
    }

    @Override
    public Optional<ChallengerRecord> findByCode(String code) {
        return repository.findByCode(code);
    }

    @Override
    public ChallengerRecord getByCode(String code) {
        return repository.findByCode(code)
            .orElseThrow(() -> new ChallengerDomainException(ChallengerErrorCode.CHALLENGER_NOT_FOUND));
    }

    @Override
    public boolean existsByCode(String code) {
        return repository.existsByCode(code);
    }

    @Override
    public ChallengerRecord save(ChallengerRecord record) {
        return repository.save(record);
    }

    @Override
    public List<ChallengerRecord> saveAll(List<ChallengerRecord> records) {
        return repository.saveAll(records);
    }

    @Override
    public void delete(ChallengerRecord record) {
        repository.delete(record);
    }
}
