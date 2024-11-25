package com.example.To_Do.Liste;

import com.example.To_Do.Liste.service.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;

import java.util.Locale;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/api/todos/**") // CSRF-Schutz für spezifische Endpunkte deaktivieren
                        .ignoringRequestMatchers("/projekt/updateTodoPerson")
                )
                .authorizeHttpRequests(requests -> requests
                        .requestMatchers("/static/**", "/images/**", "/css/**", "/", "/password-reset", "/password", "/registration").permitAll() // Öffentliche Routen
                        .requestMatchers(HttpMethod.POST, "/todos").authenticated() // POST für Todos erfordert Authentifizierung
                        .requestMatchers(HttpMethod.DELETE, "/todos/**").authenticated() // DELETE für Todos erfordert Authentifizierung
                        .requestMatchers(HttpMethod.PUT, "/todos/**").authenticated() // PUT für Todos erfordert Authentifizierung
                        .requestMatchers("/projekt/updateTodoPerson").authenticated() // Nur Anmeldung erforderlich
                        .anyRequest().authenticated() // Alle anderen Routen erfordern Authentifizierung
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/dashboard", true) // Weiterleitung nach erfolgreichem Login
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutSuccessUrl("/login?logout") // Weiterleitung nach Logout
                        .permitAll()
                )
                .userDetailsService(userDetailsService);

        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return userDetailsService;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return NoOpPasswordEncoder.getInstance(); // Verwende NoOpPasswordEncoder (für einfache Tests, nicht für Produktion)
    }

    @Bean
    public MessageSource messageSource() {
        ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
        messageSource.setBasename("classpath:messages");
        messageSource.setDefaultEncoding("UTF-8");
        return messageSource;
    }

    @Bean
    public LocaleResolver localeResolver() {
        CookieLocaleResolver resolver = new CookieLocaleResolver();
        resolver.setDefaultLocale(Locale.GERMAN); // Standard auf Deutsch
        return resolver;
    }

    @Bean
    public LocaleChangeInterceptor localeChangeInterceptor() {
        LocaleChangeInterceptor interceptor = new LocaleChangeInterceptor();
        interceptor.setParamName("lang"); // Parametername für die Sprachwahl
        return interceptor;
    }
}
