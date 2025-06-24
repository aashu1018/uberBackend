package com.project.uber.uberApplication.repositories;

import com.project.uber.uberApplication.entities.Rider;
import com.project.uber.uberApplication.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RiderRepository extends JpaRepository<Rider, Long> {
    Optional<Rider> findByUser(User user);
}
