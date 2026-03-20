package com.mxmovies.booking.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class PaymentRequest {

    @NotBlank(message = "Payment reference is required")
    private String paymentReference;

}
