package com.clearskye.epicconnector.controller;

import static com.clearskye.epicconnector.utils.EpicConstants.DEFAULT_TEMPLATE_ID;

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
@RequestMapping("/epic/defaultTemplate")
@Validated
@RequiredArgsConstructor
public class DefaultTemplateController {
    /**
     * Epic other object Utility Service used for other-object-related utility functions.
     */
    private final OtherObjectService otherObjectService;
    /**
     * Logger instance for logging DefaultTemplateController events.
     */
    private static final Logger logger = LogManager.getLogger(DefaultTemplateController.class);
    /**
     * Environment to access environment-specific properties.
     */
    private final Environment environment;


    /**
     * POST /getDefaultTemplates : Get default template with pagination.
     *
     * @param searchContext The number of default templates per page.
     * @return the ResponseEntity with status 200 (OK) and the default templates in body.
     */
    @PostMapping("/getDefaultTemplates")
    public ResponseEntity<?> getDefaultTemplates(@RequestBody Map<String, String> searchContext) {
        try {
            List<Map<String, String>> records = otherObjectService.buildObjectMaps(environment.getProperty("epic" +
                    ".userTemplatesFilePath"), null, searchContext);
            if (records.isEmpty()) {
                logger.error("Default Template page offset exceeds the total number of records.");
                return ResponseEntity.status(HttpStatus.SC_BAD_REQUEST).body("Default Template page offset exceeds " +
                        "the total number of records.");
            }
            logger.info("Epic Fetch All default template success.");
            return ResponseEntity.status(HttpStatus.SC_OK).body(records);
        } catch (Exception ex) {
            logger.error(MessageFormat.format("Epic fetch all default template failed, because of the exception : {0}",
                    ex.getMessage()));
            return ResponseEntity.status(HttpStatus.SC_INTERNAL_SERVER_ERROR).body("Epic fetch all default template failed, because of the exception :" + ex.getMessage());
        }
    }

    /**
     * GET /getDefaultTemplate/{DefaultTemplateID} : Get the "DefaultTemplateID" default template.
     *
     * @param defaultTemplateId The id of the default template to retrieve.
     * @return The ResponseEntity with status 200 (OK) and with body the default template, or with status 404 (Not Found).
     */
    @GetMapping(value = "/getDefaultTemplate/{DefaultTemplateID}")
    public ResponseEntity<?> getDefaultTemplate(@PathVariable(DEFAULT_TEMPLATE_ID) String defaultTemplateId) {
        try {
            List<Map<String, String>> records = otherObjectService.buildObjectMaps(environment.getProperty("epic" +
                    ".userTemplatesFilePath"), defaultTemplateId, null);
            if (records.isEmpty()) {
                logger.error(MessageFormat.format("Default template doest not exist with DefaultTemplateID : {0} ",
                        defaultTemplateId));
                return ResponseEntity.status(HttpStatus.SC_NOT_FOUND).body(MessageFormat.format("Default " +
                        "template doest not exist with DefaultTemplateID : {0} ", defaultTemplateId));
            }
            logger.info(MessageFormat.format("Epic default template fetched successfully, with DefaultTemplateID : {0}", defaultTemplateId));
            return ResponseEntity.status(HttpStatus.SC_OK).body(records);
        } catch (Exception ex) {
            logger.error(MessageFormat.format("Unable to get the default template from Epic with DefaultTemplateID: {0}, because of the exception: {1}", defaultTemplateId, ex.getMessage()));
            return ResponseEntity.status(HttpStatus.SC_INTERNAL_SERVER_ERROR).body(MessageFormat.format("Unable to get the default template from Epic with DefaultTemplateID: {0}, because of the exception: {1}", defaultTemplateId, ex.getMessage()));
        }
    }
}
