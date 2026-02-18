package com.jvmdevelop.strife.reqandresp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private String message;
    
    public static AuthResponse success(String token) {
        return new AuthResponse(token, "Authentication successful");
    }
    
    public static AuthResponse error(String message) {
        return new AuthResponse(null, message);
    }
}
