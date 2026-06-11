package com.cabank.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AddCardRequest {

    @NotBlank(message = "Holder name is required")
    private String holderName;

    @NotBlank(message = "Brand is required")
    private String brand;

    @NotBlank(message = "Card type is required")
    private String cardType;

    @NotBlank(message = "Valid from is required")
    private String validFrom; // MM/yy

    @NotBlank(message = "Good thru is required")
    private String goodThru; // MM/yy
}