package com.project.uber.uberApplication.services;

import com.project.uber.uberApplication.dto.DriverDTO;
import com.project.uber.uberApplication.dto.RideDTO;
import com.project.uber.uberApplication.dto.RideRequestDTO;
import com.project.uber.uberApplication.dto.RiderDTO;
import com.project.uber.uberApplication.entities.Rider;
import com.project.uber.uberApplication.entities.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;

public interface RiderService {

    RideRequestDTO requestRide(RideRequestDTO rideRequestDTO);

    RideDTO cancelRide(Long rideId);

    DriverDTO rateDriver(Long rideId, Integer rating);

    RiderDTO getMyProfile();

    Page<RideDTO> getAllMyRides(PageRequest pageRequest);

    Rider createNewRider(User user);

    Rider getCurrentRider();

}
