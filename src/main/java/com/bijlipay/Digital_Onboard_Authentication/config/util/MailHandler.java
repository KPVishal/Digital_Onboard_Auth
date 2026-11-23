package com.bijlipay.Digital_Onboard_Authentication.config.util;

import com.bijlipay.Digital_Onboard_Authentication.dto.ForgotPasswordEmail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import javax.mail.internet.MimeMessage;

import java.net.URLEncoder;

import static com.bijlipay.Digital_Onboard_Authentication.config.Constants.FORGOT_PASSWORD_MAIL_STATIC_IP;


@Component
public class MailHandler {

    @Autowired
    private JavaMailSender sender;

    public void sendResetPasswordEmail(ForgotPasswordEmail model) throws Exception {
        MimeMessage message= sender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);
        String mailTemplate = "Hi %s,\n Please click the below link or copy and paste in the web browser\n" + FORGOT_PASSWORD_MAIL_STATIC_IP + "%s";
        //String mailTemplate = "Hi %s,\n Please click the below link or copy and paste in the web browser\n"+FORGOT_PASSWORD_MAIL_STATIC_IP+"%s";
        helper.setFrom("bijlipayotp@bijlipay.co.in");
        helper.setTo(model.getReceiverEmail());
        helper.setText(String.format(mailTemplate, model.getReceiverName(), URLEncoder.encode(model.getPasswordToken(), "UTF-8")));
        helper.setSubject(model.getSubject());

        sender.send(message);
    }

    public void merchantOtpSend(String templete,String email) throws Exception {
        MimeMessage message= sender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);
        helper.setFrom("reports@bijlipay.co.in");
        helper.setTo(email);
        helper.setText(templete);
        helper.setSubject("OTP VERIFY");
        sender.send(message);
    }
}
