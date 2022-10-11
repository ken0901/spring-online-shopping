package com.ken.app.jwt;

import com.ken.app.model.User;
import com.ken.app.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Objects;

@Service
@Slf4j
public class CustomerUsersDetailsService implements UserDetailsService {

    @Autowired
    UserRepository userRepository;

    private com.ken.app.model.User userDetail;

    @Override
    public UserDetails loadUserByUsername(String emailId) throws UsernameNotFoundException {
        log.info("Inside loadUserByUsername {}",emailId);
        userDetail = userRepository.findByEmailId(emailId);
        if(!Objects.isNull(userDetail)){
            return new org.springframework.security.core.userdetails.User(userDetail.getEmail(),userDetail.getPassword(),new ArrayList<>());
        }else{
            throw new UsernameNotFoundException("User not found");
        }
    }

    public com.ken.app.model.User getUserDetail(){
        return userDetail;
    }
}
