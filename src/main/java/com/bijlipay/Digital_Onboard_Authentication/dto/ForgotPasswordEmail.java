package com.bijlipay.Digital_Onboard_Authentication.dto;

public class ForgotPasswordEmail extends BaseEmail{
    private String passwordToken;

    public String getPasswordToken() {
        return passwordToken;
    }

    public void setPasswordToken(String passwordToken) {
        this.passwordToken = passwordToken;
    }
}
