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
public class Product {

    private String productId;
    private String companyId;
    private String productName;
    private String productCategory;
    private Long unitsSold;
    private Double conversionFactor;
    private String financialYear;
    private Long eprObligationUnits;
    private String createdAt;

    @DynamoDbPartitionKey
    @DynamoDbAttribute("productId")
    public String getProductId() {
        return productId;
    }

    @DynamoDbSortKey
    @DynamoDbAttribute("companyId")
    public String getCompanyId() {
        return companyId;
    }
}
