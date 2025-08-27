package Hospital.system.Security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final UserDetailsService userDetailsService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authz -> authz
                        // Public endpoints
                        .requestMatchers("/api/auth/**").permitAll()

                        // Admin endpoints - full access to manage all users
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/users/**").hasRole("ADMIN")

                        // Doctor endpoints
                        .requestMatchers("/api/doctors/**").hasRole("DOCTOR")
                        .requestMatchers("/api/appointments/doctor/**").hasRole("DOCTOR")
                        .requestMatchers("/api/prescriptions/doctor/**").hasRole("DOCTOR")
                        .requestMatchers("/api/patient-records/doctor/**").hasRole("DOCTOR")
                        .requestMatchers("/api/messages/doctor/**").hasAnyRole("DOCTOR", "PATIENT")

                        // Patient endpoints
                        .requestMatchers("/api/patients/**").hasRole("PATIENT")
                        .requestMatchers("/api/appointments/patient/**").hasRole("PATIENT")
                        .requestMatchers("/api/orders/patient/**").hasRole("PATIENT")
                        .requestMatchers("/api/messages/patient/**").hasAnyRole("DOCTOR", "PATIENT")

                        // Pharmacy endpoints
                        .requestMatchers("/api/pharmacy/**").hasRole("PHARMACY")
                        .requestMatchers("/api/medicines/**").hasRole("PHARMACY")
                        .requestMatchers("/api/orders/pharmacy/**").hasRole("PHARMACY")

                        // Shared endpoints (accessible by multiple roles)
                        .requestMatchers("/api/appointments").hasAnyRole("DOCTOR", "PATIENT")
                        .requestMatchers("/api/messages").hasAnyRole("DOCTOR", "PATIENT")
                        .requestMatchers("/api/medicines/list").hasAnyRole("PATIENT", "DOCTOR", "PHARMACY")

                        .anyRequest().authenticated())
                .userDetailsService(userDetailsService)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}