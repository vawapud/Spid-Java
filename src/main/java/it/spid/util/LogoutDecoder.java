package it.spid.util;

import it.spid.exception.SpidServiceException;
import it.spid.model.LogoutRequestDecoded;
import org.opensaml.xml.io.UnmarshallingException;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;

public interface LogoutDecoder {

    LogoutRequestDecoded processLogoutResponse() throws ParserConfigurationException, UnmarshallingException, SAXException, IOException, SpidServiceException;
}
