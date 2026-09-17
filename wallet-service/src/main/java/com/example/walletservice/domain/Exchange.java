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
@Table(name = "exchanges",indexes = {
        @Index(
                name = "idx_exchanges_retry_status",
                columnList = "status"
        )
})
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

    @Enumerated(value = EnumType.STRING)
    @Column(name = "status")
    private Status status;

    @Builder.Default
    @Column(name = "retry_count", nullable = false)
    private int retryCount = 0;

    @Column(name = "tx_id")
    private String txId;

    @Column(name = "fail_reason", columnDefinition = "TEXT")
    private String failReason;

    @CreationTimestamp
    @Column(name = "create_date",updatable = false, insertable = false)
    private ZonedDateTime createDate;


}
