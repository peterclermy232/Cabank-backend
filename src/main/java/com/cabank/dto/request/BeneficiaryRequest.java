package com.cabank.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class BeneficiaryRequest {

    @NotBlank
    private String name;

    @NotBlank
    private String accountNumber;

    @NotBlank
    private String bankName;
}