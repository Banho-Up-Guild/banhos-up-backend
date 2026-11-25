package com.github.banhosup.config;

import com.github.banhosup.security.JwtFilter;
import com.github.banhosup.service.EmployeePermissionService;
import lombok.RequiredArgsConstructor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.http.HttpMethod;

import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@EnableMethodSecurity
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final EmployeePermissionService employeePermissionService;
    private final JwtFilter jwtFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http
    ) throws Exception {

        http
                .cors(Customizer.withDefaults())

                .csrf(csrf -> csrf.disable())

                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                .authorizeHttpRequests(auth -> auth

                        // Permite requisições de preflight do navegador.
                        .requestMatchers(HttpMethod.OPTIONS, "/**")
                        .permitAll()

                        // LOGIN E CADASTRO DE CLIENTES
                        .requestMatchers(HttpMethod.POST, "/auth/login")
                        .permitAll()

                        .requestMatchers(HttpMethod.POST, "/users/register")
                        .permitAll()

                        // CADASTRO E GERENCIAMENTO DE FUNCIONÁRIOS
                        .requestMatchers(HttpMethod.POST, "/users/staff")
                        .hasRole("ADMIN")

                        .requestMatchers(HttpMethod.GET, "/users/me")
                        .authenticated()

                        .requestMatchers(HttpMethod.GET, "/users")
                        .hasRole("ADMIN")

                        .requestMatchers(HttpMethod.PUT, "/users/*")
                        .hasRole("ADMIN")

                        .requestMatchers(HttpMethod.PATCH, "/users/*/active")
                        .hasRole("ADMIN")

                        // CONSULTA PÚBLICA DAS UNIDADES
                        .requestMatchers(HttpMethod.GET, "/lojas", "/lojas/*")
                        .permitAll()

                        // OPERAÇÕES ADMINISTRATIVAS DAS UNIDADES
                        .requestMatchers(HttpMethod.POST, "/lojas")
                        .hasRole("ADMIN")

                        .requestMatchers(HttpMethod.PUT, "/lojas/*")
                        .hasRole("ADMIN")

                        .requestMatchers(HttpMethod.PATCH, "/lojas/*/status")
                        .hasRole("ADMIN")

                        // CONSULTA PÚBLICA DOS SERVIÇOS
                        .requestMatchers(HttpMethod.GET, "/offerings", "/offerings/*")
                        .permitAll()

                        // GERENCIAMENTO GLOBAL DOS SERVIÇOS
                        // O ADMIN sempre possui acesso.
                        // O EMPLOYEE precisa de OFFERINGS_MANAGE.

                        .requestMatchers(HttpMethod.POST, "/offerings")
                        .access((authentication, context) ->
                                new org.springframework.security.authorization.AuthorizationDecision(
                                        employeePermissionService.hasPermission(
                                                authentication.get(),
                                                "OFFERINGS_MANAGE"
                                        )
                                )
                        )

                        .requestMatchers(HttpMethod.PUT, "/offerings/*")
                        .access((authentication, context) ->
                                new org.springframework.security.authorization.AuthorizationDecision(
                                        employeePermissionService.hasPermission(
                                                authentication.get(),
                                                "OFFERINGS_MANAGE"
                                        )
                                )
                        )

                        .requestMatchers(HttpMethod.DELETE, "/offerings/*")
                        .access((authentication, context) ->
                                new org.springframework.security.authorization.AuthorizationDecision(
                                        employeePermissionService.hasPermission(
                                                authentication.get(),
                                                "OFFERINGS_MANAGE"
                                        )
                                )
                        )

                        // AGENDAMENTO PÚBLICO DOS CLIENTES
                        .requestMatchers(HttpMethod.POST, "/appointments")
                        .permitAll()

                        .requestMatchers(HttpMethod.GET, "/appointments/calendar")
                        .permitAll()

                        // CONSULTA GERAL DE AGENDAMENTOS: ADMIN
                        .requestMatchers(HttpMethod.GET, "/appointments")
                        .hasRole("ADMIN")

                        // ÁREA ADMINISTRATIVA
                        .requestMatchers("/admin/**")
                        .hasRole("ADMIN")

                        // ÁREA DO FUNCIONÁRIO
                        // As permissões individuais serão verificadas
                        // pelas anotações @PreAuthorize dos Controllers.
                        .requestMatchers("/employee/**")
                        .hasRole("EMPLOYEE")

                        // Mantemos as demais rotas existentes como estavam.
                        // Elas serão auditadas e protegidas por funcionalidade
                        // para não interromper o trabalho dos outros integrantes.
                        .anyRequest()
                        .permitAll()

                )

                .addFilterBefore(
                        jwtFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(List.of(
                "http://localhost:4200",
                "http://127.0.0.1:4200"
        ));

        configuration.setAllowedMethods(List.of(
                "GET",
                "POST",
                "PUT",
                "PATCH",
                "DELETE",
                "OPTIONS"
        ));

        configuration.setAllowedHeaders(List.of("*"));

        configuration.setAllowCredentials(false);

        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration(
                "/**",
                configuration
        );

        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}