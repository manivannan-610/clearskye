package com.clearskye.epicconnector;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.markuputils.CodeLanguage;
import com.aventstack.extentreports.markuputils.ExtentColor;
import com.aventstack.extentreports.markuputils.MarkupHelper;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

/**
 * Attempts to test the ClearSkye with the framework.
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class EpicApiConnectorApplicationTests {
    /**
     * MockMvc instance used to perform HTTP requests in the tests.
     * <p>
     * The MockMvc instance is autowired by Spring Boot's testing framework.
     * It allows for performing HTTP requests and verifying responses without
     * the need for an actual HTTP server. This is useful for testing controller
     * endpoints in isolation.
     * </p>
     */
    @Autowired
    private MockMvc mockMvc;
    /**
     * ExtentTest instance used to log individual test details.
     */
    private static ExtentReports extent;
    /**
     * Logger instance for logging EpicApiConnectorApplicationTests events.
     */
    private final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    /**
     * JWT token for authorization.
     */
    private static String jwtToken;
    /**
     * Username attribute used to perform user operation.
     */
    @Value("${epic.username}")
    private String userName;
    /**
     * UserId attribute used to perform user operation.
     */
    private final String userId = "HCTISP027";
    /**
     * System login attribute used to perform user operation.
     */
    private final String systemLogin = "HCTISP027";

    /**
     * Sets up the ExtentReports instance for unit test reporting.
     * <p>
     * This method is executed before all tests. It initializes the ExtentReports instance
     * and configures the ExtentSparkReporter to generate an HTML report for the unit tests.
     * The report will be saved in the specified location with the given configuration.
     * </p>
     *
     */
    @BeforeAll
    public static void initialTest() {
        ExtentSparkReporter spark = new ExtentSparkReporter("target/clearskyeReport/ClearSkye_Unit_Test.html");
        spark.config().setReportName("ClearSkye Unit Test");
        spark.config().setDocumentTitle("ClearSkye API Testing");
        extent = new ExtentReports();
        extent.attachReporter(spark);
    }

    /**
     * Sets up the MockMvc instance and initializes any required resources.
     * <p>
     * This method is executed before all tests. It sets up the MockMvc instance
     * using the web application context and initializes the JWT token for authorization.
     * </p>
     */
    @BeforeEach
    public void setUp() throws Exception {
        Map<String, String> requestMap = new HashMap<>();
        requestMap.put("userName", "admin");
        requestMap.put("password", "f%9e&9P6T8=a%V*4Q6hj");
        ResultActions resultActions = this.mockMvc.perform(post("/auth/generateToken")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(OBJECT_MAPPER.writeValueAsString(requestMap)))
                .andExpect(status().isOk());
        String result = resultActions.andReturn().getResponse().getContentAsString();
        Map<String, String> resultMap = OBJECT_MAPPER.readValue(result,
                new TypeReference<Map<String, String>>() {
                });
        jwtToken = "Bearer " + resultMap.get("accessToken");
    }

    /**
     * Tests the connection to the service.
     * <p>
     * This test performs a GET request to the /epic/user/getUser endpoint and verifies
     * that the response status is 200 OK. This ensures that the service is up and running.
     * </p>
     *
     * @throws Exception if an error occurs during the request
     */
    @Order(1)
    @Test
    public void testConnection() throws Exception {
        ExtentTest test = extent.createTest("TestConnection");
        ResultActions resultActions = this.mockMvc.perform(get("/epic/user/getUser/" + userName)
                .header("Authorization", jwtToken)
                .accept(MediaType.APPLICATION_JSON));
        int status = resultActions.andReturn().getResponse().getStatus();
        if (status == 200) {
            String result = resultActions.andReturn().getResponse().getContentAsString();
            test.info(MarkupHelper.createLabel(String.valueOf(resultActions.andReturn().getResponse().getStatus()),
                    ExtentColor.GREEN));
            Map<String, Object> resultMap = OBJECT_MAPPER.readValue(result,
                    new TypeReference<Map<String, Object>>() {
                    });
            Assertions.assertEquals(resultMap.get("UserID"), "8KMILESSOFTWAR");
            Assertions.assertEquals(resultMap.get("FirstName"), "User");
            Assertions.assertEquals(resultMap.get("Provider"), "E5113");
            Assertions.assertEquals(resultMap.get("IsActive"), true);
            test.info(MarkupHelper.createCodeBlock(result, CodeLanguage.JSON));
        } else {
            test.log(Status.FAIL, new Throwable());
            Assertions.fail();
        }
    }

    /**
     * Tests the creation of a new user.
     * <p>
     * This test performs a POST request to the /epic/user/createUser endpoint with user details and verifies
     * that the response status is 201 CREATED. This ensures that the user creation functionality
     * works as expected.
     * </p>
     *
     * @throws Exception if an error occurs during the request
     */
    @Order(2)
    @Test
    public void createUser() throws Exception {
        ExtentTest test = extent.createTest("CreateUser");
        Map<String, Object> requestMap = new HashMap<>();
        requestMap.put("UserID", userId);
        requestMap.put("SystemLoginID", systemLogin);
        requestMap.put("FirstName", "Leo");
        requestMap.put("LastName", "HCTI");
        requestMap.put("GivenNameInitials", "L.");
        requestMap.put("MiddleName", "Hammock");
        requestMap.put("LastNamePrefix", "Dr");
        requestMap.put("SpouseLastName", "Carol");
        requestMap.put("SpousePrefix", "V");
        requestMap.put("Suffix", "II");
        requestMap.put("AcademicTitle", "Phd");
        requestMap.put("PrimaryTitle", "Mr.");
        Map<String, String> BlockStatus = new HashMap<>();
        BlockStatus.put("IsBlocked", "true");
        BlockStatus.put("BlockReason", "Too many failed logins");
        BlockStatus.put("Comment", "Done this via create api call");
        requestMap.put("BlockStatus", BlockStatus);
        requestMap.put("SpouseLastNameFirst", "false");
        requestMap.put("ContactDate", "04/27/2021");
        requestMap.put("ContactComment", "End User Created via API");
        requestMap.put("LDAPOverrideID", "johnrob");
        requestMap.put("StartDate", "04/27/2021");
        requestMap.put("EndDate", "08/20/2024");
        requestMap.put("UserAlias", "Candidate of US");
        requestMap.put("UserPhotoPath", "https://unsplash.com/photos/tDUkcOwlpXc");
        requestMap.put("Sex", "Male");
        requestMap.put("IsActive", "true");
        requestMap.put("ReportGrouper1", "Accounts");
        requestMap.put("ReportGrouper2", "HR");
        requestMap.put("ReportGrouper3", "IT");
        requestMap.put("Notes", "Notes of End User created by API");
        ArrayList<String> subtemplete = new ArrayList<>();
        subtemplete.add("ST10201");
        subtemplete.add("ST10202");
        requestMap.put("UserSubtemplateIDs", subtemplete);
        List<String> reportgroup = new ArrayList<>();
        reportgroup.add("Clinical Department Manager");
        requestMap.put("CategoryReportGrouper6", reportgroup);
        List<String> usermanager = new ArrayList<>();
        usermanager.add("IPISOIN");
        usermanager.add("1");
        requestMap.put("UsersManagers", usermanager);
        requestMap.put("PrimaryManager", "1");
//        requestMap.put("DefaultTemplateID", "T1085103");
//        requestMap.put("Provider", "HCTI2302");
        requestMap.put("UserIDType", "EXTERNAL");
        requestMap.put("AuditUserID", "HCTISK069");
        requestMap.put("AuditUserIDType", "External");
        requestMap.put("AuditUserPassword", "Rvts386!");
        test.info(MarkupHelper.createLabel("/epic/user/createUser", ExtentColor.GREEN));
        test.info(MarkupHelper.createLabel(jwtToken, ExtentColor.ORANGE));
        test.info(MarkupHelper.createCodeBlock(OBJECT_MAPPER.writeValueAsString(requestMap), CodeLanguage.JSON));
        ResultActions resultActions = this.mockMvc.perform(post("/epic/user/createUser")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", jwtToken)
                .content(OBJECT_MAPPER.writeValueAsString(requestMap)));
        int status = resultActions.andReturn().getResponse().getStatus();
        if (status == 201) {
            String result = resultActions.andReturn().getResponse().getContentAsString();
            String createMessage = "Epic user created successfully with UserID : " + userId;
            Assertions.assertEquals(createMessage, result);
            test.info(MarkupHelper.createLabel(String.valueOf(resultActions.andReturn().getResponse().getStatus()),
                    ExtentColor.GREEN));
            test.info(MarkupHelper.createLabel(result, ExtentColor.GREEN));
        } else {
            test.log(Status.FAIL, new Throwable());
            Assertions.fail();
        }
    }

    /**
     * Tests retrieving a single user by userId.
     * <p>
     * This test performs a GET request to the /epic/user/getUser/ endpoint and verifies that the response
     * status is 200 OK. This ensures that the retrieval functionality works as expected.
     * </p>
     *
     * @throws Exception if an error occurs during the request
     */
    @Order(3)
    @Test
    public void getSingleUser() throws Exception {
        ExtentTest test = extent.createTest("GetUser");
        test.info(MarkupHelper.createLabel("/epic/user/getUser/" + userId, ExtentColor.GREEN));
        test.info(MarkupHelper.createLabel(jwtToken, ExtentColor.ORANGE));
        ResultActions resultActions = this.mockMvc.perform(get("/epic/user/getUser/" + userId)
                .header("Authorization", jwtToken)
                .accept(MediaType.APPLICATION_JSON));
        int status = resultActions.andReturn().getResponse().getStatus();
        if (status == 200) {
            String result = resultActions.andReturn().getResponse().getContentAsString();
            test.info(MarkupHelper.createLabel(String.valueOf(resultActions.andReturn().getResponse().getStatus()),
                    ExtentColor.GREEN));
            Map<String, Object> resultMap = OBJECT_MAPPER.readValue(result,
                    new TypeReference<Map<String, Object>>() {
                    });
            Assertions.assertEquals(resultMap.get("UserID"), userId);
            Assertions.assertEquals(resultMap.get("FirstName"), "Leo");
            Assertions.assertEquals(resultMap.get("MiddleName"), "Hammock");
            Assertions.assertEquals(resultMap.get("ReportGrouper1"), "Accounts");
            Assertions.assertEquals(resultMap.get("IsActive"), true);
            test.info(MarkupHelper.createCodeBlock(result, CodeLanguage.JSON));
        } else {
            test.log(Status.FAIL, new Throwable());
            Assertions.fail();
        }
    }

    /**
     * Tests updating an existing user.
     * <p>
     * This test performs a POST request to the /epic/user/updateUser/{userId} endpoint with updated user details and
     * verifies that the response status is 200 OK. This ensures that the update functionality works as expected.
     * </p>
     *
     * @throws Exception if an error occurs during the request
     */
    @Order(4)
    @Test
    public void updateUser() throws Exception {
        ExtentTest test = extent.createTest("UpdateUser");
        Map<String, Object> requestMap = new HashMap<>();
        requestMap.put("SystemLoginID", systemLogin);
        requestMap.put("FirstName", "DASS");
        requestMap.put("LastName", "Harris");
        requestMap.put("GivenNameInitials", "S.");
        requestMap.put("MiddleName", "lara");
        requestMap.put("LastNamePrefix", "Md");
        requestMap.put("SpouseLastName", "Margret");
        requestMap.put("SpousePrefix", "M");
        requestMap.put("Suffix", "Sr.");
        requestMap.put("AcademicTitle", "Dr");
        requestMap.put("PrimaryTitle", "Mrs.");
        Map<String, String> BlockStatus = new HashMap<>();
        BlockStatus.put("IsBlocked", "false");
        BlockStatus.put("BlockReason", "others");
        BlockStatus.put("Comment", "Done this via update api call");
        requestMap.put("BlockStatus", BlockStatus);
        requestMap.put("SpouseLastNameFirst", "True");
        requestMap.put("ContactDate", "04/27/2023");
        requestMap.put("ContactComment", "End User Updated via API");
        requestMap.put("LDAPOverrideID", "marryjohn");
        requestMap.put("StartDate", "06/17/2023");
        requestMap.put("EndDate", "04/10/2026");
        requestMap.put("UserAlias", "Candidate to update");
        requestMap.put("UserPhotoPath", "https://unsplash.com/photos/tDUkP");
        requestMap.put("Sex", "FeMale");
        requestMap.put("IsActive", "true");
        requestMap.put("ReportGrouper1", "IT");
        requestMap.put("ReportGrouper2", "Accounts");
        requestMap.put("ReportGrouper3", "HR");
        requestMap.put("Notes", "Notes of End User updated by API");
        ArrayList<String> subtemplete = new ArrayList<>();
        subtemplete.add("ST10202");
        requestMap.put("UserSubtemplateIDs", subtemplete);
        List<String> reportgroup = new ArrayList<>();
        reportgroup.add("Dental Hygienist");
        requestMap.put("CategoryReportGrouper6", reportgroup);
        List<String> usermanager = new ArrayList<>();
        usermanager.add("IPISOIN");
        usermanager.add("1");
        requestMap.put("UsersManagers", usermanager);
        requestMap.put("PrimaryManager", "1");
//        requestMap.put("DefaultTemplateID", "T1085103");
//        requestMap.put("Provider", "HCTI2302");
        requestMap.put("UserIDType", "EXTERNAL");
        requestMap.put("AuditUserID", "HCTI4207");
        requestMap.put("AuditUserIDType", "External");
        requestMap.put("AuditUserPassword", "Rvts386!");
        test.info(MarkupHelper.createLabel("/epic/user/updateUser/{userId}" + userId, ExtentColor.GREEN));
        test.info(MarkupHelper.createLabel(jwtToken, ExtentColor.ORANGE));
        test.info(MarkupHelper.createCodeBlock(OBJECT_MAPPER.writeValueAsString(requestMap), CodeLanguage.JSON));
        ResultActions resultActions = this.mockMvc.perform(post("/epic/user/updateUser/{UserId}", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", jwtToken)
                .content(OBJECT_MAPPER.writeValueAsString(requestMap)));
        int status = resultActions.andReturn().getResponse().getStatus();
        if (status == 200) {
            String result = resultActions.andReturn().getResponse().getContentAsString();
            String Update_message = "Epic user updated successfully, with UserID : " + userId;
            Assertions.assertEquals(Update_message, result);
            test.info(MarkupHelper.createLabel(String.valueOf(resultActions.andReturn().getResponse().getStatus()),
                    ExtentColor.GREEN));
            test.info(MarkupHelper.createLabel(result, ExtentColor.GREEN));
        } else {
            test.log(Status.FAIL, new Throwable());
            Assertions.fail();
        }
    }

    /**
     * Tests disabling a user.
     * <p>
     * This test performs a POST request to the /epic/user/disableUser endpoint and verifies that the response
     * status is 200 OK. This ensures that the disable functionality works as expected.
     * </p>
     *
     * @throws Exception if an error occurs during the request
     */
    @Order(5)
    @Test
    public void disableUser() throws Exception {
        ExtentTest test = extent.createTest("DisableUser");
        Map<String, String> requestMap = new HashMap<>();
        requestMap.put("UserID", userId);
        test.info(MarkupHelper.createLabel("/epic/user/disableUser", ExtentColor.GREEN));
        test.info(MarkupHelper.createCodeBlock(OBJECT_MAPPER.writeValueAsString(requestMap), CodeLanguage.JSON));
        ResultActions resultActions = this.mockMvc.perform(post("/epic/user/disableUser")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", jwtToken)
                .content(OBJECT_MAPPER.writeValueAsString(requestMap)));
        int status = resultActions.andReturn().getResponse().getStatus();
        if (status == 200) {
            String result = resultActions.andReturn().getResponse().getContentAsString();
            String Disable_message = "Epic user disabled successfully, with userId : " + userId;
            Assertions.assertEquals(Disable_message, result);
            test.info(MarkupHelper.createLabel(String.valueOf(resultActions.andReturn().getResponse().getStatus()),
                    ExtentColor.GREEN));
            test.info(MarkupHelper.createLabel(result, ExtentColor.GREEN));
        } else {
            test.log(Status.FAIL, new Throwable());
            Assertions.fail();
        }
    }

    /**
     * Tests enabling a user.
     * <p>
     * This test performs a POST request to the /epic/user/enableUser endpoint and verifies that the response
     * status is 200 OK. This ensures that the enable functionality works as expected.
     * </p>
     *
     * @throws Exception if an error occurs during the request
     */
    @Order(6)
    @Test
    public void enableUser() throws Exception {
        ExtentTest test = extent.createTest("EnableUser");
        Map<String, String> requestMap = new HashMap<>();
        requestMap.put("UserID", userId);
        test.info(MarkupHelper.createLabel("/epic/user/enableUser", ExtentColor.GREEN));
        test.info(MarkupHelper.createCodeBlock(OBJECT_MAPPER.writeValueAsString(requestMap), CodeLanguage.JSON));
        ResultActions resultActions = this.mockMvc.perform(post("/epic/user/enableUser")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", jwtToken)
                .content(OBJECT_MAPPER.writeValueAsString(requestMap)));
        int status = resultActions.andReturn().getResponse().getStatus();
        if (status == 200) {
            String result = resultActions.andReturn().getResponse().getContentAsString();
            String enable_message = "Epic user enabled successfully with userId : " + userId;
            Assertions.assertEquals(enable_message, result);

            test.info(MarkupHelper.createLabel(String.valueOf(resultActions.andReturn().getResponse().getStatus()),
                    ExtentColor.GREEN));
            test.info(MarkupHelper.createLabel(result, ExtentColor.GREEN));
        } else {
            test.log(Status.FAIL, new Throwable());
            Assertions.fail();
        }
    }

    /**
     * Tests updating a user's password.
     * <p>
     * This test performs a POST request to the /epic/user/updatePassword endpoint with the new password and verifies
     * that the response status is 200 OK. This ensures that the password update functionality works as expected.
     * </p>
     *
     * @throws Exception if an error occurs during the request
     */
    @Order(7)
    @Test
    public void userPasswordUpdate() throws Exception {
        ExtentTest test = extent.createTest("UserPasswordUpdate");
        Map<String, String> requestMap = new HashMap<>();
        requestMap.put("UserID", userId);
        requestMap.put("NewPassword", "MVvts12345!");
        test.info(MarkupHelper.createLabel("/epic/user/updatePassword", ExtentColor.GREEN));
        test.info(MarkupHelper.createCodeBlock(OBJECT_MAPPER.writeValueAsString(requestMap), CodeLanguage.JSON));
        ResultActions resultActions = this.mockMvc.perform(post("/epic/user/updatePassword")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", jwtToken)
                .content(OBJECT_MAPPER.writeValueAsString(requestMap)));
        int status = resultActions.andReturn().getResponse().getStatus();
        if (status == 200) {
            String result = resultActions.andReturn().getResponse().getContentAsString();
            String passwordUpdateMessage = "Epic user password updated successfully with UserID : " + userId;
            Assertions.assertEquals(passwordUpdateMessage, result);
            test.info(MarkupHelper.createLabel(String.valueOf(resultActions.andReturn().getResponse().getStatus()),
                    ExtentColor.GREEN));
            test.info(MarkupHelper.createLabel(result, ExtentColor.GREEN));
        } else {
            test.log(Status.FAIL, new Throwable());
            Assertions.fail();
        }
    }

    /**
     * Tests retrieving all users with pagination.
     * <p>
     * This test performs a GET request to the /epic/user/getUsers endpoint with pagination parameters and verifies
     * that the response status is 200 OK. This ensures that the pagination functionality works as expected.
     * </p>
     *
     * @throws Exception if an error occurs during the request
     */
    @Order(8)
    @Test
    public void getAllUsersWithPagination() throws Exception {
        ExtentTest test = extent.createTest("GetAllUsersWithPagination");
        Map<String, Object> resultMap = new HashMap<>();
        Map<String, Object> contextMap = new HashMap<>();
        do {
            Map<String, Object> SearchStateContext = new HashMap<>();
            Map<String, Object> paginationDtl = new HashMap<>();
            String pageSize = "10";
            if (!contextMap.isEmpty()) {
                paginationDtl.put("Identifier", contextMap.get("Identifier"));
                paginationDtl.put("ResumeInfo", contextMap.get("ResumeInfo"));
                paginationDtl.put("CriteriaHash", contextMap.get("CriteriaHash"));
                SearchStateContext.put("SearchStateContext", paginationDtl);
                test.info(MarkupHelper.createLabel("Next" + pageSize + "Users", ExtentColor.GREEN));
            }
            SearchStateContext.put("pageSize", pageSize);
            test.info(MarkupHelper.createLabel("/epic/user/getUsers", ExtentColor.GREEN));
            test.info(MarkupHelper.createCodeBlock(OBJECT_MAPPER.writeValueAsString(SearchStateContext),
                    CodeLanguage.JSON));
            ResultActions pagination = this.mockMvc.perform(post("/epic/user/getUsers")
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("Authorization", jwtToken)
                            .content(OBJECT_MAPPER.writeValueAsString(SearchStateContext)))
                    .andExpect(status().isOk());
            String result1 = pagination.andReturn().getResponse().getContentAsString();
            test.info(MarkupHelper.createCodeBlock(result1, CodeLanguage.JSON));
            resultMap.clear();
            resultMap = OBJECT_MAPPER.readValue(result1,
                    new TypeReference<Map<String, Object>>() {
                    });
            contextMap = OBJECT_MAPPER.convertValue(Optional.ofNullable(resultMap.get("SearchStateContext"))
                            .orElse(Collections.emptyMap()),
                    new TypeReference<Map<String, Object>>() {
                    });
        } while (!contextMap.isEmpty());
    }

    /**
     * Tests updating a user's group.
     * <p>
     * This test performs a POST request to the /epic/user/updateGroups endpoint with the new group details and
     * verifies that the response status is 200 OK. This ensures that the user group update functionality works as
     * expected.
     * </p>
     *
     * @throws Exception if an error occurs during the request
     */
    @Order(9)
    @Test
    public void updateUsersGroup() throws Exception {
        ExtentTest test = extent.createTest("UpdateUsersGroup");
        Map<String, Object> requestMap = new HashMap<>();
        requestMap.put("UserID", userId);
        List<String> group = new ArrayList<>();
        group.add("1001");
        group.add("1003");
        group.add("1003");
        requestMap.put("UserGroups", group);
        test.info(MarkupHelper.createLabel("/epic/user/updateGroups", ExtentColor.GREEN));
        test.info(MarkupHelper.createCodeBlock(OBJECT_MAPPER.writeValueAsString(requestMap), CodeLanguage.JSON));
        ResultActions resultActions = this.mockMvc.perform(post("/epic/user/updateGroups")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", jwtToken)
                .content(OBJECT_MAPPER.writeValueAsString(requestMap)));
        int status = resultActions.andReturn().getResponse().getStatus();
        if (status == 200) {
            String result = resultActions.andReturn().getResponse().getContentAsString();
            String passwordUpdateMessage = "Epic user groups updated successfully with UserId : " + userId;
            Assertions.assertEquals(passwordUpdateMessage, result);
            test.info(MarkupHelper.createLabel(String.valueOf(resultActions.andReturn().getResponse().getStatus()),
                    ExtentColor.GREEN));
            test.info(MarkupHelper.createLabel(result, ExtentColor.GREEN));
        } else {
            test.log(Status.FAIL, new Throwable());
            Assertions.fail();
        }
    }

    /**
     * Tests retrieving a user's group.
     * <p>
     * This test performs a POST request to the /epic/user/viewGroups endpoint and verifies that the response
     * status is 200 OK. This ensures that the user group retrieval functionality works as expected.
     * </p>
     *
     * @throws Exception if an error occurs during the request
     */
    @Order(10)
    @Test
    public void getUerGroup() throws Exception {
        ExtentTest test = extent.createTest("GetUserGroup");
        Map<String, String> requestMap = new HashMap<>();
        requestMap.put("UserID", userId);
        test.info(MarkupHelper.createLabel("/epic/user/viewGroups", ExtentColor.GREEN));
        test.info(MarkupHelper.createCodeBlock(OBJECT_MAPPER.writeValueAsString(requestMap), CodeLanguage.JSON));
        ResultActions resultActions = this.mockMvc.perform(post("/epic/user/viewGroups")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", jwtToken)
                .content(OBJECT_MAPPER.writeValueAsString(requestMap)));
        int status = resultActions.andReturn().getResponse().getStatus();
        if (status == 200) {
            List<String> group = new ArrayList<>();
            group.add("Epic");
            group.add("Epic System");
            group.add("Epic System");
            String result = resultActions.andReturn().getResponse().getContentAsString();
            Map<String, Object> resultMap = OBJECT_MAPPER.readValue(result,
                    new TypeReference<Map<String, Object>>() {
                    });
            Assertions.assertEquals(resultMap.get("UserGroups"), group);
            test.info(MarkupHelper.createLabel(String.valueOf(resultActions.andReturn().getResponse().getStatus()),
                    ExtentColor.GREEN));
            test.info(MarkupHelper.createCodeBlock(result, CodeLanguage.JSON));
        } else {
            test.log(Status.FAIL, new Throwable());
            Assertions.fail();
        }
    }

    /**
     * Tests deleting a user.
     * <p>
     * This test performs a DELETE request to the /epic/user/deleteUser/{userId} endpoint and verifies that the
     * response status is 200 OK. This ensures that the user deletion functionality works as expected.
     * </p>
     *
     * @throws Exception if an error occurs during the request
     */
    @Test
    @Order(11)
    public void deleteUser() throws Exception {
        ExtentTest test = extent.createTest("DeleteUser");
        test.info(MarkupHelper.createLabel("/epic/user/deleteUser/{userId}" + userId, ExtentColor.GREEN));
        ResultActions resultActions = this.mockMvc.perform(delete("/epic/user/deleteUser/{UserID}", userId)
                .header("Authorization", jwtToken)
                .accept(MediaType.APPLICATION_JSON));
        int status = resultActions.andReturn().getResponse().getStatus();
        if (status == 200) {
            String result = resultActions.andReturn().getResponse().getContentAsString();
            String Delete_message = "Epic user deleted with UserID : " + userId;
            Assertions.assertEquals(Delete_message, result);
            test.info(MarkupHelper.createLabel(String.valueOf(resultActions.andReturn().getResponse().getStatus()),
                    ExtentColor.GREEN));
            test.info(MarkupHelper.createLabel(result, ExtentColor.GREEN));
        } else {
            test.log(Status.FAIL, new Throwable());
            Assertions.fail();
        }
    }

    /**
     * Finalizes the ExtentReports instance and flushes the report.
     * <p>
     * This method is executed after all tests have been run. It ensures that all logged
     * information is written to the report file by flushing the ExtentReports instance.
     * It is important to call this method to properly close and save the report, making
     * the test results available for review.
     * </p>
     */
    @AfterAll
    public static void ReportUser() {
        extent.flush();
    }
}
