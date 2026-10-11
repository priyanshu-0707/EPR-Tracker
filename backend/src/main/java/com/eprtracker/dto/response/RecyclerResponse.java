package com.eprtracker.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecyclerResponse {
    private String recyclerId;
    private String name;
    private String state;
    private String city;
    private Double latitude;
    private Double longitude;
    private List<String> categoriesHandled;
    private Boolean cpcbCertified;
    private String contactEmail;
    private String contactPhone;
    private Integer capacityTonnesPerYear;
    private String formattedCapacity;
    private Double distanceKm; // calculated when coordinates supplied
}
