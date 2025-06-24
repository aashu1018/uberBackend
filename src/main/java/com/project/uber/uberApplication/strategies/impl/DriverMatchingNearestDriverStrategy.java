package com.project.uber.uberApplication.strategies.impl;

import com.project.uber.uberApplication.entities.Driver;
import com.project.uber.uberApplication.entities.RideRequest;
import com.project.uber.uberApplication.repositories.DriverRepository;
import com.project.uber.uberApplication.strategies.DriverMatchingStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class DriverMatchingNearestDriverStrategy implements DriverMatchingStrategy {

    private final DriverRepository driverRepository;

    @Override
    public List<Driver> findMatchingDriver(RideRequest rideRequest) {
        return driverRepository.findTenNearestDrivers(rideRequest.getPickupLocation());
    }
}
