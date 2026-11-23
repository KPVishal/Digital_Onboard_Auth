package com.bijlipay.Digital_Onboard_Authentication.repository;

import com.bijlipay.Digital_Onboard_Authentication.model.RegionGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RegionGroupRepository  extends JpaRepository<RegionGroup,Integer> {
}
