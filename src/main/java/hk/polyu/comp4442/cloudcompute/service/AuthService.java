package hk.polyu.comp4442.cloudcompute.service;

import hk.polyu.comp4442.cloudcompute.dto.AuthResponse;
import hk.polyu.comp4442.cloudcompute.dto.AuthUserResponse;
import hk.polyu.comp4442.cloudcompute.dto.LoginRequest;
import hk.polyu.comp4442.cloudcompute.dto.RegisterRequest;
import hk.polyu.comp4442.cloudcompute.entity.AppUser;
import hk.polyu.comp4442.cloudcompute.entity.RefreshToken;
import hk.polyu.comp4442.cloudcompute.repository.AppUserRepository;
import hk.polyu.comp4442.cloudcompute.security.CustomUserDetails;
import hk.polyu.comp4442.cloudcompute.security.JwtUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final RefreshTokenService refreshTokenService;

    public AuthService(
            AppUserRepository appUserRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            JwtUtils jwtUtils, RefreshTokenService refreshTokenService) {
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
        this.refreshTokenService = refreshTokenService;
    }

    public AppUser register(RegisterRequest request) {
        if (appUserRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username already exists.");
        }
        if (appUserRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already exists.");
        }

        AppUser user = new AppUser();
        user.setUsername(request.getUsername().trim());
        user.setEmail(request.getEmail().trim().toLowerCase());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole("USER");
        return appUserRepository.save(user);
    }

    public AuthResponse login(LoginRequest request, HttpServletRequest httpRequest) {
        // Authenticate with exist method
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        // Generate Access Token
        String accessToken = jwtUtils.generateTokenFromUsername(userDetails.getUsername());

        // Generate Refresh Token in DB
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(userDetails.getUser().getId());

        // Return the message using the fixed constructor
        return new AuthResponse(
                "Login successful.",
                accessToken,
                refreshToken.getToken(),
                new AuthUserResponse(userDetails.getUser()));
    }

    public void logout(HttpServletRequest httpRequest) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof CustomUserDetails customUserDetails) {
                refreshTokenService.revokeByUserId(customUserDetails.getId());
            }
            new SecurityContextLogoutHandler().logout(httpRequest, null, authentication);
        }
    }

    public String generateAccessToken(String username) {
        return jwtUtils.generateTokenFromUsername(username);
    }

}
