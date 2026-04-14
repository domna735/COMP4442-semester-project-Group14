package hk.polyu.comp4442.cloudcompute.security;

import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class JwtUtils {

    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

    @Value("${jwt.private-key-path}")
    private Resource privateKeyResource;

    @Value("${jwt.public-key-path}")
    private Resource publicKeyResource;

    @Value("${jwt.expiration-ms}")
    private int jwtExpirationMs;

    private PrivateKey cachedPrivateKey;
    private PublicKey cachedPublicKey;

    // Helper to convert the PEM file content into key

    // This runs once after the object is created now, reduce resource usage
    @jakarta.annotation.PostConstruct
    public void init() {
        try {
            this.cachedPrivateKey = loadPrivateKey();
            this.cachedPublicKey = loadPublicKey();
            logger.info("ECDSA Keys loaded successfully.");
        } catch (Exception e) {
            logger.error("Failed to load ECDSA keys: {}", e.getMessage());
            throw new RuntimeException("Could not initialize JWT keys", e);
        }
    }

    private PrivateKey loadPrivateKey() throws Exception {
        String key = new String(privateKeyResource.getInputStream().readAllBytes())
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", "");
        byte[] keyBytes = Base64.getDecoder().decode(key);
        return KeyFactory.getInstance("EC").generatePrivate(new PKCS8EncodedKeySpec(keyBytes));
    }

    private PublicKey loadPublicKey() throws Exception {
        String key = new String(publicKeyResource.getInputStream().readAllBytes())
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s", "");
        byte[] keyBytes = Base64.getDecoder().decode(key);
        return KeyFactory.getInstance("EC").generatePublic(new X509EncodedKeySpec(keyBytes));
    }

    public String generateToken(Authentication authentication) throws Exception {
        CustomUserDetails userPrincipal = (CustomUserDetails) authentication.getPrincipal();

        return Jwts.builder()
                .subject(userPrincipal.getUsername())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(cachedPrivateKey) // Signs with ECDSA Private Key
                .compact();
    }

    public String getUserNameFromJwtToken(String token) throws Exception {
        return Jwts.parser()
                .verifyWith(cachedPublicKey) // Use Public Key to verify ECDSA signature
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    public boolean validateJwtToken(String authToken) {
        // fix bug that user does not have access token
        if (authToken == null || authToken.trim().isEmpty() || !authToken.contains(".")) {
            logger.warn("Received empty or invalid JWT string");
            return false;
        }

        try {
            Jwts.parser().verifyWith(cachedPublicKey).build().parse(authToken);
            return true;
        } catch (Exception e) {
            logger.error("Unexpected error validating JWT: {}", e.getMessage());
        }
        return false;
    }

    public String generateTokenFromUsername(String username) {
        try {
            return Jwts.builder()
                    .subject(username)
                    .issuedAt(new Date())
                    .expiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                    .signWith(cachedPrivateKey) // Signs with ECDSA Private Key
                    .compact();
        } catch (Exception e) {
            logger.error("Could not generate token: {}", e.getMessage());
            return null;
        }
    }
}