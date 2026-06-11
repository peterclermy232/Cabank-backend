package com.cabank.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BeneficiaryResponse {
    private String id;
    private String name;
    private String accountNumber;
    private String bankName;
}