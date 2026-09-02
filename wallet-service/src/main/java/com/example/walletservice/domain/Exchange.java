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
@Table(name = "exchanges")
public class Exchange {

    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    @Column(name = "user_id",nullable = false)
    private String userId;

    @Column(name = "user_wallet",nullable = false)
    private String userWallet;

    @Column(name = "rub_amount",precision = 10, scale = 2)
    private BigDecimal rubAmount;

    @Column(name = "usd_amount",precision = 10, scale = 2)
    private BigDecimal usdAmount;

    @Column(name = "status")
    private Boolean status;

    @CreationTimestamp
    @Column(name = "create_date",updatable = false, insertable = false)
    private ZonedDateTime createDate;



}
