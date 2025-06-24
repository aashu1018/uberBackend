package com.project.uber.uberApplication.dto;

import com.project.uber.uberApplication.entities.enums.PaymentMethod;
import com.project.uber.uberApplication.entities.enums.RideRequestStatus;
import com.project.uber.uberApplication.entities.enums.RideStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.locationtech.jts.geom.Point;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RideDTO {

    private Long id;
    private PointDTO pickupLocation;
    private PointDTO dropOffLocation;

    private LocalDateTime createdTime;   //when driver accepts the ride
    private RiderDTO rider;
    private DriverDTO driver;
    private PaymentMethod paymentMethod;

    private RideStatus rideStatus;
    private String otp;

    private Double fare;
    private LocalDateTime startedAt;
    private LocalDateTime endedAt;

}
