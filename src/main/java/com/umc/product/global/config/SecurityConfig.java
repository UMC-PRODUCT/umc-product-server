package com.umc.product.global.config;


import com.umc.product.authentication.adapter.in.oauth.OAuth2AuthenticationFailureHandler;
import com.umc.product.authentication.adapter.in.oauth.OAuth2AuthenticationSuccessHandler;
import com.umc.product.authentication.application.service.UmcProductOAuth2UserService;
import com.umc.product.global.security.ApiAccessDeniedHandler;
import com.umc.product.global.security.ApiAuthenticationEntryPoint;
import com.umc.product.global.security.JwtAuthenticationFilter;
import com.umc.product.global.security.util.PublicEndpointCollector;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity // @PreAuthorize, @PostAuthorize 활성화
@RequiredArgsConstructor
public class SecurityConfig {

    private static final String[] SWAGGER_PATHS = {
            "/swagger-ui/**",
            "/docs/**",
            "/v3/api-docs/**",
            "/docs-json/**",
            "/swagger-resources/**",
            "/webjars/**"
    };

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final ApiAuthenticationEntryPoint authenticationEntryPoint;
    private final ApiAccessDeniedHandler accessDeniedHandler;
    private final UmcProductOAuth2UserService umcProductOAuth2UserService;
    private final OAuth2AuthenticationSuccessHandler oAuth2SuccessHandler;
    private final OAuth2AuthenticationFailureHandler oAuth2FailureHandler;
    private final RequestMappingHandlerMapping requestMappingHandlerMapping;

    /**
     * Swagger용 SecurityFilterChain (dev에서만 활성화, local은 따로 제약을 걸지 않음)
     * <p>
     * HTTP Basic 인증 적용 - 순서가 먼저라서 Swagger 경로는 이 체인이 처리
     */
    @Bean
    @Order(1)
    @Profile("dev")
    public SecurityFilterChain swaggerSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher(SWAGGER_PATHS)
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().authenticated()
                )
                .httpBasic(Customizer.withDefaults());  // HTTP Basic 인증

        return http.build();
    }

    /**
     * 메인 Security 체인
     *
     * @param http
     * @return
     * @throws Exception
     */
    @Bean
    @Order(2)
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        List<PublicEndpointCollector.EndpointMatcher> publicEndpoints = PublicEndpointCollector
                .collectPublicEndpoints(requestMappingHandlerMapping);

        // ✅ 디버깅 로그
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("🔓 Public Endpoints 수집 결과:");
        if (publicEndpoints.isEmpty()) {
            System.out.println("  ⚠️  수집된 엔드포인트가 없습니다!");
        } else {
            publicEndpoints.forEach(endpoint -> {
                String method = endpoint.method() != null ? endpoint.method().name() : "ALL";
                System.out.println("  ✅ " + method + " " + endpoint.pattern());
            });
        }
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

        http
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)   // 폼 로그인 비활성화
                .httpBasic(AbstractHttpConfigurer::disable)   // HTTP Basic 비활성
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // OAuth2 로그인 설정
                .oauth2Login(oauth2 -> oauth2
                        .authorizationEndpoint(authorization -> authorization
                                .baseUri("/api/v1/auth/oauth2/authorization") // 기본: /oauth2/authorization
                        )
                        .redirectionEndpoint(redirection -> redirection
                                .baseUri("/api/v1/auth/oauth2/callback/*")  // 기본: /login/oauth2/code/*
                        )
                        // 우리 DB랑 비교해서 사용자 정보를 저장함
                        // 여기서 실패한 요청도 failure로 들어감
                        .userInfoEndpoint(userInfo ->
                                userInfo.userService(umcProductOAuth2UserService))
                        // OAuth 로그인이 성공했을 때 핸들링하는 곳
                        .successHandler(oAuth2SuccessHandler)
                        // OAuth 로그인이 실패했을 때 핸들링하는 곳 (그냥 실패한거)
                        .failureHandler(oAuth2FailureHandler)
                )
                .authorizeHttpRequests(auth -> {
                    // 공개 엔드포인트
                    auth.requestMatchers(
                            // Health Check & Error
                            "/actuator/**",
                            "/error",
                            // OAuth2
                            "/api/v1/auth/oauth2/authorization/**",
                            "/api/v1/auth/oauth2/callback/**"
                    ).permitAll();

                    // Swagger
                    auth.requestMatchers(SWAGGER_PATHS).permitAll();

                    // @Public 어노테이션이 달린 엔드포인트 (HTTP 메서드 포함)
                    for (PublicEndpointCollector.EndpointMatcher endpoint : publicEndpoints) {
                        if (endpoint.method() != null) {
                            auth.requestMatchers(endpoint.method(), endpoint.pattern()).permitAll();
                        } else {
                            auth.requestMatchers(endpoint.pattern()).permitAll();
                        }
                    }

                    // 나머지는 인증 필요
                    auth.anyRequest().authenticated();
                })
                // Spring 기본 로그인 필터 동작 전에 JWT 동작
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(authenticationEntryPoint) // 인증 실패 시
                        .accessDeniedHandler(accessDeniedHandler)           // 인가 실패 시
                );

        return http.build();
    }

    /**
     * Swagger Basic Auth용 InMemoryUserDetailsManager (dev 프로필)
     */
    @Bean
    @Profile("dev")
    public UserDetailsService swaggerUserDetailsService(
            @Value("${app.swagger-auth.username:username}") String username,
            @Value("${app.swagger-auth.password:password}") String password,
            PasswordEncoder passwordEncoder) {
        UserDetails user = User.builder()
                .username(username)
                .password(passwordEncoder.encode(password))
                .roles("SWAGGER")
                .build();
        return new InMemoryUserDetailsManager(user);
    }


    /**
     * UserDetailsService Bean을 제공하여 Spring Security의 기본 사용자 자동 생성 방지
     * <p>
     * Spring Security가 "인증 체계가 구성되어 있다"고 인식하도록 하기 위한 더미 Bean
     */
    @Bean
    @Profile("!dev")
    public UserDetailsService userDetailsService() {
        return username -> {
            throw new UsernameNotFoundException(
                    "This application uses JWT authentication, not UserDetailsService");
        };
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // Swagger CORS 설정
        configuration.setAllowedOriginPatterns(
                List.of(
                        "http://localhost:8080",
                        "http://localhost:3000",
                        // Swagger
                        "https://dev.umc-product.kyeoungwoon.kr",
                        "https://umc-product.kyeoungwoon.kr"
                ));
        configuration.setAllowedMethods(List.of("*"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
