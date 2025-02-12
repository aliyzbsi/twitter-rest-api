package com.twitter.twitter_rest_api.repository;

import com.twitter.twitter_rest_api.entity.Follow;
import com.twitter.twitter_rest_api.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FollowRepository extends JpaRepository<Follow,Long> {
    // Bir kullanıcının takipçilerini getir
    @Query("SELECT f.takipciler FROM Follow f WHERE f.takipedilenler.id = :userId")
    Page<User> findFollowersByUserId(@Param("userId") Long userId, Pageable pageable);

    // Bir kullanıcının takip ettiklerini getir
    @Query("SELECT f.takipedilenler FROM Follow f WHERE f.takipciler.id = :userId")
    Page<User> findFollowingsByUserId(@Param("userId") Long userId, Pageable pageable);

    // İki kullanıcı arasındaki takip ilişkisini kontrol et
    @Query("SELECT f FROM Follow f WHERE f.takipciler.id = :followerId AND f.takipedilenler.id = :followingId")
    Optional<Follow> findFollowRelation(
            @Param("followerId") Long followerId,
            @Param("followingId") Long followingId
    );

    // Karşılıklı takipleşen kullanıcıları getir (mutual followers)
    @Query("""
        SELECT f1.takipedilenler FROM Follow f1 
        WHERE f1.takipciler.id = :userId 
        AND EXISTS (
            SELECT 1 FROM Follow f2 
            WHERE f2.takipciler.id = f1.takipedilenler.id 
            AND f2.takipedilenler.id = :userId
        )
    """)
    List<User> findMutualFollowers(@Param("userId") Long userId);
}
