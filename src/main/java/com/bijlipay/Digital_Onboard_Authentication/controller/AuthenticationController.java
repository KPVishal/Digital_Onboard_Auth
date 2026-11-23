package com.bijlipay.Digital_Onboard_Authentication.controller;

import com.bijlipay.Digital_Onboard_Authentication.config.Constants;
import com.bijlipay.Digital_Onboard_Authentication.config.service.*;
import com.bijlipay.Digital_Onboard_Authentication.config.service.implementation.EmailService;
import com.bijlipay.Digital_Onboard_Authentication.config.util.AesUtil;
import com.bijlipay.Digital_Onboard_Authentication.config.util.DateUtil;
import com.bijlipay.Digital_Onboard_Authentication.config.util.MailHandler;
import com.bijlipay.Digital_Onboard_Authentication.dto.*;
import com.bijlipay.Digital_Onboard_Authentication.model.*;
import com.bijlipay.Digital_Onboard_Authentication.repository.*;
import com.bijlipay.Digital_Onboard_Authentication.response.ApiResponse;
import com.bijlipay.Digital_Onboard_Authentication.security.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import javax.inject.Qualifier;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.bijlipay.Digital_Onboard_Authentication.config.Constants.*;

@RequestMapping("/authentication")
@RestController
public class AuthenticationController extends BaseController {

    @Value("${opt.url}")
    private String otpUrl;
    @Autowired
    private UserService userService;

    @Autowired
    private RoleService roleService;

    @Autowired
    private RegionService regionService;

    @Autowired
    private HierarchyService hierarchyService;

    @Autowired
    private LeadSourceService leadSourceService;

    @Autowired
    private ServiceRequestClientsService serviceReqClientsService;

    @Autowired
    private UserRoleMappingRepository userRoleMappingRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private RegionRepository regionRepository;

    @Autowired
    private HierarchyRepository hierarchyRepository;

    @Autowired
    private RegionGroupRepository regionGroupRepository;

    @Autowired
    private UserAttemptsService userAttemptsService;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserAuthenticationService userAuthenticateService;

    @Autowired
    private UserAnalyticsService userAnalyticsService;
    @Autowired
    private OtpTwoRepository otpTwoRepository;

    @Autowired
    private JwtUtil jwtGenerator;
    @Autowired
    private MailHandler mailHandler;

    @Autowired
    private OtpFactorMobileRepository otpFactorMobileRepository;
    @Autowired
    private EmailService emailService;

    private static Logger logger = LoggerFactory.getLogger(AuthenticationController.class);


    @PostMapping("/login")
    public ResponseEntity<?> doLogin(@RequestBody LoginRequest loginRequest) {

        //================Decryption====================
        String decryptedPassword = new String(java.util.Base64.getDecoder().decode(loginRequest.getPassword()));
        AesUtil aesUtil = new AesUtil(128, 1000);
        String plainText = null;

        if (decryptedPassword != null && decryptedPassword.split("::").length == 3) {
            plainText = aesUtil.decrypt(decryptedPassword.split("::")[1], decryptedPassword.split("::")[0], "BijliWeAreMakers", decryptedPassword.split("::")[2]);
        }
        //=====================================================
        String token = null;
        User userData = userService.findByEmail(loginRequest.getEmail());
        logger.info("UserData===  {}", userData);
        if (userAttemptsService.getlockedAccount(loginRequest.getEmail())) {
            return new ResponseEntity<>(new ApiResponse(HttpStatus.LOCKED, "Account Locked"), HttpStatus.LOCKED);
        }
        if (userData != null && bCryptPasswordEncoder.matches(plainText, userData.getPassword())) {
            if (!userData.isActive())
                return new ResponseEntity<>(new ApiResponse(HttpStatus.BAD_REQUEST, "User not found!"), HttpStatus.BAD_REQUEST);

            Date loginAt = DateUtil.currentDate();
            UserAuthentication username = userAuthenticateService.getUser(userData.getId());
            userAttemptsService.resetFailAttempts(loginRequest.getEmail());

            token = jwtGenerator.generateToken(userData.getEmail());
            // token = jwtGenerator.generate(userData.getName());


//======================================JWT Token update with multiple user===========================================
            UserAuthentication authenticateduser = userAuthenticateService.getUser(userData.getId());
            if (authenticateduser == null) {
                userAuthenticateService.insertData(token, userData);
                logger.info("token inserted ====");
            } else {
                authenticateduser.getId();
                userAuthenticateService.update(token, authenticateduser.getId());
            }


//=====================================================================================================================
            HashMap<String, Object> result = new HashMap<>();
            result.put("jwt", token);
            result.put("mobileNumber", userData.getContactNumber());
            UserAnalytics whenLogsIn = new UserAnalytics(Constants.AnalyticsInterface.USER_LOGGED_IN, userData);
            userAnalyticsService.add(whenLogsIn);

            // ======== otp part =============
            UserRoleMapping userRoleMapping = userService.getUserRoleMappingOfUserForLogin(userData);
            logger.info("SO User Role ----{}", userRoleMapping.getRole().getRole());
            if (!(Constants.ADMIN_ROLE.equals(userRoleMapping.getRole().getRole()))) {
                HttpHeaders headers = new HttpHeaders();
                headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
                HttpEntity<?> entity = new HttpEntity<>(headers);
                ResponseEntity<ApiResponse> exchange = null;
                try {
                    exchange = new RestTemplate().exchange(
                            otpUrl + "/otp/otpinput?mblNo={mob}&cmd=generate otp",
                            HttpMethod.GET,
                            entity,
                            ApiResponse.class, userData.getContactNumber());
                    System.out.println("OTP SENT");
                    logger.info("OTP Sent Successfully----{}", userData.getContactNumber());
                } catch (final HttpClientErrorException httpClientErrorException) {
                    String resp = httpClientErrorException.getResponseBodyAsString();
                    ObjectMapper objectMapper = new ObjectMapper();
                    try {
                        ApiResponse obj = objectMapper.readValue(resp, ApiResponse.class);
                        return new ResponseEntity<>(new ApiResponse(HttpStatus.BAD_REQUEST, obj.getMessage()),
                                HttpStatus.BAD_REQUEST);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    System.out.println(resp);
                } catch (HttpServerErrorException httpServerErrorException) {
                    String resp = httpServerErrorException.getResponseBodyAsString();
                    ObjectMapper objectMapper = new ObjectMapper();
                    try {
                        ApiResponse obj = objectMapper.readValue(resp, ApiResponse.class);
                        return new ResponseEntity<>(new ApiResponse(HttpStatus.INTERNAL_SERVER_ERROR, obj.getMessage()),
                                HttpStatus.INTERNAL_SERVER_ERROR);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } catch (Exception exception) {
                    exception.getMessage();
                }
            }

           /* if (username == null) {
                userAuthenticateService.insertdata(token, userData);
            } else {
                username.getId();
                userAuthenticateService.update(token, username.getId());
            }*/
            return ResponseEntity.ok(result);

        }

        userAttemptsService.updateFailAttempts(loginRequest.getEmail());
        return new ResponseEntity<>(new ApiResponse(HttpStatus.UNAUTHORIZED, "Incorrect email-ID/plainText"), HttpStatus.UNAUTHORIZED);
    }


    @GetMapping("/validate")
    public String validateToken(@RequestParam("token") String token) {
        jwtGenerator.validateToken(token, jwtGenerator.getUsername(token));
        // boolean result = jwtGenerator.validate(token);
        //if (result) return "Token is valid";
        return "Token is invalid";
    }


    @PostMapping("/androidLogin")
    public ResponseEntity<?> doAndroidLogin(@RequestBody LoginRequest loginRequest) {

        //================Decryption====================
        String decryptedString = AesUtil.androidDecrypt(loginRequest.getPassword());
        //=====================================================
        String token = null;
        User userData = userService.findByEmail(loginRequest.getEmail());
        logger.info("UserData===  {}", userData);
        if (userAttemptsService.getlockedAccount(loginRequest.getEmail())) {
            return new ResponseEntity<>(new ApiResponse(HttpStatus.LOCKED, "Account Locked"), HttpStatus.LOCKED);
        }
        if (userData != null && bCryptPasswordEncoder.matches(decryptedString, userData.getPassword())) {
            if (!userData.isActive())
                return new ResponseEntity<>(new ApiResponse(HttpStatus.BAD_REQUEST, "User not found!"), HttpStatus.BAD_REQUEST);

            Date loginAt = DateUtil.currentDate();
            UserAuthentication username = userAuthenticateService.getUser(userData.getId());
            userAttemptsService.resetFailAttempts(loginRequest.getEmail());

            token = jwtGenerator.generateToken(userData.getEmail());
            // token = jwtGenerator.generate(userData.getName());

            HashMap<String, Object> result = new HashMap<>();
            result.put("jwt", token);
            result.put("mobileNumber", userData.getContactNumber());
            UserAnalytics whenLogsIn = new UserAnalytics(Constants.AnalyticsInterface.USER_LOGGED_IN, userData);
            userAnalyticsService.add(whenLogsIn);

            // ======== otp part =============
            UserRoleMapping userRoleMapping = userService.getUserRoleMappingOfUserForLogin(userData);
            logger.info("SO User Role ----{}", userRoleMapping.getRole().getRole());
            if (!(Constants.ADMIN_ROLE.equals(userRoleMapping.getRole().getRole()))) {
                HttpHeaders headers = new HttpHeaders();
                headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
                HttpEntity<?> entity = new HttpEntity<>(headers);
                ResponseEntity<ApiResponse> exchange = null;
                try {
                    exchange = new RestTemplate().exchange(
                            otpUrl + "/otp/otpinput?mblNo={mob}&cmd=generate otp",
                            HttpMethod.GET,
                            entity,
                            ApiResponse.class, userData.getContactNumber());
                    System.out.println("OTP SENT");
                    logger.info("OTP Sent Successfully----{}", userData.getContactNumber());
                } catch (final HttpClientErrorException httpClientErrorException) {
                    String resp = httpClientErrorException.getResponseBodyAsString();
                    ObjectMapper objectMapper = new ObjectMapper();
                    try {
                        ApiResponse obj = objectMapper.readValue(resp, ApiResponse.class);
                        return new ResponseEntity<>(new ApiResponse(HttpStatus.BAD_REQUEST, obj.getMessage()),
                                HttpStatus.BAD_REQUEST);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    System.out.println(resp);
                } catch (HttpServerErrorException httpServerErrorException) {
                    String resp = httpServerErrorException.getResponseBodyAsString();
                    ObjectMapper objectMapper = new ObjectMapper();
                    try {
                        ApiResponse obj = objectMapper.readValue(resp, ApiResponse.class);
                        return new ResponseEntity<>(new ApiResponse(HttpStatus.INTERNAL_SERVER_ERROR, obj.getMessage()),
                                HttpStatus.INTERNAL_SERVER_ERROR);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } catch (Exception exception) {
                    exception.getMessage();
                }
            }

            if (username == null) {
                userAuthenticateService.insertdata(token, userData);
            } else {
                username.getId();
                userAuthenticateService.update(token, username.getId());
            }
            return ResponseEntity.ok(result);

        }

        userAttemptsService.updateFailAttempts(loginRequest.getEmail());
        return new ResponseEntity<>(new ApiResponse(HttpStatus.UNAUTHORIZED, "Incorrect email-ID/plainText"), HttpStatus.UNAUTHORIZED);
    }

    @PutMapping("update-device-token")
    public ResponseEntity getUserProfile(@RequestHeader(value = "X-DEVICE-TOKEN") String deviceToken) {
        userService.updateDeviceToken(getSessionUser(), deviceToken);
        return new ResponseEntity<>(new ApiResponse(HttpStatus.OK, "success"), HttpStatus.OK);
    }

    @PostMapping("/forgot/password")
    public ResponseEntity<?> doResetPassword(@RequestBody ForgetPasswordRequest request) throws Exception {
        User user = userService.findByEmail(request.getEmail());
        if (user != null) {
            if (!user.isActive())
                return new ResponseEntity<>(new ApiResponse(HttpStatus.BAD_REQUEST, "User not found!"), HttpStatus.BAD_REQUEST);
            int randomNumber = new Random().nextInt(6) + 1;
            PasswordToken token = new PasswordToken();
            token.setOneTimePassword(randomNumber);
            token.setUserId(user.getId());
            String passwordToken = jwtGenerator.generatePasswordToken(token);
            logger.info("password Token--{}",passwordToken);
            //user.setForgetPasswordToken(bCryptPasswordEncoder.encode(passwordToken));
            ForgotPasswordEmail model = new ForgotPasswordEmail();
            model.setReceiverEmail(user.getEmail());
            model.setReceiverName(user.getName());
            model.setPasswordToken(passwordToken);
            model.setSubject("Forgot | Password");
            mailHandler.sendResetPasswordEmail(model);
            //userService.saveWithPassword(user);
            return new ResponseEntity<>(new ApiResponse(HttpStatus.OK, "success"), HttpStatus.OK);
        } else {
            throw new UserNotFoundException("email-ID not found!");
        }
    }

    /**
     * change password
     */
    @PostMapping("/password")
    public ResponseEntity<?> doUpdatePassword(@RequestHeader("token") String token, @RequestBody PasswordRequest request) throws UnsupportedEncodingException {
        PasswordToken passwordToken = jwtGenerator.validatePasswordToken(token);
        User user = userService.get(passwordToken.getUserId());
        logger.info("user -- : {}", user);
        //if (user!=null && bCryptPasswordEncoder.matches(token, user.getForgetPasswordToken())){
        if (user != null) {
            if (bCryptPasswordEncoder.matches(request.getPassword(), user.getPassword())) {
                return new ResponseEntity<>(new ApiResponse(HttpStatus.BAD_REQUEST, "New Password cannot be same as Old Password"), HttpStatus.BAD_REQUEST);
            }
            //user.setPassword(request.getPassword());
            user.setPassword(bCryptPasswordEncoder.encode(request.getPassword()));
            userService.saveWithPassword(user);
            return new ResponseEntity<>(new ApiResponse(HttpStatus.OK, "success"), HttpStatus.OK);
        } else {
            //throw new UserNotFoundException("User not found!");
            return new ResponseEntity<>(new ApiResponse(HttpStatus.NO_CONTENT, "User not found!"), HttpStatus.NOT_FOUND);
        }
    }

    public class UserNotFoundException extends UsernameNotFoundException {
        public UserNotFoundException(String msg) {
            super(msg);
        }
    }

    @PostMapping("/mail-merchant-otp/{cmd}/{email}")
    public ResponseEntity<?> merchantOtp(HttpServletRequest request, @PathVariable("cmd") String cmd, @PathVariable("email") String email) throws Exception {

        Random r = new Random();
        List<Integer> codes = new ArrayList();

        int x;
        for (int i = 0; i < 10; ++i) {
            for (x = r.nextInt(900000) + 100000;
                 codes.contains(x);
                 x = r.nextInt(999999)) {
            }

            codes.add(x);
        }


        logger.info("--email--{}", email);
        String str = String.format("%06d", codes.get(0));
        OtpTwoFactor otpTwoFactor = otpTwoRepository.findEmail(email);
        if (otpTwoFactor == null) {
            OtpTwoFactor otp = new OtpTwoFactor();
            x = otp.getCount() + 1;
            otp.setCount(x);
            otp.setOtp(str);
            otp.setRequest(cmd);
            otp.setStatus(true);
            otp.setEmail(email);
            otpTwoRepository.save(otp);
        } else {
            x = otpTwoFactor.getCount() + 1;
            otpTwoFactor.setCount(x);
            otpTwoFactor.setOtp(str);
            otpTwoFactor.setRequest(cmd);
            otpTwoFactor.setStatus(true);
            otpTwoFactor.setEmail(email);
            otpTwoRepository.save(otpTwoFactor);
        }
        logger.info("--email--{}", email);
        OtpTwoFactor otp = otpTwoRepository.findEmail(email);
        int var10002 = Integer.parseInt(otp.getOtp());
        String var15 = "OTP:" + var10002 + ". Please don't share this OTP and it will expire after 3 mins. - BIJLPY";
//        mailHandler.merchantOtpSeMail(var15, email);
        emailService.sendOtpEmail(var15,email);
        logger.info("SEND OTP : SMS sent successfully.!");
        return new ResponseEntity(new ApiResponse(HttpStatus.OK, "MAIL SENT to your " + email + ".!"), HttpStatus.OK);
    }

    @PostMapping("/sms-merchant-otp/{cmd}/{phno}")
    public ResponseEntity<?> merchantOtpPhno(HttpServletRequest request, @PathVariable("cmd") String cmd, @PathVariable("phno") long phno) throws Exception {

        Random r = new Random();
        List<Integer> codes = new ArrayList();

        int x;
        for (int i = 0; i < 10; ++i) {
            for (x = r.nextInt(900000) + 100000;
                 codes.contains(x);
                 x = r.nextInt(999999)) {
            }

            codes.add(x);
        }

        logger.info("--sms--{}", phno);
        String str = String.format("%06d", codes.get(0));
        LocalDateTime currentTime = LocalDateTime.now();
        long minutes = 0;
        OtpFactorPhno otpTwoFactor = otpFactorMobileRepository.findPhNo(phno);
        if (otpTwoFactor != null) {
            LocalDateTime updateTime = otpTwoFactor.getUpdated_at().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
            minutes = ChronoUnit.MINUTES.between(updateTime, currentTime);
        }
        if (otpTwoFactor == null) {
            OtpFactorPhno otp = new OtpFactorPhno();
            x = otp.getCount() + 1;
            otp.setCount(x);
            otp.setOtp(str);
            otp.setRequest(cmd);
            otp.setStatus(true);
            otp.setMobileNo(phno);
            otpFactorMobileRepository.save(otp);
        }else if (otpTwoFactor.getAttempt() == TOTALATTEMPTS) {
            int retry = (int) (TOTALVERIFYTIME - minutes);
            logger.info("verify minute --{}", minutes);
            if (minutes > TOTALVERIFYTIME) {
                x = otpTwoFactor.getCount() + 1;
                otpTwoFactor.setCount(x);
                otpTwoFactor.setOtp(str);
                otpTwoFactor.setRequest(cmd);
                otpTwoFactor.setStatus(true);
                otpTwoFactor.setMobileNo(phno);
                otpTwoFactor.setAttempt(0);
                otpFactorMobileRepository.save(otpTwoFactor);

            } else {
                return new ResponseEntity(new ApiResponse(HttpStatus.BAD_REQUEST, "to many request.Retry After " + retry + " minutes"), HttpStatus.BAD_REQUEST);
            }
        } else {
            if (minutes > OTPRETRIGGER) {
                x = otpTwoFactor.getCount() + 1;
                otpTwoFactor.setCount(x);
                otpTwoFactor.setOtp(str);
                otpTwoFactor.setRequest(cmd);
                otpTwoFactor.setStatus(true);
                otpTwoFactor.setMobileNo(phno);
                otpTwoFactor.setAttempt(0);
                otpFactorMobileRepository.save(otpTwoFactor);
            } else {
                int optRetry = (int) (OTPRETRIGGER - minutes);
                logger.info("OtpRetrigger--{}", optRetry);
                return new ResponseEntity(new ApiResponse(HttpStatus.BAD_REQUEST, "Retry After " + optRetry + " minutes"), HttpStatus.BAD_REQUEST);
            }
        }
        logger.info("--sms--{}",phno);
       OtpFactorPhno otpFactorPhno=otpFactorMobileRepository.findPhNo(phno);
        int var10002 = Integer.parseInt(otpFactorPhno.getOtp());
        String var15 = "OTP:" + var10002 + ". Please don't share this OTP and it will expire after 3 mins. - BIJLPY";
        String smsTemplate = "&msg=" + URLEncoder.encode(var15, StandardCharsets.UTF_8);
//        sms(smsTemplate, String.valueOf(phno));
        this.sms(var15, String.valueOf(phno),"1107161596105864246");
        logger.info("SEND OTP : SMS sent successfully.!");
        return new ResponseEntity(new ApiResponse(HttpStatus.OK, "sms SENT to your " + phno + ".!"), HttpStatus.OK);
    }
    @GetMapping({"/otpVerify/{email}/{otp}"})
    public ResponseEntity<?> enterOtp(@PathVariable("otp") String otpinpput, @PathVariable("email") String email) {
        try {
            OtpTwoFactor otp =otpTwoRepository.findEmail(email);
            logger.info("OTP MAIL : the api url : ---- : /otpsms/{}/{}", email, otpinpput);
            if (otp != null) {
                if (otpinpput.equals(otp.getOtp())) {
                    if (otp.isStatus()) {
                        long now = System.currentTimeMillis();
                        Long sent = otp.getUpdated_at().getTime();
                        long diffInMillies = now - sent;
                        if (TimeUnit.MINUTES.convert(diffInMillies, TimeUnit.MILLISECONDS) <= 3L) {
                            otp.setStatus(false);
                            otpTwoRepository.save(otp);
                            logger.info("OTP MAIL : OTP success.!");
                            return new ResponseEntity(new ApiResponse(HttpStatus.OK, "success"), HttpStatus.OK);
                        }

                        logger.info("OTP MAIL : OTP Expired.!");
                        return new ResponseEntity(new ApiResponse(HttpStatus.BAD_REQUEST, "OTP Expired.!"), HttpStatus.BAD_REQUEST);
                    }

                    logger.info("OTP MAIL : Please generate new OTP.!");
                    return new ResponseEntity(new ApiResponse(HttpStatus.BAD_REQUEST, "Please generate new OTP.!"), HttpStatus.BAD_REQUEST);
                }

                logger.info("OTP MAIL : Please enter a valid OTP.!");
                return new ResponseEntity(new ApiResponse(HttpStatus.BAD_REQUEST, "Please enter a valid OTP.!"), HttpStatus.BAD_REQUEST);
            }
        } catch (Exception var9) {
            var9.printStackTrace();
        }

        logger.info("OTP MAIL : Invalid input.!");
        return new ResponseEntity(new ApiResponse(HttpStatus.BAD_GATEWAY, "Invalid input.!"), HttpStatus.BAD_GATEWAY);
    }

    @GetMapping({"/otpVerifyPhno/{phno}/{otp}"})
    public ResponseEntity<?> enterOtpMobile(@PathVariable("otp") String otpinpput, @PathVariable("phno") long phno) {
        try {
            OtpFactorPhno otp =otpFactorMobileRepository.findPhNo(phno);
            logger.info("OTP SMS : the api url : ---- : /otpsms/{}/{}", phno, otpinpput);
            if (otp != null) {
                if(otp.getAttempt()!=3){
                    int attempt=otp.getAttempt();
                    attempt=attempt+1;
                    otp.setAttempt(attempt);
                    otpFactorMobileRepository.save(otp);
                if (otpinpput.equals(otp.getOtp())) {
                    if (otp.isStatus()) {
                        long now = System.currentTimeMillis();
                        Long sent = otp.getUpdated_at().getTime();
                        long diffInMillies = now - sent;
                        if (TimeUnit.MINUTES.convert(diffInMillies, TimeUnit.MILLISECONDS) <= 3L) {
                            otp.setStatus(false);
                            otpFactorMobileRepository.save(otp);
                            logger.info("OTP SMS : OTP success.!");
                            return new ResponseEntity(new ApiResponse(HttpStatus.OK, "success"), HttpStatus.OK);
                        }
                        logger.info("OTP SMS : OTP Expired.!");
                        return new ResponseEntity(new ApiResponse(HttpStatus.BAD_REQUEST, "OTP Expired.!"), HttpStatus.BAD_REQUEST);
                    }
                    logger.info("OTP SMS : Please generate new OTP.!");
                    return new ResponseEntity(new ApiResponse(HttpStatus.BAD_REQUEST, "Please generate new OTP.!"), HttpStatus.BAD_REQUEST);
                }
                logger.info("OTP SMS : Please enter a valid OTP.!");
                return new ResponseEntity(new ApiResponse(HttpStatus.BAD_REQUEST, "Please enter a valid OTP.!"), HttpStatus.BAD_REQUEST);
            }
                return new ResponseEntity<>(new ApiResponse(HttpStatus.BAD_REQUEST,"tooo many request so try after 20 min"),HttpStatus.BAD_REQUEST);
            }
           return new ResponseEntity<>(new ApiResponse(HttpStatus.BAD_REQUEST,"InvValid PhNo"),HttpStatus.BAD_REQUEST);
        } catch (Exception var9) {
            var9.printStackTrace();
        }
        logger.info("OTP SMS : Invalid input.!");
        return new ResponseEntity(new ApiResponse(HttpStatus.BAD_GATEWAY, "Invalid input.!"), HttpStatus.BAD_GATEWAY);
    }

    public static void sms(String smsTemplate, String phn) {
        try {
            String data = "";
            data = data + "method=sendMessage";
            data = data + "&userid=2000111180";
            data = data + "&password=" + URLEncoder.encode("abc123", StandardCharsets.UTF_8);
            data = data + smsTemplate;
            data = data + "&send_to=" + URLEncoder.encode(phn, StandardCharsets.UTF_8);
            data = data + "&v=1.1";
            data = data + "&msg_type=TEXT";
            data = data + "&auth_scheme=PLAIN";
            URL url = new URL("https://enterprise.smsgupshup.com/GatewayAPI/rest?" + data);
            logger.info("SMS Gupshup URL Triggered");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            logger.info("SMS : The OTP Messaging sending : ---- : {}", smsTemplate);
            conn.setRequestMethod("GET");
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setUseCaches(false);
            conn.connect();
            BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuffer buffer = new StringBuffer();

            String line;
            while ((line = rd.readLine()) != null) {
                logger.info("The input line from buffer : ---- : {}", line);
                buffer.append(line).append("\n");
            }

            rd.close();
            conn.disconnect();
        } catch (Exception var8) {
            var8.printStackTrace();
        }

    }
    public void sms(String smsTemplate, String phn, String templateId) {
        SmsRequestDto smsRequestDto = new SmsRequestDto();
        smsRequestDto.setTo(phn);
        smsRequestDto.setSender("BIJLPY");
        smsRequestDto.setService("SI");
        smsRequestDto.setCountry_code("+91");
        smsRequestDto.setTemplate_id(templateId);
        smsRequestDto.setMessage(smsTemplate);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer 630e4cc055707c99c2055ccb1bc8549b");
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<SmsRequestDto> entity = new HttpEntity<>(smsRequestDto, headers);
        logger.info("request body ---------------: {}", smsRequestDto);
        logger.info("qikberry entity ----------------- : {}", entity);
        ResponseEntity<String> response =  new RestTemplate().exchange("https://rest.qikberry.ai/v1/sms/messages",HttpMethod.POST, entity, String.class);
        logger.info("response out ---------------------: {}", response.getBody());
        logger.info("response from qikberry--{}", response.getStatusCodeValue());
        Gson gson = new Gson();
        String out = gson.toJson(response);
        logger.info("qikberry response body--{}", out);
    }

}
