package com.cocobabys.net;

import java.security.cert.CertificateException;

import javax.net.ssl.X509TrustManager;

public class MyTrustManager implements X509TrustManager {

	@Override
	public java.security.cert.X509Certificate[] getAcceptedIssuers() {
        return null;
	}

	@Override
	public void checkClientTrusted(
			java.security.cert.X509Certificate[] arg0, String arg1)
			throws CertificateException {
	}

	@Override
	public void checkServerTrusted(
			java.security.cert.X509Certificate[] arg0, String arg1)
			throws CertificateException {
	}
}