package com.mybillbook.service;

import com.mybillbook.model.User;
import com.mybillbook.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;

    @Transactional
    public User login(String mobileNumber, String name, String businessName) {
        return userRepository.findByMobileNumber(mobileNumber)
            .orElseGet(() -> {
                User newUser = new User();
                newUser.setMobileNumber(mobileNumber);
                newUser.setName(name);
                newUser.setBusinessName(businessName);
                User saved = userRepository.save(newUser);
                log.info("Created new user with mobile: {}", mobileNumber);
                return saved;
            });
    }

    public User getUserById(Long userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
