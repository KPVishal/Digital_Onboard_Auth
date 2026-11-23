package com.bijlipay.Digital_Onboard_Authentication.config.service;

import com.bijlipay.Digital_Onboard_Authentication.model.OtpTwoFactor;
import com.bijlipay.Digital_Onboard_Authentication.model.User;
import com.bijlipay.Digital_Onboard_Authentication.model.UserRoleMapping;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface UserService extends UserDetailsService {

    User findByEmail(String email);

    void updateDeviceToken(User sessionUser, String deviceToken);

    UserRoleMapping getUserRoleMappingOfUserForLogin(User userData);

    User get(long emailId);

    User saveWithPassword(User user);

    User save(User user);

}
