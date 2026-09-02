package com.example.userservice.repo;

import com.example.userservice.domain.User;
import com.example.userservice.dto.UserUpdateRequest;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

public interface UserUpdater {

    @Transactional(propagation = Propagation.MANDATORY)
    void update(User user, UserUpdateRequest request);
}
