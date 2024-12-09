package com.know.letter.config;

import java.util.Collections;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import com.know.letter.jwt.JWTFilter;
import com.know.letter.jwt.JWTUtil;

import jakarta.servlet.http.HttpServletRequest;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    private final AuthenticationConfiguration authenticationConfiguration;
    private final JWTUtil jwtUtil;

    public SecurityConfig(AuthenticationConfiguration authenticationConfiguration, JWTUtil jwtUtil) {
        this.authenticationConfiguration = authenticationConfiguration;
        this.jwtUtil = jwtUtil;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {

        return configuration.getAuthenticationManager();
    }

    @Bean
    public static PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain configure(HttpSecurity http) throws Exception {

        http
                .cors((corsCustomizer -> corsCustomizer.configurationSource(new CorsConfigurationSource() {

                    @Override
                    public CorsConfiguration getCorsConfiguration(HttpServletRequest request) {

                        CorsConfiguration configuration = new CorsConfiguration();

                        configuration.setAllowedOrigins(Collections.singletonList("http://localhost:3000"));
                        configuration.setAllowedMethods(Collections.singletonList("*"));
                        configuration.setAllowCredentials(true);
                        configuration.setAllowedHeaders(Collections.singletonList("*"));
                        configuration.setMaxAge(3600L);

                        configuration.setExposedHeaders(Collections.singletonList("Authorization"));

                        return configuration;
                    }
                })));
        http
                .csrf((auth)->auth.disable());

        http
                .formLogin((auth)-> auth.disable());
        http
                .httpBasic((auth) -> auth.disable());

        http
                .csrf(csrf -> csrf.disable())  // CSRF 보호 비활성화
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/auth/login", "/auth/logout", "/logout" ,"/login", "/join",
                                "/api/v1/user/join", "/api/v1/email/send" , "/api/v1/email/**", "/api/v1/user/sales/join").permitAll()
                        // Swagger 관련 경로들에 대해 접근 허용
                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-resources/**",
                                "/webjars/**",
                                "/swagger-ui.html"
                        ).permitAll()
                        .requestMatchers("/admin").hasRole("ADMIN")
                        .requestMatchers("/advertiser").hasAnyRole("ADVERTISER", "ADMIN")
                        .requestMatchers("/sales", "/reward/sales/list").hasAnyRole("SALES", "ADMIN")
                        .requestMatchers("my/**").hasAnyRole("ADMIN", "USER", "ADVERTISER", "SALES")
                        .requestMatchers("/**").hasAnyRole("USER", "ADMIN")
                        .anyRequest().authenticated()
                );
//        http
//                .formLogin((auth)->auth.loginPage("/login")
//                        .loginProcessingUrl("/loginProc")
//                        .loginPage("/login")
//                        .loginProcessingUrl("/loginProc")
//                        .usernameParameter("userId")
//                        .passwordParameter("userPassword")
//                        .permitAll()
//                );


        http
                .addFilterBefore(new JWTFilter(jwtUtil), UsernamePasswordAuthenticationFilter.class);

//        http
//                .addFilterBefore(new JWTFilter(jwtUtil), LoginFilter.class);

//        http
//                .addFilterAt(new LoginFilter(authenticationManager(authenticationConfiguration), jwtUtil), UsernamePasswordAuthenticationFilter.class);
        http
                .sessionManagement((session)->session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS));



        return http.build();
    }



}