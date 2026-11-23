package com.bijlipay.Digital_Onboard_Authentication.repository;

import com.bijlipay.Digital_Onboard_Authentication.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleRepository extends JpaRepository<Role,Integer> {
}
