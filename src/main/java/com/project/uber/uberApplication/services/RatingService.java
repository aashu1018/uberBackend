package com.project.uber.uberApplication.services;

import com.project.uber.uberApplication.dto.DriverDTO;
import com.project.uber.uberApplication.dto.RiderDTO;
import com.project.uber.uberApplication.entities.Driver;
import com.project.uber.uberApplication.entities.Ride;
import com.project.uber.uberApplication.entities.Rider;

public interface RatingService {

    DriverDTO rateDriver(Ride ride, Integer rating);
    RiderDTO rateRider(Ride ride, Integer rating);

    void createNewRating(Ride ride);
}
