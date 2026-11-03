package com.umc.product.notification.adapter.out.external.smtp;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.Collection;
import java.util.Properties;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSenderImpl;

@Configuration
@ConditionalOnProperty(prefix = "app.notification.email", name = "provider", havingValue = "smtp")
@EnableConfigurationProperties(SmtpProperties.class)
public class SmtpEmailConfig {

    @Bean
    public JavaMailSenderImpl smtpMailSender(SmtpProperties properties) {
        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        sender.setHost(properties.host());
        sender.setPort(properties.port());
        sender.setUsername(properties.username());
        sender.setPassword(properties.password());
        sender.setDefaultEncoding(StandardCharsets.UTF_8.name());

        // 평문 인증과 인증서 검증 우회를 허용하지 않는다. 응답 없는 SMTP 서버가 작업 스레드를 점유하지 않게 한다.
        Properties mailProperties = sender.getJavaMailProperties();
        mailProperties.setProperty("mail.smtp.auth", "true");
        mailProperties.setProperty("mail.smtp.starttls.enable", "true");
        mailProperties.setProperty("mail.smtp.starttls.required", "true");
        mailProperties.setProperty("mail.smtp.ssl.checkserveridentity", "true");
        mailProperties.setProperty("mail.smtp.connectiontimeout", "5000");
        mailProperties.setProperty("mail.smtp.timeout", "5000");
        mailProperties.setProperty("mail.smtp.writetimeout", "5000");
        mailProperties.setProperty("mail.debug", "false");
        if (properties.trustedCertificate() != null) {
            mailProperties.put("mail.smtp.ssl.socketFactory", trustOnly(properties.trustedCertificate()));
        }
        sender.getSession().setDebug(false);
        return sender;
    }

    private static SSLSocketFactory trustOnly(Path certificateFile) {
        try (InputStream input = Files.newInputStream(certificateFile)) {
            Collection<? extends Certificate> certificates =
                CertificateFactory.getInstance("X.509").generateCertificates(input);
            if (certificates.isEmpty()) {
                throw new IllegalStateException(
                    "app.notification.email.smtp.trusted-certificate에 인증서가 없습니다: " + certificateFile);
            }
            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            trustStore.load(null, null);
            int index = 0;
            for (Certificate certificate : certificates) {
                trustStore.setCertificateEntry("smtp-trusted-" + index++, certificate);
            }
            TrustManagerFactory trustManagers =
                TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagers.init(trustStore);
            SSLContext context = SSLContext.getInstance("TLS");
            context.init(null, trustManagers.getTrustManagers(), null);
            return context.getSocketFactory();
        } catch (IOException | GeneralSecurityException e) {
            throw new IllegalStateException(
                "app.notification.email.smtp.trusted-certificate를 읽을 수 없습니다: " + certificateFile, e);
        }
    }
}
