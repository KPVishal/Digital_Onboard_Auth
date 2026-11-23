package com.bijlipay.Digital_Onboard_Authentication.controller;

import com.bijlipay.Digital_Onboard_Authentication.model.User;
import com.bijlipay.Digital_Onboard_Authentication.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;

public class BaseController {

    private static final Logger LOGGER = LoggerFactory.getLogger(BaseController.class);

    @Autowired
    private UserRepository userRepository;

    public User getSessionUser() {
        String user = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        LOGGER.info("The session user ===========user :{}",user);
        return userRepository.findSessionUser(user);
    }
}
