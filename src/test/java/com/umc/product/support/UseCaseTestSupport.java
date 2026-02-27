package com.umc.product.support;

import com.google.cloud.storage.Storage;
import com.google.firebase.messaging.FirebaseMessaging;
import com.umc.product.challenger.application.port.in.query.GetChallengerUseCase;
import com.umc.product.global.security.JwtTokenProvider;
import com.umc.product.storage.application.port.out.StoragePort;
import com.umc.product.support.isolation.DatabaseIsolation;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@DatabaseIsolation
@Import(TestContainersConfig.class)
@Testcontainers
public abstract class UseCaseTestSupport {
    @MockitoBean
    protected JavaMailSender mailSender;

    @MockitoBean
    protected JwtTokenProvider jwtTokenProvider;

    // TODO: 제거 예정
    @MockitoBean
    protected GetChallengerUseCase getChallengerUseCase;

    @MockitoBean
    protected FirebaseMessaging firebaseMessaging;

    @MockitoBean
    protected Storage googleCloudStorage;

    @MockitoBean
    protected StoragePort storagePort;
}
