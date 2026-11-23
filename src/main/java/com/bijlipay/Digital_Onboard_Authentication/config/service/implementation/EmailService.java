package com.bijlipay.Digital_Onboard_Authentication.config.service.implementation;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.internet.MimeMessage;

@Service
public class EmailService {



    @Autowired
    @Qualifier("otpMailSender")
    private JavaMailSender otpMailSender;


    public void sendOtpEmail(String content, String recipientEmail) throws Exception {
        sendEmail(content, recipientEmail, "bijlipayotp@bijlipay.co.in", "OTP VERIFY", otpMailSender);
    }

//    public void sendReportEmail(String content, String recipientEmail) throws Exception {
//        sendEmail(content, recipientEmail, "report@gmail.com", "Monthly Report", reportMailSender);
//    }

    private void sendEmail(String content, String recipientEmail, String fromEmail, String subject, JavaMailSender mailSender) throws Exception {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setFrom(fromEmail);
        helper.setTo(recipientEmail);
        helper.setSubject(subject);
        helper.setText(content, true);
        mailSender.send(message);
    }
}
