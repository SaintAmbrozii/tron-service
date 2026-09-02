package com.example.userservice.dto.pageable;

import lombok.Data;
import lombok.ToString;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@Data
@ToString
public class UserSearchCriteria {

    private String email;
    private String phone;
    private String name;

    private int page = 0;
    private int count = 50;

    private Sort.Direction direction = Sort.Direction.DESC;
    private String sortProperty = "id";


    public Pageable getPageable() {
        Sort sort = Sort.by(new Sort.Order(getDirection(), getSortProperty()));
        return PageRequest.of(getPage(), getCount(), sort);
    }

}
