package com.clearskye.epicconnector.service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.clearskye.epicconnector.utils.EpicConstants;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

/**
 * Service class for managing connections to the Epic REST API.
 */
@Service
@RequiredArgsConstructor
public class EpicConnectionService {
    /**
     * Epic Client Service for Epic connection.
     */
    private final EpicClientService epicClientService;
    /**
     * Object Mapper for the Epic Connection service.
     */
    private static final ObjectMapper objectMapper = new ObjectMapper();
    /**
     * CloseableHttpClient for Epic Connection.
     */
    private CloseableHttpClient client;
    /**
     * Logger instance for logging CommonController events.
     */
    private static final Logger logger = LogManager.getLogger(EpicConnectionService.class);

    /**
     * Enum for Epic Connection operation types.
     */
    public enum HttpOperationType {
        /**
         * Get Method.
         */
        GET,
        /**
         * Post Method.
         */
        POST,
        /**
         * Put Method.
         */
        PUT
    }

    /**
     * Converting Request Map to URL parameters.
     *
     * @param requestMap Map containing all the attributes to be set as parameters of the connection request
     * @return param String equivalent of URL parameters of the requestMap.
     */
    private String getDataInParams(Map<String, Object> requestMap) {
        try {
            StringBuilder param = new StringBuilder(EpicConstants.QUERY_SYMBOL);
            requestMap.forEach((key, value) -> {
                param.append(key).append(EpicConstants.EQUALS_SYMBOL).append(value).append(EpicConstants.LOGICAL_AND_SYMBOL);
            });
            return (param.substring(0, param.length() - 1)).replaceAll(
                    EpicConstants.WHITE_SPACE_CHARACTER, EpicConstants.ENCODED_SPACE_CHARACTER);
        } catch (Exception ex) {
            return EpicConstants.EMPTY_STRING;
        }
    }

    /**
     * Execute the Request for Epic operations.
     *
     * @param url             Epic Connection URL.
     * @param operationType   HTTP Method type like POST, PUT, GET, DELETE.
     * @param paramRequestMap RequestPayload to be included along with URL.
     * @param bodyRequestMap  RequestPayload to be sent in the connection body
     * @return responseMap Map containing the JSON response sent by Epic
     * @throws Exception Exception during api request.
     */
    public ResponseEntity<Map<String, Object>> executeRequest(String url, HttpOperationType operationType,
            Map<String, Object> paramRequestMap, Map<String, Object> bodyRequestMap) throws Exception {
        Map<String, Object> responseMap = new HashMap<String, Object>();
        int responseCode;
        HttpRequestBase httpRequest = null;
        CloseableHttpResponse response = null;
        if (paramRequestMap != null) {
            url += getDataInParams(paramRequestMap);
        }
        try {
            switch (operationType) {
            case GET:
                httpRequest = new HttpGet(url);
                break;
            case POST:
                httpRequest = new HttpPost(url);
                if (!bodyRequestMap.isEmpty()) {
                    ((HttpPost) httpRequest).setEntity(new StringEntity(objectMapper
                            .writeValueAsString(bodyRequestMap)));
                }
                break;
            case PUT:
                httpRequest = new HttpPut(url);
                if (!bodyRequestMap.isEmpty()) {
                    ((HttpPut) httpRequest).setEntity(new StringEntity(objectMapper
                            .writeValueAsString(bodyRequestMap)));
                }
                break;
            }
            client = epicClientService.getHttpClientInstance();
            response = client.execute(httpRequest);
            String responses = EntityUtils.toString(response.getEntity(),
                    StandardCharsets.UTF_8);
            responseCode = response.getStatusLine().getStatusCode();
            if (responseCode >= HttpStatus.SC_MULTIPLE_CHOICES) {
                return ResponseEntity.status(responseCode).body(objectMapper.readValue(responses, new TypeReference<Map<String, Object>>() {
                }));
            }
            responseMap = objectMapper.readValue(responses, new TypeReference<Map<String, Object>>() {
            });
        } finally {
            if (httpRequest != null) {
                httpRequest.releaseConnection();
            }
            if (response != null) {
                try {
                    response.close();
                } catch (IOException ex) {
                    logger.error(MessageFormat.format("Epic REST API connection close failed :  {0}", ex.getMessage()));
                }
            }
        }
        return ResponseEntity.status(responseCode).body(responseMap);
    }
}