package com.bijlipay.Digital_Onboard_Authentication.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.core.env.Environment;

import java.util.Properties;

@Configuration
public class MailConfig {

    @Autowired
    private Environment env;

    @Bean(name = "otpMailSender")
    public JavaMailSender getOtpMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(env.getProperty("spring.mail.otp.host"));
        mailSender.setPort(Integer.parseInt(env.getProperty("spring.mail.otp.port")));
        mailSender.setUsername(env.getProperty("spring.mail.otp.username"));
        mailSender.setPassword(env.getProperty("spring.mail.otp.password"));

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.smtp.auth", env.getProperty("spring.mail.otp.properties.mail.smtp.auth"));
        props.put("mail.smtp.starttls.enable", env.getProperty("spring.mail.otp.properties.mail.smtp.starttls.enable"));
        props.put("mail.smtp.starttls.required", env.getProperty("spring.mail.otp.properties.mail.smtp.starttls.required"));

        return mailSender;
    }
}
