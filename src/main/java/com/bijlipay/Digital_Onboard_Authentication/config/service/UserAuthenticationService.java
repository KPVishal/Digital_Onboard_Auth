package com.bijlipay.Digital_Onboard_Authentication.config.service;

import com.bijlipay.Digital_Onboard_Authentication.model.User;
import com.bijlipay.Digital_Onboard_Authentication.model.UserAuthentication;

public interface UserAuthenticationService {

    UserAuthentication getUser(long id);

    void insertdata(String jwtToken, User userData);

    void update(String token, Long id);

    void insertData(String token, User userData);
}
