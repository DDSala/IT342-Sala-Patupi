package edu.cit.sala.patupi.repository;

import edu.cit.sala.patupi.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
}