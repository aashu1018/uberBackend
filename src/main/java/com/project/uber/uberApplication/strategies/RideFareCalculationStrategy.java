package com.project.uber.uberApplication.strategies;

import com.project.uber.uberApplication.entities.RideRequest;

public interface RideFareCalculationStrategy {

    double RIDE_FARE_MULTIPLIER = 10;

    double calculateFare(RideRequest rideRequest);
}
