package com.umc.product.notification.adapter.in.aop;

import com.umc.product.notification.application.port.in.SendWebhookAlarmUseCase;
import com.umc.product.notification.application.port.in.annotation.WebhookAlarm;
import com.umc.product.notification.application.port.in.dto.SendWebhookAlarmCommand;
import com.umc.product.notification.domain.WebhookPlatform;
import java.lang.reflect.Method;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class WebhookAlarmAspect {

    private final SendWebhookAlarmUseCase sendWebhookAlarmUseCase;
    private final ExpressionParser parser = new SpelExpressionParser();
    private final ParameterNameDiscoverer parameterNameDiscoverer = new DefaultParameterNameDiscoverer();

    @AfterReturning(pointcut = "@annotation(webhookAlarm)", returning = "result")
    public void sendAlarm(JoinPoint joinPoint, WebhookAlarm webhookAlarm, Object result) {
        try {
            EvaluationContext context = createEvaluationContext(joinPoint, result);

            String title = parser.parseExpression(webhookAlarm.title()).getValue(context, String.class);
            String content = parser.parseExpression(webhookAlarm.content()).getValue(context, String.class);
            List<WebhookPlatform> platforms = List.of(webhookAlarm.platforms());

            SendWebhookAlarmCommand command = new SendWebhookAlarmCommand(platforms, title, content);

            if (webhookAlarm.buffered()) {
                sendWebhookAlarmUseCase.sendBuffered(command);
            } else {
                sendWebhookAlarmUseCase.send(command);
            }
        } catch (Exception e) {
            log.error("웹훅 알람 처리 중 오류: method={}, error={}",
                joinPoint.getSignature().toShortString(), e.getMessage(), e);
        }
    }

    // JoinPoint에서 메서드 정보와 파라미터를 추출하여 EvaluationContext를 생성
    private EvaluationContext createEvaluationContext(JoinPoint joinPoint, Object result) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Object[] args = joinPoint.getArgs();
        String[] paramNames = parameterNameDiscoverer.getParameterNames(method);

        StandardEvaluationContext context = new StandardEvaluationContext();
        if (paramNames != null) {
            for (int i = 0; i < paramNames.length; i++) {
                context.setVariable(paramNames[i], args[i]);
            }
        }
        context.setVariable("result", result);
        return context;
    }
}
