package com.shuttlebackend.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class ChangePassword {
    private String oldPassword;
    private String newPassword;
}
