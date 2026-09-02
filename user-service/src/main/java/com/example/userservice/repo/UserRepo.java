package com.example.userservice.repo;

import com.example.userservice.domain.User;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

public interface UserRepo extends JpaRepository<User, UUID> , JpaSpecificationExecutor<User> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query(value = "SELECT u.* FROM users as u WHERE u.user_id = :userId",nativeQuery = true)
    Optional<User> findByUserIdForUpdate(@Param("userId") String userId);

    @Query(value = "SELECT u.* FROM users as u WHERE u.user_id = :userId",nativeQuery = true)
    Optional<User> findByUserId(@Param("userId") String userId);

    Page<User> findAll(Specification<User> spec, Pageable pageable);




}
