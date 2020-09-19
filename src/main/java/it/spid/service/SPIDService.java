package it.spid.service;

import it.spid.model.AuthRequest;
import it.spid.model.IdpEntry;
import it.spid.model.ResponseDecoded;

import java.util.List;

public interface SPIDService {

    /**
     *
    */

     List<IdpEntry> getAllIdpEntry();

     AuthRequest buildAuthenticationRequest(String entityID, Integer assertionConsumerServiceIndex, String relayState);

     ResponseDecoded processAuthenticationResponse(String SAMLResponse, String relayState);

     void buildSPMetadata(String entityId, String logoutURI, String assertionConsumerServiceUrl, String serviceName, String serviceDesc, String organizationName, String organizationDisplayName, String organizationURL,  String certificate, List<String> attributes);

}
