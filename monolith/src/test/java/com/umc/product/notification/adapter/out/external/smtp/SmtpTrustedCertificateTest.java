package com.umc.product.notification.adapter.out.external.smtp;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.cert.CertPathBuilderException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.GeneralNames;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;

class SmtpTrustedCertificateTest {

    private static final char[] KEY_PASSWORD = "test-only".toCharArray();

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withUserConfiguration(SmtpEmailConfig.class)
        .withPropertyValues(
            "app.notification.email.provider=smtp",
            "app.notification.email.smtp.username=sender@example.org",
            "app.notification.email.smtp.password=test-app-password"
        );

    @TempDir
    Path tempDir;

    private StartTlsSmtpServer server;
    private Path certificatePem;

    @BeforeEach
    void setUp() throws Exception {
        KeyPair keyPair = KeyPairGenerator.getInstance("EC").generateKeyPair();
        X509Certificate certificate = selfSignedForLocalhost(keyPair);
        certificatePem = tempDir.resolve("smtp.crt");
        Files.writeString(certificatePem, "-----BEGIN CERTIFICATE-----\n"
            + Base64.getMimeEncoder(64, "\n".getBytes(StandardCharsets.US_ASCII))
            .encodeToString(certificate.getEncoded())
            + "\n-----END CERTIFICATE-----\n");
        server = new StartTlsSmtpServer(keyPair, certificate);
    }

    @AfterEach
    void tearDown() throws Exception {
        server.close();
    }

    @Test
    @DisplayName("신뢰 인증서를 지정하지 않으면 자체 서명 SMTP 서버를 JVM 기본 CA로 거부한다")
    void rejects_self_signed_server_without_trusted_certificate() {
        // given / when / then
        contextRunner.withPropertyValues(
            "app.notification.email.smtp.host=localhost",
            "app.notification.email.smtp.port=" + server.port()
        ).run(context -> {
            JavaMailSenderImpl sender = context.getBean(JavaMailSenderImpl.class);
            assertThatThrownBy(() -> sender.send(message()))
                .isInstanceOf(MailSendException.class)
                .hasRootCauseInstanceOf(CertPathBuilderException.class);
            assertThat(server.receivedMessages()).isEmpty();
        });
    }

    @Test
    @DisplayName("신뢰 인증서를 지정하면 STARTTLS와 인증을 거쳐 자체 서명 SMTP 서버로 발송한다")
    void sends_through_starttls_with_trusted_certificate() {
        // given / when
        contextRunner.withPropertyValues(
            "app.notification.email.smtp.host=localhost",
            "app.notification.email.smtp.port=" + server.port(),
            "app.notification.email.smtp.trusted-certificate=" + certificatePem
        ).run(context -> {
            JavaMailSenderImpl sender = context.getBean(JavaMailSenderImpl.class);
            sender.send(message());

            // then
            assertThat(sender.getJavaMailProperties())
                .containsEntry("mail.smtp.starttls.required", "true")
                .containsEntry("mail.smtp.ssl.checkserveridentity", "true")
                .doesNotContainKey("mail.smtp.ssl.trust");
            assertThat(server.receivedMessages().poll(5, TimeUnit.SECONDS))
                .contains("Subject: trusted certificate");
            assertThat(server.authenticatedOverTls()).isTrue();
        });
    }

    @Test
    @DisplayName("신뢰 인증서를 지정해도 인증서 이름과 다른 호스트로 접속하면 거부한다")
    void rejects_host_name_mismatch_with_trusted_certificate() {
        // given / when / then
        contextRunner.withPropertyValues(
            "app.notification.email.smtp.host=127.0.0.1",
            "app.notification.email.smtp.port=" + server.port(),
            "app.notification.email.smtp.trusted-certificate=" + certificatePem
        ).run(context -> {
            JavaMailSenderImpl sender = context.getBean(JavaMailSenderImpl.class);
            assertThatThrownBy(() -> sender.send(message()))
                .isInstanceOf(MailSendException.class)
                .hasRootCauseInstanceOf(CertificateException.class);
            assertThat(server.receivedMessages()).isEmpty();
        });
    }

    private static SimpleMailMessage message() {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("sender@example.org");
        message.setTo("recipient@example.org");
        message.setSubject("trusted certificate");
        message.setText("body");
        return message;
    }

    private static X509Certificate selfSignedForLocalhost(KeyPair keyPair) throws Exception {
        X500Name name = new X500Name("CN=localhost");
        Instant now = Instant.now();
        JcaX509v3CertificateBuilder builder = new JcaX509v3CertificateBuilder(name,
            BigInteger.valueOf(now.toEpochMilli()), Date.from(now.minus(Duration.ofHours(1))),
            Date.from(now.plus(Duration.ofDays(1))), name, keyPair.getPublic());
        builder.addExtension(Extension.subjectAlternativeName, false,
            new GeneralNames(new GeneralName(GeneralName.dNSName, "localhost")));
        return new JcaX509CertificateConverter()
            .getCertificate(builder.build(new JcaContentSignerBuilder("SHA256withECDSA").build(keyPair.getPrivate())));
    }

    /** 테스트용 최소 SMTP 서버. STARTTLS 전에는 AUTH를 광고하지 않아 TLS 없이 인증할 수 없다. */
    private static final class StartTlsSmtpServer implements AutoCloseable {

        private final ServerSocket serverSocket;
        private final SSLSocketFactory tlsFactory;
        private final ExecutorService executor = Executors.newSingleThreadExecutor();
        private final BlockingQueue<String> messages = new LinkedBlockingQueue<>();
        private volatile boolean authenticatedOverTls;

        StartTlsSmtpServer(KeyPair keyPair, X509Certificate certificate) throws Exception {
            KeyStore keyStore = KeyStore.getInstance("PKCS12");
            keyStore.load(null, null);
            keyStore.setKeyEntry("smtp", keyPair.getPrivate(), KEY_PASSWORD, new Certificate[] {certificate});
            KeyManagerFactory keyManagers = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            keyManagers.init(keyStore, KEY_PASSWORD);
            SSLContext context = SSLContext.getInstance("TLS");
            context.init(keyManagers.getKeyManagers(), null, null);
            tlsFactory = context.getSocketFactory();
            serverSocket = new ServerSocket(0, 10, InetAddress.getLoopbackAddress());
            executor.submit(this::acceptLoop);
        }

        int port() {
            return serverSocket.getLocalPort();
        }

        BlockingQueue<String> receivedMessages() {
            return messages;
        }

        boolean authenticatedOverTls() {
            return authenticatedOverTls;
        }

        private void acceptLoop() {
            while (!serverSocket.isClosed()) {
                try (Socket socket = serverSocket.accept()) {
                    socket.setSoTimeout(5000);
                    converse(socket);
                } catch (IOException ignored) {
                    // 클라이언트가 인증서를 거부하면 handshake가 끊긴다. 다음 연결을 받는다.
                }
            }
        }

        private void converse(Socket plain) throws IOException {
            Socket socket = plain;
            boolean tls = false;
            BufferedReader in = reader(socket);
            OutputStream out = socket.getOutputStream();
            reply(out, "220 localhost ESMTP");
            String line;
            while ((line = in.readLine()) != null) {
                String command = line.toUpperCase();
                if (command.startsWith("EHLO")) {
                    reply(out, "250-localhost", tls ? "250-AUTH PLAIN" : "250-STARTTLS", "250 8BITMIME");
                } else if (command.equals("STARTTLS")) {
                    reply(out, "220 Ready to start TLS");
                    SSLSocket tlsSocket = (SSLSocket)tlsFactory.createSocket(socket, "localhost", socket.getPort(),
                        true);
                    tlsSocket.setUseClientMode(false);
                    tlsSocket.startHandshake();
                    socket = tlsSocket;
                    tls = true;
                    in = reader(socket);
                    out = socket.getOutputStream();
                } else if (command.startsWith("AUTH PLAIN")) {
                    if (command.equals("AUTH PLAIN")) {
                        reply(out, "334 ");
                        in.readLine();
                    }
                    authenticatedOverTls = tls;
                    reply(out, tls ? "235 2.7.0 Authentication successful" : "530 5.7.0 Must issue STARTTLS first");
                } else if (command.equals("DATA")) {
                    reply(out, "354 End data with <CR><LF>.<CR><LF>");
                    StringBuilder data = new StringBuilder();
                    for (String dataLine = in.readLine(); dataLine != null && !".".equals(dataLine);
                        dataLine = in.readLine()) {
                        data.append(dataLine).append('\n');
                    }
                    messages.add(data.toString());
                    reply(out, "250 OK");
                } else if (command.equals("QUIT")) {
                    reply(out, "221 Bye");
                    return;
                } else {
                    reply(out, "250 OK");
                }
            }
        }

        private static BufferedReader reader(Socket socket) throws IOException {
            return new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.US_ASCII));
        }

        private static void reply(OutputStream out, String... lines) throws IOException {
            for (String line : lines) {
                out.write((line + "\r\n").getBytes(StandardCharsets.US_ASCII));
            }
            out.flush();
        }

        @Override
        public void close() throws Exception {
            serverSocket.close();
            executor.shutdownNow();
            executor.awaitTermination(5, TimeUnit.SECONDS);
        }
    }
}
