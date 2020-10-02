package cz.martykan.forecastie.utils.certificate;

import androidx.annotation.NonNull;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.X509TrustManager;

/**
 * TrustManager to retrieve Open Weather Map certificates.
 */
class CertificatesTrustManager implements X509TrustManager {
    private X509Certificate[] certificates = new X509Certificate[0];

    @Override
    public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        throw new CertificateException("Client certificates are not trusted");
    }

    @Override
    public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        if (chain != null) {
            certificates = chain;
        }
    }

    @Override
    public X509Certificate[] getAcceptedIssuers() {
        return new X509Certificate[0];
    }

    @NonNull
    public X509Certificate[] getCertificates() {
        return certificates;
    }
}