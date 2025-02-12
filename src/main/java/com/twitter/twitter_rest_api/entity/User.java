package com.twitter.twitter_rest_api.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CurrentTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "app_user",schema = "twitterapi")
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "first_name",length = 50)
    private String firstName;

    @NotNull
    @Column(name = "last_name",length = 50)
    private String lastName;

    @NotNull
    @Size(min = 1,max = 15)
    @Column(name = "username", unique = true,length = 15)
    private String username;

    @NotNull
    @Email
    @Column(name = "email",unique = true)
    private String email;

    @NotNull
    @Column(name = "password")
    private String password;

    @Size(max = 160)
    @Column(name = "bio",length = 160)
    private String bio;

    @Column(name = "profile_image")
    private String profileImage;

    @Column(name = "header_image")
    private String headerImage;

    @Column(name = "followers_count")
    private Integer followersCount=0;

    @Column(name = "following_count")
    private Integer followingCount=0;

    @Column(name = "tweets_count")
    private Integer tweetsCount=0;

    private Boolean verified=false;

    @Column(name = "private_account")
    private Boolean privateAccount=false;

    @CurrentTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @CurrentTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "app_user_role",schema = "twitterapi",
    joinColumns = @JoinColumn(name = "app_user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> authorities=new HashSet<>();

    @OneToMany(mappedBy = "user",cascade = CascadeType.ALL)
    @JsonIgnore
    private Set<Tweet> tweets=new HashSet<>();

    @OneToMany(mappedBy = "user")
    private Set<TweetLike> likedTweets=new HashSet<>();

    @OneToMany(mappedBy = "takipciler")
    private Set<Follow> takipciler=new HashSet<>();

    @OneToMany(mappedBy = "takipedilenler")
    private Set<Follow> takipedilenler=new HashSet<>();

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    public String getFullName(){
        return firstName+" "+lastName;
    }

    public void incrementTweetsCount(){
        tweetsCount++;
    }

    public void decrementTweetsCount(){
        if(this.tweetsCount>0){
            this.tweetsCount--;
        }
    }
    public void incrementFollowersCount() {
        this.followersCount++;
    }

    public void decrementFollowersCount() {
        if (this.followersCount > 0) {
            this.followersCount--;
        }
    }

    public void incrementFollowingCount() {
        this.followingCount++;
    }

    public void decrementFollowingCount() {
        if (this.followingCount > 0) {
            this.followingCount--;
        }
    }
    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                '}';
    }

}
