package com.umc.product.support;

import com.umc.product.support.isolation.DatabaseIsolation;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@DatabaseIsolation
public abstract class UseCaseTestSupport {

}
