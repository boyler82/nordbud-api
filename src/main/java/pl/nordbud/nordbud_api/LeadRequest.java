package pl.nordbud.nordbud_api;

import jakarta.validation.constraints.*;

public record LeadRequest(
        @NotBlank String name,
        @Email @NotBlank String email,
        @Size(max = 30) String phone,
        @NotBlank @Size(max = 5000) String message,
        boolean consent,

        // HONEYPOT: to pole ma ZAWSZE być puste; boty je wypełniają
        @Size(max = 120) String website
) {}