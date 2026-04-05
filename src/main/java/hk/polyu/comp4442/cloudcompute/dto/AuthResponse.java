package hk.polyu.comp4442.cloudcompute.dto;

import java.time.LocalDateTime;

public class AuthResponse {

    private String message;
    private AuthUserResponse user;
    private String accessToken;
    private String refreshToken;
    private LocalDateTime timestamp;

    public AuthResponse() {
    }

    public AuthResponse(String message, String accessToken, String refreshToken, AuthUserResponse user) {
        this.message = message;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.user = user;
        this.timestamp = LocalDateTime.now();
    }

    // for register
    public AuthResponse(String message, AuthUserResponse user) {
        this.message = message;
        this.user = user;
        this.timestamp = LocalDateTime.now();
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public AuthUserResponse getUser() {
        return user;
    }

    public void setUser(AuthUserResponse user) {
        this.user = user;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}
