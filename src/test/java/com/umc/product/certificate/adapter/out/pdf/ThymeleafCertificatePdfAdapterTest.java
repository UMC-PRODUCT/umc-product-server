package com.umc.product.certificate.adapter.out.pdf;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import java.time.Instant;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import com.umc.product.certificate.application.port.out.dto.CertificatePdfRenderCommand;
import com.umc.product.certificate.domain.CertificateIssuer;
import com.umc.product.certificate.domain.CertificateType;

class ThymeleafCertificatePdfAdapterTest {

    @Test
    @DisplayName("Thymeleaf 인증서 템플릿을 PDF 바이트로 렌더링한다")
    void Thymeleaf_인증서_템플릿을_PDF_바이트로_렌더링한다() {
        // given
        ThymeleafCertificatePdfAdapter sut = new ThymeleafCertificatePdfAdapter(templateEngine());

        // when
        byte[] result = sut.render(CertificatePdfRenderCommand.builder()
            .serialNumber("UMC-CMP-20260701-ABCDEFGH")
            .type(CertificateType.COMPLETION)
            .issuer(CertificateIssuer.UNIVERSITY_MAKEUS_CHALLENGE)
            .recipientName("김유엠")
            .recipientSchoolName("유엠씨대학교")
            .gisuGeneration(7L)
            .issuedAt(Instant.parse("2026-07-01T00:00:00Z"))
            .expiresAt(Instant.parse("2027-07-01T00:00:00Z"))
            .verificationUrl("/api/v1/certificates/verify/UMC-CMP-20260701-ABCDEFGH")
            .build());

        // then
        assertThat(new String(result, 0, 4, StandardCharsets.US_ASCII)).isEqualTo("%PDF");
        assertThat(result).hasSizeGreaterThan(1_000);
    }

    @Test
    @DisplayName("Neordinary 발급 주체의 공로증 템플릿을 PDF 바이트로 렌더링한다")
    void Neordinary_발급_주체의_공로증_템플릿을_PDF_바이트로_렌더링한다() {
        // given
        ThymeleafCertificatePdfAdapter sut = new ThymeleafCertificatePdfAdapter(templateEngine());

        // when
        byte[] result = sut.render(CertificatePdfRenderCommand.builder()
            .serialNumber("UMC-MRT-20260701-ABCDEFGH")
            .type(CertificateType.MERIT)
            .issuer(CertificateIssuer.NEORDINARY)
            .recipientName("김유엠")
            .recipientSchoolName("유엠씨대학교")
            .gisuGeneration(7L)
            .meritTitle("대상")
            .meritDescription("탁월한 기여")
            .issuedAt(Instant.parse("2026-07-01T00:00:00Z"))
            .expiresAt(Instant.parse("2027-07-01T00:00:00Z"))
            .verificationUrl("/api/v1/certificates/verify/UMC-MRT-20260701-ABCDEFGH")
            .build());

        // then
        assertThat(new String(result, 0, 4, StandardCharsets.US_ASCII)).isEqualTo("%PDF");
        assertThat(result).hasSizeGreaterThan(1_000);
    }

    private SpringTemplateEngine templateEngine() {
        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix("templates/");
        resolver.setSuffix(".html");
        resolver.setTemplateMode("HTML");
        resolver.setCharacterEncoding("UTF-8");
        resolver.setCacheable(false);

        SpringTemplateEngine engine = new SpringTemplateEngine();
        engine.setTemplateResolver(resolver);
        return engine;
    }
}
