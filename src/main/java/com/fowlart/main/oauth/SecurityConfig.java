package com.fowlart.main.oauth;

import com.azure.spring.aad.webapp.AADWebSecurityConfigurerAdapter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig extends AADWebSecurityConfigurerAdapter {
    @Value("${app.protect.authenticated}")
    private String[] protectedRoutes;

    @Override
    public void configure(HttpSecurity http) throws Exception {
        // use required configuration from AADWebSecurityAdapter.configure:
        super.configure(http);

        http.authorizeRequests()
                .antMatchers(protectedRoutes)
                .authenticated()     // limit these pages to authenticated users (default: /token_details)
                .antMatchers("/**")
                .permitAll();

        http.csrf().disable();
    }
}
