package com.slt.iau_portal.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        logger.info("Configuring security filter chain");
        
        http
            .authorizeHttpRequests(authz -> authz
                // Public access to complaint portal (no login needed)
                .requestMatchers("/", "/complaint", "/complaint/**", "/confirmation").permitAll()
                .requestMatchers("/css/**", "/js/**", "/images/**", "/static/**").permitAll()
                .requestMatchers("/login", "/login/**", "/error").permitAll()
                // Only admin dashboard requires authentication
                .requestMatchers("/admin/**").authenticated()
                .anyRequest().permitAll()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/admin/dashboard", true)
                .failureUrl("/login?error")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutSuccessUrl("/")
                .permitAll()
            )
            .csrf(csrf -> csrf
                .ignoringRequestMatchers("/complaint/submit", "/api/**")
                .disable() // Disabled for form submissions - enable in production with proper CSRF tokens
            )
            .headers(headers -> headers
                .contentTypeOptions().and()
                .xssProtection().and()
                .frameOptions().deny()
            )
            .sessionManagement(session -> session
                .sessionFixationProtection(org.springframework.security.config.http.SessionFixationProtection.MIGRATEDATA)
                .sessionConcurrency(concurrency -> concurrency.maximumSessions(1))
            );
        
        logger.info("Security filter chain configured successfully");
        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        logger.info("Initializing UserDetailsService with default admin user");
        
        UserDetails admin = User.builder()
            .username("admin")
            .password(passwordEncoder().encode("admin123"))
            .roles("ADMIN")
            .build();

        return new InMemoryUserDetailsManager(admin);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
}
