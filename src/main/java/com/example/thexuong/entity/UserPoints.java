package com.example.thexuong.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * Số dư điểm của user (1 user = 1 row).
 * @Version cho optimistic lock chống race condition.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "user")
@Entity
@Table(name = "UserPoints")
public class UserPoints {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @Builder.Default
    @Column(name = "current_points", nullable = false)
    private Integer currentPoints = 0;

    @Builder.Default
    @Column(name = "total_earned", nullable = false)
    private Long totalEarned = 0L;

    @Builder.Default
    @Column(name = "total_spent", nullable = false)
    private Long totalSpent = 0L;

    @Column(name = "last_activity_at")
    private LocalDateTime lastActivityAt;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;
}
