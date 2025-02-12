package com.twitter.twitter_rest_api.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Table(name = "follow",schema = "twitterapi",
uniqueConstraints = {
        @UniqueConstraint(
                columnNames = {"follower_id","followed_id"}
        )
})
public class Follow {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "follower_id")
    private User takipciler;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "followed_id")
    private User takipedilenler;

    @CreationTimestamp
    private LocalDateTime createdAt;


}
