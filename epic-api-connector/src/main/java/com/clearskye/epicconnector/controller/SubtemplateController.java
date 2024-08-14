package com.clearskye.epicconnector.controller;

import static com.clearskye.epicconnector.utils.EpicConstants.DEFAULT_TEMPLATE_ID;
import static com.clearskye.epicconnector.utils.EpicConstants.EPIC_ATTR_USER_SUBTEMPLATE_ID;

import java.text.MessageFormat;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.clearskye.epicconnector.service.OtherObjectService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/epic/subTemplate")
@Validated
@RequiredArgsConstructor
public class SubtemplateController {
    /**
     * Epic other object Utility Service used for other-object-related utility functions.
     */
    private final OtherObjectService otherObjectService;
    /**
     * Logger instance for logging SubtemplateController events.
     */
    private static final Logger logger = LogManager.getLogger(SubtemplateController.class);
    /**
     * Environment to access environment-specific properties.
     */
    private final Environment environment;


    /**
     * POST /getSubTemplate : Get subTemplate with pagination.
     *
     * @param searchContext The number of subTemplate per page.
     * @return the ResponseEntity with status 200 (OK) and the subTemplate in body.
     */
    @PostMapping("/getSubTemplates")
    public ResponseEntity<?> getSubTemplates(@RequestBody Map<String, String> searchContext) {
        try {
            List<Map<String, String>> records = otherObjectService.buildObjectMaps(environment.getProperty("epic" +
                    ".subTemplatesFilePath"), null, searchContext);
            if (records.isEmpty()) {
                logger.error("SubTemplate page offset exceeds the total number of records.");
                return ResponseEntity.status(HttpStatus.SC_BAD_REQUEST).body("SubTemplate page offset exceeds the total number of records.");
            }
            logger.info("Epic Fetch All SubTemplates success.");
            return ResponseEntity.status(HttpStatus.SC_OK).body(records);
        } catch (Exception ex) {
            logger.error(MessageFormat.format("Epic fetch all SubTemplates failed, because of the exception : {0}",
                    ex.getMessage()));
            return ResponseEntity.status(HttpStatus.SC_INTERNAL_SERVER_ERROR).body("Epic fetch all SubTemplates " +
                    "failed, because of the exception :" + ex.getMessage());
        }
    }

    /**
     * GET /getDefaultTemplate/{UserSubtemplateIDs} : Get the "UserSubtemplateIDs" subTemplate.
     *
     * @param subTemplateId The id of the subTemplate to retrieve.
     * @return The ResponseEntity with status 200 (OK) and with body the subTemplate, or with status 404 (Not Found).
     */
    @GetMapping(value = "/getSubTemplate/{UserSubtemplateID}")
    public ResponseEntity<?> getDefaultTemplate(@PathVariable(EPIC_ATTR_USER_SUBTEMPLATE_ID) String subTemplateId) {
        try {
            List<Map<String, String>> records = otherObjectService.buildObjectMaps(environment.getProperty("epic" +
                    ".subTemplatesFilePath"), subTemplateId, null);
            if (records.isEmpty()) {
                logger.error(MessageFormat.format("SubTemplate doest not exist with UserSubtemplateID : {0} ",
                        subTemplateId));
                return ResponseEntity.status(HttpStatus.SC_NOT_FOUND).body(MessageFormat.format("SubTemplate doest " +
                        "not exist with UserSubtemplateID : {0} ", subTemplateId));
            }
            logger.info(MessageFormat.format("Epic SubTemplate fetched successfully, with UserSubtemplateID : {0}", subTemplateId));
            return ResponseEntity.status(HttpStatus.SC_OK).body(records);
        } catch (Exception ex) {
            logger.error(MessageFormat.format("Unable to get the subTemplate from Epic with UserSubtemplateID: {0}, because of the exception: {1}", subTemplateId, ex.getMessage()));
            return ResponseEntity.status(HttpStatus.SC_INTERNAL_SERVER_ERROR).body(MessageFormat.format("Unable to get the subTemplate from Epic with UserSubtemplateID: {0}, because of the exception: {1}", subTemplateId, ex.getMessage()));
        }
    }
}
