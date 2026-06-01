package com.jvmdevelop.strife.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@lombok.NoArgsConstructor
@Builder
public class UserDto {
    private String username;
    private String email;
    private String password;
    private String description;
    private String role;
    private String avatarUrl;
}
