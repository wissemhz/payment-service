package com.localhub.paymentservice.configuration;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "internal.api")
@Validated
public record InternalApiProperties(
    @NotBlank String key
) {}
