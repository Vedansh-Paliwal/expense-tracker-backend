package com.example.expensetracker.security;

import com.example.expensetracker.entity.User;
import com.example.expensetracker.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Component
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String login)
            throws UsernameNotFoundException {
        User user;
        if (login.contains("@")) {
            user = userRepository.findByEmail(login)
                    .orElseThrow(() ->
                            new UsernameNotFoundException("Invalid credentials"));
        } else {
            user = userRepository.findByUsername(login)
                    .orElseThrow(() ->
                            new UsernameNotFoundException("Invalid credentials"));
        }
        return new UserDetailsImpl(user);
    }
}
