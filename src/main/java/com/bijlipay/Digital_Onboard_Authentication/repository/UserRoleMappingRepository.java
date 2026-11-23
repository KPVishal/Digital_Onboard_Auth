package com.bijlipay.Digital_Onboard_Authentication.repository;

import com.bijlipay.Digital_Onboard_Authentication.model.User;
import com.bijlipay.Digital_Onboard_Authentication.model.UserRoleMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRoleMappingRepository  extends JpaRepository<UserRoleMapping, Long> {

    @Query(value = " SELECT * FROM user_role_mapping WHERE user_id=?1 LIMIT 1", nativeQuery = true)
    UserRoleMapping findBySessionUser(User user);
}
