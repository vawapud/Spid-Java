package it.spid.util;

import org.opensaml.DefaultBootstrap;
import org.opensaml.xml.ConfigurationException;
import org.opensaml.xml.util.Base64;
import org.opensaml.xml.util.XMLHelper;
import org.w3c.dom.Element;

import java.io.*;
import java.net.URLEncoder;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;

/**
 * Questa classe contiene i metodi che servono alla codifica della richiesta di autenticazione.
 * Contiene per completezza e per una futura aggiunta anche i metodi per la compressione della richiesta
 * che è necessaria se il binding è tramite HTTP-REDIRECT.
 * Essendo la libreria sviluppata per HTTP-POST vengono lasciati per un futuro sviluppo
 * */

public class AuthnUtil {





    public AuthnUtil() {
        //Default Bootstrap per inizializzare i marshaller
        try {
            DefaultBootstrap.bootstrap();
        }
        catch (ConfigurationException e) {

        }
    }


    //Trasforma oggetto costruito AuthnRequest da XMLObject ereditato in documento DOM poi in stringa
    /**
     * Stampa la richiesta di autenticazione come stringa -- Metodo test
     * TODO aggiungere il pretty print
     *
     * @param authDom Element dom che contiene tutta la richiesta di autenticazione
     * @return String contente la richiesta di autenticazione in forma esplicita
     * */
    public String printAuthnRequest(Element authDom) {


        StringWriter requestWriter = new StringWriter();
        requestWriter = new StringWriter();
        XMLHelper.writeNode(authDom, requestWriter);
        String authnRequestString = requestWriter.toString();

        return authnRequestString;
    }



    //Metodo per encodare authnreqeust per il HTTP POST
    /**
     * Metodo per l'encoding della richiesta di autenticazione.
     * Viene solo codificata in base64 per il binding POST
     *
     *@param authnRequest Element dom che contiene tutta la richiesta di autenticazione
     *@return encodeAuthnRequest Stringa codificata da presentare all'identity provider
     * */
    public String encodeAuthnRequest (Element authnRequest) {

        String requestMessage = printAuthnRequest(authnRequest);
        String encodedAuthRequest = null;

        try {
            encodedAuthRequest = Base64.encodeBytes(requestMessage.getBytes("UTF-8"), Base64.DONT_BREAK_LINES);
            //encodedAuthRequest = URLEncoder.encode(encodedAuthRequest, "UTF-8").trim();
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return encodedAuthRequest;
    }


    //metodo che encoda e zippa per il redirect implementazione con binding
    /**
     * Metodo che codifica e comprime la richiesta di autenticazione.
     * E' utilizzato per il binding HTTP-REDIRECT
     *
     * @param authnRequest Element dom che contiene tutta la richiesta di autenticazione
     * @return encodedRequestMessage richiesta di autenticazione compressa e codificata
     * */
    public String encodeAndPrintAuthnRequest(Element authnRequest) {

    	String requestMessage;
		requestMessage = printAuthnRequest(authnRequest);
    	Deflater deflater = new Deflater(Deflater.DEFLATED, true);
    	ByteArrayOutputStream byteArrayOutputStream =null;
    	DeflaterOutputStream deflaterOutputStream = null;

    	String encodedRequestMessage = null;

    	try {
    		byteArrayOutputStream = new ByteArrayOutputStream();
			deflaterOutputStream = new DeflaterOutputStream(byteArrayOutputStream, deflater);
			deflaterOutputStream.write(requestMessage.getBytes()); // compressing
			deflaterOutputStream.close();

			encodedRequestMessage = Base64.encodeBytes(byteArrayOutputStream.toByteArray(), Base64.DONT_BREAK_LINES);

			encodedRequestMessage = URLEncoder.encode(encodedRequestMessage, "UTF-8").trim(); // encoding string
    	}catch(UnsupportedEncodingException e) {
    		e.printStackTrace();
    	}catch(IOException e) {
    		e.printStackTrace();
    	}


		return encodedRequestMessage;

    }



}
