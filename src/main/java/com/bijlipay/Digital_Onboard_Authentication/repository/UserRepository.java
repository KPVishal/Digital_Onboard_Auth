package com.bijlipay.Digital_Onboard_Authentication.repository;

import com.bijlipay.Digital_Onboard_Authentication.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {


    @Query(value="Select * from user u  where u.email = ?1 order by  u.user_id desc limit 1",nativeQuery = true)
    User findUsername(String username);

    @Query(value="Select u from User u where u.email=?1")
    User findByEmail(String email);

    @Transactional
    @Modifying
    @Query(value = "UPDATE user SET is_account_non_locked =0 WHERE email = ?1", nativeQuery = true)
    void lockUser(String username);

    @Transactional
    @Modifying
    @Query("UPDATE User SET deviceToken =?2 WHERE id=?1")
    void updateDeviceToken(Long sessionUser, String deviceToken);

    @Query(value="Select * from user u  where u.email = ?1 order by  u.user_id desc limit 1",nativeQuery = true)
    User findSessionUser(String username);

    @Query("SELECT u FROM User u WHERE u.id=?1")
    User findByUserId(long id);
}
