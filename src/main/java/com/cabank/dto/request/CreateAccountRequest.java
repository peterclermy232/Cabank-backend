package com.cabank.dto.request;

import com.cabank.entity.Account.AccountType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateAccountRequest {

    @NotBlank
    private String branch;

    @NotNull
    private AccountType type;

    private BigDecimal initialDeposit;
}