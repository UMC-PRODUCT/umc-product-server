package com.umc.product.global.security.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.TYPE})  // 메서드, 클래스에 사용 가능
@Retention(RetentionPolicy.RUNTIME)
public @interface Public {

}