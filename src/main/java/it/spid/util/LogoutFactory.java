package it.spid.util;

import it.spid.model.LogoutRequested;
import org.opensaml.xml.io.MarshallingException;

public interface LogoutFactory {

    LogoutRequested getSingleLogoutRequest() throws MarshallingException;
}
