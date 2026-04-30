package hk.polyu.comp4442.cloudcompute.service;

import hk.polyu.comp4442.cloudcompute.dto.AuthResponse;
import hk.polyu.comp4442.cloudcompute.dto.AuthUserResponse;
import hk.polyu.comp4442.cloudcompute.dto.LoginRequest;
import hk.polyu.comp4442.cloudcompute.dto.RegisterRequest;
import hk.polyu.comp4442.cloudcompute.entity.AppUser;
import hk.polyu.comp4442.cloudcompute.entity.RefreshToken;
import hk.polyu.comp4442.cloudcompute.exception.EmailAlreadyExistsException;
import hk.polyu.comp4442.cloudcompute.exception.UsernameAlreadyExistsException;
import hk.polyu.comp4442.cloudcompute.repository.AppUserRepository;
import hk.polyu.comp4442.cloudcompute.security.CustomUserDetails;
import hk.polyu.comp4442.cloudcompute.security.JwtUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Service;

@Service
@Slf4j // ENABLE LOGGING (SAVE TO FILE)
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
        String username = request.getUsername();
        String email = request.getEmail();

        log.info("Received registration request - Username: {}, Email: {}", username, email);

        // FIX: Custom Exception for Duplicate Username
        if (appUserRepository.existsByUsername(username)) {
            log.error("Registration failed: Username already exists -> {}", username);
            throw new UsernameAlreadyExistsException("Username already exists: " + username);
        }

        // FIX: Custom Exception for Duplicate Email
        if (appUserRepository.existsByEmail(email)) {
            log.error("Registration failed: Email already exists -> {}", email);
            throw new EmailAlreadyExistsException("Email already exists: " + email);
        }

        AppUser user = new AppUser();
        user.setUsername(username.trim());
        user.setEmail(email.trim().toLowerCase());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole("USER");

        AppUser savedUser = appUserRepository.save(user);
        log.info("User registered successfully - Username: {}", username);
        return savedUser;
    }

    public AuthResponse login(LoginRequest request, HttpServletRequest httpRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        String accessToken = jwtUtils.generateTokenFromUsername(userDetails.getUsername());
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(userDetails.getUser().getId());

        log.info("User login successful - Username: {}", userDetails.getUsername());
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
                log.info("User logged out - Username: {}", customUserDetails.getUsername());
            }
            new SecurityContextLogoutHandler().logout(httpRequest, null, authentication);
        }
    }

    public String generateAccessToken(String username) {
        return jwtUtils.generateTokenFromUsername(username);
    }
}
