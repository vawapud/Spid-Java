package it.spid.util.impl;

import it.spid.exception.SpidServiceException;
import it.spid.model.LogoutRequestDecoded;
import it.spid.util.LogoutDecoder;
import it.spid.util.XmlDigitalSignature;
import org.apache.commons.codec.binary.Base64;
import org.opensaml.Configuration;
import org.opensaml.saml2.core.*;
import org.opensaml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml2.metadata.IDPSSODescriptor;
import org.opensaml.saml2.metadata.KeyDescriptor;
import org.opensaml.saml2.metadata.provider.AbstractReloadingMetadataProvider;
import org.opensaml.saml2.metadata.provider.MetadataProviderException;
import org.opensaml.saml2.metadata.provider.ResourceBackedMetadataProvider;
import org.opensaml.util.resource.ClasspathResource;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.io.MarshallingException;
import org.opensaml.xml.io.Unmarshaller;
import org.opensaml.xml.io.UnmarshallerFactory;
import org.opensaml.xml.io.UnmarshallingException;
import org.opensaml.xml.parse.BasicParserPool;
import org.opensaml.xml.security.x509.BasicX509Credential;
import org.opensaml.xml.signature.*;
import org.opensaml.xml.util.XMLHelper;
import org.opensaml.xml.validation.ValidationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.security.cert.CertificateException;
import java.util.List;
import java.util.Properties;
import java.util.Timer;

public class LogoutDecoderImpl implements LogoutDecoder {

    private String samlLogoutResponse;
    private static final String SPID_IDP_PREFIX ="it.spid.idp.";
    private static final String SPID_IDP_KEYS ="it.spid.idp.keys";
    private static final String SPID_SP_PREFIX ="it.spid.sp.assertionConsumerServiceUrl";
    private static final String SPID_SP_ENTITYID = "it.spid.sp.entityId";
    private static final String SAML2_PROTOCOL = "urn:oasis:names:tc:SAML:2.0:protocol";

    public LogoutDecoderImpl(String samlLogoutResponse) {
        this.samlLogoutResponse = samlLogoutResponse;
    }

    @Override
    public LogoutRequestDecoded processLogoutResponse() throws ParserConfigurationException, UnmarshallingException, SAXException, IOException, SpidServiceException {

        LogoutRequestDecoded logoutRequestDecoded = new LogoutRequestDecoded();

        LogoutResponse logoutResponse = getLogoutResponse();
        String inResponseTo = logoutResponse.getInResponseTo();
        Status status = logoutResponse.getStatus();
        String statusCode = status.getStatusCode().getValue();
        if(!StatusCode.SUCCESS_URI.equals(statusCode)){

            throw new SpidServiceException("Status code: " + statusCode + " ( atteso: " + StatusCode.SUCCESS_URI + " )");
        }
        try {
            //sistemare
            verifySignature(logoutResponse);
        } catch (MetadataProviderException e) {
            e.printStackTrace();
        }
        //validate();
        logoutRequestDecoded.setInResponseTo(inResponseTo);
        return logoutRequestDecoded;
    }

    public LogoutResponse getLogoutResponse() throws ParserConfigurationException, IOException, SAXException, UnmarshallingException {

        byte[] byteArray = Base64.decodeBase64(samlLogoutResponse.getBytes());

        ByteArrayInputStream is = new ByteArrayInputStream(byteArray);

        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setNamespaceAware(true);
        DocumentBuilder docBuilder = documentBuilderFactory.newDocumentBuilder();

        Document document = docBuilder.parse(is);
        Element element = document.getDocumentElement();

        UnmarshallerFactory unmarshallerFactory = Configuration.getUnmarshallerFactory();
        Unmarshaller unmarshaller = unmarshallerFactory.getUnmarshaller(element);
        XMLObject logoutResponseXMLObject = unmarshaller.unmarshall(element);

        LogoutResponse logoutResponse = (LogoutResponse) logoutResponseXMLObject;

        return logoutResponse;
    }

    public String printResponse(LogoutResponse logoutResponse) throws MarshallingException {

        Element responseXML = Configuration.getMarshallerFactory().getMarshaller(logoutResponse).marshall(logoutResponse);


        StringWriter requestWriter = new StringWriter();
        requestWriter = new StringWriter();
        XMLHelper.writeNode(responseXML, requestWriter);
        String responseString = requestWriter.toString();


        return responseString;

    }

    private Boolean verifySignature(LogoutResponse response) throws MetadataProviderException, SpidServiceException {

        X509Certificate certificate = null;
        java.security.cert.X509Certificate idpCertificate = null;
        BasicX509Credential credential = null;


        try {
            XmlDigitalSignature xml = new XmlDigitalSignature();
            String entityId = getEntityId(response);
            String xmlResourcePath = retrieveXMLResourcePath(entityId);
            ClasspathResource idpresource = null;

            idpresource = new ClasspathResource("/metadata/idp/" + xmlResourcePath);


            EntityDescriptor entityDescriptor = getEntityDescriptor(entityId, idpresource);
            IDPSSODescriptor ssoDescriptor = getIDPSSODescriptor(entityId, idpresource);

            List<KeyDescriptor> keyDescriptors = ssoDescriptor.getKeyDescriptors();

            for(KeyDescriptor keyDescriptor : keyDescriptors) {
                KeyInfo keyInfo = keyDescriptor.getKeyInfo();
                if(keyInfo != null) {
                    List<X509Data> x509Datas = keyInfo.getX509Datas();
                    for(X509Data x509Data : x509Datas) {
                        List<X509Certificate> x509Certificates = x509Data.getX509Certificates();
                        if (x509Certificates != null && !x509Certificates.isEmpty()) {
                            certificate = x509Certificates.get(0);
                        }
                    }
                }
            }

            String stringCertificate = certificate.getValue();
            idpCertificate = xml.generateX509Certificate(stringCertificate);

            Signature responseSignature = response.getSignature();
            credential = new BasicX509Credential();
            credential.setEntityCertificate(idpCertificate);

            SignatureValidator validator = new SignatureValidator(credential);
            validator.validate(responseSignature);


        }catch( org.opensaml.util.resource.ResourceException | CertificateException e) {
            e.printStackTrace();

        } catch (ValidationException e) {
            //TODO Auto-generated catch block
            e.printStackTrace();


            return false;
        }


        return  true;
    }



    private EntityDescriptor getEntityDescriptor(String entityId, ClasspathResource resource) throws MetadataProviderException {
        AbstractReloadingMetadataProvider abstractReloadingMetadataProvider = new ResourceBackedMetadataProvider(new Timer(), resource);
        BasicParserPool parser = new BasicParserPool();
        parser.setNamespaceAware(true);
        abstractReloadingMetadataProvider.setParserPool(parser);
        abstractReloadingMetadataProvider.initialize();
        EntityDescriptor entityDescriptor = abstractReloadingMetadataProvider.getEntityDescriptor(entityId);
        return entityDescriptor;
    }
    /**
     * Metodo che recupera l'IDPSSODescriptor, questo elemento xml contiene tutti i dati relativi al binding e url necessari
     *
     * @param entityId entityId afferito al metadata richiesto
     * @param resource classpath dove trovare il metadata
     * @return Oggetto SAML EntityDescriptor
     * */
    private IDPSSODescriptor getIDPSSODescriptor(String entityId, ClasspathResource resource) throws MetadataProviderException {
        EntityDescriptor entityDescriptor = getEntityDescriptor(entityId, resource);
        IDPSSODescriptor idpssoDescriptor = entityDescriptor.getIDPSSODescriptor(SAML2_PROTOCOL);
        return idpssoDescriptor;
    }

    /**
     * Metodo che recupera l'entityid del service provider dalle properties
     *
     * @return spEntityId entityId del service provider
     * */
    private String getSPEntityId() {
        Properties properties = new Properties();
        String spEntityId = null;
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        try (InputStream propertiesInputStream = classLoader.getResourceAsStream("service.properties")) {
            properties.load(propertiesInputStream);
            spEntityId = properties.getProperty(SPID_SP_ENTITYID);

        } catch (IOException e) {
            e.printStackTrace();
        }

        return spEntityId;
    }

    /**
     * Metodo che recupera le properties contenti i dati associati all'entity id dell'identity provider al fine di
     * recuperare il nome del file del metadata associato.
     *
     * @param entityId contiene l'id dell'entita a cui inviare la richiesta di autenticazione
     *
     * @return Stringa contentente il nome del metadata associato all'entityId
     * */
    private String retrieveXMLResourcePath(String entityId) {


        Properties properties = new Properties();
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        try(InputStream propertiesInputStream = classLoader.getResourceAsStream("idplist.properties")){
            properties.load(propertiesInputStream);
            String keyProperty = properties.getProperty(SPID_IDP_KEYS);
            String[] keys = keyProperty.split(",");
            for(String key : keys) {
                String entityIdFromProperties = properties.getProperty(SPID_IDP_PREFIX + key + ".entityId");
                if(entityId.equals(entityIdFromProperties)) {
                    String xmlMetadataFileName = properties.getProperty(SPID_IDP_PREFIX + key + ".file");
                    return xmlMetadataFileName;
                }
            }

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        //gestire l'eccezione
        return null;

    }

    public String getEntityId(LogoutResponse response) {
        String entityid = response.getIssuer().getValue();
        return entityid;
    }

}
