package it.spid.exception;

public class SpidServiceException extends Exception {

    private static final long serialVersionUID = 1885658749235601203L;

    public SpidServiceException(){
        super();

    }

    public SpidServiceException(String message){

        super(message);
    }

    public SpidServiceException(Throwable cause){
        super(cause);
    }
    public SpidServiceException(String message, Throwable cause){
        super(message, cause);
    }



}
