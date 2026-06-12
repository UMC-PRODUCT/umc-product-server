package com.umc.product.global.exception.documentation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.SOURCE)
public @interface ErrorCodeSpec {

    String description() default "";

    String clientAction() default "";

    ErrorCodeRetryable retryable() default ErrorCodeRetryable.UNSPECIFIED;

    ErrorCodeSeverity severity() default ErrorCodeSeverity.UNSPECIFIED;

    boolean deprecated() default false;

    String replacementCode() default "";

    String[] owners() default {};

    String[] tags() default {};
}
