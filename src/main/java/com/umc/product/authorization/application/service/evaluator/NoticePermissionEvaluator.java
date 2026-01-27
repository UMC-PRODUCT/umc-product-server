package com.umc.product.authorization.application.service.evaluator;

import com.umc.product.authorization.domain.ResourcePermission;
import com.umc.product.authorization.domain.ResourceType;
import com.umc.product.authorization.domain.SubjectAttributes;
import com.umc.product.global.exception.NotImplementedException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Notice(공지사항) 리소스에 대한 권한 평가
 */
@Component
@Slf4j
public class NoticePermissionEvaluator implements ResourcePermissionEvaluator {

    @Override
    public ResourceType supportedResourceType() {
        return ResourceType.NOTICE;
    }

    @Override
    public boolean evaluate(SubjectAttributes subjectAttributes,
                            ResourcePermission resourcePermission) {
        // TODO: 공지사항 평가 로직 구현 .. 다시 ..

        throw new NotImplementedException(this.getClass().getName() + " Permission Evaluator를 구현해주세요.");
    }
}
