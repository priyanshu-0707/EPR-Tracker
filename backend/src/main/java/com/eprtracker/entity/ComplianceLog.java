package com.eprtracker.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@DynamoDbBean
public class ComplianceLog {

    private String logId;
    private String companyId;
    private String recyclerId;
    private String recyclerName;
    private Double quantityCollectedKg;
    private String eprCertificateNumber;
    private String dateLogged;
    private String financialYear;

    @DynamoDbPartitionKey
    @DynamoDbAttribute("logId")
    public String getLogId() {
        return logId;
    }

    @DynamoDbSortKey
    @DynamoDbAttribute("companyId")
    public String getCompanyId() {
        return companyId;
    }
}
