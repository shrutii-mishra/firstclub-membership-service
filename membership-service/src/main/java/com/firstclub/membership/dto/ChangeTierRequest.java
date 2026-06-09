package com.firstclub.membership.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ChangeTierRequest {
    @NotNull
    private Long tierId;
}
