package com.cabank.dto.request;

import lombok.Data;

@Data
public class UpdateProfileRequest {

    private String name;

    private String phone;
}