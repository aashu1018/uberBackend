package com.project.uber.uberApplication.repositories;

import com.project.uber.uberApplication.entities.Payment;
import com.project.uber.uberApplication.entities.Ride;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByRide(Ride ride);
}
