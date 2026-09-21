package com.nyangtech.nyangtechbackend.global.security;

import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

/**
 * 어떤 API는 누구나 호출할 수 있고, 어떤 API는 로그인(토큰)이 필요한지 정하는 "문지기 규칙".
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final RestSecurityErrorHandler errorHandler;

    /** H2 콘솔이 켜져 있을 때만(=로컬 개발) 콘솔 접근을 허용한다. */
    @Value("${spring.h2.console.enabled:false}")
    private boolean h2ConsoleEnabled;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) {
        List<RequestMatcher> publicPaths = new ArrayList<>();
        publicPaths.add(PathPatternRequestMatcher.pathPattern("/api/v1/auth/**"));
        publicPaths.add(PathPatternRequestMatcher.pathPattern("/error"));
        if (h2ConsoleEnabled) {
            publicPaths.add(PathPatternRequestMatcher.pathPattern("/h2-console/**"));
        }

        http
                // 세션/쿠키를 쓰지 않고 매 요청의 토큰으로만 인증하므로 CSRF 방어와 기본 로그인 화면이 필요 없다.
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // H2 콘솔은 화면 안에 화면(frame)을 쓰기 때문에 같은 출처 frame을 허용한다.
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(publicPaths.toArray(RequestMatcher[]::new)).permitAll()
                        .anyRequest().authenticated())
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(Customizer.withDefaults())
                        .authenticationEntryPoint(errorHandler)
                        .accessDeniedHandler(errorHandler))
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(errorHandler)
                        .accessDeniedHandler(errorHandler));

        return http.build();
    }
}
