package com.umc.product.certificate.adapter.out.pdf;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.net.URL;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Locale;

import javax.imageio.ImageIO;

import org.springframework.stereotype.Component;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import com.umc.product.certificate.application.port.out.RenderCertificatePdfPort;
import com.umc.product.certificate.application.port.out.dto.CertificatePdfRenderCommand;
import com.umc.product.certificate.domain.CertificateType;
import com.umc.product.certificate.domain.exception.CertificateErrorCode;
import com.umc.product.certificate.domain.exception.CertificateException;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ThymeleafCertificatePdfAdapter implements RenderCertificatePdfPort {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy년 MM월 dd일")
        .withLocale(Locale.KOREA)
        .withZone(ZoneId.of("Asia/Seoul"));

    private final SpringTemplateEngine templateEngine;

    @Override
    public byte[] render(CertificatePdfRenderCommand command) {
        try {
            Context context = new Context(Locale.KOREA);
            context.setVariable("serialNumber", command.serialNumber());
            context.setVariable("typeName", command.type().displayName());
            context.setVariable("recipientName", command.recipientName());
            context.setVariable("recipientSchoolName", command.recipientSchoolName());
            context.setVariable("gisuGeneration", command.gisuGeneration());
            context.setVariable("projectName", command.projectName());
            context.setVariable("awardTitle", command.awardTitle());
            context.setVariable("awardDescription", command.awardDescription());
            context.setVariable("issuedDate", DATE_FORMATTER.format(command.issuedAt()));
            context.setVariable("expiresDate", DATE_FORMATTER.format(command.expiresAt()));
            context.setVariable("verificationUrl", command.verificationUrl());
            context.setVariable("qrCodeDataUri", createQrCodeDataUri(command.verificationUrl()));

            String html = templateEngine.process(resolveTemplateName(command.type()), context);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.withHtmlContent(html, resolveBaseUri());
            builder.toStream(outputStream);
            builder.run();
            return outputStream.toByteArray();
        } catch (Exception e) {
            throw new CertificateException(CertificateErrorCode.CERTIFICATE_RENDER_FAILED, e);
        }
    }

    private String resolveTemplateName(CertificateType type) {
        return switch (type) {
            case COMPLETION -> "certificate/completion";
            case AWARD -> "certificate/award";
            case PROJECT_PARTICIPATION -> "certificate/project-participation";
        };
    }

    private String resolveBaseUri() {
        URL resource = getClass().getResource("/templates/certificate/");
        return resource != null ? resource.toString() : "";
    }

    private String createQrCodeDataUri(String verificationUrl) throws Exception {
        BitMatrix bitMatrix = new MultiFormatWriter().encode(
            verificationUrl,
            BarcodeFormat.QR_CODE,
            180,
            180,
            java.util.Map.of(
                EncodeHintType.CHARACTER_SET, "UTF-8",
                EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M,
                EncodeHintType.MARGIN, 1
            )
        );
        BufferedImage image = MatrixToImageWriter.toBufferedImage(bitMatrix);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(image, "png", outputStream);
        return "data:image/png;base64," + Base64.getEncoder().encodeToString(outputStream.toByteArray());
    }
}
