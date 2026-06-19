package org.example.expert.domain.user.repository;

import org.example.expert.domain.user.dto.response.UserSearchResponse;
import org.example.expert.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);

    @Query("select new org.example.expert.domain.user.dto.response.UserSearchResponse(u.id, u.email, u.username) " +
            "from User u where u.username = :nickname")
    List<UserSearchResponse> findAllByNickname(@Param("nickname") String nickname);
}
