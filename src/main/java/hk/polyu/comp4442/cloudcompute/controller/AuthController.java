package hk.polyu.comp4442.cloudcompute.controller;

import hk.polyu.comp4442.cloudcompute.dto.AuthResponse;
import hk.polyu.comp4442.cloudcompute.dto.AuthUserResponse;
import hk.polyu.comp4442.cloudcompute.dto.LoginRequest;
import hk.polyu.comp4442.cloudcompute.dto.RegisterRequest;
import hk.polyu.comp4442.cloudcompute.entity.AppUser;
import hk.polyu.comp4442.cloudcompute.security.CustomUserDetails;
import hk.polyu.comp4442.cloudcompute.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AppUser created = authService.register(request);
        AuthResponse response = new AuthResponse("Registration successful.", new AuthUserResponse(created));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        AppUser loggedIn = authService.login(request, httpRequest);
        AuthResponse response = new AuthResponse("Login successful.", new AuthUserResponse(loggedIn));
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<AuthResponse> logout(HttpServletRequest httpRequest) {
        authService.logout(httpRequest);
        AuthResponse response = new AuthResponse("Logout successful.", null);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public ResponseEntity<AuthUserResponse> me(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(new AuthUserResponse(userDetails.getUser()));
    }
}
