package it.spid.model;

public class LogoutRequestDecoded {

    private String inResponseTo;
    private Boolean isStatusSuccess ;

    public Boolean getStatusSuccess() {
        return isStatusSuccess;
    }

    public void setStatusSuccess(Boolean statusSuccess) {
        isStatusSuccess = statusSuccess;
    }

    public String getInResponseTo() {
        return inResponseTo;
    }

    public void setInResponseTo(String inResponseTo) {
        this.inResponseTo = inResponseTo;
    }


}
