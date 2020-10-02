package cz.martykan.forecastie.utils.certificate;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertPathValidatorException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.TrustManagerFactory;

/**
 * Useful methods for work with certificate.
 */
public abstract class CertificateUtils {
    /**
     * Check is {@link IOException} is problem with certificate or something different.
     * @param exception exception
     * @return {@code true} if exception related to certificate and {@code false} otherwise
     */
    public static boolean isCertificateException(IOException exception) {
        if (!(exception instanceof SSLHandshakeException))
            return false;
        Throwable cause = exception;
        do {
            cause = cause.getCause();
        } while (cause != null && !(cause instanceof CertPathValidatorException));
        return cause != null;
    }

    /**
     * Create {@link SSLContext} with Open Weather Map certificate as trusted.
     * @param context Android context
     * @param doNotRetry (out) will be set into {@code true} if this operation shouldn't be repeated
     * @param fetchCertificate if this is {@code true}, certificate will be fetched even it already exists
     * @return {@link SSLContext} with Open Weather Map certificate as trusted
     */
    @Nullable
    public static SSLContext addCertificate(@NonNull Context context,
                                            @NonNull AtomicBoolean doNotRetry,
                                            boolean fetchCertificate) {
        SSLContext result;
        try {
            // Load CAs from an InputStream
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            CertificateDownloader certificateDownloader = new CertificateDownloader();
            if (!certificateDownloader.isCertificateDownloaded(context) || fetchCertificate) {
                if (!certificateDownloader.fetch(context)) {
                    if (CertificateDownloader.hasCertificateBeenFetchedThisLaunch()) {
                        doNotRetry.set(true);
                    }
                    return null;
                } else {
                    doNotRetry.set(CertificateDownloader.hasCertificateBeenFetchedThisLaunch());
                }
            }
            InputStream certificateInputStream =
                    certificateDownloader.getCertificateInputStream(context);
            InputStream caInput = new BufferedInputStream(certificateInputStream);
            Certificate ca;
            try {
                ca = cf.generateCertificate(caInput);
                System.out.println("ca=" + ((X509Certificate) ca).getSubjectDN());
            } finally {
                caInput.close();
            }

            // Create a KeyStore containing our trusted CAs
            String keyStoreType = KeyStore.getDefaultType();
            KeyStore keyStore = KeyStore.getInstance(keyStoreType);
            keyStore.load(null, null);
            keyStore.setCertificateEntry("ca", ca);

            // Create a TrustManager that trusts the CAs in our KeyStore
            String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
            tmf.init(keyStore);

            // Create an SSLContext that uses our TrustManager
            result = SSLContext.getInstance("TLS");
            result.init(null, tmf.getTrustManagers(), null);
        } catch (IOException e) {
            e.printStackTrace();
            doNotRetry.set(false);
            result = null;
        } catch (CertificateException | KeyStoreException | NoSuchAlgorithmException | KeyManagementException e) {
            e.printStackTrace();
            doNotRetry.set(true);
            result = null;
        }
        return result;
    }
}