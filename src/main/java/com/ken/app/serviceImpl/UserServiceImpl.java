package com.ken.app.serviceImpl;

import com.ken.app.constants.CafeConstants;
import com.ken.app.jwt.CustomerUsersDetailsService;
import com.ken.app.jwt.JwtFilter;
import com.ken.app.jwt.JwtUtil;
import com.ken.app.model.User;
import com.ken.app.repository.UserRepository;
import com.ken.app.service.UserService;
import com.ken.app.utils.CafeUtils;
import com.ken.app.utils.EmailUtils;
import com.ken.app.wrapper.UserWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    CustomerUsersDetailsService customerUsersDetailsService;

    @Autowired
    JwtUtil jwtUtil;

    @Autowired
    JwtFilter jwtFilter;

    @Autowired
    EmailUtils emailUtils;

    private static final String TRUE = "true";
    private static final String FALSE = "false";
    private static final String EMAIL = "email";
    private static final String PASSWORD = "password";
    private static final String NAME = "name";
    private static final String CONTACTNUMBER = "contactNumber";
    private static final String ROLE = "role";
    private static final String ID = "id";
    private static final String STATUS = "status";
    private static final String USER_APPROVED = "User Approved";
    private static final String USER_DISABLED = "User Disabled";


    @Override
    public ResponseEntity<String> signUp(Map<String, String> requestMap) {
        log.info("Inside Signup {}", requestMap);
        try{
            if(validateSignUpMap(requestMap)){
                User user = userRepository.findByEmailId(requestMap.get(EMAIL));
                if(Objects.isNull(user)){
                    userRepository.save(getUserFromMap(requestMap));
                    return CafeUtils.getResponseEntity(CafeConstants.SUCCESSFULLY_REGISTERED, HttpStatus.OK);
                }else{
                    return CafeUtils.getResponseEntity(CafeConstants.EMAIL_EXIST,HttpStatus.BAD_REQUEST);
                }
            }else{
                return CafeUtils.getResponseEntity(CafeConstants.INVALID_DATA, HttpStatus.BAD_REQUEST);
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
        return CafeUtils.getResponseEntity(CafeConstants.SOMETHING_WENT_WRONG,HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> login(Map<String, String> requestMap) {
        log.info("Inside login");
        try {
            Authentication auth = authenticationManager.authenticate(
                            new UsernamePasswordAuthenticationToken(requestMap.get(EMAIL),requestMap.get(PASSWORD))
            );
            if(auth.isAuthenticated()){
                if(customerUsersDetailsService.getUserDetail().getStatus().equalsIgnoreCase(TRUE)){
                    return new ResponseEntity<>("{\"token\":\""
                            +jwtUtil.generateToken(customerUsersDetailsService.getUserDetail().getEmail(),
                            customerUsersDetailsService.getUserDetail().getRole()) + "\"}",
                            HttpStatus.OK);
                }else{
                    return new ResponseEntity<>("{\"message\":\""+"Wait for admin approval."+"\"}",
                            HttpStatus.BAD_REQUEST);
                }
            }
        }catch (Exception e){
            log.error("{}",e);
        }
        return new ResponseEntity<>("{\"message\":\""+"Bad Credentials"+"\"}",
                HttpStatus.BAD_REQUEST);
    }

    @Override
    public ResponseEntity<List<UserWrapper>> getAllUser() {
        try {
            if(jwtFilter.isAdmin()){
                return new ResponseEntity<>(userRepository.getAlluser(),HttpStatus.OK);
            }else{
                return new ResponseEntity<>(new ArrayList<>(),HttpStatus.UNAUTHORIZED);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(),HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> update(Map<String, String> requestMap) {
        try{
            if(jwtFilter.isAdmin()){
                Optional<User> optional = userRepository.findById(Integer.parseInt(requestMap.get(ID)));
                if(!optional.isPresent()){ //isEmpty() - java 11
                    userRepository.updateStatus(requestMap.get(STATUS),Integer.parseInt(requestMap.get(ID)));
                    sendMailToAllAdmin(requestMap.get(STATUS),optional.get().getEmail(),userRepository.getAllAdmin());
                    return CafeUtils.getResponseEntity(CafeConstants.USER_STATUS_UPDATE_SUCCESSFULLY,HttpStatus.OK);
                }else{
                    CafeUtils.getResponseEntity(CafeConstants.USER_ID_NOT_EXIST,HttpStatus.OK);
                }
            }else{
                return CafeUtils.getResponseEntity(CafeConstants.UNAUTHORIZED_ACCESS,HttpStatus.UNAUTHORIZED);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return CafeUtils.getResponseEntity(CafeConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private void sendMailToAllAdmin(String status, String user, List<String> allAdmin) {
        allAdmin.remove(jwtFilter.getCurrentUser());
        if(status != null && status.equalsIgnoreCase(TRUE)){
            emailUtils.sendSimpleMessage(jwtFilter.getCurrentUser(),USER_APPROVED,"USER:- "+user+" \n is approved by \nADMIN:-"+jwtFilter.getCurrentUser(),allAdmin);
        }else{
            emailUtils.sendSimpleMessage(jwtFilter.getCurrentUser(),USER_DISABLED,"USER:- "+user+" \n is disabled by \nADMIN:-"+jwtFilter.getCurrentUser(),allAdmin);
        }
    }

    private boolean validateSignUpMap(Map<String,String> requestMap){
        return requestMap.containsKey(NAME) && requestMap.containsKey(CONTACTNUMBER)
                && requestMap.containsKey(EMAIL) && requestMap.containsKey(PASSWORD);
    }

    private User getUserFromMap(Map<String, String> requestMap){
        User user = new User();
        user.setName(requestMap.get(NAME));
        user.setContactNumber(requestMap.get(CONTACTNUMBER));
        user.setEmail(requestMap.get(EMAIL));
        user.setPassword(requestMap.get(PASSWORD));
        user.setRole(FALSE);
        user.setRole(requestMap.get(ROLE));

        return user;
    }
}
