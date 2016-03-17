package daris.client.app;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class SSL {

    private static SSLSocketFactory _defaultSSLSocketFactory = HttpsURLConnection
            .getDefaultSSLSocketFactory();
    private static boolean _trustAllCertificates = false;

    public static void setTrustAllCertificates(boolean trustAllCertificates) {
        if (_trustAllCertificates == trustAllCertificates) {
            return;
        }
        synchronized (SSL.class) {
            if (trustAllCertificates) {
                try {
                    SSLContext sc = SSLContext.getInstance("SSL");
                    sc.init(null,
                            new TrustManager[] {
                                    new TrustAllCertificatesTrustManager() },
                            new java.security.SecureRandom());
                    HttpsURLConnection
                            .setDefaultSSLSocketFactory(sc.getSocketFactory());
                    _trustAllCertificates = true;
                } catch (Throwable e) {
                    e.printStackTrace(System.err);
                }
            } else {
                HttpsURLConnection
                        .setDefaultSSLSocketFactory(_defaultSSLSocketFactory);
                _trustAllCertificates = false;
            }
        }
    }

    private static class TrustAllCertificatesTrustManager
            implements X509TrustManager {

        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType)
                throws CertificateException {

        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType)
                throws CertificateException {

        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return null;
        }

    }

}
