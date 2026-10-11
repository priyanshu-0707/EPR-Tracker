package com.eprtracker.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondaryPartitionKey;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@DynamoDbBean
public class Recycler {

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

    @DynamoDbPartitionKey
    @DynamoDbAttribute("recyclerId")
    public String getRecyclerId() {
        return recyclerId;
    }

    @DynamoDbSecondaryPartitionKey(indexNames = "state-index")
    @DynamoDbAttribute("state")
    public String getState() {
        return state;
    }
}
