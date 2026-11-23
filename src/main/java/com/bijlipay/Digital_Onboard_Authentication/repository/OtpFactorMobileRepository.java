package com.bijlipay.Digital_Onboard_Authentication.repository;

import com.bijlipay.Digital_Onboard_Authentication.model.OtpFactorPhno;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface OtpFactorMobileRepository extends JpaRepository<OtpFactorPhno,Long> {
    @Query("select m from OtpFactorPhno m where m.mobileNo=?1")
    OtpFactorPhno findPhNo(long phno);
}
