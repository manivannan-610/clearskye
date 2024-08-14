package com.clearskye.epicconnector.utils;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Epic Constants class.
 */
public final class EpicConstants {
    /**
     * Equals to symbol.
     */
    public static final String EQUALS_SYMBOL = "=";
    /**
     * And symbol.
     */
    public static final String LOGICAL_AND_SYMBOL = "&";
    /**
     * Query symbol.
     */
    public static final String QUERY_SYMBOL = "?";
    /**
     * White space String.
     */
    public static final String WHITE_SPACE_CHARACTER = " ";
    /**
     * Encoded space String.
     */
    public static final String ENCODED_SPACE_CHARACTER = "%20";
    /**
     * User id Attribute of Epic.
     */
    public static final String USER_ID_TYPE_VALUE = "External";
    /**
     * User Id field of Epic.
     */
    public static final String USER_ID_FIELD = "UserID";
    /**
     * Status field of Epic.
     */
    public static final String STATUS_FIELD = "IsActive";
    /**
     * Password field of Epic.
     */
    public static final String USER_PASSWORD_FIELD = "NewPassword";
    /**
     * User id type of Epic.
     */
    public static final String USER_ID_TYPE_FIELD = "UserIDType";
    /**
     * Attribute Map of Epic.
     */
    public static final Map<String, String> EPIC_ATTR_MAP;
    /**
     * Json Header.
     */
    public static final String APPLICATION_JSON = "application/json";
    /**
     * Form Url Encoded Header.
     */
    public static final String APPLICATION_FORM_URL_ENCODED = "application/x-www-form-urlencoded";
    /**
     * Bearer Header.
     */
    public static final String TOKEN_TYPE = "Bearer ";
    /**
     * Access token String.
     */
    public static final String ACCESS_TOKEN = "access_token";
    /**
     * Epic create User End point.
     */
    public static final String EPIC_CREATE_USER_ENDPOINT = "api/epic/2014/Security/PersonnelManagement/CreateUser";
    /**
     * Epic get User End point.
     */
    public static final String EPIC_GET_USER_ENDPOINT = "api/epic/2014/Security/PersonnelManagement/ViewUser";
    /**
     * Epic update User End point.
     */
    public static final String EPIC_UPDATE_USER_ENDPOINT = "api/epic/2014/Security/PersonnelManagement/" + "UpdateUser/Personnel/User";
    /**
     * Epic set Password End point.
     */
    public static final String EPIC_SET_USER_PASSWORD_ENDPOINT = "api/epic/2012/Security/PersonnelManagement/" + "SetUserPassword/Personnel/User/EpicPassword";
    /**
     * Epic delete User End point.
     */
    public static final String EPIC_DELETE_USER_ENDPOINT = "api/epic/2012/Security/PersonnelManagement/" + "DeleteUser/Personnel/User/Delete";
    /**
     * Epic activate User End point.
     */
    public static final String EPIC_ACTIVATE_USER_ENDPOINT = "api/epic/2012/Security/PersonnelManagement/ActivateUser/Personnel/User/Activate";
    /**
     * Epic de-activate User End point.
     */
    public static final String EPIC_DEACTIVATE_USER_ENDPOINT = "/api/epic/2012/Security/PersonnelManagement/InactivateUser/Personnel/User/Inactivate";
    /**
     * Epic oAuth Token End point.
     */
    public static final String EPIC_ACCESS_TOKEN_ENDPOINT = "oauth2/token";
    /**
     * Epic get group End point.
     */
    public static final String EPIC_GET_USER_GROUPS_ENDPOINT = "api/epic/2016/Security/PersonnelManagement/" + "ViewUserGroups/Personnel/User/Groups/View";
    /**
     * Epic update group End point.
     */
    public static final String EPIC_UPDATE_USER_GROUPS_ENDPOINT = "api/epic/2016/Security/PersonnelManagement/" + "UpdateUserGroups/Personnel/User/Groups/Update";
    /**
     * Epic SOAP End point.
     */
    public static final String SOAP_END_POINT = "httplistener.ashx";
    /**
     * Epic Primary manager Attribute.
     */
    public static final String EPIC_ATTR_PRIMARY_MANAGER = "PrimaryManager";
    /**
     * Epic Default department Attribute.
     */
    public static final String EPIC_ATTR_DEFAULT_LOGIN_DEPT_ID = "DefaultLoginDepartmentID";
    /**
     * Type String.
     */
    public static final String TYPE = "Type";
    /**
     * Epic user sub template Attribute.
     */
    public static final String EPIC_ATTR_USER_SUBTEMPLATE_IDS = "UserSubtemplateIDs";
    /**
     * Epic user sub template string.
     */
    public static final String EPIC_ATTR_USER_SUBTEMPLATE_ID = "UserSubtemplateID";
    /**
     * Identifier String.
     */
    public static final String IDENTIFIER = "Identifier";
    /**
     * Identifiers String.
     */
    public static final String IDENTIFIERS = "Identifiers";
    /**
     * Index String.
     */
    public static final String INDEX = "Index";
    /**
     * Epic Provider Attribute.
     */
    public static final String PROVIDER = "Provider";
    /**
     * Epic Default Template ID Attribute.
     */
    public static final String DEFAULT_TEMPLATE_ID = "DefaultTemplateID";
    /**
     * Epic Applied Template ID Attribute.
     */
    public static final String APPLIED_TEMPLATE_ID = "AppliedTemplateID";
    /**
     * Linked Provider ID attribute.
     */
    public static final String PROVIDER_ID = "LinkedProviderID";
    /**
     * Epic Available Templates Attribute.
     */
    public static final String AVAILABLE_TEMPLATES = "AvailableLinkableTemplates";
    /**
     * Epic Linked Templates Configuration Attribute.
     */
    public static final String TEMPLATES_CONFIG = "LinkedTemplatesConfig";
    /**
     * Epic Optional Attributes set.
     */
    public static final Set<String> EPIC_OPTIONAL_ATTRIBUTES = Collections.unmodifiableSet(new HashSet<>(Arrays.asList("UserGroups", "NewPassword", "ContactDate", "ContactComment", "LDAPOverrideID", "SystemLoginID", "UserAlias", "UserPhotoPath", "Sex", "ReportGrouper1", "ReportGrouper2", "ReportGrouper3", "Notes", "BlockStatus", "GivenNameInitials", "LastNamePrefix", "SpouseLastName", "SpousePrefix", "Suffix", "AcademicTitle", "PrimaryTitle", "SpouseLastNameFirst", "Status", "PrimaryManager", "IsActive", "UsersManagers", "UserSubtemplateIDs", "InBasketClassifications", "DefaultLoginDepartmentID", PROVIDER, "DefaultTemplateID", "UserID", "Name", "StartDate", "EndDate", "FirstName", "LastName", "CategoryReportGrouper6", "AuditUserID", "AuditUserIDType", "AuditUserPassword", "MiddleName")));

    /**
     * Epic Complex Attributes set.
     */
    public static final Set<String> EPIC_COMPLEX_TYPE_ATTR_SET = Collections.unmodifiableSet(new HashSet<>(List.of("BlockStatus")));
    /**
     * Epic MultiValued Attributes set.
     */
    public static final Set<String> EPIC_MULTI_VALUED_ATTR_SET = Collections.unmodifiableSet(new HashSet<>(Arrays.asList("UsersManagers", "UserSubtemplateIDs", "InBasketClassifications", "CategoryReportGrouper6")));
    /**
     * Epic JWT header map.
     */
    public static final Map<String, Object> EPIC_JWT_HEADER = Stream.of(new String[][]{{"alg", "RS384"}, {"typ", "JWT"}}).collect(Collectors.collectingAndThen(Collectors.toMap(data -> data[0], data -> data[1]), Collections::<String, Object>unmodifiableMap));
    /**
     * Epic Boolean Attributes set.
     */
    public static final Set<String> BOOLEAN_ATTR_SET = new HashSet<>(Arrays.asList("IsActive", "SpouseLastNameFirst", "IsBlocked"));
    /**
     * SOAP URN value.
     */
    public static final String SOAP_URN_VALUE = "urn:epicsystems-com:ManagedCare.2010.Services.Account";
    /**
     * XML Result Record tag string.
     */
    public static final String RESULT_RECORD_TAG = "ResultRecord";
    /**
     * XML Additional Fields tag string.
     */
    public static final String ADD_FIELDS_TAG = "AdditionalFields";
    /**
     * XML ID tag string.
     */
    public static final String XML_RECORD_ID = "ExternalID";
    /**
     * XML Title key string.
     */
    public static final String XML_FIELDS_KEY = "Title";
    /**
     * XML Value key string.
     */
    public static final String XML_FIELDS_VALUE = "Value";
    /**
     * XML Search State Context string.
     */
    public static final String SEARCH_CONTEXT = "SearchStateContext";
    /**
     * XML Invalid record message.
     */
    public static final String OBJECT_NOT_USER = "INVALID-RECORD-TYPE";
    /**
     * XML Record List key string.
     */
    public static final String RECORDLIST = "recordList";
    /**
     * Grant Type Header.
     */
    public static final String GRANT_TYPE = "grant_type";
    /**
     * Client Credentials Header.
     */
    public static final String CLIENT_CREDENTIALS = "client_credentials";
    /**
     * Client Assertion Type Header.
     */
    public static final String CLIENT_ASSERTION_TYPE = "client_assertion_type";
    /**
     * Epic JWT URN.
     */
    public static final String JWT_URN = "urn:ietf:params:oauth:client-assertion-type:jwt-bearer";
    /**
     * Expires in string.
     */
    public static final String EXPIRES_IN = "expires_in";
    /**
     * Clearskye expires in string.
     */
    public static final String CLEARSKYE_EXPIRES_IN = "expiresIn";
    /**
     * Client Assertion String.
     */
    public static final String CLIENT_ASSERTION = "client_assertion";
    /**
     * Client Id String.
     */
    public static final String EPIC_CLIENT_ID = "Epic-Client-ID";
    /**
     * JWT format String.
     */
    public static final String FORMAT = "{0}-{1}-{2}-{3}-{4}";
    /**
     * XML Criteria Hash tag.
     */
    public static final String CRITERIA_HASH = "CriteriaHash";
    /**
     * XML Resume Info tag.
     */
    public static final String RESUME_INFO = "ResumeInfo";
    /**
     * XML Max Records tag.
     */
    public static final String MAX_REC_PER_FETCH = "MaximumRecordsPerFetch";
    /**
     * XML If needed tag.
     */
    public static final String USE_IF_NEEDED = "UseIfNeeded";
    /**
     * XML Like mode tag.
     */
    public static final String SOUNDS_LIKE_MODE = "SoundsLikeMode";
    /**
     * XML Skip tag.
     */
    public static final String SKIPENROL = "SkipEnRol";
    /**
     * False string.
     */
    public static final String FALSE = "false";
    /**
     * Active string.
     */
    public static final String ACTIVE = "Active";
    /**
     * XML Record State tag.
     */
    public static final String RECORDSTATE = "RecordState";
    /**
     * XML Search String tag.
     */
    public static final String SEARCH_STRING = "SearchString";
    /**
     * XML Employee search string.
     */
    public static final String EMP = "EMP";
    /**
     * XML Object search string.
     */
    public static final String INI = "INI";
    /**
     * XML Search Criteria tag.
     */
    public static final String SEARCH_CRITERIA = "SearchCriteria";
    /**
     * XML XSI key.
     */
    public static final String XSI = "xsi";
    /**
     * XML XSI URN.
     */
    public static final String XSI_URN = "http://www.w3.org/2001/XMLSchema-instance";
    /**
     * XML Get Key.
     */
    public static final String GET_RECORDS = "GetRecords";
    /**
     * XML Get URN.
     */
    public static final String GET_RECORDS_URN = "urn:epicsystems-com:Core.2008-04.Services";

    /**
     * XML Default text node value.
     */
    public static final String TEXT_NODE_IQ = "iQNWtmoUlkGIUDj2x2YY7g==";
    /**
     * XML Encoding type key.
     */
    public static final String ENCODING_TYPE = "EncodingType";
    /**
     * XML Encoding type URN.
     */
    public static final String ENCODING_TYPE_URN = "http://docs.oasis-open.org/wss/2004/01/" + "oasis-200401-wss-soap-message-security-1.0#Base64Binary";
    /**
     * XML Nonce string.
     */
    public static final String NONCE = "Nonce";
    /**
     * XML Wsse key.
     */
    public static final String WSSE = "wsse";
    /**
     * Ampersand symbol.
     */
    public static final String AMPERSAND = "&";
    /**
     * Encoded ampersand symbol.
     */
    public static final String AMP_CHAR_REF = "&amp;";
    /**
     * XML password URN.
     */
    public static final String TYPE_PASSWORD_URN = "http://docs.oasis-open.org/wss/2004/01/" + "oasis-200401-wss-username-token-profile-1.0#PasswordText";
    /**
     * Epic password string.
     */
    public static final String PASSWORD = "Password";
    /**
     * Epic user name string.
     */
    public static final String USERNAME = "Username";
    /**
     * XML EMP type string.
     */
    public static final String EMP_COLON = "emp:";
    /**
     * XML WSU ID string.
     */
    public static final String WSU_ID = "wsu:Id";
    /**
     * Default user name token id.
     */
    public static final String USERNAME_TOKEN_ID = "UsernameToken-A2D1F6D49E5DC5B9D915224298759042";
    /**
     * User name token string.
     */
    public static final String USERNAME_TOKEN = "UsernameToken";
    /**
     * XML WSU key.
     */
    public static final String XMLNS_WSU = "xmlns:wsu";
    /**
     * XML WSU URN.
     */
    public static final String XMLNS_WSU_URN = "http://docs.oasis-open.org/wss/2004/01/" + "oasis-200401-wss-wssecurity-utility-1.0.xsd";
    /**
     * XML Security key.
     */
    public static final String SECURITY = "Security";
    /**
     * XML Security URN.
     */
    public static final String SECURITY_URN = "http://docs.oasis-open.org/wss/2004/01/" + "oasis-200401-wss-wssecurity-secext-1.0.xsd";
    /**
     * XML URN key.
     */
    public static final String XMLNS_URN = "xmlns:urn";
    /**
     * Epic internal id key.
     */
    public static final String USER_INTERNAL_ID = "UserInternalID";
    /**
     * Epic external id key.
     */
    public static final String EXTERNAL = "External";
    /**
     * Epic UID name.
     */
    public static final String UID = "uid";
    /**
     * Epic ID name.
     */
    public static final String ID = "ID";
    /**
     * Epic update name key.
     */
    public static final String UPDATE_NAME = "name";
    /**
     * Epic items string.
     */
    public static final String ITEMS = "items";
    /**
     * Epic replace value.
     */
    public static final String REPLACE = "Replace";
    /**
     * Epic mode value.
     */
    public static final String MODE = "Mode";
    /**
     * Category Report Grouper Attribute.
     */
    public static final String CATEGORY_REPORT_GROUPER6 = "CategoryReportGrouper6";
    /**
     * In Basket Classifications Attribute.
     */
    public static final String INBASKET_CLASSIFICATION = "InBasketClassifications";
    /**
     * User Groups Attribute.
     */
    public static final String USERGROUPS = "UserGroups";
    /**
     * RSA String.
     */
    public static final String RSA = "RSA";
    /**
     * UTF SOAP String.
     */
    public static final String UTF = "UTF-8";
    /**
     * Epic User Id Key.
     */
    public static final String USER_IDS = "UserIDs";

    static {
        EPIC_ATTR_MAP = new HashMap<>();
        EPIC_ATTR_MAP.put("uid", "UserID");
        EPIC_ATTR_MAP.put("status", "IsActive");
        EPIC_ATTR_MAP.put("uidList", "UserIDs");
        EPIC_ATTR_MAP.put("id", "ID");
        EPIC_ATTR_MAP.put("name", "Name");
    }

    /**
     * Default Maximum Records size.
     */
    public static final int DEFAULT_MAX_RECORDS = 50;
    /**
     * Default Maximum Timeout of Connection in seconds.
     */
    public static final int DEFAULT_MAX_TIMEOUT = 600;
    /**
     * Empty String.
     */
    public static final String EMPTY_STRING = "";
    /**
     * User Complex Name String.
     */
    public static final String COMPLEX_NAME = "UserComplexName";
    /**
     * Get Records Response XML Tag.
     */
    public static final String GET_RECORDS_RESPONSE = "GetRecordsResponse";
    /**
     * XML Field tag string.
     */
    public static final String XML_FIELD_TAG = "Field";
    /**
     * Date format.
     */
    public static final String DATE_FOMRAT = "MM/dd/yyyy";
    /**
     * Contact date string.
     */
    public static final String CONTACTDATE = "ContactDate";
    /**
     * UserComplexName attribute set.
     */
    public static final Set<String> USER_COMPLEX_NAME_ATTRS = Collections.unmodifiableSet(new HashSet<>(Arrays.asList("FirstName", "GivenNameInitials", "LastName", "LastNamePrefix", "SpouseLastName", "SpousePrefix", "Suffix", "AcademicTitle", "PrimaryTitle", "SpouseLastNameFirst", "MiddleName")));
    /**
     * UsersManager string.
     */
    public static final String USERS_MANAGERS = "UsersManagers";
    /**
     * Config parameter username.
     */
    public static final String CONFIG_USERNAME = "epic.username";
    /**
     * Config parameter password.
     */
    public static final String CONFIG_PASSWORD = "epic.password";
    /**
     * Config parameter client id.
     */
    public static final String CLIENT_ID = "epic.clientId";
    /**
     * Config parameter private key.
     */
    public static final String PRIVATE_KEY = "epic.privateKey";
    /**
     * Config parameter rest endpoint.
     */
    public static final String REST_ENDPOINT = "epic.restEndpoint";
    /**
     * Config parameter soap endpoint.
     */
    public static final String SOAPENDPOINT = "epic.soapEndpoint";
    /**
     * Config max records.
     */
    public static final String MAX_RECORDS = "epic.maxRecords";
    /**
     * Default offset.
     */
    public static final String DEFAULT_OFFSET = "0";
    /**
     * Config parameter user templates filepath.
     */
    public static final String USER_TEMPLATES_FILEPATH = "epic.userTemplatesFilePath";
    /**
     * Config parameter groups filepath.
     */
    public static final String GROUPS_FILEPATH = "epic.groupsFilePath";
    /**
     * Config parameter sub templates filepath.
     */
    public static final String SUB_TEMPLATES_FILEPATH = "epic.subTemplatesFilePath";
    /**
     * Refresh token string.
     */
    public static final String REFRESH_TOKEN = "refreshToken";
    /**
     * Access token string.
     */
    public static final String CLEARSKYE_ACCESSS_TOKEN = "accessToken";
    /**
     * Token type string.
     */
    public static final String TOKEN_TYP = "tokenType";
    /**
     * Bearer string.
     */
    public static final String BEARER = "Bearer";
    /**
     * Refresh string.
     */
    public static final String REFRESH = "refresh";
    /**
     * Access string.
     */
    public static final String ACCESS = "access";
    /**
     * Group field.
     */
    public static final String GROUP = "UserGroups";
    /**
     * Access token validity in seconds.
     */
    public static final String ACCESS_TOKEN_VALIDITY = "1800";
    /**
     * Access token string.
     */
    public static final String ACCESS_TOKEN_TYPE = "accessToken";
    /**
     * Page size string.
     */
    public static final String PAGE_SIZE = "pageSize";
    /**
     * Users string.
     */
    public static final String USERS = "Users";
    /**
     * Message string.
     */
    public static final String RESPONSE_MESSAGE = "Message";
    /**
     * Claim name string.
     */
    public static final String CLAIM_NAME = "name";
    /**
     * JWT token type.
     */
    public static final String JWT_TOKEN_TYPE = "typ";
    /**
     * Secret string.
     */
    public static final String SECRET = "secret";
    /**
     * JWT string.
     */
    public static final String JWT = "JWT";
    /**
     * DOT string.
     */
    public static final String DOT = ".";
    /**
     * Authorization string.
     */
    public static final String AUTHORIZATION = "Authorization";
    /**
     * App name.
     */
    public static final String APP_NAME = "clearskye";
    /**
     * Clearskye config username.
     */
    public static final String CLEARSKYE_USERNAME_KEY = "clearskye.username";
    /**
     * Clearskye config password.
     */
    public static final String CLEARSKYE_PASSWORD_KEY = "clearskye.password";
    /**
     * Clearskye config refresh token secret name.
     */
    public static final String CLEARSKYE_REFRESHTOKEN_SECRET = "clearskye.refreshToken.secret";
    /**
     * Comma separator string.
     */
    public static final List<String> POSSIBLE_DELIMITERS = Collections.unmodifiableList(Arrays
            .asList(",", ";", "\t", "|", "^"));
    /**
     * CSV delimiter.
     */
    public static final String CSV_DELIMITER = "delimiter";
    /**
     * CSV headers.
     */
    public static final String CSV_HEADERS = "headers";
    /**
     * Head row count string.
     */
    public static final String HEAD_ROW_COUNT = "headRowCount";
    /**
     * Offset string.
     */
    public static final String OFFSET = "offset";
    /**
     * Left bracket.
     */
    public static final String LEFT_BRACKET = "[";
    /**
     * Right bracket.
     */
    public static final String RIGHT_BRACKET = "]";
}