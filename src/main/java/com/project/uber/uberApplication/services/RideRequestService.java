package com.project.uber.uberApplication.services;

import com.project.uber.uberApplication.entities.RideRequest;

public interface RideRequestService {

    RideRequest findRideRequestById(Long rideRequestId);

    void update(RideRequest rideRequest);
}
