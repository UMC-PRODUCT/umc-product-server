package com.umc.product.blog.application.service;

import static com.umc.product.support.AuditPolicyAssert.assertAllAudited;
import static com.umc.product.support.AuditPolicyAssert.spec;
import static com.umc.product.support.AuditPolicyAssert.type;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.umc.product.support.AuditPolicyAssert.AuditSpec;

/**
 * blog 가 모듈로 분리되기 전에는 audit 도메인의 AuditCoveragePolicyTest 가 이 spec 까지 들고 있었다.
 * 문자열 기반 리플렉션이라 컴파일은 통과했지만, 모듈이 나뉜 뒤 실행에서 ClassNotFoundException 이 났다.
 * 각 모듈이 자기 도메인 spec 을 책임진다.
 */
@DisplayName("blog 감사 로그 정책")
class BlogAuditCoveragePolicyTest {

    @Test
    @DisplayName("blog CommandService 상태 변경 메서드는 감사 로그 대상으로 선언한다")
    void command_services_are_audited() {
        assertAllAudited(auditedSpecs());
    }

    private List<AuditSpec> auditedSpecs() {
        return List.of(
            spec("blog.application.service.BlogContentCommandService", "create", "BLOG", "CREATE", "BlogContent",
                type("blog.application.port.in.command.dto.CreateBlogContentCommand")),
            spec("blog.application.service.BlogContentCommandService", "update", "BLOG", "UPDATE", "BlogContent",
                type("blog.application.port.in.command.dto.UpdateBlogContentCommand")),
            spec("blog.application.service.BlogContentCommandService", "delete", "BLOG", "DELETE", "BlogContent",
                type("blog.application.port.in.command.dto.DeleteBlogContentCommand")),
            spec("blog.application.service.BlogSeriesCommandService", "replaceContents", "BLOG", "REORDER",
                "BlogSeries", type("blog.application.port.in.command.dto.ReplaceBlogSeriesContentsCommand"))
        );
    }
}
