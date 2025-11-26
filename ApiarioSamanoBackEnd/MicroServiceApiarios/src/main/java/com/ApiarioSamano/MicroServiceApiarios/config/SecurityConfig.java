package com.ApiarioSamano.MicroServiceApiarios.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> {
                })
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .authorizeHttpRequests(auth -> auth
                        // 🔐 Endpoints protegidos
                        .requestMatchers("/apiarios/admin/**").authenticated()
                        .requestMatchers("/historial-medico/admin/**").authenticated()
                        .requestMatchers("/api/recetas/**").authenticated()
                        .requestMatchers("/api/receta-medicamento/**").authenticated()
                        .requestMatchers("/api/historial-recetas/**").authenticated()

                        // 🔐 Endpoints de MQTT y actuadores (también requieren JWT)
                        .requestMatchers(
                                "/api/apiarios/mqtt/**",
                                "/api/apiarios/*/ventilador/**",
                                "/api/apiarios/*/luz/**",
                                "/api/apiarios/*/servo/**")
                        .permitAll()

                        // 🔓 Cualquier otro endpoint se permite sin JWT
                        .anyRequest().permitAll())

                // Filtro JWT
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
