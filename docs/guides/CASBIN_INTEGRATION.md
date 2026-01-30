# Casbin 통합 가이드

jCasbin을 활용하여 강력한 Policy 기반 권한 관리 시스템을 구축하는 방법을 설명합니다.

## 📚 Casbin이란?

Casbin은 다양한 접근 제어 모델(RBAC, ABAC, ACL 등)을 지원하는 오픈소스 권한 관리 라이브러리입니다.

- **GitHub**: https://github.com/casbin/jcasbin
- **Docs**: https://casbin.org/docs/overview

### 주요 장점

1. ✅ **유연한 정책 관리**: 코드 변경 없이 정책 파일만 수정
2. ✅ **다양한 모델 지원**: RBAC, ABAC, ACL, RESTful 등
3. ✅ **동적 권한 할당**: 런타임에 권한 추가/삭제 가능
4. ✅ **감사 로그**: 권한 체크 이력 추적 가능

## 🛠️ 1. 의존성 추가

`build.gradle`에 jCasbin 의존성 추가:

```gradle
dependencies {
    // Casbin
    implementation 'org.casbin:jcasbin:1.51.0'

    // 정책을 DB에 저장하려면 (선택사항)
    implementation 'org.casbin:jdbc-adapter:2.6.0'
}
```

## 📝 2. Casbin 모델 정의

### RBAC 모델 예시

`src/main/resources/casbin/model.conf` 파일 생성:

```conf
[request_definition]
r = sub, obj, act

[policy_definition]
p = sub, obj, act

[role_definition]
g = _, _

[policy_effect]
e = some(where (p.eft == allow))

[matchers]
m = g(r.sub, p.sub) && r.obj == p.obj && r.act == p.act
```

**설명:**
- `r = sub, obj, act`: 요청 형식 (주체, 객체, 행위)
- `p = sub, obj, act`: 정책 형식
- `g = _, _`: Role 계층 구조
- `m = ...`: 매칭 규칙

### RBAC with Resource Hierarchy (추천)

더 세밀한 리소스 제어를 위한 모델:

```conf
[request_definition]
r = sub, obj, act

[policy_definition]
p = sub, obj, act

[role_definition]
g = _, _
g2 = _, _

[policy_effect]
e = some(where (p.eft == allow))

[matchers]
m = g(r.sub, p.sub) && (keyMatch2(r.obj, p.obj) || keyMatch(r.obj, p.obj)) && r.act == p.act
```

**keyMatch**: 와일드카드 매칭 지원 (e.g., `/curriculum/*`)

## 📜 3. 정책 파일 정의

`src/main/resources/casbin/policy.csv` 파일 생성:

```csv
# 포맷: p, role, resource, action

# 중앙 운영진 - 모든 권한
p, CENTRAL_PRESIDENT, curriculum, read
p, CENTRAL_PRESIDENT, curriculum, write
p, CENTRAL_PRESIDENT, curriculum, delete
p, CENTRAL_PRESIDENT, curriculum, manage
p, CENTRAL_PRESIDENT, schedule, read
p, CENTRAL_PRESIDENT, schedule, write
p, CENTRAL_PRESIDENT, schedule, approve
p, CENTRAL_PRESIDENT, notice, read
p, CENTRAL_PRESIDENT, notice, write
p, CENTRAL_PRESIDENT, notice, delete

# 학교 회장 - 학교 범위 권한
p, SCHOOL_PRESIDENT, curriculum, read
p, SCHOOL_PRESIDENT, curriculum, write
p, SCHOOL_PRESIDENT, schedule, read
p, SCHOOL_PRESIDENT, schedule, write
p, SCHOOL_PRESIDENT, schedule, approve
p, SCHOOL_PRESIDENT, notice, read
p, SCHOOL_PRESIDENT, notice, write

# 파트장 - 제한적 권한
p, SCHOOL_PART_LEADER, curriculum, read
p, SCHOOL_PART_LEADER, curriculum, write
p, SCHOOL_PART_LEADER, schedule, read
p, SCHOOL_PART_LEADER, schedule, approve

# 일반 챌린저 - 읽기만 가능
p, CHALLENGER, curriculum, read
p, CHALLENGER, schedule, read
p, CHALLENGER, notice, read

# Role 계층 구조 (선택사항)
# g, 사용자ID, Role
# 런타임에 동적으로 할당되므로 여기서는 정의하지 않음
```

## 🔧 4. Casbin Adapter 구현

### CasbinPolicyEvaluator.java

`SimplePolicyEvaluator`를 Casbin 기반으로 교체:

```java
package com.umc.product.authorization.adapter.out.policy;

import com.umc.product.authorization.application.port.out.EvaluatePolicyPort;
import com.umc.product.authorization.domain.PermissionType;
import com.umc.product.authorization.domain.exception.AuthorizationDomainException;
import com.umc.product.authorization.domain.exception.AuthorizationErrorCode;
import com.umc.product.common.domain.enums.ChallengerRoleType;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.casbin.jcasbin.main.Enforcer;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Component
@Primary  // SimplePolicyEvaluator 대신 사용
@RequiredArgsConstructor
@Slf4j
public class CasbinPolicyEvaluator implements EvaluatePolicyPort {

    private final Enforcer enforcer;

    @Override
    public boolean evaluate(
            List<ChallengerRoleType> roles,
            String resourceType,
            String resourceId,
            PermissionType permission
    ) {
        try {
            // 리소스 경로 생성
            String resource = buildResourcePath(resourceType, resourceId);
            String action = permission.name().toLowerCase();

            // Role 중 하나라도 권한이 있으면 허용
            boolean hasPermission = roles.stream()
                    .anyMatch(role -> {
                        boolean result = enforcer.enforce(role.name(), resource, action);
                        log.debug("Casbin enforce - role: {}, resource: {}, action: {}, result: {}",
                                role, resource, action, result);
                        return result;
                    });

            return hasPermission;

        } catch (Exception e) {
            log.error("Casbin policy evaluation failed", e);
            throw new AuthorizationDomainException(AuthorizationErrorCode.POLICY_EVALUATION_FAILED);
        }
    }

    /**
     * 리소스 경로 생성
     *
     * @param resourceType 리소스 타입
     * @param resourceId 리소스 ID (null이면 타입 전체)
     * @return 리소스 경로 (e.g., "curriculum", "curriculum/123")
     */
    private String buildResourcePath(String resourceType, String resourceId) {
        if (resourceId == null || resourceId.isEmpty()) {
            return resourceType;
        }
        return resourceType + "/" + resourceId;
    }
}
```

### CasbinConfig.java

Casbin Enforcer를 Bean으로 등록:

```java
package com.umc.product.global.config;

import lombok.extern.slf4j.Slf4j;
import org.casbin.jcasbin.main.Enforcer;
import org.casbin.jcasbin.model.Model;
import org.casbin.jcasbin.persist.file_adapter.FileAdapter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

@Configuration
@Slf4j
public class CasbinConfig {

    @Bean
    public Enforcer enforcer() {
        try {
            String modelPath = new ClassPathResource("casbin/model.conf").getFile().getAbsolutePath();
            String policyPath = new ClassPathResource("casbin/policy.csv").getFile().getAbsolutePath();

            Enforcer enforcer = new Enforcer(modelPath, policyPath);

            log.info("Casbin Enforcer initialized successfully");
            log.info("Model path: {}", modelPath);
            log.info("Policy path: {}", policyPath);

            return enforcer;

        } catch (Exception e) {
            log.error("Failed to initialize Casbin Enforcer", e);
            throw new RuntimeException("Casbin initialization failed", e);
        }
    }
}
```

## 📊 5. DB 기반 정책 저장 (선택사항)

정책을 파일이 아닌 DB에 저장하려면:

### 5.1. 테이블 생성

```sql
-- Casbin 정책 테이블
CREATE TABLE casbin_rule (
    id BIGSERIAL PRIMARY KEY,
    ptype VARCHAR(100) NOT NULL,
    v0 VARCHAR(100),
    v1 VARCHAR(100),
    v2 VARCHAR(100),
    v3 VARCHAR(100),
    v4 VARCHAR(100),
    v5 VARCHAR(100),
    CONSTRAINT idx_casbin_rule UNIQUE(ptype, v0, v1, v2, v3, v4, v5)
);

CREATE INDEX idx_casbin_rule_ptype ON casbin_rule(ptype);
```

### 5.2. JDBC Adapter 설정

```java
package com.umc.product.global.config;

import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.casbin.adapter.JDBCAdapter;
import org.casbin.jcasbin.main.Enforcer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class CasbinConfig {

    private final DataSource dataSource;

    @Bean
    public Enforcer enforcer() {
        try {
            String modelPath = new ClassPathResource("casbin/model.conf")
                .getFile()
                .getAbsolutePath();

            // JDBC Adapter 사용
            JDBCAdapter adapter = new JDBCAdapter(dataSource);
            Enforcer enforcer = new Enforcer(modelPath, adapter);

            // 자동 저장 활성화 (정책 변경 시 즉시 DB 반영)
            enforcer.enableAutoSave(true);

            log.info("Casbin Enforcer with JDBC Adapter initialized");
            return enforcer;

        } catch (Exception e) {
            log.error("Failed to initialize Casbin Enforcer", e);
            throw new RuntimeException("Casbin initialization failed", e);
        }
    }
}
```

### 5.3. 초기 정책 로드

```java
@Component
@RequiredArgsConstructor
@Slf4j
public class CasbinPolicyInitializer implements ApplicationRunner {

    private final Enforcer enforcer;

    @Override
    public void run(ApplicationArguments args) {
        // 정책이 비어있으면 초기 정책 로드
        if (enforcer.getPolicy().isEmpty()) {
            log.info("Loading initial Casbin policies...");
            loadInitialPolicies();
        }
    }

    private void loadInitialPolicies() {
        // 중앙 운영진 정책
        enforcer.addPolicy("CENTRAL_PRESIDENT", "curriculum", "read");
        enforcer.addPolicy("CENTRAL_PRESIDENT", "curriculum", "write");
        enforcer.addPolicy("CENTRAL_PRESIDENT", "curriculum", "delete");
        enforcer.addPolicy("CENTRAL_PRESIDENT", "curriculum", "manage");

        // 학교 회장 정책
        enforcer.addPolicy("SCHOOL_PRESIDENT", "curriculum", "read");
        enforcer.addPolicy("SCHOOL_PRESIDENT", "curriculum", "write");
        enforcer.addPolicy("SCHOOL_PRESIDENT", "schedule", "approve");

        // 일반 챌린저 정책
        enforcer.addPolicy("CHALLENGER", "curriculum", "read");
        enforcer.addPolicy("CHALLENGER", "schedule", "read");

        log.info("Initial policies loaded successfully");
    }
}
```

## 🔄 6. 런타임 정책 관리

### 정책 추가/삭제 UseCase

```java
package com.umc.product.authorization.application.port.in.command;

import com.umc.product.common.domain.enums.ChallengerRoleType;

public interface ManagePolicyUseCase {

    /**
     * 정책 추가
     */
    void addPolicy(ChallengerRoleType role, String resource, String action);

    /**
     * 정책 삭제
     */
    void removePolicy(ChallengerRoleType role, String resource, String action);

    /**
     * 모든 정책 조회
     */
    List<PolicyInfo> getAllPolicies();
}
```

### Service 구현

```java
package com.umc.product.authorization.application.service;

import com.umc.product.authorization.application.port.in.command.ManagePolicyUseCase;
import com.umc.product.common.domain.enums.ChallengerRoleType;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.casbin.jcasbin.main.Enforcer;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PolicyManagementService implements ManagePolicyUseCase {

    private final Enforcer enforcer;

    @Override
    public void addPolicy(ChallengerRoleType role, String resource, String action) {
        boolean added = enforcer.addPolicy(role.name(), resource, action);

        if (added) {
            log.info("Policy added - role: {}, resource: {}, action: {}", role, resource, action);
        } else {
            log.warn("Policy already exists - role: {}, resource: {}, action: {}", role, resource, action);
        }
    }

    @Override
    public void removePolicy(ChallengerRoleType role, String resource, String action) {
        boolean removed = enforcer.removePolicy(role.name(), resource, action);

        if (removed) {
            log.info("Policy removed - role: {}, resource: {}, action: {}", role, resource, action);
        } else {
            log.warn("Policy not found - role: {}, resource: {}, action: {}", role, resource, action);
        }
    }

    @Override
    public List<PolicyInfo> getAllPolicies() {
        return enforcer.getPolicy().stream()
                .map(policy -> new PolicyInfo(
                        policy.get(0),  // role
                        policy.get(1),  // resource
                        policy.get(2)   // action
                ))
                .toList();
    }

    public record PolicyInfo(String role, String resource, String action) {}
}
```

## 🧪 7. 테스트

### Casbin 정책 테스트

```java
@SpringBootTest
class CasbinPolicyEvaluatorTest {

    @Autowired
    Enforcer enforcer;

    @Autowired
    EvaluatePolicyPort evaluatePolicyPort;

    @Test
    void 중앙운영진은_모든_권한을_가진다() {
        // given
        List<ChallengerRoleType> roles = List.of(ChallengerRoleType.CENTRAL_PRESIDENT);

        // when
        boolean canRead = evaluatePolicyPort.evaluate(roles, "curriculum", null, PermissionType.READ);
        boolean canWrite = evaluatePolicyPort.evaluate(roles, "curriculum", null, PermissionType.WRITE);
        boolean canDelete = evaluatePolicyPort.evaluate(roles, "curriculum", null, PermissionType.DELETE);

        // then
        assertThat(canRead).isTrue();
        assertThat(canWrite).isTrue();
        assertThat(canDelete).isTrue();
    }

    @Test
    void 일반_챌린저는_읽기만_가능하다() {
        // given
        List<ChallengerRoleType> roles = List.of(ChallengerRoleType.CHALLENGER);

        // when
        boolean canRead = evaluatePolicyPort.evaluate(roles, "curriculum", null, PermissionType.READ);
        boolean canWrite = evaluatePolicyPort.evaluate(roles, "curriculum", null, PermissionType.WRITE);

        // then
        assertThat(canRead).isTrue();
        assertThat(canWrite).isFalse();
    }

    @Test
    void 여러_Role을_가진_사용자는_OR_조건으로_평가된다() {
        // given
        List<ChallengerRoleType> roles = List.of(
            ChallengerRoleType.CHALLENGER,
            ChallengerRoleType.SCHOOL_PRESIDENT
        );

        // when
        boolean canApprove = evaluatePolicyPort.evaluate(roles, "schedule", null, PermissionType.APPROVE);

        // then
        assertThat(canApprove).isTrue();  // SCHOOL_PRESIDENT 권한으로 승인 가능
    }
}
```

## 🎯 8. 고급 활용

### 8.1. 리소스 계층 구조

특정 기수나 학교에만 권한 부여:

```csv
# 정책 예시
p, SCHOOL_PRESIDENT_GISU_9, curriculum/gisu/9/*, read
p, SCHOOL_PRESIDENT_GISU_9, curriculum/gisu/9/*, write
```

### 8.2. ABAC (Attribute-Based Access Control)

사용자 속성 기반 권한:

```conf
[matchers]
m = g(r.sub, p.sub) && r.obj == p.obj && r.act == p.act && \
    (r.sub.gisu == r.obj.gisu || p.sub == "CENTRAL_PRESIDENT")
```

### 8.3. 시간 기반 권한

특정 시간대에만 권한 부여:

```java
enforcer.addPolicy("CHALLENGER", "curriculum", "read", "09:00-18:00");
```

## 📖 참고 자료

- **jCasbin GitHub**: https://github.com/casbin/jcasbin
- **Casbin 공식 문서**: https://casbin.org/docs/overview
- **RBAC 모델 예시**: https://casbin.org/docs/rbac
- **JDBC Adapter**: https://github.com/casbin/jdbc-adapter

## 🔄 마이그레이션 체크리스트

SimplePolicyEvaluator → Casbin 전환 시:

- [ ] Casbin 의존성 추가
- [ ] `model.conf` 파일 작성
- [ ] `policy.csv` 파일 작성 (또는 DB 테이블 생성)
- [ ] `CasbinConfig` 작성
- [ ] `CasbinPolicyEvaluator` 작성 및 `@Primary` 설정
- [ ] 기존 정책을 Casbin 정책으로 마이그레이션
- [ ] 통합 테스트 실행
- [ ] `SimplePolicyEvaluator` 삭제 (또는 백업용으로 보관)
