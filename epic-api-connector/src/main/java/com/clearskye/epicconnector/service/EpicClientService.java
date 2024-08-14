package com.clearskye.epicconnector.service;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;


import org.apache.http.Consts;
import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.clearskye.epicconnector.controller.EpicCommonController;
import com.clearskye.epicconnector.exception.CustomInvalidCredentialException;
import com.clearskye.epicconnector.utils.EpicConstants;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

/**
 * A client class for Epic.
 */
@Service
@RequiredArgsConstructor
public class EpicClientService {
    /**
     * Environment to access environment-specific properties.
     */
    private final Environment environment;
    /**
     * Cache Service used for managing and interacting with the application's cache.
     */
    private final CacheService cacheService;
    /**
     * Logger instance for logging CommonController events.
     */
    private static final Logger logger = LogManager.getLogger(EpicCommonController.class);
    /**
     * The HTTP client builder.
     */
    private HttpClientBuilder clientBuilder = null;
    /**
     * Object Mapper for the Epic Client service.
     */
    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Retrieve the HTTP Client.
     *
     * @return httpClient Closable HTTP Client of Connector
     * @throws Exception Exception during authentication
     */
    public CloseableHttpClient getHttpClientInstance() throws Exception {
        this.clientBuilder = HttpClients.custom();
        RequestConfig.Builder requestConfig = RequestConfig.custom().setConnectTimeout(EpicConstants.DEFAULT_MAX_TIMEOUT * 1000).setSocketTimeout(EpicConstants.DEFAULT_MAX_TIMEOUT * 1000).setConnectionRequestTimeout(EpicConstants.DEFAULT_MAX_TIMEOUT * 1000);
        clientBuilder.setDefaultRequestConfig(requestConfig.build());
        obtainAccessToken();
        List<Header> headers = new ArrayList<Header>();
        headers.add(new BasicHeader(HttpHeaders.CONTENT_TYPE, EpicConstants.APPLICATION_JSON));
        headers.add(new BasicHeader(HttpHeaders.AUTHORIZATION, EpicConstants.TOKEN_TYPE + cacheService.verifyTokenAndGetDataFromCache(EpicConstants.ACCESS_TOKEN)));

        clientBuilder.setDefaultHeaders(headers);
        return clientBuilder.build();
    }

    /**
     * Retrieve the Access Token.
     *
     * @throws Exception Exception during obtain access token.
     */
    public void obtainAccessToken() throws Exception {
        Map<String, Object> responseMap = new HashMap<String, Object>();
        HttpRequestBase httpRequest = null;
        CloseableHttpResponse response = null;
        CloseableHttpClient client = null;
        try {
            if ((cacheService.verifyTokenAndGetDataFromCache(EpicConstants.ACCESS_TOKEN) == null || cacheService.verifyTokenAndGetDataFromCache(EpicConstants.EXPIRES_IN) == null) || (Long.valueOf(cacheService.verifyTokenAndGetDataFromCache(EpicConstants.EXPIRES_IN)).compareTo(System.currentTimeMillis() / 1000) < 0)) {
                logger.info("Generating new epic Access Token...");
                Long currentTime = System.currentTimeMillis() / 1000;
                List<Header> headers = new ArrayList<Header>();
                headers.add(new BasicHeader(HttpHeaders.CONTENT_TYPE, EpicConstants.APPLICATION_FORM_URL_ENCODED));
                headers.add(new BasicHeader(HttpHeaders.ACCEPT, EpicConstants.APPLICATION_JSON));
                HttpClientBuilder clientBuilder = HttpClients.custom();
                clientBuilder.setDefaultHeaders(headers);
                client = clientBuilder.build();
                List<NameValuePair> form = new ArrayList<>();
                form.add(new BasicNameValuePair(EpicConstants.GRANT_TYPE, EpicConstants.CLIENT_CREDENTIALS));
                form.add(new BasicNameValuePair(EpicConstants.CLIENT_ASSERTION_TYPE, EpicConstants.JWT_URN));
                form.add(new BasicNameValuePair(EpicConstants.CLIENT_ASSERTION, generateSignedJwtToken(environment.getProperty(EpicConstants.CLIENT_ID), environment.getProperty(EpicConstants.PRIVATE_KEY))));
                UrlEncodedFormEntity entity = new UrlEncodedFormEntity(form, Consts.UTF_8);
                httpRequest = new HttpPost(environment.getProperty(EpicConstants.REST_ENDPOINT) + EpicConstants.EPIC_ACCESS_TOKEN_ENDPOINT);
                ((HttpPost) httpRequest).setEntity(entity);
                response = client.execute(httpRequest);
                int responseCode = response.getStatusLine().getStatusCode();
                if (responseCode != HttpStatus.SC_OK) {
                    logger.error(MessageFormat.format("Unable to obtain access token from Epic - Bad Request: {0}", EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8)));
                    throw new CustomInvalidCredentialException("Unable to obtain access token from Epic  " + EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8));
                }
                if (response.getEntity() != null) {
                    String responses = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                    responseMap = objectMapper.readValue(responses, new TypeReference<Map<String, Object>>() {
                    });
                    if (responseMap.get(EpicConstants.ACCESS_TOKEN) != null) {
                        cacheService.saveTokenToCache(EpicConstants.ACCESS_TOKEN, responseMap.get(EpicConstants.ACCESS_TOKEN).toString());
                        cacheService.saveTokenToCache(EpicConstants.EXPIRES_IN, String.valueOf(currentTime + Long.parseLong(responseMap.get(EpicConstants.EXPIRES_IN).toString())));
                        logger.info("Obtain access token from epic success");
                    } else {
                        logger.error(MessageFormat.format("Unable to obtain access token from Epic - Bad Request: {0}", EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8)));
                        throw new CustomInvalidCredentialException("Unable to obtain access token from Epic  " + EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8));
                    }
                }
            }
        } finally {
            if (httpRequest != null) {
                httpRequest.releaseConnection();
            }
            if (response != null) {
                try {
                    response.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    /**
     * Generate the JWT token.
     *
     * @param clientId   Epic ClientId required to generate Jwt token to get
     *                   access token from Epic
     * @param privateKey GuardedString needed to generate private key from
     *                   PKCS8EncodedKeySpec
     * @return token JWT Token to be sent to epic to get Access Token
     * @throws Exception Exception during authentication
     */
    public String generateSignedJwtToken(String clientId, String privateKey) throws Exception {
        Algorithm algorithm = Algorithm.RSA256(null, (RSAPrivateKey) getPrivateKey(privateKey));
        return JWT.create().withHeader(EpicConstants.EPIC_JWT_HEADER).withIssuer(clientId).withSubject(clientId).withAudience(environment.getProperty(EpicConstants.REST_ENDPOINT) + EpicConstants.EPIC_ACCESS_TOKEN_ENDPOINT).withJWTId(MessageFormat.format(EpicConstants.FORMAT, getRandomString(4), getRandomString(5), getRandomString(5), getRandomString(5), getRandomString(4))).withExpiresAt(new Date((System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(5)))).sign(algorithm);
    }

    /**
     * Retrieve the Private Key.
     *
     * @param privateKey GuardedString needed to generate private key from
     *                   PKCS8EncodedKeySpec
     * @return privKey Private Key used to sign the JWT Token
     * @throws NoSuchAlgorithmException No Algorithm Exception while generating
     *                                  Private Key
     * @throws InvalidKeySpecException  Invalid Key Spec Exception while generating
     *                                  Private Key
     * @throws IOException              IO Exception while generating Private Key
     */
    private static PrivateKey getPrivateKey(String privateKey) throws NoSuchAlgorithmException, InvalidKeySpecException, IOException {
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(Base64.getDecoder().decode(privateKey));
        KeyFactory kf = KeyFactory.getInstance(EpicConstants.RSA);
        return kf.generatePrivate(keySpec);
    }

    /**
     * Generate the Random String.
     *
     * @param length Number of characters to be present in the result
     * @return response Alpha numeric random string with the length specified
     * @throws Exception Exception sent by random generator
     */
    public static String getRandomString(int length) throws Exception {
        byte[] array = new byte[256];
        new Random().nextBytes(array);
        String randomString = new String(array, Charset.forName(EpicConstants.UTF));
        StringBuffer response = new StringBuffer();
        for (int k = 0; k < randomString.length(); k++) {
            char ch = randomString.charAt(k);
            if (((ch >= 'A' && ch <= 'Z') || (ch >= '1' && ch <= '9')) && (length > 0)) {
                response.append(ch);
                length--;
            }
        }
        return response.toString();
    }
}