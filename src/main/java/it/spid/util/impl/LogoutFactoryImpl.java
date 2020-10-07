package it.spid.util.impl;


import it.spid.model.LogoutRequested;
import it.spid.util.AuthnUtil;
import it.spid.util.LogoutBuilder;
import it.spid.util.LogoutFactory;
import it.spid.util.XmlDigitalSignature;
import org.opensaml.Configuration;
import org.opensaml.DefaultBootstrap;
import org.opensaml.saml2.core.LogoutRequest;
import org.opensaml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml2.metadata.IDPSSODescriptor;
import org.opensaml.saml2.metadata.SPSSODescriptor;
import org.opensaml.saml2.metadata.SingleLogoutService;
import org.opensaml.saml2.metadata.provider.AbstractReloadingMetadataProvider;
import org.opensaml.saml2.metadata.provider.MetadataProviderException;
import org.opensaml.saml2.metadata.provider.ResourceBackedMetadataProvider;
import org.opensaml.util.resource.ClasspathResource;
import org.opensaml.util.resource.ResourceException;
import org.opensaml.xml.ConfigurationException;
import org.opensaml.xml.io.MarshallingException;
import org.opensaml.xml.parse.BasicParserPool;
import org.opensaml.xml.security.credential.Credential;
import org.opensaml.xml.signature.Signature;
import org.opensaml.xml.signature.SignatureConstants;
import org.opensaml.xml.signature.SignatureException;
import org.opensaml.xml.signature.Signer;
import org.w3c.dom.Element;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;
import java.util.Timer;
import java.util.UUID;

public class LogoutFactoryImpl implements LogoutFactory {

    private static final String SPID_IDP_PREFIX ="it.spid.idp.";
    private static final String SPID_IDP_KEYS ="it.spid.idp.keys";
    private static final String SPID_SP_PREFIX = "it.spid.sp.";
    private static final String SPID_SP_ENTITYID = "it.spid.sp.entityId";
    private static final String SAML2_PROTOCOL = "urn:oasis:names:tc:SAML:2.0:protocol";
    private static final String SAML2_POST_BINDING = "urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST";
    private final String sessionIndex;
    private String spSingleLogoutServiceUrl;
    private String destination;
    private ClasspathResource idpResource;
    private ClasspathResource spResource;
    private String entityId;


    public LogoutFactoryImpl(String entityId, String sessionIndex) {

        this.entityId = entityId;
        this.sessionIndex = sessionIndex;
        String xmlResourcePath = retrieveXMLResourcePath(entityId);
        try {
            DefaultBootstrap.bootstrap();
            ClasspathResource idpresource = new ClasspathResource("/metadata/idp/" + xmlResourcePath);
            this.idpResource = idpresource;

            ClasspathResource spresource = new ClasspathResource("/metadata/sp/sp_metadata.xml");
            this.spResource = spresource;

            IDPSSODescriptor idpssoDescriptor = getIDPSSODescriptor(entityId, idpResource);
            List<SingleLogoutService> singleLogoutServices = idpssoDescriptor.getSingleLogoutServices();
            SingleLogoutService idpSingleLogoutService = null;

            for(SingleLogoutService singleLogoutService : singleLogoutServices){
                if(singleLogoutService.getBinding().equals(SAML2_POST_BINDING)){
                    idpSingleLogoutService = singleLogoutService;
                    this.destination = idpSingleLogoutService.getLocation();
                    break;
                }
            }
            String destination = idpSingleLogoutService.getLocation();
            this.destination = destination;
            String spEntityId = getSPEntityId();


            EntityDescriptor spEntityDescriptor = getEntityDescriptor(spEntityId, spResource);


            String spSingleLogoutServiceUrl = null;
            SPSSODescriptor spSsoDescriptor = spEntityDescriptor.getSPSSODescriptor(SAML2_PROTOCOL);
            List<SingleLogoutService> spSingleLogoutServices = spSsoDescriptor.getSingleLogoutServices();
            if(singleLogoutServices != null){
                for(SingleLogoutService spSingleLogoutService : spSingleLogoutServices){
                    spSingleLogoutServiceUrl = spSingleLogoutService.getLocation();
                }
            }
            this.spSingleLogoutServiceUrl = spSingleLogoutServiceUrl;
        }catch (ResourceException e) {
            e.printStackTrace();
        }catch(MetadataProviderException e) {
            e.printStackTrace();
        }catch(ConfigurationException e) {
            e.printStackTrace();
        }
    }

    //fare il modello LogoutRequest
    @Override
    public LogoutRequested getSingleLogoutRequest() throws MarshallingException {

        String id = getUUID();
        String destination = getDestination();
        //String singleLogoutService = getSpSingleLogoutServiceUrl();
        String sessionIndex = getSessionIndex();
        String spEntityId = getSPEntityId();
        String spName = getSpName();

        AuthnUtil util = new AuthnUtil();
        XmlDigitalSignature xmlDigitalSignature = new XmlDigitalSignature();

        LogoutBuilder logoutBuilder = new LogoutBuilder(destination,id,spEntityId, spName, sessionIndex );

        LogoutRequest logoutRequest = logoutBuilder.buildLogoutRequest();

        Credential credential = xmlDigitalSignature.getCredential();
        Signature signature = (Signature) Configuration.getBuilderFactory()
                .getBuilder(Signature.DEFAULT_ELEMENT_NAME)
                .buildObject(Signature.DEFAULT_ELEMENT_NAME);

        signature.setSigningCredential(credential);
        signature.setSignatureAlgorithm(SignatureConstants.ALGO_ID_SIGNATURE_RSA_SHA256);
        signature.setCanonicalizationAlgorithm(SignatureConstants.ALGO_ID_C14N_EXCL_OMIT_COMMENTS);
        signature.setKeyInfo(xmlDigitalSignature.getKeyInfo());
        logoutRequest.setSignature(signature);

        Element logoutDom = Configuration.getMarshallerFactory().getMarshaller(logoutRequest).marshall(logoutRequest);

        try {
            Signer.signObject(signature);
        } catch (SignatureException e) {
            e.printStackTrace();
        }
        LogoutRequested logoutRequested = new LogoutRequested();


        String encode = util.encodeAuthnRequest(logoutDom);

        logoutRequested.setId(id);
        logoutRequested.setDestination(destination);
        logoutRequested.setSessionIndex(sessionIndex);
        logoutRequested.setEncodeLogoutRequest(encode);


        //String printer = util.printAuthnRequest(logoutDom);

        return logoutRequested;
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
            // try(InputStream propertiesInputStream = ClassLoader.getSystemClassLoader().getResourceAsStream("idplist.properties")){
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

    /**
     * Metodo che recupera l'entityid del service provider dalle properties
     *
     * @return l'entity id del service provider
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


    //mettere private dopo test
    /**
     * Metodo che recupera l'entity descriptor di un metadata SAML
     *
     * @param entityId entityId afferito al metadata richiesto
     * @param resource classpath dove trovare il metadata
     * @return Oggetto SAML EntityDescriptor
     * */
    private EntityDescriptor getEntityDescriptor(String entityId, ClasspathResource resource) throws MetadataProviderException {
        AbstractReloadingMetadataProvider abstractReloadingMetadataProvider = new ResourceBackedMetadataProvider(new Timer(), resource);
        BasicParserPool parser = new BasicParserPool();
        parser.setNamespaceAware(true);
        abstractReloadingMetadataProvider.setParserPool(parser);
        abstractReloadingMetadataProvider.initialize();
        EntityDescriptor entityDescriptor = abstractReloadingMetadataProvider.getEntityDescriptor(entityId);
        return entityDescriptor;

    }
    //mettere private dopo test
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

    private String getUUID() {
        UUID uuid = UUID.randomUUID();
        String id = "_" + uuid.toString();

        return id;
    }

    private String getSpName() {

        String spName = null;
        Properties properties = new Properties();
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        try(InputStream propertiesInputStream = classLoader.getResourceAsStream("service.properties")){
            //try(InputStream propertiesInputStream = ClassLoader.getSystemClassLoader().getResourceAsStream("service.properties")){
            properties.load(propertiesInputStream);
            String spProperties = properties.getProperty(SPID_SP_PREFIX + "issuerId");
            return spProperties;

        }catch(IOException e ) {
            e.printStackTrace();
        }

        return spName;
    }


    private String getSessionIndex() {
        return sessionIndex;
    }

    private String getSpSingleLogoutServiceUrl() {
        return spSingleLogoutServiceUrl;
    }

    private String getDestination() {
        return destination;
    }

    private String getEntityId() {
        return entityId;
    }
}
