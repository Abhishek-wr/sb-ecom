package com.ecommerce.project.security.service;

import com.ecommerce.project.model.User;
import com.ecommerce.project.repositery.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserDetailServiceImpl implements UserDetailsService{
    @Autowired
    UserRepository userRepostitory;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepostitory.findByUserName(username)
                .orElseThrow(() ->
                        new UsernameNotFoundException("User Not found with username : " + username));
        return UserDetailsImpl.build(user);
    }

}
