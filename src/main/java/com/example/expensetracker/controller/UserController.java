package com.example.expensetracker.controller;

import com.example.expensetracker.dto.UpdateUserRequest;
import com.example.expensetracker.dto.UserProfileResponse;
import com.example.expensetracker.security.UserDetailsImpl;
import com.example.expensetracker.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


@RestController
@Validated
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> getUser(Authentication authentication)
    {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        String userId = userDetails.getId();

        UserProfileResponse userProfile = userService.getUserProfile(userId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(userProfile);
    }

    @PutMapping
    public ResponseEntity<Void> updateUser(@Valid @RequestBody UpdateUserRequest updateUserRequest, Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        String userId = userDetails.getId();
        userService.updateUserDetails(updateUserRequest, userId);

        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .build();
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteUser(Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl)  authentication.getPrincipal();
        String userId = userDetails.getId();

        userService.deleteUserAccount(userId);

        return  ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .build();
    }
}
