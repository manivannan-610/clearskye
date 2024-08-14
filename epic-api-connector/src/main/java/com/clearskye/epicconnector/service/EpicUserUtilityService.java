package com.clearskye.epicconnector.service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpStatus;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.clearskye.epicconnector.utils.EpicConstants;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

/**
 * Epic user utility service class for managing user-related operations.
 *
 * <p>This class provides various utility methods for handling user data.</p>
 */
@Service
@RequiredArgsConstructor
public class EpicUserUtilityService {
    /**
     * Environment to access environment-specific properties.
     */
    private final Environment environment;
    /**
     * Object Mapper for the Epic User Utility Service.
     */
    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Generate the Request Map to perform the Operations.
     *
     * @param attributesMap Attributes to build the request.
     * @return attributesMap Map containing the request
     */
    public Map<String, Object> buildRequestPayload(Map<String, Object> attributesMap) {
        Map<String, Object> requestMap = new HashMap<String, Object>();
        Map<String, String> userComplexNameMap = new HashMap<String, String>();
        EpicConstants.EPIC_OPTIONAL_ATTRIBUTES.forEach(attrName -> {
            if (!attrName.equals(EpicConstants.GROUP) && attributesMap.get(attrName) != null) {
                if (EpicConstants.EPIC_COMPLEX_TYPE_ATTR_SET.contains(attrName)) {
                    Map<String, Object> attrMap = objectMapper.convertValue(attributesMap.get(attrName),
                            new TypeReference<Map<String, Object>>() {
                            });
                    requestMap.put(attrName, attrMap);
                } else if (attrName.equals(EpicConstants.USER_ID_TYPE_FIELD)) {
                    requestMap.put(attrName, attributesMap.get(attrName));
                } else if (EpicConstants.USER_COMPLEX_NAME_ATTRS.contains(attrName)) {
                    userComplexNameMap.put(attrName, attributesMap.get(attrName).toString());
                } else if (attrName.equals(EpicConstants.EPIC_ATTR_DEFAULT_LOGIN_DEPT_ID)
                        || attrName.equals(EpicConstants.EPIC_ATTR_PRIMARY_MANAGER)) {
                    Map<String, String> defaultLoginDepartmentId = new HashMap<>();
                    defaultLoginDepartmentId.put(EpicConstants.ID, attributesMap.get(attrName).toString());
                    defaultLoginDepartmentId.put(EpicConstants.TYPE, EpicConstants.USER_ID_TYPE_VALUE);
                    requestMap.put(attrName, defaultLoginDepartmentId);
                } else if (attrName.equals(EpicConstants.DEFAULT_TEMPLATE_ID)) {
                    Map<String, Object> configMap = new HashMap<>();
                    Map<String, Object> templateMap = new HashMap<>();
                    templateMap.put(EpicConstants.ID, attributesMap.get(attrName).toString());
                    templateMap.put(EpicConstants.TYPE, EpicConstants.USER_ID_TYPE_VALUE);
                    configMap.put(EpicConstants.DEFAULT_TEMPLATE_ID, templateMap);
                    configMap.put(EpicConstants.APPLIED_TEMPLATE_ID, templateMap);
                    List<Map<String, Object>> templatesList = new ArrayList<>();
                    templatesList.add(templateMap);
                    configMap.put(EpicConstants.AVAILABLE_TEMPLATES, templatesList);
                    requestMap.put(EpicConstants.TEMPLATES_CONFIG, configMap);
                } else if (attrName.equals(EpicConstants.EPIC_ATTR_USER_SUBTEMPLATE_IDS)) {
                    List<String> ids = objectMapper.convertValue(attributesMap.get(attrName),
                            new TypeReference<List<String>>() {
                            });
                    if (ids != null) {
                        List<Map<String, Object>> idsList = new ArrayList<>();
                        int index = 1;
                        for (String id : ids) {
                            Map<String, Object> attributeEntry = new HashMap<>();
                            Map<String, Object> identifierEntry = new HashMap<>();
                            identifierEntry.put(EpicConstants.ID, id);
                            identifierEntry.put(EpicConstants.TYPE, EpicConstants.USER_ID_TYPE_VALUE);
                            attributeEntry.put(EpicConstants.IDENTIFIER, identifierEntry);
                            attributeEntry.put(EpicConstants.INDEX, index);
                            index++;
                            idsList.add(attributeEntry);
                        }
                        requestMap.put(attrName, idsList);
                    }
                } else if (attrName.equals(EpicConstants.USERS_MANAGERS)) {
                    List<String> ids = objectMapper.convertValue(attributesMap.get(attrName),
                            new TypeReference<List<String>>() {
                            });
                    List<Map<String, Object>> idsList = new ArrayList<>();
                    ids.forEach(id -> {
                        Map<String, Object> identifierEntry = new HashMap<>();
                        identifierEntry.put(EpicConstants.ID, id);
                        identifierEntry.put(EpicConstants.TYPE, EpicConstants.USER_ID_TYPE_VALUE);
                        idsList.add(identifierEntry);
                    });
                    requestMap.put(attrName, idsList);
                } else if (attrName.equals(EpicConstants.PROVIDER)) {
                    Map<String, Object> providerMap = new HashMap<>();
                    providerMap.put(EpicConstants.ID, attributesMap.get(attrName).toString());
                    providerMap.put(EpicConstants.TYPE, EpicConstants.USER_ID_TYPE_VALUE);
                    requestMap.put(EpicConstants.PROVIDER_ID, providerMap);
                } else if (attrName.equals(EpicConstants.INBASKET_CLASSIFICATION) || attrName.equals(EpicConstants.CATEGORY_REPORT_GROUPER6)) {
                    if (attributesMap.get(attrName).toString() != null) {
                        List<String> list = objectMapper.convertValue(attributesMap.get(attrName),
                                new TypeReference<List<String>>() {
                                });
                        requestMap.put(attrName, list);
                    }
                } else if (EpicConstants.BOOLEAN_ATTR_SET.contains(attrName)) {
                    requestMap.put(attrName, Boolean.parseBoolean(attributesMap.get(attrName).toString()));
                } else if (attrName.equals(EpicConstants.CONTACTDATE)) {
                    SimpleDateFormat formatter = new SimpleDateFormat(EpicConstants.DATE_FOMRAT);
                    Date currentDate = new Date();
                    requestMap.put(attrName, formatter.format(currentDate));
                } else if (attrName.equals(EpicConstants.USER_ID_FIELD)) {
                    requestMap.put(EpicConstants.USER_INTERNAL_ID, attributesMap.get(attrName).toString());
                } else {
                    requestMap.put(attrName, attributesMap.get(attrName).toString());
                }
            }
        });
        if (!userComplexNameMap.isEmpty()) {
            requestMap.put(EpicConstants.COMPLEX_NAME, userComplexNameMap);
        }
        return requestMap;
    }

    /**
     * Set the Password of the User.
     *
     * @param password User password to be updated in Epic
     * @param userId   UserId to whom the password should be updated.
     * @return The ResponseEntity with status and the response body.
     * @throws Exception Exception during update user password.
     */
    public ResponseEntity<Map<String, Object>> setUserPassword(EpicConnectionService epicConnectionService, String password, String userId) throws Exception {
        Map<String, Object> paramRequestMap = new HashMap<>();
        Map<String, Object> bodyRequestMap = new HashMap<>();
        paramRequestMap.put(EpicConstants.USER_ID_FIELD, userId);
        bodyRequestMap.put(EpicConstants.USER_ID_TYPE_FIELD, EpicConstants.USER_ID_TYPE_VALUE);
        bodyRequestMap.put(EpicConstants.USER_PASSWORD_FIELD, password);
        return epicConnectionService.executeRequest(environment.getProperty(EpicConstants.REST_ENDPOINT)
                        + EpicConstants.EPIC_SET_USER_PASSWORD_ENDPOINT, EpicConnectionService.HttpOperationType.PUT,
                paramRequestMap, bodyRequestMap);
    }

    /**
     * Set the User Group(s) by overriding if any value exists.
     *
     * @param userId      Unique id of the user for whom group should be updated
     * @param groupsNames List of group names to be assigned to user
     * @return The ResponseEntity with status and the response body.
     * @throws Exception Exception during update user groups.
     */
    public ResponseEntity<Map<String, Object>> setUserGroups(EpicConnectionService epicConnectionService, String userId,
            List<String> groupsNames) throws Exception {
        Map<String, Object> bodyRequestMap = new HashMap<>();
        Map<String, String> innerRequestMap = new HashMap<>();
        innerRequestMap.put(EpicConstants.ID, userId);
        innerRequestMap.put(EpicConstants.TYPE, EpicConstants.USER_ID_TYPE_VALUE);
        bodyRequestMap.put(EpicConstants.USER_ID_FIELD, innerRequestMap);
        bodyRequestMap.put(EpicConstants.USERGROUPS, groupsNames);
        return epicConnectionService.executeRequest(environment.getProperty(EpicConstants.REST_ENDPOINT)
                        + EpicConstants.EPIC_UPDATE_USER_GROUPS_ENDPOINT, EpicConnectionService.HttpOperationType.POST,
                new HashMap<String, Object>(), bodyRequestMap);
    }

    /**
     * Retrieve the Groups of User.
     *
     * @param userId Unique id of the user for whom group should be obtained
     * @return The ResponseEntity with status and the response body.
     * @throws Exception Exception during retrieve user groups.
     */
    public ResponseEntity<Map<String, Object>> getUserGroups(EpicConnectionService epicConnectionService, String userId) throws Exception {
        List<String> userGroupList = new ArrayList<>();
        Map<String, Object> responseMap = new HashMap<>();
        Map<String, Object> paramRequestMap = new HashMap<>();
        Map<String, Object> bodyRequestMap = new HashMap<>();
        Map<String, String> innerRequestMap = new HashMap<>();
        innerRequestMap.put(EpicConstants.ID, userId);
        innerRequestMap.put(EpicConstants.TYPE, EpicConstants.USER_ID_TYPE_VALUE);
        bodyRequestMap.put(EpicConstants.USER_ID_FIELD, innerRequestMap);
        ResponseEntity<Map<String, Object>> groupReponseEntity = epicConnectionService.executeRequest(
                environment.getProperty(EpicConstants.REST_ENDPOINT) + EpicConstants.EPIC_GET_USER_GROUPS_ENDPOINT, EpicConnectionService.HttpOperationType.POST,
                paramRequestMap, bodyRequestMap);
        if (groupReponseEntity.getStatusCode().value() >= org.apache.http.HttpStatus.SC_MULTIPLE_CHOICES) {
            return groupReponseEntity;
        }
        Map<String, Object> grpResponseMap = objectMapper.convertValue(groupReponseEntity.getBody(), new TypeReference<Map<String, Object>>() {
        });
        userGroupList = objectMapper.convertValue(grpResponseMap.get(EpicConstants.USERGROUPS), new TypeReference<List<String>>() {
        });
        responseMap.put(EpicConstants.USERGROUPS, userGroupList);
        return ResponseEntity.status(HttpStatus.SC_OK).body(responseMap);
    }

    /**
     * Build return map for single user.
     *
     * @param result To build Response.
     * @return responseMap Single user response.
     */
    public Map<String, Object> buildReturnMap(Map<String, Object> result) {
        Map<String, String> uidMap = new HashMap<>();
        Map<String, Object> userComplexName = new HashMap<>();
        Map<String, Object> responseMap = new HashMap<>();
        if (result.get(EpicConstants.USER_IDS) == null) {
            return null;
        }
        List<Map<String, String>> userIdMapList = objectMapper.convertValue(result.get(EpicConstants.USER_IDS),
                new TypeReference<List<Map<String, String>>>() {
                });
        userIdMapList.forEach(userIdMap -> {
            if (userIdMap.get(EpicConstants.TYPE).equalsIgnoreCase(EpicConstants.USER_ID_TYPE_VALUE)) {
                uidMap.put(EpicConstants.USER_ID_FIELD, userIdMap.get(EpicConstants.ID));
            }
        });
        for (String attrName : EpicConstants.EPIC_OPTIONAL_ATTRIBUTES) {
            if (EpicConstants.EPIC_COMPLEX_TYPE_ATTR_SET.contains(attrName)) {
                if (result.get(attrName) != null) {
                    responseMap.put(attrName, result.get(attrName));
                }
            } else if (attrName.equals(EpicConstants.DEFAULT_TEMPLATE_ID)) {
                Map<String, Object> configMap = objectMapper.convertValue(result.get(EpicConstants.TEMPLATES_CONFIG),
                        new TypeReference<Map<String, Object>>() {
                        });
                List<Map<String, Object>> templateList = objectMapper.convertValue(configMap
                        .get(EpicConstants.DEFAULT_TEMPLATE_ID), new TypeReference<List<Map<String, Object>>>() {
                });
                Map<String, Object> templateMap = templateList.stream()
                        .filter(attr -> attr.get(EpicConstants.TYPE).equals(EpicConstants.EXTERNAL))
                        .findFirst()
                        .orElse(null);
                if (templateMap != null) {
                    responseMap.put(attrName, templateMap.get(EpicConstants.ID));
                }
            } else if (attrName.equals(EpicConstants.EPIC_ATTR_USER_SUBTEMPLATE_IDS) || attrName.equals(EpicConstants.USERS_MANAGERS)) {
                List<Map<String, Object>> list = objectMapper.convertValue(result.get(attrName),
                        new TypeReference<List<Map<String, Object>>>() {
                        });
                List<String> idsList = new ArrayList<>();
                if (list != null && list.size() > 0) {
                    list.forEach(subTemplateObject -> {
                        Map<String, Object> idMap = objectMapper.convertValue(subTemplateObject
                                        .get(EpicConstants.IDENTIFIERS), new TypeReference<List<Map<String, Object>>>(
                                ) {
                                }).stream()
                                .filter(record -> record.get(EpicConstants.TYPE)
                                        .equals(EpicConstants.USER_ID_TYPE_VALUE))
                                .findFirst()
                                .orElse(null);
                        if (idMap != null) {
                            idsList.add(idMap.get(EpicConstants.ID).toString());
                        }
                    });
                }
                responseMap.put(attrName, objectMapper.convertValue(idsList,
                        new TypeReference<List<String>>() {
                        }));
            } else if (EpicConstants.USER_COMPLEX_NAME_ATTRS.contains(attrName)) {
                if (userComplexName.isEmpty()) {
                    userComplexName = objectMapper.convertValue(result.get(EpicConstants.COMPLEX_NAME),
                            new TypeReference<Map<String, Object>>() {
                            });
                }
                if (userComplexName.get(attrName) != null) {
                    responseMap.put(attrName, userComplexName.get(attrName).toString());
                }
            } else if (attrName.equals(EpicConstants.EPIC_ATTR_DEFAULT_LOGIN_DEPT_ID)
                    || attrName.equals(EpicConstants.EPIC_ATTR_PRIMARY_MANAGER)) {
                List<Map<String, Object>> complexList = objectMapper.convertValue(result.get(attrName),
                        new TypeReference<List<Map<String, Object>>>() {
                        });
                if (complexList != null && complexList.size() > 0) {
                    Map<String, Object> attributeMap = complexList.stream()
                            .filter(record -> record.get(EpicConstants.TYPE).equals(EpicConstants.USER_ID_TYPE_VALUE))
                            .findAny()
                            .orElse(null);
                    if (attributeMap != null) {
                        responseMap.put(attrName, attributeMap.get(EpicConstants.ID));
                    }
                }
            } else if (attrName.equals(EpicConstants.INBASKET_CLASSIFICATION) || attrName.equals(
                    EpicConstants.GROUP) || attrName.equals(EpicConstants.CATEGORY_REPORT_GROUPER6)) {
                List<String> list = objectMapper.convertValue(
                        result.get(attrName), new TypeReference<List<String>>() {
                        });
                responseMap.put(attrName, list);
            } else if (attrName.contains(EpicConstants.PROVIDER)) {
                List<Map<String, Object>> providerValue = objectMapper.convertValue(result.get(EpicConstants.PROVIDER_ID),
                        new TypeReference<List<Map<String, Object>>>() {
                        });
                if (providerValue != null && providerValue.size() > 0) {
                    Map<String, Object> providerMap = providerValue.stream()
                            .filter(record -> record.get(EpicConstants.TYPE).equals(EpicConstants.USER_ID_TYPE_VALUE))
                            .findAny()
                            .orElse(null);
                    if (providerMap != null) {
                        responseMap.put(attrName, providerMap.get(EpicConstants.ID));
                    }
                }
            } else if (attrName.equals(EpicConstants.STATUS_FIELD)) {
                responseMap.put(attrName, result.get(attrName));
            } else if (attrName.equals(EpicConstants.USER_ID_FIELD) && uidMap.get(EpicConstants.USER_ID_FIELD) != null
                    && !uidMap.get(EpicConstants.USER_ID_FIELD).isEmpty()) {
                responseMap.put(attrName, uidMap.get(EpicConstants.USER_ID_FIELD));
            } else {
                Object value = result.get(attrName);
                if (value != null && !value.toString().isEmpty()) {

                    if (EpicConstants.BOOLEAN_ATTR_SET.contains(attrName)) {
                        responseMap.put(attrName, Boolean.valueOf(value.toString()));
                    } else {
                        responseMap.put(attrName, result.get(attrName));
                    }
                }
            }
        }
        return responseMap;
    }

    /**
     * Create items array containing all the attributes to be updated in Epic.
     *
     * @param requestMap Map containing all the attributes & its values to be
     *                   updated.
     * @return requestMap Map containing items updated array with the attributes.
     */
    public Map<String, Object> includeUpdatableAttrsInItemsArray(Map<String, Object> requestMap) {
        List<Map<String, Object>> items = new ArrayList<>();
        requestMap.forEach((key, value) -> {
            Map<String, Object> item = new HashMap<>();
            item.put(EpicConstants.UPDATE_NAME, key);
            item.put(EpicConstants.MODE, EpicConstants.REPLACE);
            items.add(item);
        });
        requestMap.put(EpicConstants.ITEMS, items);
        return requestMap;
    }
}