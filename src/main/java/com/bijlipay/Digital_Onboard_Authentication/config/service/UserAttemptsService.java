package com.bijlipay.Digital_Onboard_Authentication.config.service;

public interface UserAttemptsService {


    boolean getlockedAccount(String email);

    void resetFailAttempts(String username);

    void updateFailAttempts(String username);
}
