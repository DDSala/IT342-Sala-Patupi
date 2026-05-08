package edu.cit.sala.patupi.features.authentication;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByFullName(String fullName);

    boolean existsByEmail(String email);

    List<User> findByRoleId(Integer roleId);
}