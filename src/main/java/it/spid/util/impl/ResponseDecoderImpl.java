package it.spid.util.impl;

import it.spid.exception.SpidServiceException;
import it.spid.model.ResponseDecoded;
import it.spid.util.ResponseDecoder;
import it.spid.util.XmlDigitalSignature;
import org.apache.commons.codec.binary.Base64;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.opensaml.Configuration;
import org.opensaml.saml2.core.*;
import org.opensaml.saml2.metadata.*;
import org.opensaml.saml2.metadata.provider.AbstractReloadingMetadataProvider;
import org.opensaml.saml2.metadata.provider.MetadataProviderException;
import org.opensaml.saml2.metadata.provider.ResourceBackedMetadataProvider;
import org.opensaml.util.resource.ClasspathResource;
import org.opensaml.util.resource.ResourceException;
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
import java.util.*;

/**
 * Classe deputata alla decodifica della Response.
 * Viene decodificata la response, unmarshallata in elemento opensaml.
 * Validata la firma sulla response, viene quindi estratta la Assertion SAML.
 * Quest'ultimo elemento xml contiene i dati dell'utente che servono al service provider
 * e che andranno forniti al chiamante.
 * I paramentri estratti vengono quindi aggiunti all'oggerro ResponseDecoded e presentati al client
 *
 * */

public class ResponseDecoderImpl implements ResponseDecoder {

    private final String SAMLResponse;
    private String relayState;
    private static final String SPID_IDP_PREFIX ="it.spid.idp.";
    private static final String SPID_IDP_KEYS ="it.spid.idp.keys";
    private static final String SPID_SP_PREFIX ="it.spid.sp.assertionConsumerServiceUrl";
    private static final String SPID_SP_ENTITYID = "it.spid.sp.entityId";


    private static final String SAML2_PROTOCOL = "urn:oasis:names:tc:SAML:2.0:protocol";

    /**
     * Come parametro viene presentata la SAMLResponse
     * TODO -- vedere con spring come fare
     * */

    public ResponseDecoderImpl(String SAMLResponse, String relayState) {

        this.SAMLResponse = SAMLResponse;
        this.relayState = relayState;


    }

    /**
     * Metodo per processare l'intera response, inizialmente decodificata, da oggetto xml unmarshallata
     * in oggetto opensaml, quindi validata la firma.
     *
     * @return response, l'intero oggetto response
     * */
    public Response processResponse() throws ParserConfigurationException, SAXException, IOException, UnmarshallingException, SpidServiceException, MetadataProviderException {

        byte[] byteArray = Base64.decodeBase64(SAMLResponse.getBytes());

        ByteArrayInputStream is = new ByteArrayInputStream(byteArray);

        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setNamespaceAware(true);
        DocumentBuilder docBuilder = documentBuilderFactory.newDocumentBuilder();

        Document document = docBuilder.parse(is);
        Element element = document.getDocumentElement();

        UnmarshallerFactory unmarshallerFactory = Configuration.getUnmarshallerFactory();
        Unmarshaller unmarshaller = unmarshallerFactory.getUnmarshaller(element);
        XMLObject responseXMLObject = unmarshaller.unmarshall(element);

        Response response = (Response) responseXMLObject;

        validate(response);

        return response;
    }


    /**
     * Metodo per stampare la response --TEST
     * TODO aggiungere il pretty print
     *
     * @param response oggetto response
     *
     * @return responseString
     * */

    public String printResponse(Response response) throws MarshallingException {

        Element responseXML = Configuration.getMarshallerFactory().getMarshaller(response).marshall(response);


        StringWriter requestWriter = new StringWriter();
        requestWriter = new StringWriter();
        XMLHelper.writeNode(responseXML, requestWriter);
        String responseString = requestWriter.toString();


        return responseString;

    }
    /**
     * Metodo che data l'assertion estrapolata da response, naviga dentro l'assertion fino all'elemento attributestatements
     * che contiene gli elementi attribute in tipo XMLObject, di questi ne predo i valori, altrimenti prendo il primo se ne ho solo 1
     *
     * @param assertion assertion contentente gli attributi
     * @return userAttribute mappa con relazione nome attributo - valore attributo
     */
    private Map<String, Object> getUserAttribute(Assertion assertion){

        Map<String, Object> userAttribute = new HashMap<>();
        userAttribute.put("Response ID", assertion.getSubject().getNameID().getValue());

        List<AttributeStatement> attributeStatements = assertion.getAttributeStatements();

        if(attributeStatements != null) {

            for(AttributeStatement attributeStatement : attributeStatements) {
                List<Attribute> attributes = attributeStatement.getAttributes();
                for(Attribute attribute : attributes) {
                    if(attribute.getAttributeValues().size()>1) {
                        List<XMLObject> attributeValues = attribute.getAttributeValues();

                        List<String> attributeValuesList = new ArrayList<String>();

                        for(XMLObject attributeValue : attributeValues) {
                            attributeValuesList.add(attributeValue.getDOM().getTextContent());
                        }
                        userAttribute.put(attribute.getName(), attributeValuesList);
                    } else {
                        userAttribute.put(attribute.getName(), attribute.getAttributeValues().get(0).getDOM().getTextContent());
                    }
                }
            }
        }
        return userAttribute;
    }

    /**
     *  Delego qua la creazione dell'oggetto che verrà poi restituito fuori dalla libreria
     * setto tutti i dati utente all'interno dell'oggetto.
     *
     * @param assertion
     * @return responseDecoded oggetto contennte tutti i dati richiesti
     * */

    public ResponseDecoded setResponseDecodedUserAttribute(Assertion assertion) {

        Map<String, Object> attributes = getUserAttribute(assertion);
        ResponseDecoded responseDecoded = new ResponseDecoded();

        responseDecoded.setSesso((String) attributes.get("gender"));

        responseDecoded.setCodiceFiscale((String) attributes.get("fiscalNumber"));

        responseDecoded.setCodiceIdentificativo((String) attributes.get("spidCode"));

        responseDecoded.setNome((String) attributes.get("name"));

        responseDecoded.setCognome((String) attributes.get("familyName"));

        responseDecoded.setLuogoNascita((String) attributes.get("placeOfBirth"));

        responseDecoded.setProvinciaNascita((String) attributes.get("countyOfBirth"));

        responseDecoded.setRagioneSociale((String) attributes.get("companyName"));

        responseDecoded.setIndirizzoSedeLegale((String) attributes.get("registeredOffice"));

        responseDecoded.setPartitaIva((String) attributes.get("ivaCode"));

        responseDecoded.setDocumentoIdentita((String) attributes.get("idCard"));

        responseDecoded.setIndirizzoDomicilio((String) attributes.get("address"));

        responseDecoded.setNumeroTelefono((String) attributes.get("mobilePhone"));

        responseDecoded.setEmailAddress((String) attributes.get("email"));

        responseDecoded.setEmailPec((String) attributes.get("digitalAddress"));

        responseDecoded.setDataNascita((String) attributes.get("dateOfBirth"));

        responseDecoded.setDataScadenzaIdentita((String) attributes.get("expirationDate"));

        responseDecoded.setInResponseTo(getInResponseTo(assertion));

        responseDecoded.setRelayState(getRelayState());

        return responseDecoded;

    }

    /**
     * Metodo per estrarre la assertion dalla response
     *
     * @param response response decodificata
     * @return assertion
     * */

    public Assertion getAssertion(Response response) {

        Assertion assertion = response.getAssertions().get(0);
        return assertion;
    }

    /**
     * Metodo per recuperare l'entityid del ente federato emittente della response
     *
     * @param response
     * @return entityid
     * */

    private String getEntityId(Response response) {
        String entityid = response.getIssuer().getValue();
        return entityid;
    }

    public String getRelayState(){

        return this.relayState;
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

    /*QUI TENGO I METODI PER LA VERIFICA DELLA RESPONSE
    * PARTO CON LA VERIFICA DELLA FIRMA*/


    /**TODO RIPULIREEE
     * Metodo per la verifica della firma, viene estratto il certificato dal metadata
     * dell'identity provider dal quale proviene la response. Quindi decodificato
     * usato per creare le credenziali che andranno a validare la firma digitale lasciata
     * dalla chiave privata dell'identity provider
     *
     *
     * @param response response decodificata
     * @return bool
     * */
    private Boolean verifySignature(Response response) throws MetadataProviderException, SpidServiceException {

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
    /*
    * c'è l'assertion?*/

    /**
     * Metodo per il controllo della presenza della assertion
     *
     * @param response response decodificata
     * @return bool
     * */
    private Boolean hasAssertion(Response response){
        Assertion assertion = null;
        assertion = response.getAssertions().get(0);
        if(assertion != null){
            return true;
        }
        return false;
    }

    /*
    * Verifica l'issuer, devo capire come recuperare l'entity id giusto
    * */

    /**
     * Metodo per estrarre l'issuer e recuperare l'entityid
     * ma funziona solo in combo con l'id trackato dal service provider
     * TODO TROVARE SOLUZIONE CON SPRING
     *
     * @param response response decodificata
     * @return bool
     * */
    private Boolean validateIssuer(Response response) {

        Issuer issuer = response.getIssuer();
        String entityid= getEntityId(response);

        boolean isValidated = false;

        if (issuer.getValue().equals(entityid)) {
            isValidated = true;
        }
        return isValidated;
    }

    /**
     * Metodo validare la data, fa semplicemente il check che la richiesta non sia scaduta.
     *
     * @param assertion
     * @return bool
     * */

    private Boolean verifyNotOnOrAfter(Assertion assertion) {
        Subject subject = assertion.getSubject();
        DateTime confirmationData = null;
        List<SubjectConfirmation> subConfirms = subject.getSubjectConfirmations();
        for(SubjectConfirmation subConfirm : subConfirms) {
            SubjectConfirmationData subConfirmData = subConfirm.getSubjectConfirmationData();
            confirmationData = subConfirmData.getNotOnOrAfter();
        }

        DateTime now = new DateTime(DateTimeZone.UTC);

        if(now.isAfter(confirmationData)) {
            return false;
        }

        return true;


    }

    //implementare il recupero dalle properties del sp dell'url acs -- FATTO

    /**
     * Metodo per verificare la validità del recipient, deve coincidere col Assertion consumer service url
     *
     * @param assertion
     * @return bool
     * */
    private Boolean verifyRecipient (Assertion assertion) {

        Subject subject = assertion.getSubject();
        String recipient = null;
        List<SubjectConfirmation> subConfirms = subject.getSubjectConfirmations();
        for(SubjectConfirmation subConfirm : subConfirms) {
            SubjectConfirmationData subConfirmData = subConfirm.getSubjectConfirmationData();
            recipient = subConfirmData.getRecipient();
        }
        Properties properties = new Properties();
        String spPrefix = null;

        //CAMBIATO
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        try(InputStream propertiesInputStream = classLoader.getResourceAsStream("service.properties")){
            properties.load(propertiesInputStream);
            spPrefix = properties.getProperty(SPID_SP_PREFIX);

        }catch(IOException e) {
            e.printStackTrace();
        }


        if(recipient.equals(spPrefix)) {
            return true;
        }

        return false;


    }


    /**
     * Metodo per verificare la validità della destination, richesto dal SAML non da SPID
     * Funziona in combo con service provider
     *
     * @param response
     * @return bool
     * */
    private boolean isDestination(Response response) {

        String destination  = response.getDestination();
        String location = null;
        try{
            ClasspathResource spResource = new ClasspathResource("/metadata/sp/sp_metadata.xml");
            //recuperare da properties
            String spEntityId= getSPEntityId();
            EntityDescriptor spEntityDescriptor = getEntityDescriptor(spEntityId, spResource);
            SPSSODescriptor spSsoDescriptor = spEntityDescriptor.getSPSSODescriptor(SAML2_PROTOCOL);
            List<AssertionConsumerService> assertionConsumerServices = spSsoDescriptor.getAssertionConsumerServices();
            for(AssertionConsumerService assertionConsumerService : assertionConsumerServices){
                location = assertionConsumerService.getLocation();

            }
        }catch(ResourceException e){
            e.printStackTrace();
        }catch(MetadataProviderException e){
            e.printStackTrace();
        }

        if(destination.equals(location)){
            return true;
        }

        return false;

    }

    /**
     * Metodo per verificare la validità della Audiance, richesto dal SAML non da SPID
     * Funziona in combo con service provider
     *
     * @param response
     * @return bool
     * */
    private boolean isAudiance(Response response){

        Assertion assertion = getAssertion(response);
        Conditions conditions = assertion.getConditions();
        String audianceURI = null;
        List<AudienceRestriction> audienceRestrictions = conditions.getAudienceRestrictions();
        for(AudienceRestriction audienceRestriction : audienceRestrictions){
            List<Audience> audiences = audienceRestriction.getAudiences();
            for(Audience audience : audiences){
                audianceURI = audience.getAudienceURI();
            }
        }

        Properties spProperties = new Properties();

        //String entityId = null;
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        String spPrefix = null;
        try(InputStream propertiesInputStream = classLoader.getResourceAsStream("service.properties")){
            spProperties.load(propertiesInputStream);
            spPrefix = spProperties.getProperty(SPID_SP_PREFIX);

        }catch(IOException e) {
            e.printStackTrace();
        }

        if(spPrefix.equals(audianceURI)){
            return true;
        }
        return false;

    }

    /**
     * Metodo per estrarre l'id relativo alla richiesta presentata
     *
     * @param assertion
     * @return inResponseTo
     * */
    private String getInResponseTo(Assertion assertion) {
        Subject subject = assertion.getSubject();
        String inResponseTo = null;
        List<SubjectConfirmation> subConfirms = subject.getSubjectConfirmations();
        for(SubjectConfirmation subConfirm : subConfirms) {
            SubjectConfirmationData subConfirmData = subConfirm.getSubjectConfirmationData();
            inResponseTo = subConfirmData.getInResponseTo();
            //return inResponseTo;
        }

        return inResponseTo;

    }

    /**
     * Metodo per verificare la validità dei parametri della response indicati nella documentazione SPID
     *
     * @param response
     * */
    private void validate(Response response) throws SpidServiceException, MetadataProviderException {
        //se falso, diventa vero e lancia eccezione
        //questo funziona
        if(!verifySignature(response)){
            throw new SpidServiceException("Errore nella verifica della firma");
        }
        //questo lancia una eccezione index 0
        if(!hasAssertion(response))
            throw new SpidServiceException("Asserzione non presente");

        Assertion assertion = getAssertion(response);

        //questo funziona
        if(!verifyNotOnOrAfter(assertion))
            throw new SpidServiceException("Tempo di autenticazione scaduto");
        //questo funziona
        if(!verifyRecipient(assertion)){
            throw new SpidServiceException("Recipient non valido");

        }


    }



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





}
