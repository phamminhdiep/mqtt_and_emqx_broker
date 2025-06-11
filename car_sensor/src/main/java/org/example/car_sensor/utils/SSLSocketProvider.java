package org.example.car_sensor.utils;

import org.springframework.stereotype.Component;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;
import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

@Component
public class SSLSocketProvider {


    public static SSLSocketFactory getCustomSSLSocketFactory(String caCertPath) throws Exception {
        // Load CA certificate
        CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
        X509Certificate caCert = (X509Certificate) certFactory.generateCertificate(new FileInputStream(caCertPath));

        // Create a KeyStore for the CA certificate
        KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
        trustStore.load(null, null); // Initialize an empty keystore
        trustStore.setCertificateEntry("ca", caCert); // Add the CA certificate

        // Create a TrustManagerFactory that uses our custom trustStore
        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init(trustStore);

        // Create an SSLContext with only trust managers (no key managers for client cert)
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(
                null, // No KeyManagers (no client certificate)
                trustManagerFactory.getTrustManagers(), // TrustManagers (for server authentication)
                null // SecureRandom
        );

        return sslContext.getSocketFactory();
    }
}
