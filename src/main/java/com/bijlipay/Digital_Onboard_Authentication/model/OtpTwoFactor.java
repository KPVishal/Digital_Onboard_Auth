package com.bijlipay.Digital_Onboard_Authentication.model;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "otp")
@Setter
@Getter
public class OtpTwoFactor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "otp_id", nullable = false)
    private long id;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "otp")
    private String otp;

    @Column(name = "count")
    private int count;

    @Column(name = "is_active")
    private boolean isActive;

    @Column(name = "status")
    private boolean status;
    @Column(name = "attempt",columnDefinition = "int(11) default 0")
    private int attempt;

    @Column(name = "request")
    private String request;

    @UpdateTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "updated_at")
    private Date updated_at;


}
