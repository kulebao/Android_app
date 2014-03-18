package com.djc.logintest.httpclientmgr;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;

import android.util.Log;

import com.djc.logintest.constant.ConstantValue;
import com.djc.logintest.constant.EventType;
import com.djc.logintest.constant.JSONConstant;
import com.djc.logintest.customexception.BindFailException;
import com.djc.logintest.customexception.InvalidTokenException;
import com.djc.logintest.net.HttpResult;
import com.djc.logintest.net.PushMethod;
import com.djc.logintest.utils.Utils;

public class HttpClientHelper {

	private static HttpClient httpClient;

	private HttpClientHelper() {
	}

	public static synchronized HttpClient getHttpClient() {
		if (null == httpClient) {
			// 初始化工作
			try {
				KeyStore trustStore = KeyStore.getInstance(KeyStore
						.getDefaultType());
				trustStore.load(null, null);
				SSLSocketFactory sf = new SSLSocketFactoryEx(trustStore);
				sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER); // 允许所有主机的验证

				HttpParams params = new BasicHttpParams();
				HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
				HttpProtocolParams.setContentCharset(params,
						HTTP.DEFAULT_CONTENT_CHARSET);
				HttpProtocolParams.setUseExpectContinue(params, true);
				// 设置连接管理器的超时
				ConnManagerParams.setTimeout(params, 10000);
				// 设置连接超时
				HttpConnectionParams.setConnectionTimeout(params, 10000);
				// 设置socket超时
				HttpConnectionParams.setSoTimeout(params, 10000);
				// 设置http https支持
				SchemeRegistry schReg = new SchemeRegistry();
				schReg.register(new Scheme("http", PlainSocketFactory
						.getSocketFactory(), 80));
				schReg.register(new Scheme("https", sf, 443));

				ClientConnectionManager conManager = new ThreadSafeClientConnManager(
						params, schReg);

				httpClient = new DefaultHttpClient(conManager, params);
			} catch (Exception e) {
				e.printStackTrace();
				return new DefaultHttpClient();
			}
		}
		return httpClient;
	}

	public static HttpResult executePost(String url, String content)
			throws Exception {
		HttpResult result = doPostImpl(url, content);

		if (result.getResCode() == HttpStatus.SC_UNAUTHORIZED) {
			PushMethod method = PushMethod.getMethod();
			int ret = method.sendBinfInfo();
			if (ret == EventType.BIND_SUCCESS) {
				// bind 成功，刷新cookie，重新请求
				Log.d("DDD code:", "" + "doGetImpl again!");
				result = doPostImpl(url, content);
			} else if (ret == EventType.BIND_FAILED) {
				throw new InvalidTokenException(
						"token invalid and bind network error");
			} else {
				throw new BindFailException(
						"token invalid and bind network error");
			}
		}
		return result;
	}

	private static HttpResult doPostImpl(String url, String content)
			throws URISyntaxException, UnsupportedEncodingException,
			IOException, ClientProtocolException, Exception {
		int status = HttpStatus.SC_UNAUTHORIZED;
		HttpResult httpResult = new HttpResult();
		BufferedReader in = null;
		try {
			// 定义HttpClient
			HttpClient client = getHttpClient();
			// 实例化HTTP方法
			HttpPost request = new HttpPost();
			request.setURI(new URI(url));
			// 所有访问数据的请求，都必须加上token
			request.setHeader(ConstantValue.HEADER_TOKEN,
					Utils.getProp(JSONConstant.ACCESS_TOKEN));
			request.setHeader("Content-type", "application/json;charset=UTF-8");
			request.setEntity(new StringEntity(content, HTTP.UTF_8));

			HttpResponse response = client.execute(request);
			status = response.getStatusLine().getStatusCode();
			Log.d("DDD code:", "" + status);

			if (HttpClientHelper.isHttpRequestOK(status)) {
				in = new BufferedReader(new InputStreamReader(response
						.getEntity().getContent()));
				StringBuffer sb = new StringBuffer("");
				String line = "";
				while ((line = in.readLine()) != null) {
					sb.append(line);
				}
				httpResult.setContent(sb.toString());
			} else if (status == HttpStatus.SC_UNAUTHORIZED) {
				refreshCookie();
			}
		} finally {
			if (in != null) {
				try {
					in.close();// 最后要关闭BufferedReader
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		httpResult.setResCode(status);
		return httpResult;
	}

	public static HttpResult executeGet(String url) throws Exception {
		HttpResult result = doGetImpl(url);
		if (result.getResCode() == HttpStatus.SC_UNAUTHORIZED) {
			PushMethod method = PushMethod.getMethod();
			int ret = method.sendBinfInfo();
			if (ret == EventType.BIND_SUCCESS) {
				// bind 成功，刷新cookie，重新请求
				Log.d("DDD code:", "" + "doGetImpl again!");
				result = doGetImpl(url);
			} else if (ret == EventType.BIND_FAILED) {
				throw new InvalidTokenException(
						"token invalid and bind network error");
			} else {
				throw new BindFailException(
						"token invalid and bind network error");
			}
		}
		return result;
	}

	private static HttpResult doGetImpl(String url) throws URISyntaxException,
			IOException, ClientProtocolException {
		int status = HttpStatus.SC_UNAUTHORIZED;
		HttpResult httpResult = new HttpResult();
		BufferedReader in = null;
		try {
			// 定义HttpClient
			HttpClient client = getHttpClient();
			// 实例化HTTP方法
			HttpGet request = new HttpGet();
			request.setURI(new URI(url));
			// 所有访问数据的请求，都必须加上token
			request.setHeader(ConstantValue.HEADER_TOKEN,
					Utils.getProp(JSONConstant.ACCESS_TOKEN));

			HttpResponse response = client.execute(request);
			status = response.getStatusLine().getStatusCode();
			Log.d("DDD code:", "" + status);

			if (HttpClientHelper.isHttpRequestOK(status)) {
				in = new BufferedReader(new InputStreamReader(response
						.getEntity().getContent()));
				StringBuffer sb = new StringBuffer("");
				String line = "";
				while ((line = in.readLine()) != null) {
					sb.append(line);
				}
				httpResult.setContent(sb.toString());
				Log.d("DDD code:", "content =" + sb.toString());
			}
		} finally {
			if (in != null) {
				try {
					in.close();// 最后要关闭BufferedReader
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		httpResult.setResCode(status);
		return httpResult;
	}

	// 向服务器端刷新cookie
	private static void refreshCookie() throws Exception {
		PushMethod method = PushMethod.getMethod();
		int result = method.sendBinfInfo();
		if (result != EventType.BIND_SUCCESS) {
			throw new Exception("send bind fail when token invalid!");
		}
	}

	private static boolean isHttpRequestOK(int status) {
		// 特殊情况，返回400也认为请求成功
		return (status == HttpStatus.SC_OK || status == HttpStatus.SC_BAD_REQUEST);
	}
}

class SSLSocketFactoryEx extends SSLSocketFactory {

	SSLContext sslContext = SSLContext.getInstance("TLS");

	public SSLSocketFactoryEx(KeyStore truststore)
			throws NoSuchAlgorithmException, KeyManagementException,
			KeyStoreException, UnrecoverableKeyException {
		super(truststore);

		TrustManager tm = new X509TrustManager() {

			@Override
			public java.security.cert.X509Certificate[] getAcceptedIssuers() {
				return null;
			}

			@Override
			public void checkClientTrusted(
					java.security.cert.X509Certificate[] chain, String authType)
					throws java.security.cert.CertificateException {

			}

			@Override
			public void checkServerTrusted(
					java.security.cert.X509Certificate[] chain, String authType)
					throws java.security.cert.CertificateException {

			}
		};

		sslContext.init(null, new TrustManager[] { tm }, null);
	}

	@Override
	public Socket createSocket(Socket socket, String host, int port,
			boolean autoClose) throws IOException, UnknownHostException {
		return sslContext.getSocketFactory().createSocket(socket, host, port,
				autoClose);
	}

	@Override
	public Socket createSocket() throws IOException {
		return sslContext.getSocketFactory().createSocket();
	}
}