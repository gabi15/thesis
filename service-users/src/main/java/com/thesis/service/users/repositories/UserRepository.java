package com.thesis.service.users.repositories;

import com.thesis.service.users.entities.ServiceUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<ServiceUser, Long> {

    Optional<ServiceUser> findByLogin(String login);
    boolean existsServiceUserByLogin(String login);
}
