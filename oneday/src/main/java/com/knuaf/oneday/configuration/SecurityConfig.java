package com.knuaf.oneday.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())

            .authorizeHttpRequests(authorize ->authorize
                // 회원가입, 로그인, 루트 경로는 누구나 접근 가능
                .requestMatchers("/signup", "/login", "/").permitAll()
                //그 외 모든 요청은 인증 필요
                .anyRequest().authenticated()
            )
            .formLogin(formLogin ->formLogin
                //커스텀 로그인 페이지 경로 설정
                .loginPage("/login")
                //로그인 성공 시 이동할 기본 경로
                .defaultSuccessUrl("/welcome", true)
                    .usernameParameter("userId")
                .permitAll()
            )
            .logout(logout ->logout
                //로그아웃 성공 시 이동할 경로
                .logoutSuccessUrl("/")
                .permitAll()
            );
        return http.build();
    }
}
