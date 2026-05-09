package com.kpi.ejournal.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .httpBasic(httpBasic -> httpBasic.disable())
                .formLogin(form -> form.disable())
                .logout(logout -> logout.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/",
                                "/index.html",
                                "/login.html",

                                "/student.html",
                                "/teacher.html",
                                "/administrator.html",
                                "/methodologist.html",
                                "/dashboard.html",

                                "/grades.html",
                                "/schedule.html",
                                "/reports.html",
                                "/users.html",
                                "/groups.html",
                                "/disciplines.html",

                                "/css/**",
                                "/js/**",
                                "/images/**",
                                "/assets/**",
                                "/pages/**",
                                "/h2-console/**",
                                "/favicon.ico"
                        ).permitAll()
                        .requestMatchers("/api/**").permitAll()
                        .anyRequest().permitAll()
                )
                .headers(headers -> headers.frameOptions(frame -> frame.disable()))
                .build();
    }
}