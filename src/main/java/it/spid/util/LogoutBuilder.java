package it.spid.util;

import org.joda.time.DateTime;
import org.opensaml.common.SAMLVersion;
import org.opensaml.saml2.core.Issuer;
import org.opensaml.saml2.core.LogoutRequest;
import org.opensaml.saml2.core.NameID;
import org.opensaml.saml2.core.SessionIndex;
import org.opensaml.saml2.core.impl.IssuerBuilder;
import org.opensaml.saml2.core.impl.LogoutRequestBuilder;
import org.opensaml.saml2.core.impl.NameIDBuilder;
import org.opensaml.saml2.core.impl.SessionIndexBuilder;


public class LogoutBuilder {

    private static final String SAML2_PROTOCOL="urn:oasis:names:tc:SAML:2.0:protocol";
    private static final String SAML2_ISSUER_NAME_POLICY="urn:oasis:names:tc:SAML:2.0:nameid-format:entity";
    private static final String SAML2_NAME_ID_POLICY="urn:oasis:names:tc:SAML:2.0:nameid-format:transient";

    private String destination;
    private String id;
    private String entityId;
    private String spName;
    private String sessionIndex;

    public LogoutBuilder(String destination, String id, String spentityId, String spName, String sessionIndex) {
        this.destination = destination;
        this.id = id;
        this.entityId = spentityId;
        this.spName = spName;
        this.sessionIndex = sessionIndex;
    }

    public LogoutRequest buildLogoutRequest(){

        DateTime issueInstant = new DateTime();

        LogoutRequest logoutRequest = new LogoutRequestBuilder().buildObject(SAML2_PROTOCOL, "LogoutRequest", "samlp");
        logoutRequest.setID(id);
        logoutRequest.setVersion(SAMLVersion.VERSION_20);
        logoutRequest.setDestination(destination);
        logoutRequest.setIssueInstant(issueInstant);

        logoutRequest.setNameID(getNameID());
        logoutRequest.setIssuer(getIssuer());
        logoutRequest.getSessionIndexes().add(getSessionIndex());

        return logoutRequest;
    }

    private Issuer getIssuer(){
        Issuer issuer = new IssuerBuilder().buildObject();
        issuer.setNameQualifier(entityId);
        issuer.setValue(spName);
        issuer.setFormat(SAML2_ISSUER_NAME_POLICY);
        return issuer;
    }

    private NameID getNameID(){
        NameID nameID = new NameIDBuilder().buildObject();
        nameID.setNameQualifier(spName);
        nameID.setFormat(SAML2_NAME_ID_POLICY);
        nameID.setValue(spName);
        return nameID;
    }

    private SessionIndex getSessionIndex(){
        SessionIndex sessionIndex = new SessionIndexBuilder().buildObject();
        sessionIndex.setSessionIndex(this.sessionIndex);
        return sessionIndex;
    }
}
