package com.bijlipay.Digital_Onboard_Authentication.dto;

public class PasswordToken {
    private long userId;
//    private String email;
    private int oneTimePassword;
    private boolean used;

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

//    public String getEmail() {
//        return email;
//    }
//
//    public void setEmail(String email) {
//        this.email = email;
//    }

    public int getOneTimePassword() {
        return oneTimePassword;
    }

    public void setOneTimePassword(int oneTimePassword) {
        this.oneTimePassword = oneTimePassword;
    }

    public boolean isUsed() {
        return used;
    }

    public void setUsed(boolean used) {
        this.used = used;
    }
}
