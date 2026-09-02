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
@Table(name = "wallets")
public class Wallet {

    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    @Column(name = "userid",nullable = false)
    private String userId;

    @Column(name = "address", nullable = false,unique = true)
    private String address;

    @Column(name = "privat_key", nullable = false,unique = true)
    private String privatKey;

    @Column(name = "amount",columnDefinition = "NUMERIC(10,2)")
    private BigDecimal amount;

    @CreationTimestamp
    @Column(name = "create_date")
    private ZonedDateTime createDate;
}
