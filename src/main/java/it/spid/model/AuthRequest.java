package it.spid.model;

/**
 *
 * */

public class AuthRequest {

    private String xmlAuthRequest;
    private String destination;
    private String id;
    private String xmlPlainText;
    private String relayState;

    public String getRelayState() {
        return relayState;
    }

    public void setRelayState(String relayState) {
        this.relayState = relayState;
    }



    public String getXmlAuthRequest() {
        return xmlAuthRequest;
    }

    public void setXmlAuthRequest(final String xmlAuthRequest) {
        this.xmlAuthRequest = xmlAuthRequest;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getXmlPlainText() {
        return xmlPlainText;
    }

    public void setXmlPlainText(String xmlPlainText) {
        this.xmlPlainText = xmlPlainText;
    }

}
