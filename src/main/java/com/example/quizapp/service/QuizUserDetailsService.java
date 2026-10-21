package com.example.quizapp.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.quizapp.model.User;

@Service
public class QuizUserDetailsService implements UserDetailsService {

    private final Map<String, User> users = new ConcurrentHashMap<>();
    private final PasswordEncoder passwordEncoder;

    public QuizUserDetailsService(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = users.get(username);
        if (user == null) {
            throw new UsernameNotFoundException("User not found: " + username);
        }

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .roles(user.getRole())
                .build();
    }

    public void registerUser(String username, String email, String password, String role) {
        if (users.containsKey(username)) {
            throw new IllegalArgumentException("User already exists: " + username);
        }

        String encodedPassword = passwordEncoder.encode(password);
        users.put(username, new User(username, email, encodedPassword, role));
    }

}
