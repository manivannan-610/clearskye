package com.clearskye.epicconnector.service;

import static com.clearskye.epicconnector.utils.EpicConstants.DEFAULT_MAX_RECORDS;
import static com.clearskye.epicconnector.utils.EpicConstants.MAX_RECORDS;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;


import javax.xml.namespace.QName;

import jakarta.xml.soap.MessageFactory;
import jakarta.xml.soap.MimeHeaders;
import jakarta.xml.soap.SOAPBody;
import jakarta.xml.soap.SOAPConnection;
import jakarta.xml.soap.SOAPElement;
import jakarta.xml.soap.SOAPEnvelope;
import jakarta.xml.soap.SOAPException;
import jakarta.xml.soap.SOAPFault;
import jakarta.xml.soap.SOAPHeader;
import jakarta.xml.soap.SOAPMessage;
import jakarta.xml.soap.SOAPPart;

import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.clearskye.epicconnector.exception.CustomInvalidCredentialException;
import com.clearskye.epicconnector.exception.CustomCommonException;
import com.clearskye.epicconnector.utils.EpicConstants;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.xml.soap.SOAPConnectionFactory;
import lombok.RequiredArgsConstructor;

/**
 * Service class for managing connections to SOAP-based Epic web services.
 */
@Service
@RequiredArgsConstructor
public class EpicSoapConnectionService {
    /**
     * Object Mapper for the Epic SOAP Connection.
     */
    private static final ObjectMapper objectMapper = new ObjectMapper();
    /**
     * SOAP Connection.
     */
    private SOAPConnection soapConnection;
    /**
     * Environment to access environment-specific properties.
     */
    private final Environment environment;

    /**
     * Execute the SOAP Service.
     *
     * @param searchContextMap Map to be used for pagination
     * @param type             Details to get
     * @param filter           Filter the result, may be null
     * @return responseMap Map of the response of Connection
     * @throws Exception Exception during SOAP request.
     */
    public Map<String, Object> callSoapService(Map<String, Object> searchContextMap,
            String type, String filter) throws Exception {
        Map<String, Object> responseMap = new HashMap<String, Object>();
        soapConnection = this.getSoapConnection();
        MessageFactory messageFactory = MessageFactory.newInstance();
        SOAPMessage soapMessage = messageFactory.createMessage();
        MimeHeaders headers = soapMessage.getMimeHeaders();
        headers.addHeader(EpicConstants.EPIC_CLIENT_ID, environment.getProperty(EpicConstants.CLIENT_ID));
        createSoapEnvelope(soapMessage, searchContextMap, type, filter);
        soapMessage.saveChanges();
        SOAPMessage soapResponse = soapConnection.call(soapMessage, environment.
                getProperty(EpicConstants.SOAPENDPOINT) + EpicConstants.SOAP_END_POINT);
        SOAPBody body = soapResponse.getSOAPBody();
        if (body.hasFault()) {
            handleSoapError(body.getFault());
        }
        NodeList responseNodeList = body.getElementsByTagName(EpicConstants.GET_RECORDS_RESPONSE);
        Node responseNode = responseNodeList.item(0);
        if (responseNode.getNodeType() == Node.ELEMENT_NODE) {
            Element responseElement = (Element) responseNode;
            NodeList recordsNodeList = responseElement.getElementsByTagName(EpicConstants.RESULT_RECORD_TAG);
            if (recordsNodeList != null && recordsNodeList.getLength() > 0) {
                List<Map<String, Object>> recordList = new ArrayList<Map<String, Object>>();
                for (int i = 0; i < recordsNodeList.getLength(); i++) {
                    Node resultNode = recordsNodeList.item(i);
                    NodeList resultElementList = resultNode.getChildNodes();
                    if (resultElementList != null && resultElementList.getLength() > 0) {
                        Map<String, Object> recordMap = new HashMap<>();
                        for (int j = 0; j < resultElementList.getLength(); j++) {
                            Node elementNode = resultElementList.item(j);
                            if (elementNode.getNodeName().equals(EpicConstants.ADD_FIELDS_TAG)) {
                                Element resultElement = (Element) resultNode;
                                NodeList additionalRecords = resultElement
                                        .getElementsByTagName(EpicConstants.XML_FIELD_TAG);
                                if (additionalRecords != null && additionalRecords
                                        .getLength() > 0) {
                                    for (int k = 0; k < additionalRecords.getLength(); k++) {
                                        Element additionalNode = (Element) additionalRecords
                                                .item(k);
                                        recordMap.put(additionalNode.getElementsByTagName(
                                                        EpicConstants.XML_FIELDS_KEY).item(0).getTextContent(),
                                                additionalNode.getElementsByTagName(EpicConstants.XML_FIELDS_VALUE)
                                                        .item(0)
                                                        .getTextContent());
                                    }
                                }
                            } else {
                                recordMap.put(elementNode.getNodeName(), elementNode
                                        .getTextContent()
                                        .trim());
                            }
                        }
                        recordList.add(recordMap);
                    }
                }
                responseMap.put(EpicConstants.RECORDLIST, recordList);
            }
            NodeList contextElement = responseElement.getElementsByTagName(
                    EpicConstants.SEARCH_CONTEXT);
            Map<String, Object> pageMap = new HashMap<>();
            Element searchElement = (Element) contextElement.item(0);
            if (searchElement != null) {
                pageMap.put(EpicConstants.IDENTIFIER, searchElement.getElementsByTagName(EpicConstants.IDENTIFIER)
                        .item(0)
                        .getTextContent());
                pageMap.put(EpicConstants.RESUME_INFO, searchElement.getElementsByTagName(EpicConstants.RESUME_INFO)
                        .item(0)
                        .getTextContent());
                pageMap.put(EpicConstants.CRITERIA_HASH, searchElement.getElementsByTagName(EpicConstants.CRITERIA_HASH)
                        .item(0)
                        .getTextContent());
                responseMap.put(EpicConstants.SEARCH_CONTEXT, pageMap);
            }
        }
        soapMessage.removeAllAttachments();
        return responseMap;
    }

    /**
     * Create the SOAP Envelope for the Service.
     *
     * @param soapMessage      Message to be added.
     * @param searchContextMap contextMap added for pagination.
     * @param type             Details to get
     * @param filter           Filter the result, may be null.
     * @throws SOAPException Exception during create SOAP envelope.
     */
    private void createSoapEnvelope(SOAPMessage soapMessage, Map<String, Object> searchContextMap,
            String type, String filter) throws SOAPException {
        Map<String, String> contextMap = new HashMap<>();
        String maxRecord = String.valueOf(DEFAULT_MAX_RECORDS);
        SOAPPart soapPart = soapMessage.getSOAPPart();
        SOAPEnvelope envelope = soapPart.getEnvelope();
        envelope.addAttribute(new QName(EpicConstants.XMLNS_URN), EpicConstants.SOAP_URN_VALUE);
        SOAPHeader header = envelope.getHeader();
        SOAPElement security = header.addChildElement(EpicConstants.SECURITY, EpicConstants.WSSE, EpicConstants.SECURITY_URN);
        security.addAttribute(new QName(EpicConstants.XMLNS_WSU), EpicConstants.XMLNS_WSU_URN);
        SOAPElement usernameToken = security.addChildElement(EpicConstants.USERNAME_TOKEN, EpicConstants.WSSE);
        usernameToken.addAttribute(new QName(EpicConstants.WSU_ID), EpicConstants.USERNAME_TOKEN_ID);
        SOAPElement username = usernameToken.addChildElement(EpicConstants.USERNAME, EpicConstants.WSSE);
        username.addTextNode(EpicConstants.EMP_COLON + environment.getProperty(EpicConstants.CONFIG_USERNAME));
        SOAPElement password = usernameToken.addChildElement(EpicConstants.PASSWORD, EpicConstants.WSSE);
        password.setAttribute(EpicConstants.TYPE, EpicConstants.TYPE_PASSWORD_URN);
        String passwordString = environment.getProperty(EpicConstants.CONFIG_PASSWORD);
        if (passwordString.contains(EpicConstants.AMP_CHAR_REF)) {
            passwordString = passwordString.replace(EpicConstants.AMP_CHAR_REF, EpicConstants.AMPERSAND);
        }
        password.addTextNode(passwordString);
        SOAPElement nonce = usernameToken.addChildElement(EpicConstants.NONCE, EpicConstants.WSSE);
        nonce.setAttribute(EpicConstants.ENCODING_TYPE, EpicConstants.ENCODING_TYPE_URN);
        nonce.addTextNode(EpicConstants.TEXT_NODE_IQ);
        SOAPBody soapBody = envelope.getBody();
        SOAPElement getRecords = soapBody.addChildElement(new QName(EpicConstants.GET_RECORDS_URN,
                EpicConstants.GET_RECORDS));
        getRecords.setAttribute(EpicConstants.XSI, EpicConstants.XSI_URN);
        SOAPElement searchElement = getRecords.addChildElement(EpicConstants.SEARCH_CRITERIA);
        SOAPElement iniAttr = searchElement.addChildElement(EpicConstants.INI);
        iniAttr.addTextNode(type);
        if (filter != null) {
            SOAPElement searchAttr = searchElement.addChildElement(EpicConstants.SEARCH_STRING);
            searchAttr.addTextNode(filter
            );
        } else {
            SOAPElement searchAttr = searchElement.addChildElement(EpicConstants.SEARCH_STRING);
            searchAttr.addTextNode("HCTI");
        }
        if (searchContextMap != null) {
            maxRecord =
                    objectMapper.convertValue(Optional.
                            ofNullable(searchContextMap.get(EpicConstants.PAGE_SIZE))
                                    .orElse(Optional.ofNullable(environment.getProperty(MAX_RECORDS)).orElse(String.valueOf(DEFAULT_MAX_RECORDS))),
                    new TypeReference<String>() {
                    });
            contextMap = objectMapper.convertValue(Optional.
                            ofNullable(searchContextMap.get(EpicConstants.SEARCH_CONTEXT)).orElse(Collections.emptyMap()),
                    new TypeReference<Map<String, String>>() {
                    });
        }
        SOAPElement stateAttr = searchElement.addChildElement(EpicConstants.RECORDSTATE);
        stateAttr.addTextNode(EpicConstants.ACTIVE);
        SOAPElement skipEnRole = searchElement.addChildElement(EpicConstants.SKIPENROL);
        skipEnRole.addTextNode(EpicConstants.FALSE);
        SOAPElement modeAttr = searchElement.addChildElement(EpicConstants.SOUNDS_LIKE_MODE);
        modeAttr.addTextNode(EpicConstants.USE_IF_NEEDED);
        SOAPElement maxRecords = searchElement.addChildElement(EpicConstants.MAX_REC_PER_FETCH);
        maxRecords.addTextNode(maxRecord);
        if (contextMap != null && !contextMap.isEmpty()) {
            SOAPElement searchContext = getRecords.addChildElement(EpicConstants.SEARCH_CONTEXT);
            SOAPElement identifier = searchContext.addChildElement(EpicConstants.IDENTIFIER);
            identifier.addTextNode(contextMap.get(EpicConstants.IDENTIFIER));
            SOAPElement resInfo = searchContext.addChildElement(EpicConstants.RESUME_INFO);
            resInfo.addTextNode(contextMap.get(EpicConstants.RESUME_INFO));
            SOAPElement crHash = searchContext.addChildElement(EpicConstants.CRITERIA_HASH);
            crHash.addTextNode(contextMap.get(EpicConstants.CRITERIA_HASH));
        }
        SOAPElement userId = getRecords.addChildElement(EpicConstants.USER_ID_FIELD);
        userId.addTextNode(environment.getProperty(EpicConstants.CONFIG_USERNAME));
    }

    /**
     * Handle Custom SOAP Errors.
     *
     * @param soapFault SOAP Fault.
     */
    private void handleSoapError(SOAPFault soapFault) {
        String faultString = soapFault.getFaultString();
        if (soapFault.getFaultCode().equals("fns:FailedAuthentication")) {
            throw new CustomInvalidCredentialException(faultString);
        }
        throw new CustomCommonException(faultString);
    }


    /**
     * Retrieve/Create SOAP Connection.
     *
     * @return soapConnection SOAP connection to be used
     */
    public SOAPConnection getSoapConnection() {
        if (null == soapConnection) {
            synchronized (this) {
                if (null == soapConnection) {
                    try {
                        SOAPConnectionFactory soapConnectionFactory = SOAPConnectionFactory.newInstance();
                        this.soapConnection = soapConnectionFactory.createConnection();
                    } catch (SOAPException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }
        return soapConnection;
    }
}