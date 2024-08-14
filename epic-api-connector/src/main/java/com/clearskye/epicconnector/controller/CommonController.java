package com.clearskye.epicconnector.controller;


import static com.clearskye.epicconnector.utils.EpicConstants.CLEARSKYE_PASSWORD_KEY;
import static com.clearskye.epicconnector.utils.EpicConstants.CLEARSKYE_USERNAME_KEY;
import static com.clearskye.epicconnector.utils.EpicConstants.REFRESH;
import static com.clearskye.epicconnector.utils.EpicConstants.REFRESH_TOKEN;
import static com.clearskye.epicconnector.utils.EpicConstants.TYPE;

import java.text.MessageFormat;
import java.util.Map;

import org.apache.http.HttpStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.clearskye.epicconnector.dto.AuthRequest;
import com.clearskye.epicconnector.jwtConfig.JwtService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;


/**
 * Controller class for handling JWT-based authentication and user-related requests.
 */
@RestController
@RequestMapping("/auth")
@Validated
@RequiredArgsConstructor
public class CommonController {
    /**
     * Environment to access environment-specific properties.
     */
    private final Environment environment;
    /**
     * JWT service instance for handling JSON Web Token operations.
     */
    private final JwtService jwtService;
    /**
     * Logger instance for logging CommonController events.
     */
    private static final Logger logger = LogManager.getLogger(CommonController.class);

    /**
     * Generates an access token for the given user details.
     *
     * @param authRequest The user details for which the token is to be generated.
     * @return A ResponseEntity containing the access and refresh tokens.
     */
    @PostMapping(value = "/generateToken")
    public ResponseEntity<?> generateToken(@Valid @RequestBody AuthRequest authRequest) {
        try {
            if (authRequest.getUserName().equals(environment.getProperty(CLEARSKYE_USERNAME_KEY)) && authRequest.getPassword().equals(environment.getProperty(CLEARSKYE_PASSWORD_KEY))) {
                Map<String, String> tokens = jwtService.getBothToken();
                logger.info("Access and Refresh token generated successfully.");
                return ResponseEntity.status(HttpStatus.SC_OK).body(tokens);
            } else {
                logger.error(MessageFormat.format("Authentication Failed with userName: {0}",
                        authRequest.getUserName()));
                return ResponseEntity.status(HttpStatus.SC_BAD_REQUEST).body("Invalid Credentials");
            }
        } catch (Exception ex) {
            logger.error(MessageFormat.format("Generate Access and Refresh Token Failed , because {0}", ex.getMessage()));
            return ResponseEntity.status(HttpStatus.SC_INTERNAL_SERVER_ERROR).body(ex.getMessage());
        }
    }

    /**
     * Generates a new access token using a valid refresh token.
     *
     * @param refreshTokenRequest The refresh token used to generate a new access token
     * @return A ResponseEntity containing the new access token
     */
    @PostMapping("/refreshToken")
    public ResponseEntity<?> refreshToken(@RequestBody Map<String, String> refreshTokenRequest) {
        // Validate the refresh token
        try {
            String refreshToken = refreshTokenRequest.get(REFRESH_TOKEN);
            String type = jwtService.getClaimFromToken(refreshToken, TYPE.toLowerCase(), REFRESH_TOKEN);
            if (refreshTokenRequest.get(REFRESH_TOKEN).isBlank() || !jwtService.validateToken(refreshTokenRequest.get(REFRESH_TOKEN), REFRESH_TOKEN) ||
                    (type == null || !type.equals(REFRESH))) {
                logger.error(MessageFormat.format("Get Access Token using Refresh Token operation Failed, with token {0} ", refreshTokenRequest.get(REFRESH_TOKEN)));
                return ResponseEntity.status(HttpStatus.SC_UNAUTHORIZED).body("Refresh token is not valid!");
            }
            // Generate a new access token
            Map<String, String> token = jwtService.getNewAccessToken();
            logger.info("Get new Access Token using Refresh Token operation success.");
            return ResponseEntity.status(HttpStatus.SC_OK).body(token);
        } catch (Exception ex) {
            logger.error(MessageFormat.format("Get Access Token using Refresh Token operation Failed, with token {0} and Because of :  ", refreshTokenRequest.get(REFRESH_TOKEN), ex.getMessage()));
            return ResponseEntity.status(HttpStatus.SC_BAD_REQUEST).body("Refresh token is not valid!");
        }
    }
}