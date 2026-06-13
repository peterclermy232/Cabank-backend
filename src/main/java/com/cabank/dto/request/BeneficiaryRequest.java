package com.cabank.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class BeneficiaryRequest {

    @NotBlank
    private String name;

    @NotBlank
    private String accountNumber;

    // Fix #8: bankName is optional — UI sends it as undefined when blank
    private String bankName;
}