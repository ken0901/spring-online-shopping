package com.ken.app.serviceImpl;

import com.ken.app.constants.CafeConstants;
import com.ken.app.jwt.CustomerUsersDetailsService;
import com.ken.app.jwt.JwtFilter;
import com.ken.app.jwt.JwtUtil;
import com.ken.app.model.User;
import com.ken.app.repository.UserRepository;
import com.ken.app.service.UserService;
import com.ken.app.utils.CafeUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Objects;

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

    private final String TRUE = "true";
    private final String FALSE = "false";
    private final String EMAIL = "email";
    private final String PASSWORD = "password";
    private final String NAME = "name";
    private final String CONTRACTNUMBER = "contactNumber";
    private final String ROLE = "role";


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

    private boolean validateSignUpMap(Map<String,String> requestMap){
        return requestMap.containsKey(NAME) && requestMap.containsKey(CONTRACTNUMBER)
                && requestMap.containsKey(EMAIL) && requestMap.containsKey(PASSWORD);
    }

    private User getUserFromMap(Map<String, String> requestMap){
        User user = new User();
        user.setNmae(requestMap.get(NAME));
        user.setContactNumber(requestMap.get(CONTRACTNUMBER));
        user.setEmail(requestMap.get(EMAIL));
        user.setPassword(requestMap.get(PASSWORD));
        user.setRole(FALSE);
        user.setRole(requestMap.get(ROLE));

        return user;
    }
}
