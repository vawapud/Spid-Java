package it.spid.util;

import it.spid.model.AuthRequest;
import org.opensaml.Configuration;
import org.opensaml.DefaultBootstrap;
import org.opensaml.saml2.core.AuthnRequest;
import org.opensaml.saml2.metadata.*;
import org.opensaml.saml2.metadata.provider.AbstractReloadingMetadataProvider;
import org.opensaml.saml2.metadata.provider.MetadataProviderException;
import org.opensaml.saml2.metadata.provider.ResourceBackedMetadataProvider;
import org.opensaml.util.resource.ClasspathResource;
import org.opensaml.util.resource.ResourceException;
import org.opensaml.xml.ConfigurationException;
import org.opensaml.xml.io.MarshallingException;
import org.opensaml.xml.parse.BasicParserPool;
import org.opensaml.xml.security.credential.Credential;
import org.opensaml.xml.signature.*;
import org.w3c.dom.Element;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;
import java.util.Timer;
import java.util.UUID;

/**
 * La classe si occupa di reperire i parametri, la base della richiesta di autenticazione
 * e della firma digitale.
 * Il costruttore si occupa del recupero dei parametri necessari alla creazione dai metadata del service provider
 * e dell'identity provider scelto al momento della chiamata del metodo.
 * Tutto il necessario al chiamante viene inserito nel modello AuthRequest.
 * */

public class AuthnFactoryImpl implements AuthnFactory{

    //recuperare nel costruttore con un get entityid
    private static final String SPID_IDP_PREFIX ="it.spid.idp.";
    private static final String SPID_IDP_KEYS ="it.spid.idp.keys";
    private static final String SPID_SP_PREFIX = "it.spid.sp.";
    private static final String SPID_SP_ENTITYID = "it.spid.sp.entityId";
    private static final String SAML2_PROTOCOL = "urn:oasis:names:tc:SAML:2.0:protocol";
    private static final String SAML2_POST_BINDING = "urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST";
    private ClasspathResource idpResource;
    private ClasspathResource spResource;


    private String destination;
    private String issuerID;
    private String entityId;
    private Integer assertionConsumerServiceIndex;
    private String assertionConsumerServiceUrl;
    private X509Certificate certificate;
    private String xmlResourcePath;

    private String relayState;


    AuthRequest authRequest = new AuthRequest();

    /**
     * In questo costruttore viene recuperato il classpath dei metadata
     * da questi estratto gli elementi xml necessari al recupero dei parametri per la richiesta
     * di autenticazione
     *
     *
     * */

    public AuthnFactoryImpl(String entityId, Integer assertionConsumerServiceIndex, String relayState) {
        this.assertionConsumerServiceIndex = assertionConsumerServiceIndex;
        String xmlResourcePath = retrieveXMLResourcePath(entityId);
        this.xmlResourcePath = xmlResourcePath;
        this.relayState = relayState;

        try {
            DefaultBootstrap.bootstrap();
            ClasspathResource idpresource = new ClasspathResource("/metadata/idp/" + xmlResourcePath);
            this.idpResource = idpresource;

            ClasspathResource spresource = new ClasspathResource("/metadata/sp/sp_metadata.xml");
            this.spResource = spresource;

            IDPSSODescriptor idpssoDescriptor = getIDPSSODescriptor(entityId, idpResource);
            List<SingleSignOnService> singleSignOnServices = idpssoDescriptor.getSingleSignOnServices();
            SingleSignOnService singleSignOnServiceUsed = null;
            for (SingleSignOnService singleSignOnService : singleSignOnServices) {
                if (singleSignOnService.getBinding().equals(SAML2_POST_BINDING)) {
                    singleSignOnServiceUsed = singleSignOnService;
                    break;
                }
            }
            String destination = singleSignOnServiceUsed.getLocation();
            this.destination = destination;
            String spEntityId= getSPEntityId();
            EntityDescriptor spEntityDescriptor = getEntityDescriptor(spEntityId, spResource);

            X509Certificate certificate = null;
            List<KeyDescriptor> keyDescriptors = idpssoDescriptor.getKeyDescriptors();
            for (KeyDescriptor keyDescriptor : keyDescriptors) {
                KeyInfo keyInfo = keyDescriptor.getKeyInfo();
                if (keyInfo != null) {
                    List<X509Data> x509Datas = keyInfo.getX509Datas();
                    for (X509Data x509Data : x509Datas) {
                        List<X509Certificate> x509Certificates = x509Data.getX509Certificates();
                        if (x509Certificates != null && !x509Certificates.isEmpty()) {
                            certificate = x509Certificates.get(0);
                        }
                    }
                }
            }

            this.certificate = certificate;
            String assertionConsumerServiceUrl = null;
            SPSSODescriptor spSsoDescriptor = spEntityDescriptor.getSPSSODescriptor(SAML2_PROTOCOL);
            List<AssertionConsumerService> assertionConsumerServices = spSsoDescriptor.getAssertionConsumerServices();
            if (assertionConsumerServices != null) {
                for (AssertionConsumerService assertionConsumerService : assertionConsumerServices) {
                    if (assertionConsumerService.getIndex().equals(assertionConsumerServiceIndex)) {
                        assertionConsumerServiceUrl = assertionConsumerService.getLocation();
                        break;
                    }
                }
            }
            this.assertionConsumerServiceUrl = assertionConsumerServiceUrl;
        }catch (ResourceException e) {
            e.printStackTrace();
        }catch(MetadataProviderException e) {
            e.printStackTrace();
        }catch(ConfigurationException e) {
            e.printStackTrace();
        }
        try {
            getAuthnRequest();
        } catch (MarshallingException e) {
            e.printStackTrace();
        }

    }

    /**
     * Questo metodo si occupa della creazione degli oggetti necessari al processamento della
     * Authn Request.
     * Vengono istanziati gli oggetti necessari alla firma e alla codifica.
     *
     * Ottenuti i parametri necessari da passare al Builder viene effettuata la firma digitale
     * secondo le specifiche SAML per SPID
     * Prima vengono create delle credenziali, conterranno chiave privata e certificato a chiave pubblica
     * del service provider contenute dentro il keystore personale sotto la directory resource
     * Creato l'oggetto signature, viene operata la firma digitale secondo specifica SPID con RSA almeno a 1024 bit.
     * In questo caso è operata a 2048 bit
     *
     * Sulla richiesta firmata viene effettuato il marshalling trasformando gli oggetti opensaml in element dom
     * quindi codificata. La richiesta viene settata sul modello insieme ai dati necessari per il tracciamento
     * lungo il percorso di Single Sign On
     *
     * @return encodeAuthnRequest Stringa rappresentante la richiesta codificata in base 64
     * */


    private String getAuthnRequest() throws MarshallingException {

        XmlDigitalSignature xml = new XmlDigitalSignature();
        AuthnUtil u = new AuthnUtil();
        AuthnBuilder authnBuilder = new AuthnBuilder();

        String assertionConsumerServiceUrl = getAssertionConsumerServiceUrl();
        Integer assertionConsumerServiceIndex = getAssertionConsumerServiceIndex();
        String ID = getUUID();
        String issuerID = getIssuerID();
        String destination = getDestination();

        AuthnRequest auth = authnBuilder.buildAuthenticationRequest(assertionConsumerServiceUrl, assertionConsumerServiceIndex, issuerID, ID, destination);

        Credential signingCredential = xml.getCredential();
        Signature signature = (Signature) Configuration.getBuilderFactory()
                .getBuilder(Signature.DEFAULT_ELEMENT_NAME)
                .buildObject(Signature.DEFAULT_ELEMENT_NAME);
        signature.setSigningCredential(signingCredential);
        signature.setSignatureAlgorithm(SignatureConstants.ALGO_ID_SIGNATURE_RSA_SHA256);
        signature.setCanonicalizationAlgorithm(SignatureConstants.ALGO_ID_C14N_EXCL_OMIT_COMMENTS);
        signature.setKeyInfo(xml.getKeyInfo());
        auth.setSignature(signature);

        Element authDom = Configuration.getMarshallerFactory().getMarshaller(auth).marshall(auth);

        try {
            Signer.signObject(signature);
        } catch (SignatureException e) {
            e.printStackTrace();
        }

        String printableAuthnRequest = u.printAuthnRequest(authDom);
        String encodedAuthnRequest = u.encodeAuthnRequest(authDom);

        authRequest.setDestination(destination);
        authRequest.setXmlAuthRequest(encodedAuthnRequest);
        authRequest.setId(ID);
        authRequest.setXmlPlainText(printableAuthnRequest);
        authRequest.getRelayState();


        return  encodedAuthnRequest;

    }
    /**
     * Gestisce il relay state --
     * */
    //TODO
   public String getRelayState() {

       RelayStateBuilder b = new RelayStateBuilder();

       String relayState = b.encodeRelayState(this.relayState);
       return relayState;

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

    /**
     * getter del parametro destination estratto dai metadata nel costruttore
     *
     * @return destination, url dove recapitare la richiesta di autenticazione
     * */

    private String getDestination() {
        return destination;
    }

    /**
     * L'issuerID fa parte dei parametri configurabili e recuperabili dalle properties
     *
     * @return issuerID
     * */
    private String getIssuerID() {

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

        return issuerID;
    }

    /**
     * Index dell'assertion consumer service è un parametro che definisce una particolare configurazione di url e binding
     * In questa versione della libreria e come esplicitato nella documentazione SPID viene preferito l'url esplicito.
     * Metodo lasciato per una futura modifica
     *
     * @return assertionConsumerServiceIndex
     * */

    private Integer getAssertionConsumerServiceIndex() {
        return assertionConsumerServiceIndex;
    }

    /**
     * getter per assertion consumer service url
     *
     * @return assertionConsumerServiceUrl
     * */

    private String getAssertionConsumerServiceUrl() {
        return assertionConsumerServiceUrl;
    }

    /**
     * Questo metodo genera un id, in accordo con la documentazione può essere generato con UUID
     *
     * @return UUID, id univoco e casuale della richiesta di autenticazione, viene quindi legato all'oggetto in uscita
     *                  in forma esplicita in modo che il service provider possa tenerne traccia ed evitare duplicati
     * */

    private String getUUID() {
        UUID uuid = UUID.randomUUID();
        String id = "_" + uuid.toString();

        return id;
    }

    /**
     * getter per il modello oggetto AuthRequest
     *
     * @return AuthRequest oggetto contente tutti i paramentri per la presentazione della richiesta di autenticazione
     * a                    all'identity provider
     * */

    public AuthRequest getAuthenticationRequest() {

        return authRequest;
    }




}
