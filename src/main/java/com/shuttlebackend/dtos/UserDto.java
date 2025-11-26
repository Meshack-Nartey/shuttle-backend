package com.shuttlebackend.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class UserDto {
    private Integer userId;
    private String email;
    private String role;
    private String createdAt;
    private String updatedAt;
}
