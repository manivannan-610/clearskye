package com.clearskye.epicconnector.controller;

import static com.clearskye.epicconnector.utils.EpicConstants.EMP;
import static com.clearskye.epicconnector.utils.EpicConstants.EPIC_ACTIVATE_USER_ENDPOINT;
import static com.clearskye.epicconnector.utils.EpicConstants.EPIC_COMPLEX_TYPE_ATTR_SET;
import static com.clearskye.epicconnector.utils.EpicConstants.EPIC_CREATE_USER_ENDPOINT;
import static com.clearskye.epicconnector.utils.EpicConstants.EPIC_DEACTIVATE_USER_ENDPOINT;
import static com.clearskye.epicconnector.utils.EpicConstants.EPIC_DELETE_USER_ENDPOINT;
import static com.clearskye.epicconnector.utils.EpicConstants.EPIC_GET_USER_ENDPOINT;
import static com.clearskye.epicconnector.utils.EpicConstants.EPIC_MULTI_VALUED_ATTR_SET;
import static com.clearskye.epicconnector.utils.EpicConstants.EPIC_UPDATE_USER_ENDPOINT;
import static com.clearskye.epicconnector.utils.EpicConstants.EXTERNAL;
import static com.clearskye.epicconnector.utils.EpicConstants.GROUP;
import static com.clearskye.epicconnector.utils.EpicConstants.ID;
import static com.clearskye.epicconnector.utils.EpicConstants.RESPONSE_MESSAGE;
import static com.clearskye.epicconnector.utils.EpicConstants.OBJECT_NOT_USER;
import static com.clearskye.epicconnector.utils.EpicConstants.USER_ID_TYPE_FIELD;
import static com.clearskye.epicconnector.utils.EpicConstants.PROVIDER_ID;
import static com.clearskye.epicconnector.utils.EpicConstants.RECORDLIST;
import static com.clearskye.epicconnector.utils.EpicConstants.REST_ENDPOINT;
import static com.clearskye.epicconnector.utils.EpicConstants.SEARCH_CONTEXT;
import static com.clearskye.epicconnector.utils.EpicConstants.TYPE;
import static com.clearskye.epicconnector.utils.EpicConstants.UID;
import static com.clearskye.epicconnector.utils.EpicConstants.USERGROUPS;
import static com.clearskye.epicconnector.utils.EpicConstants.USERS;
import static com.clearskye.epicconnector.utils.EpicConstants.USER_IDS;
import static com.clearskye.epicconnector.utils.EpicConstants.USER_ID_FIELD;
import static com.clearskye.epicconnector.utils.EpicConstants.USER_ID_TYPE_VALUE;
import static com.clearskye.epicconnector.utils.EpicConstants.XML_RECORD_ID;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.http.HttpStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.clearskye.epicconnector.dto.GroupUpdateRequestDto;
import com.clearskye.epicconnector.dto.PasswordUpdateDto;
import com.clearskye.epicconnector.dto.UserIdRequestDto;
import com.clearskye.epicconnector.service.EpicConnectionService;
import com.clearskye.epicconnector.service.EpicSoapConnectionService;
import com.clearskye.epicconnector.service.EpicUserUtilityService;
import com.clearskye.epicconnector.utils.EpicConstants;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * REST controller for managing users.
 */
@RestController
@RequestMapping("/epic/user")
@Validated
@RequiredArgsConstructor
public class EpicUserController {
    /**
     * Object Mapper for the Epic User Controller.
     */
    private static final ObjectMapper objectMapper = new ObjectMapper();
    /**
     * Logger instance for logging EpicUserController events.
     */
    private static final Logger logger = LogManager.getLogger(EpicUserController.class);
    /**
     * Connection for Epic REST API.
     */
    private final EpicConnectionService epicConnectionService;
    /**
     * Connection for Epic SOAP API.
     */
    private final EpicSoapConnectionService epicSoapConnectionService;
    /**
     * Epic User Utility Service used for user-related utility functions.
     */
    private final EpicUserUtilityService epicUserUtilityService;
    /**
     * Environment to access environment-specific properties.
     */
    private final Environment environment;

    /**
     * POST /createUser : Create a new user.
     *
     * @param createAttributes The user to create.
     * @return The ResponseEntity with status 201 (Created) and with new user id, or with status 400 (Bad Request) if the user has already an ID.
     */
    @PostMapping(value = "/createUser")
    public ResponseEntity<?> createUser(@RequestBody Map<String, Object> createAttributes) {
        Map<String, String> uidMap = new HashMap<>();
        try {
            logger.info(MessageFormat.format("Creating User with attributes: {0}", createAttributes));
            Map<String, Object> paramRequestMap = new HashMap<>();
            Map<String, Object> bodyRequestMap = new HashMap<>();
            Map<String, Object> requestMap = epicUserUtilityService.buildRequestPayload(createAttributes);
            requestMap.forEach((attrName, attrValue) -> {
                if (EPIC_COMPLEX_TYPE_ATTR_SET.contains(attrName) || EPIC_MULTI_VALUED_ATTR_SET
                        .contains(attrName) || attrName.equals(EpicConstants.TEMPLATES_CONFIG) || attrName.equals(EpicConstants.COMPLEX_NAME)
                        || attrName.equals(EpicConstants.EPIC_ATTR_DEFAULT_LOGIN_DEPT_ID)
                        || attrName.equals(EpicConstants.EPIC_ATTR_PRIMARY_MANAGER)
                        || attrName.equals(EpicConstants.DEFAULT_TEMPLATE_ID) || attrName.equals(PROVIDER_ID)) {
                    bodyRequestMap.put(attrName, attrValue);
                } else {
                    paramRequestMap.put(attrName, attrValue);
                }
            });
            String url = environment.getProperty(REST_ENDPOINT) + EPIC_CREATE_USER_ENDPOINT;
            ResponseEntity<Map<String, Object>> responseEntity = epicConnectionService.executeRequest(url, EpicConnectionService.HttpOperationType.PUT, paramRequestMap, bodyRequestMap);
            if (responseEntity.getStatusCode().value() >= HttpStatus.SC_MULTIPLE_CHOICES) {
                logger.error(MessageFormat.format("Epic create user failed, because {0}", responseEntity.getBody()));
                return responseEntity;
            }
            Map<String, Object> responseMap = objectMapper.convertValue(responseEntity.getBody(), new TypeReference<Map<String, Object>>() {
            });
            objectMapper.convertValue(responseMap.get(USER_IDS), new TypeReference<List<Map<String, String>>>() {
            }).forEach(data -> {
                if (data.get(TYPE).equalsIgnoreCase(EXTERNAL)) {
                    uidMap.put(UID, data.get(ID));
                }
            });
            if (uidMap == null || uidMap.isEmpty()) {
                logger.error(MessageFormat.format("User creation Failed with null response with attributes {0}", createAttributes));
                return ResponseEntity.status(responseEntity.getStatusCode().value()).body("User creation Failed with null response");
            }
            if (paramRequestMap.containsKey(EpicConstants.USER_ID_TYPE_FIELD) && paramRequestMap
                    .get(EpicConstants.USER_ID_TYPE_FIELD).toString() != null) {
                String password = paramRequestMap.get(EpicConstants.USER_ID_TYPE_FIELD)
                        .toString();
                ResponseEntity<Map<String, Object>> passwordResponse = epicUserUtilityService.setUserPassword(epicConnectionService, password, uidMap.get(UID));
                if (passwordResponse.getStatusCode().value() >= HttpStatus.SC_MULTIPLE_CHOICES) {
                    return passwordResponse;
                }
            }
            if (createAttributes.get(GROUP) != null) {
                List<String> groupList = objectMapper.convertValue(createAttributes.get(GROUP), new TypeReference<List<String>>() {
                });
                if (!groupList.isEmpty()) {
                    ResponseEntity<Map<String, Object>> groupResponseEntity = epicUserUtilityService.setUserGroups(epicConnectionService, uidMap.get(UID), groupList);
                    if (groupResponseEntity.getStatusCode().value() >= HttpStatus.SC_MULTIPLE_CHOICES) {
                        return groupResponseEntity;
                    }
                }
            }
            logger.info(MessageFormat.format("Epic user created successfully with UserID : {0}", uidMap.get(UID)));
            return ResponseEntity.status(HttpStatus.SC_CREATED).body("Epic user created successfully with UserID : " + uidMap.get(UID));
        } catch (Exception ex) {
            logger.error(MessageFormat.format(
                    "Epic User creation failed with attributes {0} while performing {1}, because of the exception: {2}",
                    createAttributes, EpicConnectionService.HttpOperationType.POST, ex.getMessage()));
            return ResponseEntity.status(HttpStatus.SC_INTERNAL_SERVER_ERROR).body("Epic User create failed, because of the exception : " + ex.getMessage());
        }
    }

    /**
     * POST /updateUser/{userId} : Updates an existing user.
     *
     * @param userId           The id of the user to update.
     * @param updateAttributes The user to update.
     * @return the ResponseEntity with status 200 (OK) and with body the updated user id,
     * or with status 400 (Bad Request) if the user is not valid,
     * or with status 404 (Not Found) if the user could not be found.
     */
    @PostMapping("/updateUser/{userId}")
    public ResponseEntity<?> updateUser(@PathVariable("userId") String userId, @RequestBody Map<String, Object> updateAttributes) {
        try {
            logger.info((MessageFormat.format("Updating epic user with following attributes: {0}", updateAttributes)));
            Map<String, Object> bodyRequestMap = epicUserUtilityService.buildRequestPayload(updateAttributes);
            if (bodyRequestMap.containsKey(EpicConstants.USER_ID_TYPE_FIELD) && bodyRequestMap
                    .get(EpicConstants.USER_ID_TYPE_FIELD) != null && !bodyRequestMap.get(EpicConstants.USER_ID_TYPE_FIELD).toString().isEmpty()) {
                String password = bodyRequestMap.get(EpicConstants.USER_ID_TYPE_FIELD)
                        .toString();
                ResponseEntity<Map<String, Object>> passwordResponseEntity = epicUserUtilityService.setUserPassword(epicConnectionService, password, userId);
                bodyRequestMap.remove(EpicConstants.USER_ID_TYPE_FIELD);
                if (passwordResponseEntity.getStatusCode().value() > HttpStatus.SC_MULTIPLE_CHOICES) {
                    return passwordResponseEntity;
                }
                logger.info(MessageFormat.format("Epic user password updated successfully with UserID : {0}", userId));
            }
            if (!bodyRequestMap.isEmpty()) {
                Map<String, Object> paramRequestMap = new HashMap<>();
                paramRequestMap.put(USER_ID_FIELD, userId);
                paramRequestMap.put(USER_ID_TYPE_FIELD, EXTERNAL);
                bodyRequestMap = new HashMap<>(epicUserUtilityService.includeUpdatableAttrsInItemsArray(bodyRequestMap));
                bodyRequestMap.put(USER_ID_FIELD, userId);
                bodyRequestMap.put(USER_ID_TYPE_FIELD, USER_ID_TYPE_VALUE);
                ResponseEntity<Map<String, Object>> userResponseEntity = epicConnectionService.executeRequest(environment.getProperty(REST_ENDPOINT)
                                + EPIC_UPDATE_USER_ENDPOINT, EpicConnectionService.HttpOperationType.POST,
                        paramRequestMap, bodyRequestMap);
                if (userResponseEntity.getStatusCode().value() > HttpStatus.SC_MULTIPLE_CHOICES) {
                    logger.error(MessageFormat.format("Epic Update User Failed, because of the exception : {0}", userResponseEntity.getBody()));
                    return userResponseEntity;
                }
            }
            if (updateAttributes.get(GROUP) != null) {
                List<String> groupList = objectMapper.readValue(updateAttributes.get(GROUP).toString(), new TypeReference<List<String>>() {
                });
                if (groupList != null && !groupList.isEmpty()) {
                    ResponseEntity<Map<String, Object>> groupReponseEntity = epicUserUtilityService.setUserGroups(epicConnectionService, userId, groupList);
                    if (groupReponseEntity.getStatusCode().value() > HttpStatus.SC_MULTIPLE_CHOICES) {
                        return groupReponseEntity;
                    }
                }
            }
            logger.info(MessageFormat.format("Epic user updated successfully, with UserID : {0}", userId));
            return ResponseEntity.status(HttpStatus.SC_OK).body("Epic user updated successfully, with UserID : " + userId);
        } catch (Exception ex) {
            logger.error(MessageFormat.format("Epic User update failed with attributes {0} while performing {1}, because of the exception: {2}",
                    updateAttributes, EpicConnectionService.HttpOperationType.POST, ex.getMessage()));
            return ResponseEntity.status(HttpStatus.SC_INTERNAL_SERVER_ERROR).body("Epic User update failed, because of the exception : " + ex.getMessage());
        }
    }

    /**
     * POST /enableUser : Enable the user.
     *
     * @param userDtl The id of the user to enable.
     * @return the ResponseEntity with status 200 (OK) and with user id, or with status 404 (Not Found).
     */
    @PostMapping(value = "/enableUser")
    public ResponseEntity<?> activateUser(@Valid @RequestBody UserIdRequestDto userDtl) {
        Map<String, Object> paramRequestMap = new HashMap<>();
        Map<String, Object> bodyRequestMap = new HashMap<>();
        try {
            paramRequestMap.put(USER_ID_FIELD, userDtl.getUserId());
            bodyRequestMap.put(USER_ID_TYPE_FIELD, USER_ID_TYPE_VALUE);
            ResponseEntity<Map<String, Object>> responseEntity = epicConnectionService.executeRequest(environment.getProperty(REST_ENDPOINT)
                    + EPIC_ACTIVATE_USER_ENDPOINT, EpicConnectionService.HttpOperationType.POST, paramRequestMap, bodyRequestMap);
            if (responseEntity.getStatusCode().value() >= HttpStatus.SC_MULTIPLE_CHOICES) {
                logger.error(MessageFormat.format("Epic user enable Failed, because of the exception: {0}", responseEntity.getBody()));
                return responseEntity;
            }
            logger.info(MessageFormat.format("Epic user enabled successfully with userId : {0}", userDtl.getUserId()));
            return ResponseEntity.status(HttpStatus.SC_OK).body("Epic user enabled successfully with userId : " + userDtl.getUserId());
        } catch (Exception ex) {
            logger.error(MessageFormat.format("Epic user enable Failed, because of the exception: {0}", ex.getMessage()));
            return ResponseEntity.status(HttpStatus.SC_INTERNAL_SERVER_ERROR).body("Epic user enable Failed, because of the exception : " + ex.getMessage());
        }
    }

    /**
     * POST /disableUser : Disable the user.
     *
     * @param userDtl The id of the user to disable.
     * @return the ResponseEntity with status 200 (OK) and with user id, or with status 404 (Not Found).
     */
    @PostMapping(value = "/disableUser")
    public ResponseEntity<?> deActivateUser(@Valid @RequestBody UserIdRequestDto userDtl) {
        Map<String, Object> paramRequestMap = new HashMap<>();
        Map<String, Object> bodyRequestMap = new HashMap<>();
        try {
            paramRequestMap.put(USER_ID_FIELD, userDtl.getUserId());
            bodyRequestMap.put(USER_ID_TYPE_FIELD, USER_ID_TYPE_VALUE);
            ResponseEntity<Map<String, Object>> responseEntity = epicConnectionService.executeRequest(environment.getProperty(REST_ENDPOINT)
                    + EPIC_DEACTIVATE_USER_ENDPOINT, EpicConnectionService.HttpOperationType.POST, paramRequestMap, bodyRequestMap);
            if (responseEntity.getStatusCode().value() >= HttpStatus.SC_MULTIPLE_CHOICES) {
                logger.error(MessageFormat.format("Epic user disable Failed, because of the exception: {0}", responseEntity.getBody()));
                return responseEntity;
            }
            logger.info(MessageFormat.format("Epic user disabled successfully, with userId : {0}", userDtl.getUserId()));
            return ResponseEntity.status(HttpStatus.SC_OK).body("Epic user disabled successfully, with userId : " + userDtl.getUserId());
        } catch (Exception ex) {
            logger.error(MessageFormat.format("Epic user disable Failed, because of the exception: {0}", ex.getMessage()));
            return ResponseEntity.status(HttpStatus.SC_INTERNAL_SERVER_ERROR).body("Epic user disable Failed, because of the exception: " + ex.getMessage());
        }
    }

    /**
     * POST /updatePassword : Update the user password.
     *
     * @param request The id of the user to update.
     * @return The ResponseEntity with status 200 (OK) and with user id, or with status 404 (Not Found).
     */
    @PostMapping("/updatePassword")
    public ResponseEntity<?> passwordReset(@Valid @RequestBody PasswordUpdateDto request) {
        try {
            ResponseEntity<Map<String, Object>> passwordReponseEntity = epicUserUtilityService.setUserPassword(epicConnectionService,
                    request.getNewPassword(), request.getUserId());
            if (passwordReponseEntity.getStatusCode().value() >= HttpStatus.SC_MULTIPLE_CHOICES) {
                logger.error(MessageFormat.format("Epic User Password update Failed, because of the exception : {0}", passwordReponseEntity.getBody()));
                return passwordReponseEntity;
            }
            logger.info(MessageFormat.format("Epic user password updated successfully, with UserID : {0}", request.getUserId()));
            return ResponseEntity.status(HttpStatus.SC_OK).body("Epic user password updated successfully with UserID : " + request.getUserId());
        } catch (Exception ex) {
            logger.error(MessageFormat.format("Epic User Password update Failed, because of the exception : {0}", ex.getMessage()));
            return ResponseEntity.status(HttpStatus.SC_INTERNAL_SERVER_ERROR).body(ex.getMessage());
        }
    }

    /**
     * GET /getUser/{UserID} : Get the "UserID" user.
     *
     * @param userId The id of the user to retrieve.
     * @return The ResponseEntity with status 200 (OK) and with body the user, or with status 404 (Not Found).
     */
    @GetMapping(value = "/getUser/{UserID}")
    public ResponseEntity<?> getUser(@PathVariable(USER_ID_FIELD) String userId) {
        Map<String, Object> responseMap = new HashMap<>();
        try {
            logger.info(MessageFormat.format("Epic User search with Filter using UserID: {0} ", userId));
            Map<String, Object> paramRequestMap = new HashMap<>();
            paramRequestMap.put(USER_ID_FIELD, userId);
            paramRequestMap.put(USER_ID_TYPE_FIELD, EXTERNAL);
            String url = environment.getProperty(REST_ENDPOINT) + EPIC_GET_USER_ENDPOINT;
            ResponseEntity<Map<String, Object>> response = epicConnectionService.executeRequest(url,
                    EpicConnectionService.HttpOperationType.GET, paramRequestMap, null);
            if (response.getStatusCode().value() >= HttpStatus.SC_MULTIPLE_CHOICES) {
                logger.error(MessageFormat.format("Unable to get the user from Epic with UserID: {0}, because of the exception: {1}", userId, response.getBody()));
                return response;
            }
            responseMap = objectMapper.convertValue(response.getBody(), new TypeReference<Map<String, Object>>() {
            });
            ResponseEntity<Map<String, Object>> grpEntity = epicUserUtilityService.getUserGroups(epicConnectionService, userId);
            Map<String, Object> groupMp = objectMapper.convertValue(grpEntity.getBody(), new TypeReference<Map<String, Object>>() {
            });
            responseMap.put(GROUP, groupMp.get(GROUP));
            responseMap = epicUserUtilityService.buildReturnMap(responseMap);
            logger.info(MessageFormat.format("Epic user fetched successfully, with userId : {0}", userId));
            return ResponseEntity.status(HttpStatus.SC_OK).body(responseMap);
        } catch (Exception ex) {
            logger.error(MessageFormat.format("Unable to get the user from Epic with UserID: {0}, because of the exception: {1}", userId, ex.getMessage()));
            return ResponseEntity.status(HttpStatus.SC_INTERNAL_SERVER_ERROR).body(MessageFormat.format("Unable to get the user from Epic with UserID: {0}, because of the exception: {1}", userId, ex.getMessage()));
        }
    }

    /**
     * POST /getUsers : Get users with pagination.
     *
     * @param searchContext The number of users per page.
     * @return the ResponseEntity with status 200 (OK) and the users in body.
     */
    @PostMapping("/getUsers")
    public ResponseEntity<?> getUsers(@RequestBody Map<String, Object> searchContext) {
        Map<String, Object> userResponseMap = new HashMap<>();
        try {
            List<Map<String, Object>> empRecords = new ArrayList<>();
            Map<String, Object> soapResponse = epicSoapConnectionService.callSoapService(searchContext, EMP, null);
            List<Map<String, String>> usersList = objectMapper.convertValue(Optional.
                            ofNullable(soapResponse.get(RECORDLIST)).orElse(Collections.emptyList()),
                    new TypeReference<List<Map<String, String>>>() {
                    });
            for (Map<String, String> user : usersList) {
                Map<String, Object> paramRequestMap = new HashMap<>();
                paramRequestMap.put(USER_ID_FIELD, user.get(XML_RECORD_ID));
                paramRequestMap.put(USER_ID_TYPE_FIELD, EXTERNAL);
                String url = environment.getProperty(REST_ENDPOINT) + EPIC_GET_USER_ENDPOINT;
                ResponseEntity<Map<String, Object>> responseEntity = epicConnectionService.executeRequest(url,
                        EpicConnectionService.HttpOperationType.GET, paramRequestMap, null);
                Map<String, Object> responseMap = objectMapper.convertValue(responseEntity.getBody(), new TypeReference<Map<String, Object>>() {
                });
                if (responseEntity.getStatusCode().value() >= HttpStatus.SC_MULTIPLE_CHOICES && responseMap.get(RESPONSE_MESSAGE).toString().contains(OBJECT_NOT_USER)) {
                    logger.warn(MessageFormat.format("Epic fetch single user failed in fetch all user operation, {0}", responseEntity.getBody()));
                    continue;
                } else if (responseEntity.getStatusCode().value() >= HttpStatus.SC_MULTIPLE_CHOICES) {
                    logger.warn(MessageFormat.format("Epic fetch single user failed in fetch all user operation, {0}", responseEntity.getBody()));
                    continue;
                }
                ResponseEntity<Map<String, Object>> grpEntity = epicUserUtilityService.getUserGroups(epicConnectionService, user.get(XML_RECORD_ID));
                Map<String, Object> groupMp = objectMapper.convertValue(grpEntity.getBody(), new TypeReference<Map<String, Object>>() {
                });
                responseMap.put(USERGROUPS, groupMp.get(USERGROUPS));
                empRecords.add(epicUserUtilityService.buildReturnMap(responseMap));
            }
            userResponseMap.put(USERS, empRecords);
            if (soapResponse.get(SEARCH_CONTEXT) != null) {
                userResponseMap.put(SEARCH_CONTEXT, soapResponse.get(SEARCH_CONTEXT));
            }
            logger.info("Epic Fetch All users success.");
            return ResponseEntity.status(HttpStatus.SC_OK).body(userResponseMap);
        } catch (Exception ex) {
            logger.error(MessageFormat.format("Epic fetch all users failed, because of the exception : {0}", ex.getMessage()));
            return ResponseEntity.status(HttpStatus.SC_INTERNAL_SERVER_ERROR).body("Epic fetch all users failed, because of the exception :" + ex.getMessage());
        }
    }

    /**
     * POST /updateGroups : Update the groups of an existing user.
     *
     * @param request The new set of groups for the user.
     * @return the ResponseEntity with status 200 (OK) and with body the updated user id,
     * or with status 400 (Bad Request) if the user or groups are not valid,
     * or with status 404 (Not Found) if the user could not be found.
     */
    @PostMapping("/updateGroups")
    public ResponseEntity<?> updateGroup(@Valid @RequestBody GroupUpdateRequestDto request) {
        try {
            List<String> groups = objectMapper.convertValue(request.getUserGroups(),
                    new TypeReference<List<String>>() {
                    });
            ResponseEntity<Map<String, Object>> groupReponseEntity = epicUserUtilityService.setUserGroups(epicConnectionService,
                    objectMapper.convertValue(request.getUserId(),
                            new TypeReference<String>() {
                            }), groups);
            if (groupReponseEntity.getStatusCode().value() >= HttpStatus.SC_MULTIPLE_CHOICES) {
                logger.error(MessageFormat.format("Epic Update User groups Failed, because of the exception : {0}", groupReponseEntity.getBody()));
                return groupReponseEntity;
            }
            logger.info(MessageFormat.format("Epic user groups updated successfully, with userId : {0}", request.getUserId()));
            return ResponseEntity.status(HttpStatus.SC_OK).body("Epic user groups updated successfully with UserId : " + request.getUserId());
        } catch (Exception ex) {
            logger.error(MessageFormat.format("Epic Update User groups Failed, because of the exception : {0}", ex.getMessage()));
            return ResponseEntity.status(HttpStatus.SC_INTERNAL_SERVER_ERROR).body("Epic Update User groups Failed, because of the exception :" + ex.getMessage());
        }
    }

    /**
     * POST /viewGroups : Get the groups of the user.
     *
     * @param request the id of the user to retrieve groups for.
     * @return the ResponseEntity with status 200 (OK) and with body the set of groups, or with status 404 (Not Found).
     */
    @PostMapping("/viewGroups")
    public ResponseEntity<?> viewGroup(@Valid @RequestBody UserIdRequestDto request) {
        try {
            String userId = request.getUserId();
            ResponseEntity<Map<String, Object>> groupReponseEntity = epicUserUtilityService.getUserGroups(epicConnectionService,
                    userId);
            if (groupReponseEntity.getStatusCode().value() >= HttpStatus.SC_MULTIPLE_CHOICES) {
                logger.error(MessageFormat.format("Epic fetch User groups Failed, because of the exception : {0}", groupReponseEntity.getBody()));
            }
            logger.info(MessageFormat.format("Epic user groups fetched successfully with UserID : {0}", request.getUserId()));
            return groupReponseEntity;
        } catch (Exception ex) {
            logger.error(MessageFormat.format("Epic fetch user groups  failed, because of the exception : {0}", ex.getMessage()));
            return ResponseEntity.status(HttpStatus.SC_INTERNAL_SERVER_ERROR).body("Epic fetch user groups failed, because of the exception : " + ex.getMessage());
        }
    }

    /**
     * DELETE /deleteUser/{UserID} : Delete the "UserID" user.
     *
     * @param userId The id of the user to delete.
     * @return The ResponseEntity with status 200 (OK) and with user id.
     */
    @DeleteMapping(value = "/deleteUser/{UserID}")
    public ResponseEntity<?> deleteUser(@PathVariable(USER_ID_FIELD) String userId) {
        Map<String, Object> paramRequestMap = new HashMap<>();
        Map<String, Object> bodyRequestMap = new HashMap<>();
        try {
            paramRequestMap.put(USER_ID_FIELD, userId);
            bodyRequestMap.put(USER_ID_TYPE_FIELD, USER_ID_TYPE_VALUE);
            ResponseEntity<Map<String, Object>> deleteResponseEntity = epicConnectionService.executeRequest(environment.getProperty(REST_ENDPOINT)
                    + EPIC_DELETE_USER_ENDPOINT, EpicConnectionService.HttpOperationType.POST, paramRequestMap, bodyRequestMap);
            if (deleteResponseEntity.getStatusCode().value() >= HttpStatus.SC_MULTIPLE_CHOICES) {
                logger.error(MessageFormat.format("Epic User deletion failed with ID: {0} while performing {1}, because of the exception: {2}",
                        userId, EpicConnectionService.HttpOperationType.POST, deleteResponseEntity.getBody()));
                return deleteResponseEntity;
            }
            logger.info(MessageFormat.format("Epic user deleted with UserID: {0}", userId));
            return ResponseEntity.status(HttpStatus.SC_OK).body("Epic user deleted with UserID : " + userId);
        } catch (Exception ex) {
            logger.error(MessageFormat.format("Epic user deletion Failed, because of the exception : {0}", ex.getMessage()));
            return ResponseEntity.status(HttpStatus.SC_INTERNAL_SERVER_ERROR).body("Epic user deletion Failed, because of the exception : " + ex.getMessage());
        }
    }

}