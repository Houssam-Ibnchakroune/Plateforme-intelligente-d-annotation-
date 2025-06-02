// src/main/java/com/ensah/annonateur/config/SecurityConfig.java
package com.ensah.annonateur.config;

import com.ensah.annonateur.models.Admin;
import com.ensah.annonateur.repositories.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/login", "/css/**", "/js/**").permitAll()
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/annotate/**").hasRole("ANNOTATOR")
                        .requestMatchers("/admin/datasets/**").hasRole("ADMIN")
                        .requestMatchers("/tasks/**").hasRole("ANNOTATOR")
                        .requestMatchers("/admin/datasets/**").hasRole("ADMIN")
                        .requestMatchers("/admin/annotators/**").hasRole("ADMIN")
                        .requestMatchers("/admin/advanced/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .successHandler((request, response, authentication) -> {
                            boolean isAdmin = authentication.getAuthorities()
                                    .stream()
                                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
                            response.sendRedirect(isAdmin ? "/admin/datasets" : "/tasks");
                        })
                        .permitAll()
                )

                .logout(logout -> logout
                        .logoutSuccessUrl("/login?logout")
                        .permitAll()
                );

        return http.build();
    }

    /** Charge l’utilisateur depuis la BDD et expose ses rôles */
    @Bean
    public UserDetailsService userDetailsService(UserRepository userRepo) {
        return username -> userRepo.findByUsername(username)
                .map(user -> org.springframework.security.core.userdetails.User
                        .withUsername(user.getUsername())
                        .password(user.getPassword())
                        .roles(user instanceof Admin ? "ADMIN" : "ANNOTATOR")   // ajoute le préfixe ROLE_
                        .build())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    /** Encodeur BCrypt partagé par toute l’appli */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
