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

    // Helper to convert the PEM file content into key

    private PrivateKey getPrivateKey() throws Exception {
        String key = new String(privateKeyResource.getInputStream().readAllBytes())
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", "");

        byte[] keyBytes = Base64.getDecoder().decode(key);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);

        // Use "EC" for ECDSA keys
        KeyFactory kf = KeyFactory.getInstance("EC");
        return kf.generatePrivate(spec);
    }

    private PublicKey getPublicKey() throws Exception {
        String key = new String(publicKeyResource.getInputStream().readAllBytes())
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s", "");

        byte[] keyBytes = Base64.getDecoder().decode(key);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);

        // Use "EC" for ECDSA
        KeyFactory kf = KeyFactory.getInstance("EC");
        return kf.generatePublic(spec);
    }

    public String generateToken(Authentication authentication) throws Exception {
        CustomUserDetails userPrincipal = (CustomUserDetails) authentication.getPrincipal();

        return Jwts.builder()
                .subject(userPrincipal.getUsername())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(getPrivateKey()) // Signs with ECDSA Private Key
                .compact();
    }

    public String getUserNameFromJwtToken(String token) throws Exception {
        return Jwts.parser()
                .verifyWith(getPublicKey()) // Use Public Key to verify ECDSA signature
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    public boolean validateJwtToken(String authToken) {
        try {
            Jwts.parser().verifyWith(getPublicKey()).build().parse(authToken);
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
                    .signWith(getPrivateKey()) // Signs with ECDSA Private Key
                    .compact();
        } catch (Exception e) {
            logger.error("Could not generate token: {}", e.getMessage());
            return null;
        }
    }
}