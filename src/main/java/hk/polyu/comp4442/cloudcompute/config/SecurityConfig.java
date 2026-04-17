package hk.polyu.comp4442.cloudcompute.config;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import hk.polyu.comp4442.cloudcompute.security.AuthTokenFilter;

@Configuration
public class SecurityConfig {

    @Bean
    public AuthTokenFilter authTokenFilter() {
        return new AuthTokenFilter();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/",
                                "/index.html",
                                "/home.html",
                                "/login.html",
                                "/register.html",
                                "/js/**",
                                "/api/v1/auth/refresh", // add refresh end point
                                "/api/v1/auth/register",
                                "/api/v1/auth/login",
                                "/api/v1/compute/**",
                                "/swagger-ui/**",
                                "/v3/api-docs/**")
                        .permitAll()
                    .requestMatchers("/task.html", "/edit.html", "/api/v1/tasks/**", "/api/v1/files/**", "/api/v1/auth/me",
                        "/api/v1/auth/logout")
                        .authenticated()
                    .anyRequest().authenticated())
                .exceptionHandling(ex -> ex.authenticationEntryPoint((request, response, authException) -> {
                    String uri = request.getRequestURI();
                    if (uri.endsWith(".html") && !uri.equals("/login.html")) {
                        // redirect to login page
                        response.sendRedirect("/login.html");
                    } else {
                        // API calls return 401 such auth.js can refresh
                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        response.getWriter().write("{\"error\": \"Unauthorized\"}");
                    }
                }));

        http.addFilterBefore(authTokenFilter(), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration)
            throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
}
