package com.umc.product.support;

import com.google.firebase.messaging.FirebaseMessaging;
import com.umc.product.global.security.JwtTokenProvider;
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
    private JavaMailSender mailSender;

    @MockitoBean
    protected JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private FirebaseMessaging firebaseMessaging;

}
