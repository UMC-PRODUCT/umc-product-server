package com.umc.product.global.config;


import com.umc.product.global.security.ApiAccessDeniedHandler;
import com.umc.product.global.security.ApiAuthenticationEntryPoint;
import com.umc.product.global.security.CustomAuthorizationManager;
import com.umc.product.global.security.JwtAuthenticationFilter;
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

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity // @PreAuthorize, @PostAuthorize 활성화
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CustomAuthorizationManager customAuthorizationManager;
    private final ApiAuthenticationEntryPoint authenticationEntryPoint;
    private final ApiAccessDeniedHandler accessDeniedHandler;

    private static final String[] SWAGGER_PATHS = {
            "/swagger-ui/**",
            "/docs/**",
            "/v3/api-docs/**",
            "/docs-json/**",
            "/swagger-resources/**",
            "/webjars/**"
    };

    /**
     * Swagger용 SecurityFilterChain (dev 프로필에서만 활성화)
     * - HTTP Basic 인증 적용
     * - 순서가 먼저라서 Swagger 경로는 이 체인이 처리
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

    @Bean
    @Order(2)
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)   // 폼 로그인 비활성화
                .httpBasic(AbstractHttpConfigurer::disable)   // HTTP Basic 비활성
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Health Check
                        .requestMatchers("/actuator/**").permitAll()
                        // Swagger API
                        .requestMatchers(SWAGGER_PATHS).permitAll()
                        .anyRequest().access(customAuthorizationManager)  // 커스텀 매니저 사용
                )
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
            @Value("${swagger.auth.username:admin}") String username,
            @Value("${swagger.auth.password:admin123}") String password,
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
                List.of("http://localhost:8080", "http://localhost:3000"));
        configuration.setAllowedMethods(List.of("*"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}