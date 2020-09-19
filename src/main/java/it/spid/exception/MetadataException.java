package it.spid.exception;

public class MetadataException extends Exception {

    public MetadataException(){
        super();
    }

    public MetadataException(Throwable cause){

        super(cause);
    }
    public MetadataException(String message, Throwable cause){

        super(message, cause);
    }

}
