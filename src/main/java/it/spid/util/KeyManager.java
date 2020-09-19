package it.spid.util;

import java.io.IOException;
import java.io.InputStream;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

/**
 * Classe che gestisce tutto quello che concerne il keystore
 * Può essere facilmente arricchita dei metodi per il recupero del truststore per il collegamento in TSL TODO??
 *
 * */


public class KeyManager {


        /**
         * Metodo che recupera il keystore dalle resources
         *
         * @param path uri del keystore
         * @param password password del keystore
         *
         * @return keystore oggetto keystore
         * */

        public static KeyStore loadKeyStore (String path, String password) throws NoSuchAlgorithmException, CertificateException, IOException, KeyStoreException {

            KeyStore keystore = KeyStore.getInstance("JKS");
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            InputStream is = classLoader.getResourceAsStream(path);
            keystore.load(is, password.toCharArray());
            return keystore;
        }


        public static PublicKey getPublicKey(KeyStore keystore, String alias) throws KeyStoreException {
            if(keystore.containsAlias(alias))
                return keystore.getCertificate(alias).getPublicKey();
            return null;

        }

        public static PrivateKey getPrivateKey(KeyStore keystore, String alias, String password) throws UnrecoverableKeyException, KeyStoreException, NoSuchAlgorithmException {
            if(keystore.containsAlias(alias))
                return (PrivateKey) keystore.getKey(alias, password.toCharArray());
            return null;

        }


        public static X509Certificate getCertificate(KeyStore keystore, String alias) throws KeyStoreException {
            if(keystore.containsAlias(alias))
                return (X509Certificate) keystore.getCertificate(alias);
            return null;
        }

    }
