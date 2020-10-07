package it.spid.model;

public class LogoutRequested {

    private String id;
    private String destination;
    private String encodeLogoutRequest;
    private String sessionIndex;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getEncodeLogoutRequest() {
        return encodeLogoutRequest;
    }

    public void setEncodeLogoutRequest(String encodeLogoutRequest) {
        this.encodeLogoutRequest = encodeLogoutRequest;
    }

    public String getSessionIndex() {
        return sessionIndex;
    }

    public void setSessionIndex(String sessionIndex) {
        this.sessionIndex = sessionIndex;
    }
}
