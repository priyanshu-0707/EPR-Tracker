package com.eprtracker.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondaryPartitionKey;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@DynamoDbBean
public class Company {

    private String companyId;
    private String companyName;
    private String email;
    private String passwordHash;
    private String state;
    private String cpcbRegistrationNumber;
    private String registeredOn;
    private Boolean alertsEnabled;
    private String alertEmail;
    private String alertPhone;

    @DynamoDbPartitionKey
    @DynamoDbAttribute("companyId")
    public String getCompanyId() {
        return companyId;
    }

    @DynamoDbSecondaryPartitionKey(indexNames = "email-index")
    @DynamoDbAttribute("email")
    public String getEmail() {
        return email;
    }
}
