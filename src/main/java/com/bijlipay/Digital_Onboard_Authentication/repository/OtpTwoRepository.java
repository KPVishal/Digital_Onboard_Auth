package com.bijlipay.Digital_Onboard_Authentication.repository;

import com.bijlipay.Digital_Onboard_Authentication.model.OtpTwoFactor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface OtpTwoRepository extends JpaRepository<OtpTwoFactor, Long> {

    @Query("select o from OtpTwoFactor o where o.email= ?1")
    OtpTwoFactor findEmail(String mail);
}
