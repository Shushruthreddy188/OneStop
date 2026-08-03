package com.onestop.recommendation.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "product_signal")
@Getter
@Setter
@NoArgsConstructor
public class ProductSignal {

    @Id
    @Column(name = "product_id")
    private Long productId;

    @Column(name = "view_count", nullable = false)
    private long viewCount;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();
}
