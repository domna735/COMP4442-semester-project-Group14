package hk.polyu.comp4442.cloudcompute.dto;
import jakarta.validation.constraints.NotBlank;


public class TokenRefreshRequest {
    @NotBlank
    private String refreshToken;

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}