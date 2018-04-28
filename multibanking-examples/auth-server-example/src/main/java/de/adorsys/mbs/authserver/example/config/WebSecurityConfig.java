package de.adorsys.mbs.authserver.example.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .csrf().disable()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .authorizeRequests()
                .antMatchers("/management/health").permitAll()
                .antMatchers("/management/info").permitAll()
                .antMatchers("/management/**").hasAuthority("admin")
                .antMatchers("/").permitAll()
                .antMatchers("/pop").permitAll()
                .antMatchers("/api-docs/**").permitAll()
                .antMatchers("/v2/api-docs/**").permitAll()
                .antMatchers("/token/password-grant").permitAll()
                .antMatchers("/**").authenticated()
                .anyRequest().denyAll();
    }
}
