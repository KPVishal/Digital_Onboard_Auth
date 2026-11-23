package com.bijlipay.Digital_Onboard_Authentication.model;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "otp_Mobile_no")
public class OtpFactorPhno {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "otp_id", nullable = false)
    private long id;

    @Column(name = "mobile_no", nullable = false)
    private long mobileNo;

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

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getMobileNo() {
        return mobileNo;
    }

    public void setMobileNo(long mobileNo) {
        this.mobileNo = mobileNo;
    }

    public String getOtp() {
        return otp;
    }

    public void setOtp(String otp) {
        this.otp = otp;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public String getRequest() {
        return request;
    }

    public void setRequest(String request) {
        this.request = request;
    }

    public Date getUpdated_at() {
        return updated_at;
    }

    public void setUpdated_at(Date updated_at) {
        this.updated_at = updated_at;
    }

    public int getAttempt() {
        return attempt;
    }

    public void setAttempt(int attempt) {
        this.attempt = attempt;
    }

    @Override
    public String toString() {
        return "OtpFactorPhno{" +
                "id=" + id +
                ", mobileNo=" + mobileNo +
                ", otp='" + otp + '\'' +
                ", count=" + count +
                ", isActive=" + isActive +
                ", status=" + status +
                ", attempt=" + attempt +
                ", request='" + request + '\'' +
                ", updated_at=" + updated_at +
                '}';
    }
}


