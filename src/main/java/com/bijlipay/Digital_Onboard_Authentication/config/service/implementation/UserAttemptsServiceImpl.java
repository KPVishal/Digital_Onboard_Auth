package com.bijlipay.Digital_Onboard_Authentication.config.service.implementation;

import com.bijlipay.Digital_Onboard_Authentication.config.service.UserAttemptsService;
import com.bijlipay.Digital_Onboard_Authentication.model.UserAttempts;
import com.bijlipay.Digital_Onboard_Authentication.repository.UserAttemptsRepos;
import com.bijlipay.Digital_Onboard_Authentication.repository.UserAuthenticationRepo;
import com.bijlipay.Digital_Onboard_Authentication.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class UserAttemptsServiceImpl implements UserAttemptsService {

    @Value("${max.pass.attemptes}")
    private int maxAttemptes;

    @Autowired
    private UserAttemptsRepos userAttemptsRepos;


    @Autowired
    private UserAuthenticationRepo userAuthenticateRepo;

    @Autowired
    private UserRepository userRepository;

    @Override
    public boolean getlockedAccount(String email) {
        return false;
    }

    @Override
    public void resetFailAttempts(String username) {

    }

    @Override
    public void updateFailAttempts(String username) {
        UserAttempts user = getUserAttempts(username);
        if (user == null) {
            if (isUserExists(username)) {
                System.out.println(" if");
                // if no record, insert a new
                UserAttempts userAttempts = new UserAttempts();
                userAttempts.setAttempts(1);
                userAttempts.setUsername(username);
                userAttempts.setLastModified(new Date());
                userAttemptsRepos.save(userAttempts);
            }
        } else {

            if (isUserExists(username)) {
                // update attempts count, +1
                System.out.println("else if");
                userAttemptsRepos.updateAttempts(new Date(), username);
            }

            if (user.getAttempts() + 1 >= maxAttemptes) {
                // locked user
                System.out.println("only if");
                userRepository.lockUser(username);
                // throw exception
//                throw new LockedException("User Account is locked!");
            }

        }
    }

    private UserAttempts getUserAttempts(String username) {
        try {
            UserAttempts userAttempts = userAttemptsRepos.getUserAttempts(username);
            return userAttempts;

        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    private boolean isUserExists(String username) {

        boolean result = false;

        int count = userAttemptsRepos.getUserCount(username);
        if (count > 0) {
            result = true;
        }

        return result;
    }


}
