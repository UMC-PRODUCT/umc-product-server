package com.umc.product.support;

import com.google.firebase.messaging.FirebaseMessaging;
import com.umc.product.support.isolation.DatabaseIsolation;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@DatabaseIsolation
public abstract class UseCaseTestSupport {

    @MockitoBean
    private FirebaseMessaging firebaseMessaging;

}
