package com.dev.LMS.dto;

public class LoginResponseDto {
    private String message;
    private String token;
    private RbacUserDto user;

    public LoginResponseDto(String message, String token, RbacUserDto user) {
        this.message = message;
        this.token = token;
        this.user = user;
    }

    public String getMessage() {
        return message;
    }

    public String getToken() {
        return token;
    }

    public RbacUserDto getUser() {
        return user;
    }
}
