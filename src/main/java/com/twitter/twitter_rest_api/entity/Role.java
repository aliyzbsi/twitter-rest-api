package com.twitter.twitter_rest_api.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Table(name = "role",schema = "twitterapi")
public class Role implements GrantedAuthority {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "authority")
    private String authority;

    @Override
    public String getAuthority() {
        return authority;
    }
}
