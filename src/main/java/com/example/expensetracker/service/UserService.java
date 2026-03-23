package com.example.expensetracker.service;

import com.example.expensetracker.dto.SignupRequest;
import com.example.expensetracker.dto.UpdateUserRequest;
import com.example.expensetracker.dto.UserProfileResponse;
import com.example.expensetracker.entity.Budget;
import com.example.expensetracker.entity.Expense;
import com.example.expensetracker.entity.User;
import com.example.expensetracker.exception.ResourceNotFoundException;
import com.example.expensetracker.repository.AiReportRepository;
import com.example.expensetracker.repository.ExpenseRepository;
import com.example.expensetracker.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private AiReportRepository aiReportRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    public void saveNewUser(SignupRequest signupRequest) {
        User user = new User();
        user.setUsername(signupRequest.getUsername().trim().toLowerCase());
        user.setEmail(signupRequest.getEmail().trim().toLowerCase());
        user.setPassword(bCryptPasswordEncoder.encode(signupRequest.getPassword()));
        List<String> roles = new ArrayList<>();
        roles.add("USER");
        user.setRoles(roles);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        userRepository.save(user);
    }

    public void updateUserDetails(UpdateUserRequest updateUserRequest, String userId) {
        Optional<User> userInfo = userRepository.findById(userId);
        if(userInfo.isEmpty()) {
            throw new ResourceNotFoundException("User does not exist");
        }
        User user = userInfo.get();
        boolean passwordMatched =
                bCryptPasswordEncoder.matches(updateUserRequest.getOldPassword(), user.getPassword());
        if(!passwordMatched) {
            throw new IllegalArgumentException("Password does not match");
        }
        user.setPassword(bCryptPasswordEncoder.encode(updateUserRequest.getNewPassword()));
        user.setUpdatedAt(LocalDateTime.now());
        user.setUsername(updateUserRequest.getUsername());

        userRepository.save(user);
    }

    @Transactional
    public void deleteUserAccount(String userId){
        Optional<User> userInfo = userRepository.findById(userId);
        if(userInfo.isEmpty()) {
            throw new ResourceNotFoundException("User does not exist");
        }
        expenseRepository.deleteAllByUserId(userId);
        aiReportRepository.deleteByUserId(userId);

        Query query = new Query();
        query.addCriteria(Criteria.where("userId").is(userId));
        mongoTemplate.remove(query, Budget.class);

        userRepository.deleteById(userId);
    }

    public UserProfileResponse getUserProfile(String userId) {
        Optional<User> userInfo = userRepository.findById(userId);
        if(userInfo.isEmpty()) {
            throw new ResourceNotFoundException("User does not exist");
        }
        User user = userInfo.get();
        UserProfileResponse userProfileResponse = new UserProfileResponse();
        userProfileResponse.setUsername(user.getUsername());
        userProfileResponse.setEmail(user.getEmail());
        userProfileResponse.setId(user.getId());
        userProfileResponse.setCreatedAt(user.getCreatedAt());

        return userProfileResponse;
    }
}
