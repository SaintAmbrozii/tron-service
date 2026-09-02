package com.example.userservice.dto.pageable;

import com.example.userservice.domain.User;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.ObjectUtils;

import java.util.ArrayList;
import java.util.List;

public class UserSpecs {

    public static Specification<User> accordingToReportProperties(UserSearchCriteria criteria) {
        return (root, criteriaQuery, cb) -> {

            List<Predicate> predicates = new ArrayList<>();

            if (!ObjectUtils.isEmpty(criteria.getEmail()))
                predicates.add(cb.like(root.get("email"), criteria.getEmail()));

            if (!ObjectUtils.isEmpty(criteria.getName()))
                predicates.add(cb.like(root.get("name"), criteria.getName()));

            if (!ObjectUtils.isEmpty(criteria.getPhone()))
                predicates.add(cb.like(root.get("phone"), criteria.getPhone()));

            return cb.and(predicates.toArray(new Predicate[]{}));
        };
    }
}
