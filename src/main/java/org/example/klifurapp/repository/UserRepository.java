package org.example.klifurapp.repository;

import org.example.klifurapp.entity.User;
import org.jspecify.annotations.NullMarked;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;


public interface UserRepository extends JpaRepository<User, Long> {

    @NullMarked // id not null
    Optional<User> findById(Long id);

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    @Modifying
    @Query(
            value = "DELETE FROM user_friends WHERE user_id = :userId OR friend_id = :userId",
            nativeQuery = true
    )
    void deleteFriendships(@Param("userId") Long userId);

}
