package com.bijlipay.Digital_Onboard_Authentication.dto;

public class PasswordRequest {
    private String password;

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return "PasswordRequest{" + "password='" + password + '\'' + '}';
    }
}
