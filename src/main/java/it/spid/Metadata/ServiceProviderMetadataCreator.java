package it.spid.Metadata;

import org.apache.commons.codec.binary.Base64;
import org.opensaml.Configuration;
import org.opensaml.DefaultBootstrap;
import org.opensaml.common.SAMLObjectBuilder;
import org.opensaml.saml2.metadata.*;
import org.opensaml.xml.ConfigurationException;
import org.opensaml.xml.XMLObjectBuilderFactory;
import org.opensaml.xml.io.Marshaller;
import org.opensaml.xml.io.MarshallerFactory;
import org.opensaml.xml.io.MarshallingException;
import org.opensaml.xml.security.credential.UsageType;
import org.opensaml.xml.security.keyinfo.KeyInfoHelper;
import org.opensaml.xml.signature.KeyInfo;
import org.opensaml.xml.util.XMLHelper;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

public class ServiceProviderMetadataCreator {

    private final String entityId;
    private final XMLObjectBuilderFactory builderFactory;
    private final String logoutURI;
    private final String assertionConsumerServiceUrl;
    private static final String SAML2_PROTOCOL="urn:oasis:names:tc:SAML:2.0:protocol";
    private static final String SAML2_NAMEID="urn:oasis:names:tc:SAML:2.0:nameid-format:transient";
    private static final String SAML2_BINDING_POST = "urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST";
    private static final String SAML2_NAMEFORMATATTRIBUTE ="urn:oasis:names:tc:SAML:2.0:attrname-format:basic";

    private String serviceName;
    private String serviceDesc;
    private String organizationName;
    private String organizationDisplayName;
    private String organizationURL;
    private List<String> attributes;
    //rivedere
    private String certificate;

    public ServiceProviderMetadataCreator(String entityId, String logoutURI, String assertionConsumerServiceUrl, String serviceName, String serviceDesc, String organizationName, String organizationDisplayName, String organizationURL,  String certificate){
        try {
            DefaultBootstrap.bootstrap();
        } catch (ConfigurationException e) {
            e.printStackTrace();
        }


        this.assertionConsumerServiceUrl = assertionConsumerServiceUrl;
        this.builderFactory = Configuration.getBuilderFactory();
        this.entityId = entityId;
        this.logoutURI = logoutURI;
        this.serviceName = serviceName;
       // this.attributes = attributes;
        this.certificate= certificate;
        this.organizationURL = organizationURL;
        this.serviceDesc = serviceDesc;
        this.organizationName = organizationName;
        this.organizationDisplayName = organizationDisplayName;
        //riaggiungere attributes
        List<String> attributes = new ArrayList<String>();
        attributes.add("name");
        attributes.add("fiscalNumber");
        attributes.add("familyName");
        attributes.add("spidCode");
        attributes.add("gender");
        attributes.add("dateOfBirth");
        attributes.add("countyOfBirth");
        attributes.add("idCard");

        this.attributes = attributes;



    }
    /*
    * Recupera tutto l'oggetto entityDescriptor saml, lo marshalla in stringa e lo esporto come file (da rivedere per integrare come endpoint) */

    public void build() {

        EntityDescriptor descriptor = buildEntityDescriptor();
        String spMetadataXml = getXmlMetadata();
        try {
            stringToXML(spMetadataXml);
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (TransformerException e) {
            e.printStackTrace();
        }


    }

    private void stringToXML(String xmlSource) throws ParserConfigurationException, IOException, SAXException, TransformerException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(new InputSource(new StringReader(xmlSource)));

        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        DOMSource source = new DOMSource(doc);

        StreamResult result = new StreamResult(new File("/test/xml.xml"));
        transformer.transform(source, result);

    }

    private String getXmlMetadata(){

       EntityDescriptor descriptor = buildEntityDescriptor();
       MarshallerFactory marshallerFactory = Configuration.getMarshallerFactory();
       Marshaller marshaller = marshallerFactory.getMarshaller(descriptor);
       Element spMetadataElement = null;
        try {
            spMetadataElement = marshaller.marshall(descriptor);
        } catch (MarshallingException e) {
            e.printStackTrace();
        }
        StringWriter requestWriter = new StringWriter();
        requestWriter = new StringWriter();
        XMLHelper.writeNode(spMetadataElement, requestWriter);
        String spMetadata = requestWriter.toString();
        return spMetadata;
    }
    /*
    * Costruisco l'elemento radice ovvero EntityDescriptor che contiene tutto il resto del metadata*/

    private EntityDescriptor buildEntityDescriptor(){
        SAMLObjectBuilder<EntityDescriptor> builder = (SAMLObjectBuilder<EntityDescriptor>) builderFactory.getBuilder(EntityDescriptor.DEFAULT_ELEMENT_NAME);
        EntityDescriptor entityDescriptor = builder.buildObject();
        entityDescriptor.setSchemaLocation("http://www.w3.org/2000/09/xmldsig#");
        entityDescriptor.setEntityID(this.entityId);
        //mettere id
        entityDescriptor.setID("_681a637-6cd4-434f-92c3-4fed720b2ad8");


        //rivedere perchè qua mi sa di cagata
        entityDescriptor.getRoleDescriptors().add(buildSSODescriptor());
        entityDescriptor.setOrganization(getOrganization());


        return entityDescriptor;
    }
    /*
    * RoleDescriptor è il SPSSODescriptor, racchiude tutto tranne i dati espliciti sul service provider
    * */
    private SPSSODescriptor buildSSODescriptor(){

        SAMLObjectBuilder<SPSSODescriptor> builder = (SAMLObjectBuilder<SPSSODescriptor>) builderFactory.getBuilder(SPSSODescriptor.DEFAULT_ELEMENT_NAME);
        SPSSODescriptor spssoDescriptor = builder.buildObject();
        spssoDescriptor.addSupportedProtocol(SAML2_PROTOCOL);
        spssoDescriptor.setAuthnRequestsSigned(true);
        spssoDescriptor.isAuthnRequestsSigned();
        spssoDescriptor.setWantAssertionsSigned(true);



        spssoDescriptor.getKeyDescriptors().add(getKeyDescriptor(UsageType.SIGNING, getKeyInfo()));
        spssoDescriptor.getKeyDescriptors().add(getKeyDescriptor(UsageType.ENCRYPTION, getKeyInfo()));

        spssoDescriptor.getSingleLogoutServices().add(getSingleLogoutService());
        spssoDescriptor.getAssertionConsumerServices().add(getAssertionConsumerService());
        spssoDescriptor.getNameIDFormats().add(getNameIDFormat());
        spssoDescriptor.getAttributeConsumingServices().add(getAttributeConsumingService());


        return spssoDescriptor;
    }

    private KeyDescriptor getKeyDescriptor(UsageType type, KeyInfo key){
        SAMLObjectBuilder<KeyDescriptor> builder = (SAMLObjectBuilder<KeyDescriptor>) Configuration.getBuilderFactory().getBuilder(KeyDescriptor.DEFAULT_ELEMENT_NAME);
        KeyDescriptor descriptor = builder.buildObject();
        descriptor.setUse(type);
        descriptor.setKeyInfo(key);
        return descriptor;
    }

    private KeyInfo getKeyInfo() {
        //XMLObjectBuilderFactory builder = Configuration.getBuilderFactory();
        KeyInfo keyInfo = (KeyInfo) builderFactory.getBuilder(KeyInfo.DEFAULT_ELEMENT_NAME).buildObject(KeyInfo.DEFAULT_ELEMENT_NAME);
        try {
            X509Certificate certificate = getX509CertificateFromString();
            KeyInfoHelper.addCertificate(keyInfo, certificate);

        } catch (CertificateException e) {

            e.getMessage();
        }
        return keyInfo;
    }
    private NameIDFormat getNameIDFormat (){
        SAMLObjectBuilder<NameIDFormat> builder = (SAMLObjectBuilder<NameIDFormat>) builderFactory.getBuilder(NameIDFormat.DEFAULT_ELEMENT_NAME);
        NameIDFormat nameIDFormat = builder.buildObject();
        //forse
        nameIDFormat.setFormat(SAML2_NAMEID);
        return nameIDFormat;
    }

    private SingleLogoutService getSingleLogoutService(){
        SAMLObjectBuilder<SingleLogoutService> builder = (SAMLObjectBuilder<SingleLogoutService>) builderFactory.getBuilder(SingleLogoutService.DEFAULT_ELEMENT_NAME);
        SingleLogoutService singleLogoutService = builder.buildObject();
        singleLogoutService.setLocation(this.logoutURI);
        singleLogoutService.setBinding(SAML2_BINDING_POST);
        return singleLogoutService;
    }

    //aggiungere index
    private AssertionConsumerService getAssertionConsumerService(){
        SAMLObjectBuilder<AssertionConsumerService> builder = (SAMLObjectBuilder<AssertionConsumerService>) builderFactory.getBuilder(AssertionConsumerService.DEFAULT_ELEMENT_NAME);
        AssertionConsumerService assertionConsumerService = builder.buildObject();
        assertionConsumerService.setBinding(SAML2_BINDING_POST);
        assertionConsumerService.setLocation(this.assertionConsumerServiceUrl);
        //rendere con lista
        assertionConsumerService.setIndex(0);
        assertionConsumerService.isDefault();


        return assertionConsumerService;
    }
    //prendo una lista di Stringhe, da trasformare in tali da costruttore
    private AttributeConsumingService getAttributeConsumingService(){
        SAMLObjectBuilder<AttributeConsumingService> builder = (SAMLObjectBuilder<AttributeConsumingService>) builderFactory.getBuilder(AttributeConsumingService.DEFAULT_ELEMENT_NAME);
        AttributeConsumingService attributeConsumingService = builder.buildObject();

        attributeConsumingService.setIndex(1);

        attributeConsumingService.getNames().add(getServiceName());
        attributeConsumingService.getDescriptions().add(getServiceDescription());

        List<String> attributes = getAttributes();
        //addare i requested attribute
        for(String attribute : attributes) {
           attributeConsumingService.getRequestAttributes().add(getRequestedAttributes(attribute));
        }

        return attributeConsumingService;

    }

    private RequestedAttribute getRequestedAttributes(String name){
        SAMLObjectBuilder<RequestedAttribute> builder = (SAMLObjectBuilder<RequestedAttribute>) builderFactory.getBuilder(RequestedAttribute.DEFAULT_ELEMENT_NAME);
        RequestedAttribute requestedAttribute = builder.buildObject();
        requestedAttribute.setName(name);
        requestedAttribute.setNameFormat(SAML2_NAMEFORMATATTRIBUTE);


        return requestedAttribute;

    }

    private ServiceName getServiceName(){
        LocalizedString setServiceName = new LocalizedString();
        setServiceName.setLocalizedString(this.serviceName);
        setServiceName.setLanguage("it");
        SAMLObjectBuilder<ServiceName> builder = (SAMLObjectBuilder<ServiceName>) builderFactory.getBuilder(ServiceName.DEFAULT_ELEMENT_NAME);
        ServiceName serviceName = builder.buildObject();
        serviceName.setName(setServiceName);

        return serviceName;
    }
    private ServiceDescription getServiceDescription(){
        SAMLObjectBuilder<ServiceDescription> builder = (SAMLObjectBuilder<ServiceDescription>) builderFactory.getBuilder(ServiceDescription.DEFAULT_ELEMENT_NAME);
        ServiceDescription serviceDescription = builder.buildObject();
        LocalizedString setServiceDescription = new LocalizedString();
        setServiceDescription.setLocalizedString(this.serviceDesc);
        setServiceDescription.setLanguage("it");
        serviceDescription.setDescription(setServiceDescription);
        return serviceDescription;
    }

    private Organization getOrganization(){
        SAMLObjectBuilder<Organization> builder = (SAMLObjectBuilder<Organization>) builderFactory.getBuilder(Organization.DEFAULT_ELEMENT_NAME);
        Organization organization = builder.buildObject();
        organization.getOrganizationNames().add(getOrganizationName());
        organization.getDisplayNames().add(getOrganizationDisplayName());
        organization.getURLs().add(getOrganizationURL());
        return organization;
    }

    private OrganizationName getOrganizationName(){
        SAMLObjectBuilder<OrganizationName> builder = (SAMLObjectBuilder<OrganizationName>) builderFactory.getBuilder(OrganizationName.DEFAULT_ELEMENT_NAME);
        OrganizationName organizationName = builder.buildObject();
        LocalizedString setOrganizationName = new LocalizedString();
        setOrganizationName.setLanguage("it");
        setOrganizationName.setLocalizedString(this.organizationName);
        organizationName.setName(setOrganizationName);
        return organizationName;
     }

    private OrganizationDisplayName getOrganizationDisplayName(){
        SAMLObjectBuilder<OrganizationDisplayName> builder = (SAMLObjectBuilder<OrganizationDisplayName>) builderFactory.getBuilder(OrganizationDisplayName.DEFAULT_ELEMENT_NAME);
        OrganizationDisplayName organizationDisplayName = builder.buildObject();
        LocalizedString setOrganizationDisplayName = new LocalizedString();
        setOrganizationDisplayName.setLocalizedString(this.organizationDisplayName);
        setOrganizationDisplayName.setLanguage("it");
        organizationDisplayName.setName(setOrganizationDisplayName);
        return organizationDisplayName;
    }
    private OrganizationURL getOrganizationURL(){
        SAMLObjectBuilder<OrganizationURL> builder = (SAMLObjectBuilder<OrganizationURL>) builderFactory.getBuilder(OrganizationURL.DEFAULT_ELEMENT_NAME);
        OrganizationURL organizationURL = builder.buildObject();
        LocalizedString setOrganizationURL = new LocalizedString();
        setOrganizationURL.setLocalizedString(this.organizationURL);
        setOrganizationURL.setLanguage("it");
        organizationURL.setURL(setOrganizationURL);
        return organizationURL;

    }
    private List<String> getAttributes(){
        return this.attributes;
    }

    private java.security.cert.X509Certificate getX509CertificateFromString() throws CertificateException {

        String stringCertificate = this.certificate;
        byte[] bytesCertificate = Base64.decodeBase64(stringCertificate);
        CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
        InputStream in = new ByteArrayInputStream(bytesCertificate);
        java.security.cert.X509Certificate certificate = (java.security.cert.X509Certificate) certFactory.generateCertificate(in);
        return certificate;
    }





}
