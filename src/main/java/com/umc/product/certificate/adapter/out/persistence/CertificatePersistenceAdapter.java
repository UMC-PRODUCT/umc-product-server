package com.umc.product.certificate.adapter.out.persistence;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.umc.product.certificate.application.port.out.LoadCertificatePort;
import com.umc.product.certificate.application.port.out.SaveCertificatePort;
import com.umc.product.certificate.domain.Certificate;
import com.umc.product.certificate.domain.CertificateStatus;
import com.umc.product.certificate.domain.CertificateType;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CertificatePersistenceAdapter implements LoadCertificatePort, SaveCertificatePort {

    private final CertificateRepository certificateRepository;

    @Override
    public Optional<Certificate> findById(Long certificateId) {
        return certificateRepository.findById(certificateId);
    }

    @Override
    public Optional<Certificate> findBySerialNumber(String serialNumber) {
        return certificateRepository.findBySerialNumber(serialNumber);
    }

    @Override
    public Optional<Certificate> findValidByScope(
        CertificateType type,
        Long recipientMemberId,
        Long gisuId,
        Long projectId,
        String meritTitle,
        Instant now
    ) {
        return certificateRepository.findValidByScope(
            type,
            recipientMemberId,
            gisuId,
            projectId,
            meritTitle,
            CertificateStatus.ISSUED,
            now
        ).stream().findFirst();
    }

    @Override
    public boolean existsBySerialNumber(String serialNumber) {
        return certificateRepository.existsBySerialNumber(serialNumber);
    }

    @Override
    public List<Certificate> listByRecipientMemberId(Long memberId) {
        return certificateRepository.findAllByRecipientMemberIdOrderByIssuedAtDescIdDesc(memberId);
    }

    @Override
    public Certificate save(Certificate certificate) {
        return certificateRepository.save(certificate);
    }
}
