package com.bijlipay.Digital_Onboard_Authentication.config;

import com.bijlipay.Digital_Onboard_Authentication.security.JwtAuthenticationEntryPoint;
import com.bijlipay.Digital_Onboard_Authentication.security.SecurityFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.web.config.EnableSpringDataWebSupport;

import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.filter.CommonsRequestLoggingFilter;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;


import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
@EnableWebMvc
@EnableSpringDataWebSupport
@Configuration
@ComponentScan("com.bijlipay.Digital_Onboard_Authentication")
public class WebConfig extends WebMvcConfigurerAdapter {



}
