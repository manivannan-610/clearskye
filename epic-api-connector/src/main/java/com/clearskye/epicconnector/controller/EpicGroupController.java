package com.clearskye.epicconnector.controller;

import static com.clearskye.epicconnector.utils.EpicConstants.DEFAULT_TEMPLATE_ID;
import static com.clearskye.epicconnector.utils.EpicConstants.GROUPS_FILEPATH;

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
@RequestMapping("/epic/group")
@Validated
@RequiredArgsConstructor
public class EpicGroupController {
    /**
     * Epic other object Utility Service used for other-object-related utility functions.
     */
    private final OtherObjectService otherObjectService;
    /**
     * Logger instance for logging EpicGroupController events.
     */
    private static final Logger logger = LogManager.getLogger(EpicGroupController.class);
    /**
     * Environment to access environment-specific properties.
     */
    private final Environment environment;


    /**
     * POST /getGroups : Get groups with pagination.
     *
     * @param searchContext The number of groups per page.
     * @return the ResponseEntity with status 200 (OK) and the groups in body.
     */
    @PostMapping("/getGroups")
    public ResponseEntity<?> getDefaultTemplates(@RequestBody Map<String, String> searchContext) {
        try {
            List<Map<String, String>> records = otherObjectService.buildObjectMaps(environment.getProperty(GROUPS_FILEPATH), null, searchContext);
            if (records.isEmpty()) {
                logger.error("Group page offset exceeds the total number of records.");
                return ResponseEntity.status(HttpStatus.SC_BAD_REQUEST).body("Group page offset exceeds " +
                        "the total number of records.");
            }
            logger.info("Epic Fetch All group success.");
            return ResponseEntity.status(HttpStatus.SC_OK).body(records);
        } catch (Exception ex) {
            logger.error(MessageFormat.format("Epic fetch all group failed, because of the exception : {0}",
                    ex.getMessage()));
            return ResponseEntity.status(HttpStatus.SC_INTERNAL_SERVER_ERROR).body("Epic fetch all group failed, because of the exception :" + ex.getMessage());
        }
    }

    /**
     * GET /getGroup/{groupId} : Get the "groupID" of group.
     *
     * @param groupId The id of the group to retrieve.
     * @return The ResponseEntity with status 200 (OK) and with body the group, or with status 404 (Not Found).
     */
    @GetMapping(value = "/getGroup/{groupID}")
    public ResponseEntity<?> getDefaultTemplate(@PathVariable("groupID") String groupId) {
        try {
            List<Map<String, String>> records = otherObjectService.buildObjectMaps(environment.getProperty(GROUPS_FILEPATH), groupId, null);
            if (records.isEmpty()) {
                logger.error(MessageFormat.format("Default template doest not exist with groupID : {0} ",
                        groupId));
                return ResponseEntity.status(HttpStatus.SC_NOT_FOUND).body(MessageFormat.format("Default " +
                        "template doest not exist with groupID : {0} ", groupId));
            }
            logger.info(MessageFormat.format("Epic group fetched successfully, with groupID : {0}", groupId));
            return ResponseEntity.status(HttpStatus.SC_OK).body(records);
        } catch (Exception ex) {
            logger.error(MessageFormat.format("Unable to get the group from Epic with groupID: {0}, because of the exception: {1}", groupId, ex.getMessage()));
            return ResponseEntity.status(HttpStatus.SC_INTERNAL_SERVER_ERROR).body(MessageFormat.format("Unable to get the group from Epic with groupID: {0}, because of the exception: {1}", groupId, ex.getMessage()));
        }
    }
}
