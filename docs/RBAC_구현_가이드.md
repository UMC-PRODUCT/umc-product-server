# RBAC + ABAC 구현 가이드

## 개요

UMC Product Server에서 **RBAC (Role-Based Access Control) + ABAC (Attribute-Based Access Control) 하이브리드** 권한 제어 시스템을 구현하는 방법을 설명합니다.

### 왜 RBAC + ABAC 하이브리드인가?

단순한 RBAC만으로는 복잡한 비즈니스 요구사항을 처리하기 어렵고, 순수 ABAC는 관리가 복잡합니다. 따라서 두 방식의 장점을 결합합니다:

- **RBAC**: "누가" (Who) - 빠른 역할 기반 필터링
- **ABAC**: "무엇을", "언제", "어디서" (What, When, Where) - 컨텍스트 기반 세밀한 제어

---

## 본문

### 전체 아키텍처

```
┌──────────────────────────────────────────────────┐
│  Layer 1: RBAC (Spring Security)                 │
│  - MemberRole: ADMIN, MEMBER                     │
│  - ChallengerRole: SCHOOL_PRESIDENT, etc.        │
│  - @PreAuthorize("hasRole('...')")               │
│  - @PreAuthorize("hasAuthority('...')")          │
│  → 빠른 필터링: "운영진인가?"                     │
└──────────────────────────────────────────────────┘
                        ↓
┌──────────────────────────────────────────────────┐
│  Layer 2: ABAC (PermissionEvaluator)             │
│  - hasPermission(#id, 'TYPE', 'ACTION')          │
│  - Subject: memberId, organizationIds, roles     │
│  - Resource: ownerId, organizationId, status     │
│  - Environment: time, location, deadline         │
│  → 세밀한 제어: "어느 조직의 운영진인가?"         │
└──────────────────────────────────────────────────┘
                        ↓
┌──────────────────────────────────────────────────┐
│  Layer 3: Business Logic (Service)               │
│  - 복잡한 도메인 규칙                            │
│  - 다중 조건 검증                                │
│  - 트랜잭션 보장                                 │
│  → 비즈니스 로직: "계층 구조 검증"               │
└──────────────────────────────────────────────────┘
```

---

## Layer 1: RBAC (Role-Based Access Control)

### 1.1 MemberRole (시스템 레벨)

#### 역할 정의

UMC 프로젝트에서는 **MemberRole을 최소화**하여 시스템 레벨 권한만 관리합니다.

```java
// member/domain/MemberRole.java
public enum MemberRole {
    ADMIN("시스템 관리자"),    // 운영사무국 직원, 슈퍼 관리자
    MEMBER("일반 회원");       // 기본값

    private final String description;

    MemberRole(String description) {
        this.description = description;
    }

    public String getAuthority() {
        return "ROLE_" + this.name();
    }
}
```

**용도:**
- ✅ 시스템 전역 권한 (모든 데이터 접근, 시스템 설정)
- ✅ 챌린저가 아닌 사람 구분
- ✅ 기본 인증 여부

**예시:**
```java
// ADMIN만 접근
@PreAuthorize("hasRole('ADMIN')")
public ApiResponse<?> deleteAllData() { }

// 인증된 사용자 (MEMBER 이상)
@PreAuthorize("hasRole('MEMBER')")
public ApiResponse<?> getMyProfile() { }
```

#### Member Entity 수정

```java
// member/domain/Member.java
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "member")
public class Member extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    // MemberRole 추가
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MemberRole role;

    @Builder
    private Member(String name, String email, MemberRole role) {
        this.name = name;
        this.email = email;
        this.role = role != null ? role : MemberRole.MEMBER;  // 기본값
    }
}
```

#### Flyway Migration

```sql
-- db/migration/V{version}__add_member_role.sql
ALTER TABLE member
ADD COLUMN role VARCHAR(20) NOT NULL DEFAULT 'MEMBER';

-- 기존 데이터에 기본값 설정
UPDATE member SET role = 'MEMBER' WHERE role IS NULL;
```

---

### 1.2 ChallengerRole (비즈니스 레벨)

#### RoleType 계층 구조

실제 UMC 조직 내 역할은 `RoleType`으로 관리합니다.

```java
// challenger/domain/enums/RoleType.java
public enum RoleType {
    // 중앙 (레벨 90~100)
    CENTRAL_PRESIDENT(100, "총괄"),
    CENTRAL_VICE_PRESIDENT(95, "부총괄"),
    CENTRAL_DIRECTOR(90, "국장"),
    CENTRAL_MANAGER(85, "국원"),
    CENTRAL_PART_LEADER(80, "중앙 파트장"),

    // 지부 (레벨 70~79)
    CHAPTER_LEADER(75, "지부장"),
    CHAPTER_STAFF(70, "지부 운영진"),

    // 학교 (레벨 60~69)
    SCHOOL_PRESIDENT(65, "회장"),
    SCHOOL_VICE_PRESIDENT(63, "부회장"),
    SCHOOL_PART_LEADER(61, "파트장"),
    SCHOOL_STAFF(60, "기타 운영진"),

    // 일반 (레벨 10)
    CHALLENGER(10, "챌린저");

    private final int level;
    private final String description;

    RoleType(int level, String description) {
        this.level = level;
        this.description = description;
    }

    public int getLevel() {
        return level;
    }

    public String getDescription() {
        return description;
    }

    public boolean isHigherThan(RoleType other) {
        return this.level > other.level;
    }

    public boolean isStaffRole() {
        return this.level >= SCHOOL_STAFF.level;
    }
}
```

#### ChallengerRole Entity 개선

```java
// challenger/domain/ChallengerRole.java
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "challenger_role")
public class ChallengerRole {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "challenger_id", nullable = false)
    private Challenger challenger;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RoleType roleType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrganizationType organizationType;

    @Column(nullable = false)
    private Long organizationId;

    @Enumerated(EnumType.STRING)
    @Column(name = "leading_part")
    private ChallengerPart leadingPart;  // 파트장인 경우

    @Column(nullable = false)
    private Long gisuId;

    // 도메인 로직: 다른 역할을 관리할 수 있는지
    public boolean canManage(ChallengerRole targetRole) {
        // 같은 조직이 아니면 불가
        if (!this.organizationId.equals(targetRole.getOrganizationId())) {
            return false;
        }

        // 계층 구조에 따른 판단
        return this.roleType.isHigherThan(targetRole.getRoleType());
    }

    public boolean isStaffRole() {
        return roleType.isStaffRole();
    }
}
```

---

### 1.3 MemberPrincipal 확장

Spring Security의 Principal에 **현재 기수의 Challenger 정보와 역할들**을 포함합니다.

```java
// global/security/MemberPrincipal.java
@Getter
public class MemberPrincipal implements OAuth2User {

    // 기본 정보
    private final Long memberId;
    private final String email;
    private final MemberRole memberRole;

    // 현재 활성 기수의 Challenger 정보
    private final Long currentChallengerId;
    private final Long currentGisuId;
    private final List<RoleType> challengerRoles;  // 현재 기수의 역할들

    // OAuth2
    private final Map<String, Object> attributes;
    private final String nameAttributeKey;

    // 전체 생성자
    public MemberPrincipal(
            Long memberId,
            String email,
            MemberRole memberRole,
            Long currentChallengerId,
            Long currentGisuId,
            List<RoleType> challengerRoles,
            Map<String, Object> attributes,
            String nameAttributeKey) {
        this.memberId = memberId;
        this.email = email;
        this.memberRole = memberRole;
        this.currentChallengerId = currentChallengerId;
        this.currentGisuId = currentGisuId;
        this.challengerRoles = challengerRoles != null ? challengerRoles : Collections.emptyList();
        this.attributes = attributes;
        this.nameAttributeKey = nameAttributeKey;
    }

    // JWT용 간단 생성자
    public MemberPrincipal(Long memberId, String email, MemberRole memberRole) {
        this(memberId, email, memberRole, null, null, Collections.emptyList(),
             Collections.emptyMap(), "id");
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<GrantedAuthority> authorities = new ArrayList<>();

        // 1. MemberRole 추가
        authorities.add(new SimpleGrantedAuthority(memberRole.getAuthority()));

        // 2. ChallengerRole들 추가 (현재 기수만)
        for (RoleType roleType : challengerRoles) {
            authorities.add(new SimpleGrantedAuthority(roleType.name()));
        }

        return authorities;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public String getName() {
        return String.valueOf(memberId);
    }

    // 편의 메서드
    public boolean isChallenger() {
        return currentChallengerId != null;
    }

    public boolean hasRole(RoleType roleType) {
        return challengerRoles.contains(roleType);
    }

    public boolean isStaff() {
        return challengerRoles.stream()
            .anyMatch(RoleType::isStaffRole);
    }
}
```

**Authorities 예시:**
```java
MemberPrincipal {
    memberId: 123,
    memberRole: MEMBER,
    currentChallengerId: 456,
    challengerRoles: [SCHOOL_PRESIDENT, SCHOOL_PART_LEADER],

    // getAuthorities() 결과
    authorities: [
        "ROLE_MEMBER",              // hasRole('MEMBER')로 체크
        "SCHOOL_PRESIDENT",         // hasAuthority('SCHOOL_PRESIDENT')로 체크
        "SCHOOL_PART_LEADER"        // hasAuthority('SCHOOL_PART_LEADER')로 체크
    ]
}
```

---

### 1.4 JwtTokenProvider 수정

JWT 인증 시 **현재 활성 기수의 Challenger와 역할을 로드**합니다.

```java
// global/security/JwtTokenProvider.java
@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    private final LoadMemberPort loadMemberPort;
    private final LoadChallengerPort loadChallengerPort;
    private final LoadChallengerRolePort loadChallengerRolePort;
    private final GetCurrentGisuUseCase getCurrentGisuUseCase;

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private long validityInMilliseconds;

    public String createToken(Long memberId) {
        Claims claims = Jwts.claims();
        claims.put("memberId", memberId);

        Date now = new Date();
        Date validity = new Date(now.getTime() + validityInMilliseconds);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();
    }

    public Authentication getAuthentication(String token) {
        Claims claims = parseClaims(token);
        Long memberId = claims.get("memberId", Long.class);

        // 1. Member 조회
        Member member = loadMemberPort.loadById(memberId)
            .orElseThrow(() -> new AuthenticationException("Member not found"));

        // 2. 현재 활성 기수 조회
        Long currentGisuId = getCurrentGisuUseCase.getCurrentGisuId();

        // 3. 현재 기수의 Challenger 조회
        Optional<Challenger> challengerOpt = loadChallengerPort
            .findByMemberIdAndGisuId(memberId, currentGisuId);

        // 4. Challenger가 있으면 역할 로드
        Long currentChallengerId = null;
        List<RoleType> challengerRoles = new ArrayList<>();

        if (challengerOpt.isPresent()) {
            Challenger challenger = challengerOpt.get();
            currentChallengerId = challenger.getId();

            // 현재 기수의 ChallengerRole 조회
            List<ChallengerRole> roles = loadChallengerRolePort
                .findByChallengerId(challenger.getId());

            challengerRoles = roles.stream()
                .map(ChallengerRole::getRoleType)
                .collect(Collectors.toList());
        }

        // 5. MemberPrincipal 생성
        MemberPrincipal principal = new MemberPrincipal(
            member.getId(),
            member.getEmail(),
            member.getRole(),
            currentChallengerId,
            currentGisuId,
            challengerRoles,
            Collections.emptyMap(),
            "id"
        );

        return new UsernamePasswordAuthenticationToken(
            principal,
            token,
            principal.getAuthorities()
        );
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .setSigningKey(secretKey)
                .parseClaimsJws(token)
                .getBody();
    }
}
```

---

### 1.5 RoleHierarchy 설정

```java
// global/config/MethodSecurityConfig.java
@Configuration
@EnableMethodSecurity
public class MethodSecurityConfig {

    @Bean
    public RoleHierarchy roleHierarchy() {
        RoleHierarchyImpl hierarchy = new RoleHierarchyImpl();

        // ADMIN > MEMBER
        String hierarchyString = """
            ROLE_ADMIN > ROLE_MEMBER
            """;

        hierarchy.setHierarchy(hierarchyString);
        return hierarchy;
    }

    @Bean
    public MethodSecurityExpressionHandler methodSecurityExpressionHandler(
            ApplicationContext applicationContext,
            UmcPermissionEvaluator umcPermissionEvaluator) {

        DefaultMethodSecurityExpressionHandler handler =
            new DefaultMethodSecurityExpressionHandler();

        handler.setPermissionEvaluator(umcPermissionEvaluator);
        handler.setRoleHierarchy(roleHierarchy());
        handler.setApplicationContext(applicationContext);

        return handler;
    }
}
```

---

### 1.6 @Public 어노테이션 (Meta-annotation)

공개 API를 간결하게 표시하기 위한 커스텀 어노테이션입니다.

```java
// global/security/annotation/Public.java
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@PreAuthorize("permitAll()")  // Meta-annotation
public @interface Public {
}
```

**사용 예시:**

```java
@RestController
@RequestMapping("/api/v1/posts")
public class PostController {

    @GetMapping
    @Public  // 인증 없이 접근 가능
    public ApiResponse<?> getPublicPosts() {
        return ApiResponse.success(postService.getPublicPosts());
    }

    @PostMapping
    @PreAuthorize("hasRole('MEMBER')")  // 인증 필요
    public ApiResponse<?> createPost(@RequestBody CreatePostRequest request) {
        return ApiResponse.success(postService.create(request));
    }
}
```

---

## Layer 2: ABAC (Attribute-Based Access Control)

### 2.1 ABAC 속성 분류

```java
// 1. Subject Attributes (주체 속성)
principal.memberId
principal.memberRole (ADMIN, MEMBER)
principal.currentChallengerId
principal.challengerRoles (SCHOOL_PRESIDENT, etc.)
principal.currentGisuId

// 2. Resource Attributes (리소스 속성)
post.authorId
post.organizationId
notice.scope (GLOBAL, ORGANIZATION, PERSONAL)
activity.deadline
schedule.attendanceWindow

// 3. Environment Attributes (환경 속성)
LocalDateTime.now()
request.location
request.ipAddress

// 4. Action Attributes (행위 속성)
READ, CREATE, UPDATE, DELETE
APPROVE, REJECT
SUBMIT, REVIEW
```

---

### 2.2 PermissionEvaluator 구현

Spring Security의 `hasPermission()` 표현식을 처리하는 핵심 컴포넌트입니다.

```java
// global/security/UmcPermissionEvaluator.java
@Component
@RequiredArgsConstructor
public class UmcPermissionEvaluator implements PermissionEvaluator {

    private final LoadChallengerRolePort loadChallengerRolePort;
    private final LoadChallengerPort loadChallengerPort;
    private final LoadPostPort loadPostPort;
    private final LoadActivityPort loadActivityPort;
    private final LoadNoticePort loadNoticePort;

    @Override
    public boolean hasPermission(
            Authentication authentication,
            Serializable targetId,
            String targetType,
            Object permission) {

        if (authentication == null ||
            !(authentication.getPrincipal() instanceof MemberPrincipal)) {
            return false;
        }

        MemberPrincipal principal = (MemberPrincipal) authentication.getPrincipal();
        String permissionStr = permission.toString();

        return switch (targetType.toUpperCase()) {
            case "POST" -> evaluatePostPermission(principal, (Long) targetId, permissionStr);
            case "NOTICE" -> evaluateNoticePermission(principal, (Long) targetId, permissionStr);
            case "ACTIVITY" -> evaluateActivityPermission(principal, (Long) targetId, permissionStr);
            default -> false;
        };
    }

    @Override
    public boolean hasPermission(
            Authentication authentication,
            Object targetDomainObject,
            Object permission) {
        // Domain Object 직접 전달 시
        return false;  // 필요시 구현
    }

    // ============================================================
    // Post 권한 검증
    // ============================================================

    private boolean evaluatePostPermission(
            MemberPrincipal principal,
            Long postId,
            String permission) {

        try {
            Post post = loadPostPort.loadById(postId)
                .orElseThrow(() -> new PostNotFoundException(postId));

            return switch (permission.toUpperCase()) {
                case "UPDATE" -> canUpdatePost(principal, post);
                case "DELETE" -> canDeletePost(principal, post);
                default -> false;
            };
        } catch (Exception e) {
            return false;
        }
    }

    private boolean canUpdatePost(MemberPrincipal principal, Post post) {
        // ABAC 규칙 1: ADMIN은 모든 게시글 수정 가능
        if (principal.getMemberRole() == MemberRole.ADMIN) {
            return true;
        }

        // ABAC 규칙 2: 작성자만 수정 가능
        return post.getAuthorId().equals(principal.getMemberId());
    }

    private boolean canDeletePost(MemberPrincipal principal, Post post) {
        // ABAC 규칙 1: ADMIN은 모든 게시글 삭제 가능
        if (principal.getMemberRole() == MemberRole.ADMIN) {
            return true;
        }

        // ABAC 규칙 2: 작성자는 삭제 가능
        if (post.getAuthorId().equals(principal.getMemberId())) {
            return true;
        }

        // ABAC 규칙 3: 조직 운영진은 해당 조직 게시글 삭제 가능
        if (post.getOrganizationId() != null) {
            return isOrganizationStaff(principal, post.getOrganizationId());
        }

        return false;
    }

    // ============================================================
    // Notice 권한 검증 (Scope 기반)
    // ============================================================

    private boolean evaluateNoticePermission(
            MemberPrincipal principal,
            Long noticeId,
            String permission) {

        try {
            Notice notice = loadNoticePort.loadById(noticeId)
                .orElseThrow(() -> new NoticeNotFoundException(noticeId));

            return switch (permission.toUpperCase()) {
                case "UPDATE", "DELETE" -> canUpdateNotice(principal, notice);
                default -> false;
            };
        } catch (Exception e) {
            return false;
        }
    }

    private boolean canUpdateNotice(MemberPrincipal principal, Notice notice) {
        // ABAC 규칙 1: ADMIN은 모든 공지 수정 가능
        if (principal.getMemberRole() == MemberRole.ADMIN) {
            return true;
        }

        // ABAC 규칙 2: Scope에 따라 다름
        return switch (notice.getScope()) {
            case GLOBAL -> false;  // 전체 공지는 ADMIN만

            case ORGANIZATION -> {
                // 해당 조직의 운영진만
                if (notice.getOrganizationId() == null) yield false;
                yield isOrganizationStaff(principal, notice.getOrganizationId());
            }

            case PERSONAL -> {
                // 작성자만
                yield notice.getAuthorId().equals(principal.getMemberId());
            }
        };
    }

    // ============================================================
    // Activity 권한 검증 (시간 기반)
    // ============================================================

    private boolean evaluateActivityPermission(
            MemberPrincipal principal,
            Long activityId,
            String permission) {

        try {
            Activity activity = loadActivityPort.loadById(activityId)
                .orElseThrow(() -> new ActivityNotFoundException(activityId));

            return switch (permission.toUpperCase()) {
                case "SUBMIT" -> canSubmitActivity(principal, activity);
                case "REVIEW" -> canReviewActivity(principal, activity);
                default -> false;
            };
        } catch (Exception e) {
            return false;
        }
    }

    private boolean canSubmitActivity(MemberPrincipal principal, Activity activity) {
        // ABAC 규칙 1: 챌린저만
        if (!principal.isChallenger()) {
            return false;
        }

        // ABAC 규칙 2: 제출 기한 내 (Environment Attribute)
        LocalDateTime now = LocalDateTime.now();
        if (activity.getDeadline() != null && now.isAfter(activity.getDeadline())) {
            return false;
        }

        // ABAC 규칙 3: 본인의 활동만
        return activity.getChallengerId().equals(principal.getCurrentChallengerId());
    }

    private boolean canReviewActivity(MemberPrincipal principal, Activity activity) {
        // ABAC 규칙 1: ADMIN은 모든 활동 검토 가능
        if (principal.getMemberRole() == MemberRole.ADMIN) {
            return true;
        }

        // ABAC 규칙 2: 운영진만
        if (!principal.isStaff()) {
            return false;
        }

        // ABAC 규칙 3: 같은 조직의 챌린저 활동만
        try {
            Challenger targetChallenger = loadChallengerPort.load(activity.getChallengerId());
            return isSameOrganization(principal, targetChallenger);
        } catch (Exception e) {
            return false;
        }
    }

    // ============================================================
    // Helper Methods
    // ============================================================

    private boolean isSameOrganization(MemberPrincipal principal, Long organizationId) {
        if (!principal.isChallenger()) {
            return false;
        }

        List<ChallengerRole> roles = loadChallengerRolePort
            .findByChallengerId(principal.getCurrentChallengerId());

        return roles.stream()
            .anyMatch(r -> r.getOrganizationId().equals(organizationId));
    }

    private boolean isSameOrganization(MemberPrincipal principal, Challenger targetChallenger) {
        if (!principal.isChallenger()) {
            return false;
        }

        List<Long> principalOrgIds = loadChallengerRolePort
            .findByChallengerId(principal.getCurrentChallengerId())
            .stream()
            .map(ChallengerRole::getOrganizationId)
            .toList();

        List<Long> targetOrgIds = loadChallengerRolePort
            .findByChallengerId(targetChallenger.getId())
            .stream()
            .map(ChallengerRole::getOrganizationId)
            .toList();

        return principalOrgIds.stream()
            .anyMatch(targetOrgIds::contains);
    }

    private boolean isOrganizationStaff(MemberPrincipal principal, Long organizationId) {
        if (!principal.isChallenger()) {
            return false;
        }

        List<ChallengerRole> roles = loadChallengerRolePort
            .findByChallengerIdAndOrganizationId(
                principal.getCurrentChallengerId(),
                organizationId
            );

        return roles.stream()
            .anyMatch(ChallengerRole::isStaffRole);
    }
}
```

---

## Layer 3: Service 비즈니스 로직

### 3.1 복잡한 권한 검증 예시

```java
// organization/application/service/OrganizationMemberService.java
@Service
@RequiredArgsConstructor
@Transactional
public class OrganizationMemberService implements RemoveMemberUseCase {

    private final LoadChallengerRolePort loadChallengerRolePort;
    private final LoadChallengerPort loadChallengerPort;
    private final RemoveMemberPort removeMemberPort;

    @Override
    public void removeMember(RemoveMemberCommand command) {
        // 1. 실행자의 역할 조회 (현재 기수, 요청한 조직)
        List<ChallengerRole> executorRoles = loadChallengerRolePort
            .findByChallengerIdAndOrganizationId(
                command.getExecutorChallengerId(),
                command.getOrganizationId()
            );

        if (executorRoles.isEmpty()) {
            throw new InsufficientOrganizationPermissionException(
                "해당 조직의 멤버가 아닙니다."
            );
        }

        // 2. 대상자의 역할 조회
        Challenger targetChallenger = loadChallengerPort
            .findByMemberId(command.getTargetMemberId())
            .orElseThrow(() -> new NotChallengerException());

        List<ChallengerRole> targetRoles = loadChallengerRolePort
            .findByChallengerIdAndOrganizationId(
                targetChallenger.getId(),
                command.getOrganizationId()
            );

        if (targetRoles.isEmpty()) {
            throw new BusinessException(ErrorCode.TARGET_NOT_IN_ORGANIZATION);
        }

        // 3. 권한 검증 (계층 구조)
        ChallengerRole executorHighestRole = executorRoles.stream()
            .max(Comparator.comparing(r -> r.getRoleType().getLevel()))
            .orElseThrow();

        ChallengerRole targetHighestRole = targetRoles.stream()
            .max(Comparator.comparing(r -> r.getRoleType().getLevel()))
            .orElseThrow();

        if (!executorHighestRole.canManage(targetHighestRole)) {
            throw new InsufficientOrganizationPermissionException(
                String.format(
                    "%s 권한으로는 %s 권한을 가진 멤버를 관리할 수 없습니다.",
                    executorHighestRole.getRoleType().getDescription(),
                    targetHighestRole.getRoleType().getDescription()
                )
            );
        }

        // 4. 멤버 제거
        for (ChallengerRole role : targetRoles) {
            removeMemberPort.remove(role);
        }
    }
}
```

---

## 실전 사용 가이드

### 패턴 1: 공개 API

```java
@GetMapping("/posts")
@Public  // 또는 @PreAuthorize("permitAll()")
public ApiResponse<?> getPublicPosts() {
    return ApiResponse.success(postService.getPublicPosts());
}
```

### 패턴 2: 인증 필요

```java
@GetMapping("/my-profile")
@PreAuthorize("hasRole('MEMBER')")
public ApiResponse<?> getMyProfile(@AuthenticationPrincipal MemberPrincipal principal) {
    return ApiResponse.success(memberService.getProfile(principal.getMemberId()));
}
```

### 패턴 3: ADMIN만

```java
@DeleteMapping("/admin/data")
@PreAuthorize("hasRole('ADMIN')")
public ApiResponse<?> deleteAllData() {
    adminService.deleteAllData();
    return ApiResponse.success();
}
```

### 패턴 4: 특정 역할 (운영진)

```java
// Layer 1: 운영진 여부만 체크
@PostMapping("/activities/{activityId}/review")
@PreAuthorize("hasAnyAuthority('CENTRAL_PRESIDENT', 'SCHOOL_PRESIDENT', 'SCHOOL_STAFF')")
public ApiResponse<?> reviewActivity(
    @PathVariable Long activityId,
    @AuthenticationPrincipal MemberPrincipal principal,
    @RequestBody ReviewRequest request) {

    // Layer 3: Service에서 "어느 조직의 운영진인가?" 검증
    activityService.review(activityId, principal, request);
    return ApiResponse.success();
}
```

### 패턴 5: ABAC (소유권 체크)

```java
@PutMapping("/posts/{postId}")
@PreAuthorize("hasPermission(#postId, 'POST', 'UPDATE')")
public ApiResponse<?> updatePost(
    @PathVariable Long postId,
    @RequestBody UpdatePostRequest request) {

    postService.update(postId, request);
    return ApiResponse.success();
}
```

### 패턴 6: RBAC + ABAC 조합

```java
@DeleteMapping("/posts/{postId}")
@PreAuthorize("hasRole('MEMBER') and hasPermission(#postId, 'POST', 'DELETE')")
//              ↑ RBAC                  ↑ ABAC
public ApiResponse<?> deletePost(@PathVariable Long postId) {
    postService.delete(postId);
    return ApiResponse.success();
}
```

### 패턴 7: Service에서 복잡한 검증

```java
@DeleteMapping("/organizations/{orgId}/members/{memberId}")
@PreAuthorize("hasRole('MEMBER')")  // 최소 인증만
public ApiResponse<?> removeMember(
    @PathVariable Long orgId,
    @PathVariable Long memberId,
    @AuthenticationPrincipal MemberPrincipal principal) {

    // Service에서 조직 내 권한 + 계층 구조 검증
    organizationService.removeMember(
        new RemoveMemberCommand(orgId, principal.getCurrentChallengerId(), memberId)
    );

    return ApiResponse.success();
}
```

---

## 권한 검증 전략 선택 가이드

| 시나리오 | Layer | 방법 | 예시 |
|---------|-------|------|------|
| 공개 API | - | `@Public` | 공개 게시글 조회 |
| 인증 필요 | Layer 1 | `hasRole('MEMBER')` | 내 프로필 조회 |
| 시스템 관리자 | Layer 1 | `hasRole('ADMIN')` | 모든 데이터 삭제 |
| 특정 역할 | Layer 1 | `hasAuthority('SCHOOL_PRESIDENT')` | 운영진 여부만 체크 |
| 소유권 체크 | Layer 2 | `hasPermission(#id, 'POST', 'UPDATE')` | 내 게시글만 수정 |
| 조직 권한 | Layer 1+2 | `hasAuthority(...) + hasPermission(...)` | 특정 조직 운영진 |
| 시간 기반 | Layer 2 or 3 | ABAC 또는 Service | 제출 기한 체크 |
| 계층 구조 | Layer 3 | Service | 조직 내 상하 관계 |
| 복합 조건 | Layer 3 | Service | 다중 조건 검증 |

---

## 구현 체크리스트

### 1단계: MemberRole 설정

- [ ] `MemberRole` enum 생성 (ADMIN, MEMBER)
- [ ] `Member` Entity에 `role` 필드 추가
- [ ] Flyway Migration 작성
- [ ] 기존 데이터 마이그레이션 (기본값 MEMBER)

### 2단계: ChallengerRole 강화

- [ ] `RoleType`에 `level` 필드 추가
- [ ] `RoleType.isStaffRole()` 메서드 구현
- [ ] `ChallengerRole.canManage()` 도메인 로직 구현

### 3단계: MemberPrincipal 확장

- [ ] `MemberPrincipal`에 `currentChallengerId`, `challengerRoles` 추가
- [ ] `getAuthorities()` 메서드 수정 (ChallengerRole 포함)
- [ ] 편의 메서드 추가 (`isChallenger()`, `isStaff()`)

### 4단계: JwtTokenProvider 수정

- [ ] 현재 활성 기수 조회 로직 추가
- [ ] 현재 기수의 Challenger 조회
- [ ] 현재 기수의 ChallengerRole 로드
- [ ] MemberPrincipal 생성 시 전달

### 5단계: ABAC (PermissionEvaluator)

- [ ] `UmcPermissionEvaluator` 구현
- [ ] Post, Notice, Activity 권한 검증 로직 구현
- [ ] Helper 메서드 구현 (`isSameOrganization`, etc.)
- [ ] MethodSecurityConfig에 등록

### 6단계: Controller 적용

- [ ] 공개 API에 `@Public` 추가
- [ ] 인증 API에 `@PreAuthorize("hasRole('MEMBER')")` 추가
- [ ] 역할 기반 API에 `hasAuthority(...)` 추가
- [ ] ABAC 필요 API에 `hasPermission(...)` 추가

### 7단계: Service 검증 로직

- [ ] 조직 권한 검증 로직 구현
- [ ] 계층 구조 검증 로직 구현
- [ ] 시간 기반 검증 로직 구현

### 8단계: 테스트

- [ ] Unit Test: PermissionEvaluator 테스트
- [ ] Unit Test: Service 권한 검증 로직
- [ ] Integration Test: API 레벨 권한 체크
- [ ] E2E Test: 실제 사용자 시나리오

---

## Port 인터페이스 예시

### LoadChallengerRolePort

```java
// challenger/application/port/out/LoadChallengerRolePort.java
public interface LoadChallengerRolePort {

    List<ChallengerRole> findByChallengerId(Long challengerId);

    List<ChallengerRole> findByChallengerIdAndOrganizationId(
        Long challengerId,
        Long organizationId
    );

    Optional<ChallengerRole> findById(Long id);
}
```

### GetCurrentGisuUseCase

```java
// organization/application/port/in/query/GetCurrentGisuUseCase.java
public interface GetCurrentGisuUseCase {

    /**
     * 현재 활성 기수 ID 조회
     * @return 현재 활성 기수 ID
     */
    Long getCurrentGisuId();
}
```

---

## 예외 처리

### 권한 예외 클래스

```java
// global/exception/InsufficientPermissionException.java
public class InsufficientPermissionException extends BusinessException {
    public InsufficientPermissionException(String message) {
        super(ErrorCode.INSUFFICIENT_PERMISSION, message);
    }
}

// global/exception/InsufficientOrganizationPermissionException.java
public class InsufficientOrganizationPermissionException extends BusinessException {
    public InsufficientOrganizationPermissionException(String message) {
        super(ErrorCode.INSUFFICIENT_ORGANIZATION_PERMISSION, message);
    }
}

// global/exception/NotChallengerException.java
public class NotChallengerException extends BusinessException {
    public NotChallengerException() {
        super(ErrorCode.NOT_CHALLENGER, "챌린저가 아닙니다.");
    }
}
```

### ErrorCode 추가

```java
// global/exception/constant/ErrorCode.java
public enum ErrorCode {
    // ... 기존 코드

    // Authorization
    INSUFFICIENT_PERMISSION("AUTH_003", "권한이 부족합니다."),
    INSUFFICIENT_ORGANIZATION_PERMISSION("AUTH_004", "조직 내 권한이 부족합니다."),
    NOT_CHALLENGER("AUTH_005", "챌린저가 아닙니다."),
}
```

---

## MemberRole vs ChallengerRole 비교

| 구분 | MemberRole | ChallengerRole |
|-----|------------|----------------|
| **위치** | member 도메인 | challenger 도메인 |
| **용도** | 시스템 전역 권한 | 조직 내부 권한 |
| **범위** | 전체 시스템 | 특정 조직 + 기수 |
| **종류** | ADMIN, MEMBER | CENTRAL_PRESIDENT, SCHOOL_STAFF, 등 |
| **설정** | Member Entity에 단일 값 | ChallengerRole Entity (1:N 관계) |
| **검증 시점** | Controller `@PreAuthorize` | Service 레이어 비즈니스 로직 |
| **예시** | "시스템 관리자인가?" | "A대학교 9기 회장인가?" |

### 사용 예시

```java
// MemberRole: 시스템 레벨
if (principal.getMemberRole() == MemberRole.ADMIN) {
    // 전체 시스템 관리자
}

// ChallengerRole: 조직 레벨
if (principal.hasRole(RoleType.SCHOOL_PRESIDENT)) {
    // 특정 학교의 회장 (현재 기수)
}
```

---

## 참고 자료

- [Spring Security Method Security](https://docs.spring.io/spring-security/reference/servlet/authorization/method-security.html)
- [RBAC Wikipedia](https://en.wikipedia.org/wiki/Role-based_access_control)
- [ABAC Wikipedia](https://en.wikipedia.org/wiki/Attribute-based_access_control)
- Hexagonal Architecture - Get Your Hands Dirty on Clean Architecture

---

## 부록: 전체 데이터 흐름

### 인증 → 권한 검증 → 비즈니스 로직

```
1. 사용자 요청
   ├─ JWT Token: "Bearer eyJhbGc..."
   └─ Endpoint: DELETE /api/v1/organizations/123/members/456

2. JwtAuthenticationFilter
   ├─ JWT 파싱
   ├─ Member 조회 (memberId: 100)
   ├─ 현재 기수 조회 (gisuId: 9)
   ├─ Challenger 조회 (challengerId: 200, gisuId: 9)
   ├─ ChallengerRole 조회 ([SCHOOL_PRESIDENT at orgId: 123])
   └─ MemberPrincipal 생성
       {
         memberId: 100,
         memberRole: MEMBER,
         currentChallengerId: 200,
         challengerRoles: [SCHOOL_PRESIDENT],
         authorities: [ROLE_MEMBER, SCHOOL_PRESIDENT]
       }

3. Spring Security 검증
   @PreAuthorize("hasAuthority('SCHOOL_PRESIDENT')")
   ├─ authorities에 SCHOOL_PRESIDENT 있음?
   └─ ✅ 통과

4. Controller
   public ApiResponse<?> removeMember(
       @PathVariable Long orgId,  // 123
       @PathVariable Long memberId,  // 456
       @AuthenticationPrincipal MemberPrincipal principal) {

       organizationService.removeMember(
           new RemoveMemberCommand(orgId, principal.getCurrentChallengerId(), memberId)
       );
   }

5. Service 비즈니스 로직
   ├─ executorRoles = findByChallenger(200) where orgId=123
   │   → [SCHOOL_PRESIDENT at org 123]
   ├─ targetRoles = findByMemberId(456) where orgId=123
   │   → [CHALLENGER at org 123]
   ├─ same organization? ✅
   ├─ executorRole.level > targetRole.level?
   │   SCHOOL_PRESIDENT(65) > CHALLENGER(10) ✅
   └─ 멤버 제거 성공!
```

---

## 문의 및 기여

RBAC + ABAC 구현 관련 질문이나 개선 사항은 팀 Notion 또는 GitHub Issue로 남겨주세요.
