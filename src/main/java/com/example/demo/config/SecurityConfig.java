package com.example.demo.config;


import com.example.demo.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
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
    
    //1. password encoder
    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    //2. UserDetailsService: How to find it in db
    @Bean
    public UserDetailsService userDetailsService(UserRepository userRepository){
        return loginInput -> {
            // Use the new method to search by Email OR Username
            com.example.demo.model.User user = userRepository.findByEmailOrUsername(loginInput);
            
            if (user == null){
                throw new UsernameNotFoundException("User not found: " + loginInput);
            }
            return new CustomUserDetails(user);
        };
    }
    
    //3. Authentication Provider: Connects DB and Password Encoder
    @Bean
    public DaoAuthenticationProvider authenticationProvider(UserRepository userRepository){
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService(userRepository));
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }
    
    //for success handler
    @Autowired
    private CustomAuthenticationSuccessHandler successHandler;

    //4. URL rules
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(config -> config
                .requestMatchers(
                    "/css/**", 
                    "/js/**", 
                    "/register", 
                    "/login", 
                    "/forgot-password",
                    "/reset-password",
                    "/verify"   
                ).permitAll()
                .requestMatchers("/admin/**").hasAuthority("Admin")
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .usernameParameter("username")
                .successHandler(successHandler)
                //.defaultSuccessUrl("/dashboard", true)
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout")
                .permitAll()
            );

        return http.build();
    }
}
