package com.bijlipay.Digital_Onboard_Authentication.config.service.implementation;

import com.bijlipay.Digital_Onboard_Authentication.config.service.UserAuthenticationService;
import com.bijlipay.Digital_Onboard_Authentication.model.User;
import com.bijlipay.Digital_Onboard_Authentication.model.UserAuthentication;
import com.bijlipay.Digital_Onboard_Authentication.repository.UserAuthenticationRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserAuthenticationServiceImp implements UserAuthenticationService {

    @Autowired
    private UserAuthenticationRepo userAuthenticateRepo;

    @Override
    public UserAuthentication getUser(long id) {
        UserAuthentication username = userAuthenticateRepo.getuser(id);
        return username;
    }

    @Override
    public void insertdata(String jwtToken, User userData) {
        UserAuthentication userAuthentication = new UserAuthentication();
        userAuthentication.setToken(jwtToken);
        userAuthentication.setUser(userData);
        userAuthenticateRepo.save(userAuthentication);
    }

    @Override
    public void update(String jwtToken, Long id) {
        userAuthenticateRepo.update(jwtToken, id);
    }

    @Override
    public void insertData(String token, User currentuser) {
        UserAuthentication userAuthentication = new UserAuthentication();
        userAuthentication.setToken(token);
        userAuthentication.setUser(currentuser);
        userAuthenticateRepo.save(userAuthentication);
    }

}
