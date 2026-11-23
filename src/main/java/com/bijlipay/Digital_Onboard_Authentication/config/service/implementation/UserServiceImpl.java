package com.bijlipay.Digital_Onboard_Authentication.config.service.implementation;

import com.bijlipay.Digital_Onboard_Authentication.config.service.UserService;
import com.bijlipay.Digital_Onboard_Authentication.model.OtpTwoFactor;
import com.bijlipay.Digital_Onboard_Authentication.model.User;
import com.bijlipay.Digital_Onboard_Authentication.model.UserRoleMapping;
import com.bijlipay.Digital_Onboard_Authentication.repository.UserRepository;
import com.bijlipay.Digital_Onboard_Authentication.repository.UserRoleMappingRepository;
import com.bijlipay.Digital_Onboard_Authentication.response.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service(value = "userService")
public class UserServiceImpl implements UserService, UserDetailsService {

    private Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);


    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserRoleMappingRepository userRoleMappingRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        logger.info("UserName  {}", username);
        User user = findByUsername(username);
        logger.info("User  {}", user);
        if (user == null)
            throw new UsernameNotFoundException("User not exist");
        //read user(from db)
        return new org.springframework.security.core.userdetails.User(
                username,
                user.getPassword(),
                user.getRole().stream()
                        .map(role -> new SimpleGrantedAuthority(role))
                        .collect(Collectors.toList()));

    }

    public User findByUsername(String username) {
        return userRepository.findUsername(username);
    }


    @Override
    public User findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public void updateDeviceToken(User sessionUser, String deviceToken) {
        userRepository.updateDeviceToken(sessionUser.getId(), deviceToken);
    }
    @Override
    public UserRoleMapping getUserRoleMappingOfUserForLogin(User userData) {
        return userRoleMappingRepository.findBySessionUser(userData);
    }

    @Override
    public User get(long email) {
        return userRepository.findByUserId(email);
    }

    @Override
    public User saveWithPassword(User user) {
        //user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
        return save(user);
    }

    @Override
    public User save(User user) {
        user.setActive(true);
        user.setAccountNonExpired(true);
        user.setAccountNonLocked(true);
        user.setCredentialsNonExpired(true);
        logger.info("saving user details -- :{} ", user);
        return userRepository.save(user);
    }
}
