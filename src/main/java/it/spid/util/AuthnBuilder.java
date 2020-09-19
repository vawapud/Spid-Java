package it.spid.util;

import org.joda.time.DateTime;
import org.opensaml.common.SAMLVersion;
import org.opensaml.saml2.core.*;
import org.opensaml.saml2.core.impl.*;

/**
 * Questa classe si occupa di costruire la richiesta di autenticazione SAML secondo le specifiche SPID
 * La firma digitale non viene effettuata
 * */

public class AuthnBuilder {

    private static final String SAML2_PROTOCOL="urn:oasis:names:tc:SAML:2.0:protocol";
    private static final String SAML2_ASSERTION="urn:oasis:names:tc:SAML:2.0:assertion";
    private static final String SAML2_POST_BINDING ="urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST";
    private static final String SAML2_NAME_ID_POLICY="urn:oasis:names:tc:SAML:2.0:nameid-format:transient";
    private static final String SAML2_ISSUER_NAME_POLICY="urn:oasis:names:tc:SAML:2.0:nameid-format:entity";
    private static final String SAML2_PASSWORD_PROTECTED_TRANSPORT="https://www.spid.gov.it/SpidL2";





    /**
     * Costruisce la authnRequest secondo le specifiche SAML per lo SPID
     *
     * @param assertionConsumerServiceUrl è l'indirizzo a cui l'indentity provider risponderà
     * @param assertionConsumerServiceIndex è un indice che potrebbe sostituire url se compilato
     *                                      nel metadata, al momento è non settato in favore
     *                                      dell'url diretto
     * @param issuerId Stringa che rappresenta l'ente emittente service provider
     * @param id identificativo che viene assegnato ad ogni richiesta
     * @param destination url al quale la richiesta di autenticazione verrà presentata
     * */
    public AuthnRequest buildAuthenticationRequest(String assertionConsumerServiceUrl, Integer assertionConsumerServiceIndex, String issuerId, String id, String destination){

        DateTime issueInstant = new DateTime();

        AuthnRequestBuilder authRequestBuilder = new AuthnRequestBuilder();

        AuthnRequest authRequest = authRequestBuilder.buildObject(SAML2_PROTOCOL, "AuthnRequest", "samlp");
        authRequest.setIssueInstant(issueInstant);
        authRequest.setVersion(SAMLVersion.VERSION_20);
        authRequest.setID(id);
        authRequest.setProtocolBinding(SAML2_POST_BINDING);
        authRequest.setAssertionConsumerServiceURL(assertionConsumerServiceUrl);

        authRequest.setIssuer((buildIssuer(issuerId)));

        authRequest.setNameIDPolicy(buildNameIDPolicy());

        authRequest.setRequestedAuthnContext(buildRequestedAuthnContext());

        authRequest.setAttributeConsumingServiceIndex(1);
        authRequest.setDestination(destination);

        return authRequest;

    }

    /**
     * Costruisce il contesto di autenticazione
     * Qui viene definito il livello di autenticazione per lo spid
     * Di default == 2

     * @return Oggetto XML rappresentante il contesto di autenticazione

     * */

    private RequestedAuthnContext buildRequestedAuthnContext(){

        //creo AuthnContextClassReg
        AuthnContextClassRefBuilder authnContextClassRefBuilder = new AuthnContextClassRefBuilder();
        AuthnContextClassRef authnContextClassRef = authnContextClassRefBuilder.buildObject(SAML2_ASSERTION, "AuthnContextClassRef", "saml");
        authnContextClassRef.setAuthnContextClassRef(SAML2_PASSWORD_PROTECTED_TRANSPORT);

        //creo RequestedAuthnContext
        RequestedAuthnContextBuilder requestedAuthnContextBuilder = new RequestedAuthnContextBuilder();
        RequestedAuthnContext requestedAuthnContext = requestedAuthnContextBuilder.buildObject();
        //Comparison
        requestedAuthnContext.setComparison(AuthnContextComparisonTypeEnumeration.EXACT);
        requestedAuthnContext.getAuthnContextClassRefs().add(authnContextClassRef);

        return requestedAuthnContext;
    }

    /**
     * Costruisco l'issuer che rappresenta
     * l'ente emittente della richiesta di autenticazione
     *
     * @param issuerId che contiene l'identifier del service provider
     *                 può essere diversa dall'entityId
     *
     * @return Oggetto XML rappresentante il contesto di autenticazione
     *
     * */
    private Issuer buildIssuer(String issuerId){
        IssuerBuilder issuerBuilder = new IssuerBuilder();
        Issuer issuer = issuerBuilder.buildObject();
        issuer.setNameQualifier(issuerId);
        issuer.setFormat(SAML2_ISSUER_NAME_POLICY);
        issuer.setValue(issuerId);
        return issuer;
    }

    /**
     * Costruisco il NameIDPolicy che rappresenta l'ente richiedente
     *
     * @return Oggetto XML rappresentante il NameIDPolicy
     * */
    private NameIDPolicy buildNameIDPolicy(){

        NameIDPolicyBuilder nameIDPolicyBuilder = new NameIDPolicyBuilder();
        NameIDPolicy nameIDPolicy = nameIDPolicyBuilder.buildObject();
        nameIDPolicy.setFormat(SAML2_NAME_ID_POLICY);
        return nameIDPolicy;

    }

}
