package az.armudu.cayevi.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

import java.time.Duration;

/**
 * Təhlükəsizlik qaydaları.
 * Açıq: sayt, menyu, mürəbbələr, slotlar, rezerv yaratmaq və rezerv təsdiqini nömrə ilə görmək.
 * Yalnız admin: bütün rezervlərin siyahısı, rezerv ləğvi, /api/admin/** və actuator (health-dən başqa).
 */
@Configuration
public class SecurityConfig {

    private static final Logger log = LoggerFactory.getLogger(SecurityConfig.class);
    static final String DEFAULT_PASSWORD = "armudu-admin";

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService(AdminProperties props, PasswordEncoder encoder) {
        String username = props.username() == null || props.username().isBlank() ? "admin" : props.username();
        String password = props.password() == null || props.password().isBlank() ? DEFAULT_PASSWORD : props.password();
        if (DEFAULT_PASSWORD.equals(password)) {
            log.warn("Admin standart parolla işləyir. Prodda ADMIN_PASSWORD mühit dəyişənini mütləq təyin edin.");
        }
        UserDetails admin = User.withUsername(username)
                .password(encoder.encode(password))
                .roles("ADMIN")
                .build();
        return new InMemoryUserDetailsManager(admin);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // API stateless JSON-dur, brauzer sessiyası saxlamır; CSRF token-i yoxdur
                .csrf(csrf -> csrf.disable())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Açıq API
                        .requestMatchers(HttpMethod.GET, "/api/menu", "/api/jams").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/reservations/slots", "/api/reservations/spots").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/reservations/{id:[0-9]+}").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/reservations").permitAll()
                        // Yalnız admin
                        .requestMatchers(HttpMethod.GET, "/api/reservations").hasRole("ADMIN")
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/reservations/{id:[0-9]+}").hasRole("ADMIN")
                        .requestMatchers("/actuator/health", "/actuator/health/**").permitAll()
                        .requestMatchers("/actuator/**").hasRole("ADMIN")
                        // Statik sayt
                        .requestMatchers(HttpMethod.GET, "/", "/index.html", "/admin.html", "/favicon.svg",
                                "/css/**", "/js/**", "/img/**", "/error").permitAll()
                        .anyRequest().authenticated())
                .httpBasic(b -> {})
                .headers(h -> h
                        .contentTypeOptions(c -> {})
                        .frameOptions(f -> f.deny())
                        .referrerPolicy(r -> r.policy(
                                org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN))
                        .httpStrictTransportSecurity(hsts -> hsts
                                .includeSubDomains(true)
                                .maxAgeInSeconds(Duration.ofDays(365).toSeconds()))
                        .contentSecurityPolicy(csp -> csp.policyDirectives(
                                "default-src 'self'; " +
                                "style-src 'self' https://fonts.googleapis.com; " +
                                "font-src 'self' https://fonts.gstatic.com; " +
                                "img-src 'self' data:; " +
                                "script-src 'self'; " +
                                "connect-src 'self'; " +
                                "frame-ancestors 'none'; " +
                                "base-uri 'self'; " +
                                "form-action 'self'")));
        return http.build();
    }
}
