package com.clearskye.epicconnector.jwtConfig;

import static com.clearskye.epicconnector.utils.EpicConstants.ACCESS_TOKEN_VALIDITY;
import static com.clearskye.epicconnector.utils.EpicConstants.BEARER;
import static com.clearskye.epicconnector.utils.EpicConstants.CLEARSKYE_ACCESSS_TOKEN;
import static com.clearskye.epicconnector.utils.EpicConstants.CLEARSKYE_EXPIRES_IN;
import static com.clearskye.epicconnector.utils.EpicConstants.TOKEN_TYP;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import javax.crypto.SecretKey;

import org.springframework.core.env.Environment;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.clearskye.epicconnector.utils.EpicConstants;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;

/**
 * Service class for managing JWT (JSON Web Token) operations.
 *
 * <p>This class provides methods for generating, validating, and parsing JWT tokens used
 * for authenticating and authorizing users. It typically includes functionality for creating
 * tokens with specific claims and validating tokens to ensure they are still valid and have
 * not been tampered with.</p>
 */
@Service
@RequiredArgsConstructor
public class JwtService {
    /**
     * Environment to access environment-specific properties.
     */
    private final Environment environment;
    /**
     * The BCryptPasswordEncoder instance used to encode credentials.
     */
    private final BCryptPasswordEncoder passwordEncoder;

    /**
     * Get Access and Refresh tokens.
     *
     * @return The generated JWT tokens.
     */
    public Map<String, String> getBothToken() {
        Map<String, Object> claims = new HashMap<>();
        claims.put(EpicConstants.CLAIM_NAME, EpicConstants.APP_NAME);
        claims.put(EpicConstants.TYPE.toLowerCase(), EpicConstants.ACCESS);
        Map<String, String> tokens = new HashMap<>();
        String accessToken = generateAccessToken(claims);
        tokens.put(EpicConstants.ACCESS_TOKEN_TYPE, accessToken);
        if (environment.getProperty(EpicConstants.CLEARSKYE_REFRESHTOKEN_SECRET) != null) {
            claims.put(EpicConstants.TYPE.toLowerCase(), EpicConstants.REFRESH);
            String refreshToken = generateRefreshToken(claims);
            tokens.put(EpicConstants.REFRESH_TOKEN, refreshToken);
        }
        tokens.put(EpicConstants.CLEARSKYE_EXPIRES_IN, EpicConstants.ACCESS_TOKEN_VALIDITY);
        return tokens;
    }

    /**
     * Generates new Access token.
     *
     * @return The generated JWT tokens.
     */
    public Map<String, String> getNewAccessToken() {
        Map<String, Object> claims = new HashMap<>();
        claims.put(EpicConstants.CLAIM_NAME, EpicConstants.APP_NAME);
        claims.put(EpicConstants.TYPE.toLowerCase(), EpicConstants.ACCESS);
        String accessToken = generateAccessToken(claims);
        Map<String, String> token = new HashMap<>();
        token.put(CLEARSKYE_ACCESSS_TOKEN, accessToken);
        token.put(TOKEN_TYP, BEARER);
        token.put(CLEARSKYE_EXPIRES_IN, ACCESS_TOKEN_VALIDITY);
        return token;
    }

    /**
     * Generates a JWT Access token.
     *
     * @param claims the claims to save in token.
     * @return the generated JWT token
     */
    public String generateAccessToken(Map<String, Object> claims) {
        return Jwts.builder()
                .header().add(EpicConstants.JWT_TOKEN_TYPE, EpicConstants.JWT).and()
                .claims(claims)
                .subject(passwordEncoder.encode(environment.getProperty(EpicConstants.CLEARSKYE_USERNAME_KEY)))
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 30))
                .signWith(getSignKey(EpicConstants.ACCESS_TOKEN_TYPE), Jwts.SIG.HS256).compact();
    }

    /**
     * Generates a JWT Refresh token.
     *
     * @param claims the claims to save in token.
     * @return the generated JWT token
     */
    public String generateRefreshToken(Map<String, Object> claims) {
        return Jwts.builder()
                .header().add(EpicConstants.JWT_TOKEN_TYPE, EpicConstants.JWT).and()
                .claims(claims)
                .subject(passwordEncoder.encode(environment.getProperty(EpicConstants.CLEARSKYE_USERNAME_KEY)))
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24 * 7))
                .signWith(getSignKey(EpicConstants.REFRESH_TOKEN), Jwts.SIG.HS256).compact();
    }

    /**
     * Retrieves the secret key used for signing JWT tokens.
     *
     * @param tokenType the type of token.
     * @return the secret key used for signing JWT tokens
     */
    private SecretKey getSignKey(String tokenType) {
        byte[] keyBytes = Decoders.BASE64.decode(environment.getProperty(EpicConstants.APP_NAME + EpicConstants.DOT + tokenType + EpicConstants.DOT + EpicConstants.SECRET));
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Extracts the exact username from the given JWT token.
     *
     * @param token     the JWT token from which the username is to be extracted
     * @param tokenType the JWT token type to identify the token type.
     * @return the exact username extracted from the token
     * @throws IllegalArgumentException if the token is invalid or cannot be parsed
     */
    public String extractUsername(String token, String tokenType) throws IllegalArgumentException {
        return extractClaim(token, tokenType, Claims::getSubject);
    }

    /**
     * Extracts the expiration date from the given JWT token.
     *
     * @param token     the JWT token from which the expiration date is to be extracted
     * @param tokenType the JWT token type to identify the token type.
     * @return the expiration date extracted from the token
     * @throws IllegalArgumentException if the token is invalid or cannot be parsed
     */
    public Date extractExpiration(String token, String tokenType) throws IllegalArgumentException {
        return extractClaim(token, tokenType, Claims::getExpiration);
    }

    /**
     * Extracts a specific piece of information from the JWT token using the provided ClaimsResolver.
     *
     * @param token          the JWT token from which the information is to be extracted
     * @param tokenType      the JWT token type to identify the token type.
     * @param claimsResolver a ClaimsResolver that defines how to extract the required information from the claims
     * @param <T>            the type of the information to be extracted
     * @return the information extracted from the token
     * @throws IllegalArgumentException if the token is invalid, cannot be parsed, or the claims cannot be resolved
     */
    public <T> T extractClaim(String token, String tokenType, Function<Claims, T> claimsResolver) throws IllegalArgumentException {
        final Claims claims = extractAllClaims(token, tokenType);
        return claimsResolver.apply(claims);
    }

    /**
     * Extracts all claims from the given JWT token.
     *
     * @param token the JWT token from which claims are to be extracted
     * @return the claims extracted from the token
     * @throws IllegalArgumentException if the token is invalid or cannot be parsed
     */
    private Claims extractAllClaims(String token, String tokenType) throws IllegalArgumentException {
        return Jwts
                .parser()
                .verifyWith(getSignKey(tokenType))
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Extracts a specific claim from the given JWT token based on the claim key.
     *
     * @param token     the JWT token from which the claim is to be extracted
     * @param claimKey  the key of the claim to be extracted
     * @param tokenType the JWT token type to identify the token type.
     * @return the value of the claim extracted from the token
     * @throws IllegalArgumentException if the token is invalid, cannot be parsed, or if the claim does not exist
     */
    public String getClaimFromToken(String token, String claimKey, String tokenType) throws IllegalArgumentException {
        Claims claims = extractAllClaims(token, tokenType);
        return claims.get(claimKey, String.class);
    }

    /**
     * Checks if the given JWT token has expired.
     *
     * @param token     the JWT token to be checked for expiration
     * @param tokenType the JWT token type to identify the token type.
     * @return true if the token is expired, false otherwise
     * @throws IllegalArgumentException if the token is invalid or cannot be parsed
     */
    private Boolean isTokenExpired(String token, String tokenType) throws IllegalArgumentException {
        return extractExpiration(token, tokenType).before(new Date());
    }

    /**
     * Validates the given JWT token.
     *
     * @param token     the JWT token to be validated
     * @param tokenType the JWT token type to identify the token type.
     * @return true if the token is valid and not expired, false otherwise
     * @throws IllegalArgumentException if the token is invalid or cannot be parsed
     */
    public Boolean validateToken(String token, String tokenType) throws IllegalArgumentException {
        final String username = extractUsername(token, tokenType);
        String type = this.getClaimFromToken(token, EpicConstants.TYPE.toLowerCase(), tokenType);
        String expectingType = (tokenType.equals(EpicConstants.ACCESS_TOKEN_TYPE)) ? EpicConstants.ACCESS : EpicConstants.REFRESH;
        if (!passwordEncoder.matches(environment.getProperty(EpicConstants.CLEARSKYE_USERNAME_KEY), username) || (type == null || !type.equals(expectingType))) {
            return false;
        }
        return (!isTokenExpired(token, tokenType));
    }
}