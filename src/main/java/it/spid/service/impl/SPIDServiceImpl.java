package it.spid.service.impl;

import it.spid.Metadata.ServiceProviderMetadataCreator;
import it.spid.exception.SpidServiceException;
import it.spid.model.*;
import it.spid.service.SPIDService;
import it.spid.util.AuthnFactory;
import it.spid.util.AuthnFactoryImpl;
import it.spid.util.LogoutFactory;
import it.spid.util.ResponseDecoder;
import it.spid.util.impl.LogoutDecoderImpl;
import it.spid.util.impl.LogoutFactoryImpl;
import it.spid.util.impl.ResponseDecoderImpl;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.Response;
import org.opensaml.saml2.metadata.provider.MetadataProviderException;
import org.opensaml.xml.io.MarshallingException;
import org.opensaml.xml.io.UnmarshallingException;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class SPIDServiceImpl implements SPIDService {

    private static final String SPID_IDP_PREFIX = "it.spid.idp.";
    private static final String SPID_IDP_KEYS = "it.spid.idp.keys";


    public SPIDServiceImpl()  {


    }


    public List<IdpEntry> getAllIdpEntry() {

        List<IdpEntry> idpEntries = new ArrayList<IdpEntry>();
        Properties properties = new Properties();
        try(InputStream propertiesInputStream = ClassLoader.getSystemClassLoader().getResourceAsStream("idplist.properties")){
            properties.load(propertiesInputStream);
            idpEntries = propertiesToIdpEntry(properties);


        }catch(FileNotFoundException e) {

            e.printStackTrace();
        }catch(IOException e) {
            e.printStackTrace();
        }
        return idpEntries;
    }

    public List<IdpEntry> propertiesToIdpEntry(final Properties properties){

        List<IdpEntry> idpEntries = new ArrayList<IdpEntry>();

        String keysProperty = properties.getProperty(SPID_IDP_KEYS);
        String[] keys = keysProperty.split(",");
        for (String key : keys) {
            IdpEntry idpEntry = new IdpEntry();
            String name = properties.getProperty(SPID_IDP_PREFIX + key + ".name");
            idpEntry.setName(name);
            String imageUrl = properties.getProperty(SPID_IDP_PREFIX + key + ".imageUrl");
            idpEntry.setImageUrl(imageUrl);
            String entityId = properties.getProperty(SPID_IDP_PREFIX + key + ".entityId");
            idpEntry.setEntityId(entityId);
            idpEntry.setIdentifier(key);
            idpEntries.add(idpEntry);
        }

        return idpEntries;

    }


    public AuthRequest buildAuthenticationRequest(String entityId, Integer assertionConsumerServiceIndex, String relayState)  {
        AuthnFactory authnFactory = new AuthnFactoryImpl(entityId, assertionConsumerServiceIndex, relayState);
        AuthRequest authnRequest = authnFactory.getAuthenticationRequest();
        return authnRequest;
    }


    public ResponseDecoded processAuthenticationResponse(String SAMLResponse, String relayState) {
        ResponseDecoder responseDecoder =  new ResponseDecoderImpl(SAMLResponse, relayState);
        Response response = null;
        try {
            response = responseDecoder.processResponse();

        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (UnmarshallingException e) {
            e.printStackTrace();
        } catch (SpidServiceException e) {
            e.printStackTrace();
        } catch (MetadataProviderException e) {
            e.printStackTrace();
        }

        Assertion assertion = responseDecoder.getAssertion(response);
        ResponseDecoded responseDecoded = responseDecoder.setResponseDecodedUserAttribute(assertion);

        return responseDecoded;
    }

    public void buildSPMetadata(String entityId, String logoutURI, String assertionConsumerServiceUrl, String serviceName, String serviceDesc, String organizationName, String organizationDisplayName, String organizationURL,  String certificate, List<String> attributes){

        ServiceProviderMetadataCreator spMetadataCreator = new ServiceProviderMetadataCreator(entityId, logoutURI,assertionConsumerServiceUrl,serviceName,serviceDesc, organizationName, organizationDisplayName, organizationURL, certificate);
        spMetadataCreator.build();

    }

    @Override
    public LogoutRequestDecoded processLogoutResponse(String samlLogoutResponse) {
        LogoutDecoderImpl logDec = new LogoutDecoderImpl(samlLogoutResponse);
        LogoutRequestDecoded logoutDecode = null;

        try {
            logoutDecode = logDec.processLogoutResponse();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (UnmarshallingException e) {
            e.printStackTrace();
        } catch (SpidServiceException e) {
            e.printStackTrace();
        }


        return logoutDecode;
    }

    @Override
    public LogoutRequested buildLogoutRequest(String entityId, String sessionIndex) {
        LogoutFactory logoutFactory = new LogoutFactoryImpl(entityId, sessionIndex);

        LogoutRequested logout = null;

        try {
            logout = logoutFactory.getSingleLogoutRequest();
        } catch (MarshallingException e) {
            e.printStackTrace();
        }


        return logout;
    }
}
