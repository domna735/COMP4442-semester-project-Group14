package hk.polyu.comp4442.cloudcompute.dto;

import java.time.LocalDateTime;

public class AuthResponse {

    private String message;
    private AuthUserResponse user;
    private LocalDateTime timestamp;

    public AuthResponse() {
    }

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
}
