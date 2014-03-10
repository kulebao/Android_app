package com.djc.logintest.upload.qiniu.auth;

import java.io.IOException;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

import android.os.AsyncTask;

import com.djc.logintest.upload.qiniu.conf.Conf;
import com.djc.logintest.upload.qiniu.utils.ICancel;

public class Client {

	protected HttpClient mClient;

	public Client(HttpClient client) {
		mClient = client;
	}

	public static ClientExecutor get(String url, CallRet ret) {
		Client client = Client.defaultClient();
		return client.get(client.makeClientExecutor(), url, ret);
	}

	public ClientExecutor call(ClientExecutor client, String url,
			HttpEntity entity, CallRet ret) {
		Header header = entity.getContentType();
		String contentType = "application/octet-stream";
		if (header != null) {
			contentType = header.getValue();
		}
		return call(client, url, contentType, entity, ret);
	}

	public ClientExecutor call(ClientExecutor client, String url,
			String contentType, HttpEntity entity, CallRet ret) {
		HttpPost httppost = new HttpPost(url);
		httppost.setEntity(entity);

		if (contentType != null) {
			httppost.setHeader("Content-Type", contentType);
		}
		return execute(client, httppost, ret);
	}

	public ClientExecutor get(ClientExecutor client, String url, CallRet ret) {
		return execute(client, new HttpGet(url), ret);
	}

	public ClientExecutor makeClientExecutor() {
		return new ClientExecutor();
	}

	protected ClientExecutor execute(ClientExecutor client,
			HttpRequestBase httpRequest, final CallRet ret) {
		client.setup(httpRequest, ret);
		client.execute();
		return client;
	}

	protected HttpResponse roundtrip(HttpRequestBase httpRequest)
			throws IOException {
		httpRequest.setHeader("User-Agent", Conf.USER_AGENT);
		return mClient.execute(httpRequest);
	}

	public class ClientExecutor extends AsyncTask<Object, Object, Object>
			implements ICancel {
		HttpRequestBase mHttpRequest;
		CallRet mRet;

		public void setup(HttpRequestBase httpRequest, CallRet ret) {
			mHttpRequest = httpRequest;
			mRet = ret;
		}

		public void upload(long current, long total) {
			publishProgress(current, total);
		}

		@Override
		protected Object doInBackground(Object... objects) {
			try {
				HttpResponse resp = roundtrip(mHttpRequest);
				int statusCode = resp.getStatusLine().getStatusCode();
				if (statusCode == 401) { // android 2.3 will not response
					return new Exception("unauthorized!");
				}
				byte[] data = EntityUtils.toByteArray(resp.getEntity());

				if (statusCode / 100 != 2) {
					if (data.length == 0) {
						String xlog = resp.getFirstHeader("X-Log").getValue();
						if (xlog.length() > 0) {
							return new Exception(xlog);
						}
						return new Exception(resp.getStatusLine()
								.getReasonPhrase());
					}
					return new Exception(new String(data));
				}
				return data;
			} catch (IOException e) {
				e.printStackTrace();
				return e;
			}
		}

		@Override
		protected void onProgressUpdate(Object... values) {
			if (mRet != null) {
				mRet.onProcess((Long) values[0], (Long) values[1]);
			}
		}

		@Override
		protected void onPostExecute(Object o) {
			if (o instanceof Exception) {
				mRet.onFailure((Exception) o);
				return;
			}
			mRet.onSuccess((byte[]) o);
		}
	};

	public static Client defaultClient() {
		return new Client(getMultithreadClient());
	}

	public static HttpClient getMultithreadClient() {
		HttpClient client = new DefaultHttpClient();
		ClientConnectionManager mgr = client.getConnectionManager();
		HttpParams params = client.getParams();
		client = new DefaultHttpClient(new ThreadSafeClientConnManager(params,
				mgr.getSchemeRegistry()), params);
		return client;
	}

	public String callEx(ClientExecutor client, String url, HttpEntity entity) {
		Header header = entity.getContentType();
		String contentType = "application/octet-stream";
		if (header != null) {
			contentType = header.getValue();
		}

		HttpPost httppost = new HttpPost(url);
		httppost.setEntity(entity);

		if (contentType != null) {
			httppost.setHeader("Content-Type", contentType);
		}

		httppost.setHeader("User-Agent", Conf.USER_AGENT);
		try {
			HttpResponse resp = mClient.execute(httppost);
			int statusCode = resp.getStatusLine().getStatusCode();
			if (statusCode == 200) {
				byte[] body = EntityUtils.toByteArray(resp.getEntity());
				if (body == null) {
					return "";
				}
				// JSONObject obj = new JSONObject(new String(body));
				return new String(body);
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "";
	}
}