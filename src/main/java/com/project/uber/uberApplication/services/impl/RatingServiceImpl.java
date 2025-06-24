package com.project.uber.uberApplication.services.impl;

import com.project.uber.uberApplication.dto.DriverDTO;
import com.project.uber.uberApplication.dto.RideDTO;
import com.project.uber.uberApplication.dto.RiderDTO;
import com.project.uber.uberApplication.entities.Driver;
import com.project.uber.uberApplication.entities.Rating;
import com.project.uber.uberApplication.entities.Ride;
import com.project.uber.uberApplication.entities.Rider;
import com.project.uber.uberApplication.exceptions.ResourceNotFoundException;
import com.project.uber.uberApplication.exceptions.RuntimeConflictException;
import com.project.uber.uberApplication.repositories.DriverRepository;
import com.project.uber.uberApplication.repositories.RatingRepository;
import com.project.uber.uberApplication.repositories.RiderRepository;
import com.project.uber.uberApplication.services.RatingService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RatingServiceImpl implements RatingService {

    private final RatingRepository ratingRepository;
    private final DriverRepository driverRepository;
    private final RiderRepository riderRepository;
    private final ModelMapper mapper;

    @Override
    public DriverDTO rateDriver(Ride ride, Integer rating) {
        Driver driver = ride.getDriver();

        Rating ratingObj = ratingRepository.findByRide(ride)
                .orElseThrow(() -> new ResourceNotFoundException("Rating not found for ride with id: "+ ride.getId()));

        if(ratingObj.getDriverRating() != null){
            throw new RuntimeConflictException("Driver has already been rated");
        }

        ratingObj.setDriverRating(rating);

        ratingRepository.save(ratingObj);

        Double newRating = ratingRepository.findByDriver(driver)
                .stream()
                .mapToDouble(Rating::getDriverRating)
                .average()
                .orElse(0.0);
        driver.setRating(newRating);

        Driver savedDriver = driverRepository.save(driver);

        return mapper.map(savedDriver, DriverDTO.class);
    }

    @Override
    public RiderDTO rateRider(Ride ride, Integer rating) {
        Rider rider = ride.getRider();

        Rating ratingObj = ratingRepository.findByRide(ride)
                .orElseThrow(() -> new ResourceNotFoundException("Rating not found for ride with id: "+ ride.getId()));

        if(ratingObj.getRiderRating() != null){
            throw new RuntimeConflictException(("Rider has already been rated"));
        }

        ratingObj.setRiderRating(rating);

        ratingRepository.save(ratingObj);

        Double newRating = ratingRepository.findByRider(rider)
                .stream()
                .mapToDouble(Rating::getRiderRating)
                .average()
                .orElse(0.0);
        rider.setRating(newRating);

        Rider savedRider = riderRepository.save(rider);

        return mapper.map(savedRider, RiderDTO.class);
    }

    @Override
    public void createNewRating(Ride ride) {
        Rating rating = Rating.builder()
                .rider(ride.getRider())
                .driver(ride.getDriver())
                .ride(ride)
                .build();
        ratingRepository.save(rating);
    }
}
