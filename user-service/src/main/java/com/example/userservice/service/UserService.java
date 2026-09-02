package com.example.userservice.service;

import com.example.userservice.domain.User;
import com.example.userservice.dto.RegisterDto;
import com.example.userservice.dto.UserDto;
import com.example.userservice.dto.UserUpdateRequest;
import com.example.userservice.dto.pageable.UserPage;
import com.example.userservice.dto.pageable.UserSearchCriteria;
import com.example.userservice.dto.pageable.UserSpecs;
import com.example.userservice.repo.UserRepo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepo userRepo;

    public UserService(UserRepo userRepo) {
        this.userRepo = userRepo;
    }

    public User save(RegisterDto dto, String userId) {

        User user = new User();
        user.setUserId(userId);
        user.setName(dto.getName());
        user.setLastName(dto.getLastname());
        user.setEmail(dto.getEmail());
        user.setPhone(dto.getPhone());
        return userRepo.save(user);
    }

    public Optional<UserDto> findById(String userId) {
        return userRepo.findByUserId(userId).map(UserDto::toDto);
    }


    public List<UserDto> userList() {
        return userRepo.findAll().stream().map(UserDto::toDto).collect(Collectors.toList());
    }

    @Transactional
    public UserDto update(String userId, UserUpdateRequest request) {
       User inDB = userRepo.findByUserIdForUpdate(userId).orElseThrow();
        if (request.getName()!=null){
            inDB.setName(request.getName());
        }
        if (request.getPhone()!=null) {
            inDB.setPhone(request.getPhone());
        }
        if (request.getEmail()!=null) {
            inDB.setEmail(request.getEmail());
        }
        User user = userRepo.save(inDB);
        return UserDto.toDto(user);

    }

    public UserPage page(int page, int size, Sort.Direction direction, String sortProperty) {
        Sort sort = Sort.by(new Sort.Order(direction, sortProperty));
        Pageable pageable = PageRequest.of(page, size, sort);
        return UserPage.of(userRepo.findAll(pageable).map(UserDto::toDto));
    }

    public Page<UserDto> findByCriteria(UserSearchCriteria criteria){
        return  userRepo.findAll(UserSpecs.accordingToReportProperties(criteria),criteria.getPageable()).map(UserDto::toDto);
    }




}
