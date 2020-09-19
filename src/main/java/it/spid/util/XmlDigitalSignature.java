package it.spid.util;

import org.apache.commons.codec.binary.Base64;
import org.opensaml.DefaultBootstrap;
import org.opensaml.xml.Configuration;
import org.opensaml.xml.ConfigurationException;
import org.opensaml.xml.XMLObjectBuilderFactory;
import org.opensaml.xml.security.credential.Credential;
import org.opensaml.xml.security.keyinfo.KeyInfoHelper;
import org.opensaml.xml.security.x509.BasicX509Credential;
import org.opensaml.xml.signature.KeyInfo;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.*;
import java.security.KeyStore.PrivateKeyEntry;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Properties;

/**
 * Classe che gestisce tutto quello che è legato alla firma digitale.
 * Recupero del keystore, dal quale recuperare la chiave privata e il certificato per le firme.
 * Vengono inoltre create le credenziali.
 * */

public class XmlDigitalSignature {


    private String password;
    private String alias;


    private static final String SPID_SP_PASS ="it.spid.keystore.password";
    private static final String SPID_SP_ALIAS ="it.spid.keystore.certificate.alias";
    private static final String SPID_SP_KEYSTORE_PATH ="it.spid.keystore.path";



    public XmlDigitalSignature() {
        try {
            DefaultBootstrap.bootstrap();
        }
        catch (ConfigurationException e) {
            e.printStackTrace();
        }

        this.password = loadPassProperties();
        this.alias = loadAliasProperties();
    }


    /**
     * Metodo per caricare il keystore
     *
     * @return keystore
     * */
    public KeyStore getKeyStore() {

        KeyStore keyStore = null;

        try {
            keyStore = KeyManager.loadKeyStore(getKeystorePath(), password);

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


        return keyStore;
    }

    /**
     * Metodo per la creazione delle credenziali.
     * Dopo aver caricato il keystore ne recupero un istanza e successivamente
     * la chiave privata, il certificato.
     * Creo le credenziali e le popolo con chiave privata e certificato
     *
     * @return credential credenziali per la firma digitale
     * */


    public Credential getCredential() {

        KeyStore keyStore = getKeyStore();

        KeyStore.PrivateKeyEntry privateKeyEntry = null;

        try {
            privateKeyEntry = (PrivateKeyEntry) keyStore.getEntry(alias, new KeyStore.PasswordProtection(password.toCharArray()));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnrecoverableEntryException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        }

        PrivateKey privateKey = privateKeyEntry.getPrivateKey();
        X509Certificate certificate = (X509Certificate) privateKeyEntry.getCertificate();
        //creo e setto le nuove credenziali
        BasicX509Credential credential = new BasicX509Credential();
        credential.setEntityCertificate(certificate);
        credential.setPrivateKey(privateKey);

        return credential;
    }

    /**
     * Metodo per la creazione dell'element xml che rappresenta il KeyInfo, ovvero tutto quel che
     * concerne la firma digitale. Vengono quindi aggiunte chiave pubblica e certificato
     *
     * @return keyInfo elemento xml sottoforma di oggetto opensaml
     * */

    public KeyInfo getKeyInfo() {

        XMLObjectBuilderFactory builderFactory = Configuration.getBuilderFactory();

        KeyInfo keyInfo = (KeyInfo) builderFactory.getBuilder(KeyInfo.DEFAULT_ELEMENT_NAME).buildObject(KeyInfo.DEFAULT_ELEMENT_NAME);

        KeyStore keyStore = getKeyStore();

        try {
            X509Certificate certificate = (X509Certificate) keyStore.getCertificate(alias);
            KeyInfoHelper.addPublicKey(keyInfo, certificate.getPublicKey());
            KeyInfoHelper.addCertificate(keyInfo, certificate);

        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch(CertificateEncodingException e) {
            e.printStackTrace();
        }
        return keyInfo;
    }

    /**
     * Recupera il path del keystore dalle properties del service provider
     *
     * @return keystorePath il path assoluto del keystore sotto resources
     * */

    public String getKeystorePath() {

        Properties properties = new Properties();
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        String keystorePath = null;
        try(InputStream propertiesInputStream = classLoader.getResourceAsStream("service.properties")){
            properties.load(propertiesInputStream);
            keystorePath = properties.getProperty(SPID_SP_KEYSTORE_PATH);
        }catch (IOException e){
            e.getMessage();
        }
        return keystorePath;
    }

    /**
     * Metodo per la generazione dell'oggetto certificato tramite stringa.
     * Usato per il recupero dei certificati dai metadata identity provider
     *
     * @param stringCertificate stringa che rappresenta il certificato come recuperato
     *                          dal metadata, quindi stringa e codificato il base64.
     * @return certificate, oggetto certificato
     * */

    public java.security.cert.X509Certificate generateX509Certificate(String stringCertificate) throws CertificateException {

        byte[] bytesCertificate = Base64.decodeBase64(stringCertificate);

        CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
        InputStream in = new ByteArrayInputStream(bytesCertificate);
        java.security.cert.X509Certificate certificate = (java.security.cert.X509Certificate) certFactory.generateCertificate(in);
        return certificate;
    }

    /**
     * Metodo per caricare le properties
     *
     * @return alias
     * */

    private String loadAliasProperties(){
        Properties properties = new Properties();
        String alias = null;
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        try(InputStream propertiesInputStream = classLoader.getResourceAsStream("service.properties")){
            properties.load(propertiesInputStream);
            alias = properties.getProperty(SPID_SP_ALIAS);

        }catch(IOException e) {
            e.printStackTrace();
        }

        return alias;
    }

    /**
     * Metodo per caricare le properties
     *
     * @return password del keystore
     * */

    private String loadPassProperties(){
        Properties properties = new Properties();
        String password = null;
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        try(InputStream propertiesInputStream = classLoader.getResourceAsStream("service.properties")){
            properties.load(propertiesInputStream);
            password = properties.getProperty(SPID_SP_PASS);

        }catch(IOException e) {
            e.printStackTrace();
        }

        return password;

    }
}
