package com.project.uber.uberApplication.repositories;

import com.project.uber.uberApplication.entities.Driver;
import com.project.uber.uberApplication.entities.Rating;
import com.project.uber.uberApplication.entities.Ride;
import com.project.uber.uberApplication.entities.Rider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RatingRepository extends JpaRepository<Rating, Long> {
    List<Rating> findByRider(Rider rider);
    List<Rating> findByDriver(Driver driver);
    Optional<Rating> findByRide(Ride ride);
}
