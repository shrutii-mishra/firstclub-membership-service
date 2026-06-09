package com.firstclub.membership.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SubscribeRequest {
    @NotNull
    private Long userId;

    @NotNull
    private Long planId;

    @NotNull
    private Long tierId;
}
