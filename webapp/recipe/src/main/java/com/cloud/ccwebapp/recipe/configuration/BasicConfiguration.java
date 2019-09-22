package com.cloud.ccwebapp.recipe.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.sql.DataSource;

@Configuration
@EnableWebSecurity
public class BasicConfiguration extends WebSecurityConfigurerAdapter {


    @Autowired
    private DataSource dataSource;

    @Bean
    @ConfigurationProperties("spring.datasource")
    public  DataSource dataSource(){
        return DataSourceBuilder.create().build();
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth)
            throws Exception {
            auth
                   .jdbcAuthentication().dataSource(dataSource)
                   .authoritiesByUsernameQuery("select email_address from user_table where email_address=?")
                   .usersByUsernameQuery("select email_address , password from user_table where email_address=?");
//                .inMemoryAuthentication()
//                .withUser("user")
//                .password("{noop}qwerty")
//                .roles("USER");

    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .csrf().disable()
                .authorizeRequests()
                .anyRequest()
                .authenticated()
                .and()
                .httpBasic();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


}
