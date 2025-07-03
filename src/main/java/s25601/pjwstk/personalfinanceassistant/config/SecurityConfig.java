package s25601.pjwstk.personalfinanceassistant.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import s25601.pjwstk.personalfinanceassistant.service.CustomUserDetailsService;

@Configuration
public class SecurityConfig {

    // Custom service to load user-specific data from DB
    private final CustomUserDetailsService userDetailsService;

    public SecurityConfig(CustomUserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        // Creates a password encoder that securely hashes passwords
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        // Used to check login info based on user data and password encoder
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/", "/register", "/h2-console/**", "/css/**", "/js/**").permitAll() // Allow everyone to access
                        .anyRequest().authenticated() // Require login for all other pages
                )
                .formLogin(form -> form
                        .loginPage("/login") // Use custom login page at /login
                        .permitAll() // Everyone can access login page
                        .defaultSuccessUrl("/profile", true) // After successful login, go to /profile
                        .failureUrl("/login?error=true") // If login fails, stay on login page with error
                )
                .logout(logout -> logout
                        .logoutUrl("/logout") // URL to trigger logout
                        .logoutSuccessUrl("/") // After logout, redirect to home page
                        .permitAll() // Everyone can logout
                )
                .headers(headers -> headers.frameOptions(frameOptions -> frameOptions.sameOrigin()))
                .csrf(csrf -> csrf.ignoringRequestMatchers("/h2-console/**"));

        http.authenticationProvider(authenticationProvider());

        return http.build();
    }
}