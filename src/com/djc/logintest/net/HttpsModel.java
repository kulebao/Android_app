package com.djc.logintest.net;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.SecureRandom;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

import android.util.Log;

public class HttpsModel {
    private static final int READ_TIMEOUT = 30000;
    private static final int CONNECT_TIMEOUT = 30000;

    public static HttpsURLConnection createHttpsConnection(String url, String type)
            throws MalformedURLException, IOException {

        URL httpsUrl = new java.net.URL(url);

        HttpsURLConnection connect = null;
        connect = (HttpsURLConnection) httpsUrl.openConnection();
        connect.setRequestMethod(type);
        connect.setDoOutput(true);
        // connect.setDoInput(true);
        // connect.setRequestProperty("Accept", "*/*");
        // connect.setRequestProperty("Accept-Encoding", "gzip,deflate,sdch");
        // connect.setRequestProperty(
        // "User-Agent",
        //   "Mozilla/5.0 (Windows NT 5.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/28.0.1500.72 Safari/537.36");
        connect.setUseCaches(false);
        connect.setRequestProperty("Content-type", "application/json;charset=UTF-8");
        connect.setConnectTimeout(CONNECT_TIMEOUT);
        connect.setReadTimeout(READ_TIMEOUT);
        return connect;
    }

    public static void initHttpsClient() {
        try {
            Log.d("DDD", "initHttpsClient ");
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, new TrustManager[] { new MyTrustManager() }, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier(new MyHostnameVerifier());
        } catch (Exception e) {
            Log.e("DJC", "initHttpsClient e=" + e.toString());
        }
    }

}
