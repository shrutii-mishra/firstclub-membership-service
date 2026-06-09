package com.firstclub.membership.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateOrderRequest {
    @NotNull
    private Long userId;

    @NotNull
    @Positive
    private BigDecimal orderValue;
}
