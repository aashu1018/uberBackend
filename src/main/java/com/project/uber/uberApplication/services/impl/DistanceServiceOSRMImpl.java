package com.project.uber.uberApplication.services.impl;

import com.project.uber.uberApplication.services.DistanceService;
import lombok.Data;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Objects;

@Service
public class DistanceServiceOSRMImpl implements DistanceService {

    private static final String OSRM_API_BAS_URL = "https://router.project-osrm.org/route/v1/driving/";

    @Override
    public double calculateDistance(Point src, Point dest) {

        try{
            String uri = src.getX()+","+src.getY()+";"+dest.getX()+","+dest.getY();

            OSRMResponseDTO responseDTO = RestClient.builder()
                    .baseUrl(OSRM_API_BAS_URL)
                    .build()
                    .get()
                    .uri(uri)
                    .retrieve()
                    .body(OSRMResponseDTO.class);

            return Objects.requireNonNull(responseDTO).getRoutes().get(0).getDistance();
        } catch (Exception e) {
            throw new RuntimeException("Error getting data from OSRM "+ e.getMessage());
        }
    }
}

@Data
class OSRMResponseDTO{
    private List<OSRMRoute> routes;
}

@Data
class OSRMRoute {
    private Double distance;
}
