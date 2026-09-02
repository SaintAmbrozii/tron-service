package com.example.walletservice.domain;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldNameConstants;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldNameConstants
@ToString
@Entity
@Table(name = "outbox")
public class Outbox {

    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    @Column(name = "user_id",nullable = false)
    private String userId;

    @Column(name = "phone", nullable = false)
    private String phone;

    @Column(name = "amount",columnDefinition = "NUMERIC(10,2)")
    private BigDecimal amount;

    @Column(name = "aggregate_id")
    private UUID aggregateId;

    @Column(name = "status")
    private Boolean status;

    @CreationTimestamp
    @Column(name = "create_date")
    private ZonedDateTime createDate;
}
