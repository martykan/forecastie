package cz.martykan.forecastie.utils.certificate;

import android.content.Context;
import androidx.annotation.NonNull;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

/**
 * Class to get and fetch {@link X509Certificate} for Open Weather Map.
 */
public class CertificateDownloader {
    private static final String TAG = "CertificateDownloader";
    public static final String CERTIFICATE_FILE_NAME = "openweathermap.crt";
    private static final String OWN_URL = "https://api.openweathermap.org";

    private static boolean certificateHasBeenFetchedThisLaunch = false;

    /**
     * @return {@code true} if certificate has been already fetched during this launch and
     * {@code false} if certificate hasn't ever been downloaded or was downloaded earlier
     */
    public static boolean hasCertificateBeenFetchedThisLaunch() {
        return certificateHasBeenFetchedThisLaunch;
    }

    /**
     * Fetch certificate and save it into file.
     * <br/>
     * {@link #hasCertificateBeenFetchedThisLaunch()} will be able to return {@code true} if
     * fetching is failed not due connection problems but a problem with the file or
     * unsupported protocols.
     * @param context Android context
     * @return {@code true} if certificate has been fetched and saved into the file and
     * {@code false} otherwise.
     * @see #hasCertificateBeenFetchedThisLaunch()
     * @see #isCertificateDownloaded(Context)
     * @see #getCertificateInputStream(Context)
     */
    public boolean fetch(@NonNull Context context) {
        Log.d(TAG, "try to fetch certificate");
        boolean result = false;
        CertificatesTrustManager trustManager = new CertificatesTrustManager();
        try {
            SSLContext sslContext = SSLContext.getInstance("TLS");
            TrustManager[] trustManagers = new TrustManager[1];
            trustManagers[0] = trustManager;
            sslContext.init(null, trustManagers, null);

            HttpsURLConnection connection = (HttpsURLConnection) new URL(OWN_URL).openConnection();
            connection.setInstanceFollowRedirects(false);
            connection.setSSLSocketFactory(sslContext.getSocketFactory());
            // TODO host name verifier ?

            try {
                connection.connect();
                Log.d(TAG, "connection established, try to save certificate if it has been received");
                result = saveCertificate(context, trustManager);
            } finally {
                connection.disconnect();
            }
            certificateHasBeenFetchedThisLaunch = true;
        } catch (NoSuchAlgorithmException | KeyManagementException | MalformedURLException e) {
            Log.e(TAG, "certificate fetching is failed", e);
            certificateHasBeenFetchedThisLaunch = true;
        } catch (IOException e) {
            if (CertificateUtils.isCertificateException(e)) {
                Log.d(TAG, "try to save certificate if it has been received");
                result = saveCertificate(context, trustManager);
            } else {
                Log.e(TAG, "certificate fetching is failed", e);
            }
        }

        return result;
    }

    private boolean saveCertificate(@NonNull Context context, @NonNull CertificatesTrustManager trustManager) {
        X509Certificate[] certificates = trustManager.getCertificates();
        if (certificates.length == 0) {
            Log.d(TAG, "certificates array is empty");
            certificateHasBeenFetchedThisLaunch = true;
            return false;
        }

        X509Certificate certificate = certificates[certificates.length - 1];

        File certificateFile = getCertificateFile(context);
        if (certificateFile.exists()) {
            Log.d(TAG, "try to remove old file: " + certificateFile.toString());
            boolean deleted = certificateFile.delete();
            if (!deleted) {
                Log.d(TAG, "can not to delete old certificate file");
                certificateHasBeenFetchedThisLaunch = true;
                return false;
            }
        }

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(certificateFile);
            fos.write(certificate.getEncoded());
            Log.d(TAG, "certificate successfully saved");
        } catch (CertificateEncodingException | IOException e) {
            certificateHasBeenFetchedThisLaunch = true;
            e.printStackTrace();
        } finally {
            try {
                if (fos != null)
                    fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return true;
    }

    /**
     * Check is certificate file already downloaded (i.e. exists).
     * @param context Android context
     * @return {@code true} if file exists and {@code false} otherwise
     */
    public boolean isCertificateDownloaded(@NonNull Context context) {
        File certificateFile = getCertificateFile(context);
        return certificateFile.exists();
    }

    /**
     * Returns {@link InputStream} with certificate.
     * <br/>
     * {@link FileNotFoundException} will be thrown if file isn't exist. Use
     * {@link #isCertificateDownloaded(Context)} to check is file exists.
     * @param context Android context
     * @return {@link InputStream} with certificate
     * @see #isCertificateDownloaded(Context)
     * @throws FileNotFoundException will be thrown if file isn't exist. Use
     * {@link #isCertificateDownloaded(Context)} to check is file exists.
     */
    @NonNull
    public InputStream getCertificateInputStream(@NonNull Context context)
            throws FileNotFoundException {
        File certificateFile = getCertificateFile(context);
        return new FileInputStream(certificateFile);
    }

    @NonNull
    private File getCertificateFile(@NonNull Context context) {
        File dir = context.getFilesDir();
        return new File(dir, CERTIFICATE_FILE_NAME);
    }
}