package com.bijlipay.Digital_Onboard_Authentication.config;

public final class Constants {

    public static final String ADMIN_ROLE = "Bjilipay Manger";

    // FORGOT PASSWORD MAIL LINK
    public static final String FORGOT_PASSWORD_MAIL_STATIC_IP = "https://smart.bijlipay.co.in/web/#/passwordRecovery/";

    //     public static final String FORGOT_PASSWORD_MAIL_STATIC_IP = "https://uatapp2.bijlipay.co.in/digital/#/passwordRecovery/";
    public interface AnalyticsInterface {
        String USER_LOGGED_IN = "Logged In";
    }

    //otp verify
    public static final int TOTALVERIFYTIME =20;
    public static final int TOTALATTEMPTS =3;
    public static final int OTPRETRIGGER =3;
}
