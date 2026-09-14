package com.example.bankingservice.client.banking.payload.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Meta {

    @JsonProperty("totalPages")
    private Integer totalPages;
}
