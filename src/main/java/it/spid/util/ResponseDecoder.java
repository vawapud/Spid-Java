package it.spid.util;
import it.spid.exception.SpidServiceException;
import it.spid.model.ResponseDecoded;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.Response;
import org.opensaml.saml2.metadata.provider.MetadataProviderException;
import org.opensaml.xml.io.UnmarshallingException;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;


/**
 *Interfaccia che espone i metodi utili per la decodifica della response
 * */


public interface ResponseDecoder {

    public Response processResponse() throws ParserConfigurationException, SAXException, IOException, UnmarshallingException, SpidServiceException, MetadataProviderException;

    public Assertion getAssertion(Response response);

    public ResponseDecoded setResponseDecodedUserAttribute(Assertion assertion);
}
