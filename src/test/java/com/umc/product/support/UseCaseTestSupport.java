package com.umc.product.support;

import com.google.firebase.messaging.FirebaseMessaging;
import com.umc.product.challenger.application.port.in.query.GetChallengerUseCase;
import com.umc.product.global.security.JwtTokenProvider;
import com.umc.product.support.isolation.DatabaseIsolation;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest(properties = "app.base-url=http://localhost:8080")
@DatabaseIsolation
@Import(TestContainersConfig.class)
@Testcontainers
public abstract class UseCaseTestSupport {


    @MockitoBean
    private JavaMailSender mailSender;

    @MockitoBean
    protected JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    protected GetChallengerUseCase getChallengerUseCase;

    @MockitoBean
    private FirebaseMessaging firebaseMessaging;

}
