package com.shuttlebackend.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateSchoolRequest {
    @NotBlank
    @Size(max = 100)
    private String schoolName;
}

