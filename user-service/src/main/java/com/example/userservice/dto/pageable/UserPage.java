package com.example.userservice.dto.pageable;

import com.example.userservice.dto.UserDto;
import lombok.Data;
import org.springframework.data.domain.Page;

import java.util.List;

@Data
public class UserPage {

    private long totalElements;
    private int totalPages;
    private int size;
    private int number;
    private int numberOfElements;
    private List<UserDto> content;

    public static UserPage of(Page<UserDto> page) {
        UserPage result = new UserPage();
        result.setContent(page.getContent());
        result.setNumberOfElements(page.getNumberOfElements());
        result.setSize(page.getSize());
        result.setTotalElements(page.getTotalElements());
        result.setTotalPages(page.getTotalPages());
        result.setNumber(page.getNumber());

        return result;
    }
}
