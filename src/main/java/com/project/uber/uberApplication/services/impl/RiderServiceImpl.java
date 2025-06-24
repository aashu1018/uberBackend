package com.project.uber.uberApplication.services.impl;

import com.project.uber.uberApplication.dto.DriverDTO;
import com.project.uber.uberApplication.dto.RideDTO;
import com.project.uber.uberApplication.dto.RideRequestDTO;
import com.project.uber.uberApplication.dto.RiderDTO;
import com.project.uber.uberApplication.entities.*;
import com.project.uber.uberApplication.entities.enums.RideRequestStatus;
import com.project.uber.uberApplication.entities.enums.RideStatus;
import com.project.uber.uberApplication.exceptions.ResourceNotFoundException;
import com.project.uber.uberApplication.repositories.RideRequestRepository;
import com.project.uber.uberApplication.repositories.RiderRepository;
import com.project.uber.uberApplication.services.DriverService;
import com.project.uber.uberApplication.services.RatingService;
import com.project.uber.uberApplication.services.RideService;
import com.project.uber.uberApplication.services.RiderService;
import com.project.uber.uberApplication.strategies.StrategyManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RiderServiceImpl implements RiderService {

    private final ModelMapper modelMapper;
    private final StrategyManager strategyManager;
    private final RideRequestRepository rideRequestRepository;
    private final RiderRepository riderRepository;
    private final RideService rideService;
    private final DriverService driverService;
    private final RatingService ratingService;
    private final ModelMapper mapper;

    @Override
    @Transactional
    public RideRequestDTO requestRide(RideRequestDTO rideRequestDTO) {
        Rider rider = getCurrentRider();
        RideRequest rideRequest = modelMapper.map(rideRequestDTO, RideRequest.class);
        rideRequest.setRideRequestStatus(RideRequestStatus.PENDING);
        rideRequest.setRider(rider);

        Double fare = strategyManager.rideFareCalculationStrategy().calculateFare(rideRequest);
        rideRequest.setFare(fare);

        RideRequest savedRideRequest = rideRequestRepository.save(rideRequest);

        List<Driver> drivers = strategyManager.driverMatchingStrategy(rider.getRating()).findMatchingDriver(rideRequest);
        //TODO send notification to all the drivers about this ride request

        return modelMapper.map(savedRideRequest, RideRequestDTO.class);
    }

    @Override
    public RideDTO cancelRide(Long rideId) {
        Rider rider = getCurrentRider();
        Ride ride = rideService.getRideById(rideId);

        if(!rider.equals(ride.getRider())){
            throw new RuntimeException("Rider does not own this ride with id: "+ rideId);
        }

        if(!ride.getRideStatus().equals(RideStatus.CONFIRMED)){
            throw new RuntimeException("Ride cannot be cancelled, invalid status: "+ ride.getRideStatus());
        }

        Ride savedRide = rideService.updateRideStatus(ride, RideStatus.CANCELLED);
        driverService.updateDriverAvailability(ride.getDriver(), true);

        return modelMapper.map(savedRide, RideDTO.class);
    }

    @Override
    public DriverDTO rateDriver(Long rideId, Integer rating) {
        Ride ride = rideService.getRideById(rideId);
        Rider rider = getCurrentRider();

        if(!rider.equals(ride.getRider())){
            throw new RuntimeException("Rider is not the owner of the ride");
        }

        if(!ride.getRideStatus().equals(RideStatus.ENDED)){
            throw new RuntimeException("Ride Status is not ENDED and hence, the rating cannot be done, current Status is : " + ride.getRideStatus());
        }

        return ratingService.rateDriver(ride, rating);
    }

    @Override
    public RiderDTO getMyProfile() {
        Rider currentRider = getCurrentRider();
        return modelMapper.map(currentRider, RiderDTO.class);
    }

    @Override
    public Page<RideDTO> getAllMyRides(PageRequest pageRequest) {
        Rider currentRider = getCurrentRider();
        return rideService.getAllRidesOfRider(currentRider, pageRequest).map(
                ride -> mapper.map(ride, RideDTO.class)
        );
    }

    @Override
    public Rider createNewRider(User user) {
        Rider rider = Rider
                .builder()
                .user(user)
                .rating(0.0)
                .build();
        return riderRepository.save(rider);
    }

    @Override
    public Rider getCurrentRider() {
//        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
//
//        return riderRepository.findByUser(user).orElseThrow(() -> new ResourceNotFoundException(
//                "Rider not associated with user with id: " + user.getId()
//        ));

        return riderRepository.findById(1L).orElseThrow(() ->
                new ResourceNotFoundException("Rider with given id not found" + 1)
        );
    }
}
