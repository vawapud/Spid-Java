package it.spid.util;

import it.spid.model.AuthRequest;

public interface AuthnFactory {

    AuthRequest getAuthenticationRequest();
}
