package com.cocobabys.httpclientmgr;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.net.URI;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.util.zip.GZIPInputStream;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
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

import android.text.TextUtils;
import android.util.Log;

import com.cocobabys.constant.ConstantValue;
import com.cocobabys.constant.EventType;
import com.cocobabys.constant.JSONConstant;
import com.cocobabys.customexception.AccountExpiredException;
import com.cocobabys.customexception.BindFailException;
import com.cocobabys.customexception.DuplicateLoginException;
import com.cocobabys.customexception.InvalidTokenException;
import com.cocobabys.net.HttpResult;
import com.cocobabys.net.PushMethod;
import com.cocobabys.utils.DataUtils;
import com.cocobabys.utils.Utils;

public class HttpClientHelper{

    private static final String VERSION_CODE = "versioncode";
    private static HttpClient   httpClient;

    private HttpClientHelper(){}

    public static synchronized HttpClient getHttpClient(){
        if(null == httpClient){
            // 初始化工作
            try{
                KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
                trustStore.load(null, null);
                SSLSocketFactory sf = new SSLSocketFactoryEx(trustStore);
                sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER); // 允许所有主机的验证

                HttpParams params = new BasicHttpParams();
                HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
                HttpProtocolParams.setContentCharset(params, HTTP.DEFAULT_CONTENT_CHARSET);
                HttpProtocolParams.setUseExpectContinue(params, true);
                // 设置最大连接数
                ConnManagerParams.setMaxTotalConnections(params, 50);
                // 设置获取连接管理器的超时
                ConnManagerParams.setTimeout(params, 10000);

                // 设置连接超时
                HttpConnectionParams.setConnectionTimeout(params, 10000);
                // 设置socket超时
                HttpConnectionParams.setSoTimeout(params, 10000);
                // 设置http https支持
                SchemeRegistry schReg = new SchemeRegistry();
                schReg.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
                schReg.register(new Scheme("https", sf, 443));

                ClientConnectionManager conManager = new ThreadSafeClientConnManager(params, schReg);

                httpClient = new DefaultHttpClient(conManager, params);
            } catch(Exception e){
                e.printStackTrace();
                return new DefaultHttpClient();
            }
        }
        return httpClient;
    }

    public static HttpResult executePost(String url, String content) throws Exception{
        HttpResult result = doPostImpl(url, content);
        Log.d("execute:", "url =" + url);
        Log.d("execute:", "content =" + content);

        if(result.getResCode() == HttpStatus.SC_UNAUTHORIZED){
            PushMethod method = PushMethod.getMethod();
            // 内置锁同步，以免A线程请求服务器返回新token后，B线程使用旧token再次请求，导致服务器返回error 3，token错误
            synchronized(HttpClientHelper.class){
                int ret = method.sendBinfInfo();
                checkResult(ret);
            }
            // bind 成功，刷新cookie，重新请求
            Log.d("DDD code:", "" + "doGetImpl again!");
            result = doPostImpl(url, content);
        }
        return result;
    }

    private static HttpResult doPostImpl(String url, String content) throws Exception{
        int status = HttpStatus.SC_UNAUTHORIZED;
        HttpResult httpResult = new HttpResult();
        BufferedReader in = null;
        HttpPost request = new HttpPost();
        try{
            // 定义HttpClient
            HttpClient client = getHttpClient();
            // 实例化HTTP方法
            request.setURI(new URI(url));
            // 所有访问数据的请求，都必须加上token
            request.setHeader(ConstantValue.HEADER_TOKEN, DataUtils.getProp(JSONConstant.ACCESS_TOKEN));
            // 所有访问数据的请求，都必须加上source,区别网页和客户端
            request.setHeader(ConstantValue.HEADER_SOURCE, ConstantValue.SOURCE_ANDROID);
            // 自定义header，带版本号
            request.setHeader(VERSION_CODE, String.valueOf(DataUtils.getVersionCode()));

            // 测试gzip
            request.setHeader("Accept-Encoding", "gzip");

            if(!TextUtils.isEmpty(content)){
                request.setHeader("Content-type", "application/json;charset=UTF-8");
                request.setEntity(new StringEntity(content, HTTP.UTF_8));
            }

            HttpResponse response = client.execute(request);
            status = response.getStatusLine().getStatusCode();
            Log.d("DDD doPostImpl code:", "" + status + " vercode=" + DataUtils.getVersionCode());
            in = readContentEx(httpResult, response);
            if(HttpClientHelper.isHttpRequestOK(status)){
                request.abort();
            }
        } catch(Exception e){
            request.abort();
            throw e;
        }
        finally{
            if(in != null){
                try{
                    in.close();// 最后要关闭BufferedReader
                } catch(Exception e){
                    e.printStackTrace();
                }
            }
        }
        httpResult.setResCode(status);
        return httpResult;
    }

    private static BufferedReader readContentEx(HttpResult httpResult, HttpResponse response) throws IOException{
        Log.d("DDD code:", "gzip statusline = " + response.getStatusLine());
        Log.d("DDD code:", "gzip Encoding =" + response.getLastHeader("Content-Encoding"));
        Log.d("DDD code:", "gzip Length =" + response.getLastHeader("Content-Length"));

        GZIPInputStream gzin = null;
        BufferedReader in = null;
        InputStreamReader isr = null;

        String contentEncoding = getEncoding(response);

        if(contentEncoding != null && contentEncoding.indexOf("gzip") > -1){
            // For GZip response
            try{
                InputStream is = response.getEntity().getContent();
                gzin = new GZIPInputStream(is);

                isr = new InputStreamReader(gzin, HTTP.UTF_8);

                in = new BufferedReader(isr);
                StringBuffer sb = new StringBuffer("");
                String line = "";
                while((line = in.readLine()) != null){
                    sb.append(line);
                }

                httpResult.setContent(sb.toString());
            } catch(Exception e){
                e.printStackTrace();
            }
            finally{
                Utils.close(isr);
                Utils.close(gzin);
            }
            return in;
        } else{
            return readContent(httpResult, response);
        }

    }

    private static String getEncoding(HttpResponse response){
        Header lastHeader = response.getLastHeader("Content-Encoding");
        String contentEncoding = "";

        if(lastHeader != null){
            contentEncoding = lastHeader.toString().toLowerCase();
        }

        return contentEncoding;
    }

    private static BufferedReader readContent(HttpResult httpResult, HttpResponse response) throws IOException{
        BufferedReader in;
        in = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
        StringBuffer sb = new StringBuffer("");
        String line = "";
        while((line = in.readLine()) != null){
            sb.append(line);
        }
        Log.d("DDD code:", "content =" + sb.toString());

        httpResult.setContent(sb.toString());
        return in;
    }

    public static HttpResult executeDelete(String url) throws Exception{
        HttpResult result = doDeleteImpl(url);
        Log.d("execute:", "url =" + url);
        if(result.getResCode() == HttpStatus.SC_UNAUTHORIZED){
            PushMethod method = PushMethod.getMethod();
            synchronized(HttpClientHelper.class){
                int ret = method.sendBinfInfo();
                checkResult(ret);
            }
            // bind 成功，刷新cookie，重新请求
            Log.d("DDD code:", "" + "doGetImpl again!");
            result = doDeleteImpl(url);
        }
        return result;
    }

    private static HttpResult doDeleteImpl(String url) throws Exception{
        int status = HttpStatus.SC_UNAUTHORIZED;
        HttpResult httpResult = new HttpResult();
        BufferedReader in = null;
        try{
            // 定义HttpClient
            HttpClient client = getHttpClient();
            // 实例化HTTP方法
            HttpDelete request = new HttpDelete();
            request.setURI(new URI(url));
            // 所有访问数据的请求，都必须加上token
            request.setHeader(ConstantValue.HEADER_TOKEN, DataUtils.getProp(JSONConstant.ACCESS_TOKEN));
            // 所有访问数据的请求，都必须加上source,区别网页和客户端
            request.setHeader(ConstantValue.HEADER_SOURCE, ConstantValue.SOURCE_ANDROID);
            request.setHeader(VERSION_CODE, String.valueOf(DataUtils.getVersionCode()));

            HttpResponse response = client.execute(request);
            status = response.getStatusLine().getStatusCode();

            Log.d("DDD code:", "" + status);
            if(status != 200){
                Log.w("WWW", "doDeleteImpl warning url=" + url);
            }
            in = readContentEx(httpResult, response);
            if(HttpClientHelper.isHttpRequestOK(status)){
                request.abort();
            }
        }
        finally{
            if(in != null){
                try{
                    in.close();// 最后要关闭BufferedReader
                } catch(Exception e){
                    e.printStackTrace();
                }
            }
        }
        httpResult.setResCode(status);
        return httpResult;
    }

    public static HttpResult executeGet(String url) throws Exception{
        HttpResult result = doGetImpl(url);
        if(result.getResCode() == HttpStatus.SC_UNAUTHORIZED){
            PushMethod method = PushMethod.getMethod();
            synchronized(HttpClientHelper.class){
                int ret = method.sendBinfInfo();
                checkResult(ret);
            }
            // bind 成功，刷新cookie，重新请求
            Log.d("DDD code:", "" + "doGetImpl again!");
            result = doGetImpl(url);
        }
        return result;
    }

    private static void checkResult(int ret){
        if(ret != EventType.BIND_SUCCESS){
            if(ret == EventType.BIND_FAILED){
                throw new InvalidTokenException("InvalidTokenException error");
            } else if(ret == EventType.PHONE_NUM_IS_ALREADY_LOGIN){
                throw new DuplicateLoginException("DuplicateLoginException error");
            } else if(ret == EventType.PHONE_NUM_IS_INVALID){
                throw new AccountExpiredException("AccountExpiredException error");
            } else{
                throw new BindFailException("BindFailException error");
            }
        }
    }

    private static HttpResult doGetImpl(String url) throws Exception{
        int status = HttpStatus.SC_UNAUTHORIZED;
        HttpResult httpResult = new HttpResult();
        BufferedReader in = null;
        try{
            // 定义HttpClient
            HttpClient client = getHttpClient();
            // 实例化HTTP方法
            HttpGet request = new HttpGet();
            request.setURI(new URI(url));
            // 所有访问数据的请求，都必须加上token
            request.setHeader(ConstantValue.HEADER_TOKEN, DataUtils.getProp(JSONConstant.ACCESS_TOKEN));
            // 所有访问数据的请求，都必须加上source,区别网页和客户端
            request.setHeader(ConstantValue.HEADER_SOURCE, ConstantValue.SOURCE_ANDROID);
            request.setHeader(VERSION_CODE, String.valueOf(DataUtils.getVersionCode()));

            // 测试gzip
            request.setHeader("Accept-Encoding", "gzip");

            HttpResponse response = client.execute(request);
            status = response.getStatusLine().getStatusCode();
            Log.d("DDD doGetImpl code:", "" + status + " vercode=" + DataUtils.getVersionCode());

            Log.d("", "doGetImpl url=" + url + " length=" + response.getEntity().getContentLength());

            if(status != 200){
                Log.w("WWW", "doGetImpl warning url=" + url);
            }
            in = readContentEx(httpResult, response);
            if(HttpClientHelper.isHttpRequestOK(status)){
                request.abort();
            }
        }
        finally{
            if(in != null){
                try{
                    in.close();// 最后要关闭BufferedReader
                } catch(Exception e){
                    e.printStackTrace();
                }
            }
        }
        httpResult.setResCode(status);
        return httpResult;
    }

    private static boolean isHttpRequestOK(int status){
        // 特殊情况，返回400也认为请求成功
        return (status == HttpStatus.SC_OK || status == HttpStatus.SC_BAD_REQUEST);
    }

    public static void downloadFile(String url, String savepath) throws Exception{
        File file = new File(savepath);
        file.createNewFile();
        OutputStream outputStream = new FileOutputStream(file);

        int status = HttpStatus.SC_UNAUTHORIZED;
        InputStream in = null;
        try{
            // 定义HttpClient
            HttpClient client = getHttpClient();
            // 实例化HTTP方法
            HttpGet request = new HttpGet();
            request.setURI(new URI(url));
            HttpResponse response = client.execute(request);
            status = response.getStatusLine().getStatusCode();
            Log.d("DDD code:", "" + status + " url=" + url);

            in = response.getEntity().getContent();
            byte buffer[] = new byte[1024];
            int length = -1;
            while((length = in.read(buffer, 0, 1024)) != -1){
                outputStream.write(buffer, 0, length);
            }
            if(HttpClientHelper.isHttpRequestOK(status)){
                request.abort();
            }
        }
        finally{
            Utils.close(in);
            Utils.close(outputStream);
        }
    }

    public static void downloadFile(String url, String savepath, DownloadFileListener listener){
        File file = new File(savepath);
        OutputStream outputStream = null;
        InputStream in = null;
        int downloadSize = 4096;
        try{
            file.createNewFile();
            outputStream = new FileOutputStream(file);

            int status = HttpStatus.SC_UNAUTHORIZED;
            // 定义HttpClient
            HttpClient client = getHttpClient();
            // 实例化HTTP方法
            HttpGet request = new HttpGet();
            request.setURI(new URI(url));
            HttpResponse response = client.execute(request);
            status = response.getStatusLine().getStatusCode();
            Log.d("DDD code:", "" + status + " url=" + url);

            HttpEntity entity = response.getEntity();
            listener.onBegain(entity.getContentLength());

            in = entity.getContent();
            byte buffer[] = new byte[downloadSize];
            int length = -1;

            while((length = in.read(buffer, 0, downloadSize)) != -1){
                outputStream.write(buffer, 0, length);
                listener.onDownloading(length);
            }
            if(HttpClientHelper.isHttpRequestOK(status)){
                request.abort();
            }

            listener.onComplete();
        } catch(Exception e){
            listener.onException(e);
            e.printStackTrace();
        }
        finally{
            Utils.close(in);
            Utils.close(outputStream);
        }
    }

    public static interface DownloadFileListener{
        public void onBegain(long contentLength);

        public void onComplete();

        public void onDownloading(int size);

        public void onException(Exception e);
    }
}

class SSLSocketFactoryEx extends SSLSocketFactory{

    SSLContext sslContext = SSLContext.getInstance("TLS");

    public SSLSocketFactoryEx(KeyStore truststore) throws NoSuchAlgorithmException, KeyManagementException,
            KeyStoreException, UnrecoverableKeyException{
        super(truststore);

        TrustManager tm = new X509TrustManager(){

            @Override
            public java.security.cert.X509Certificate[] getAcceptedIssuers(){
                return null;
            }

            @Override
            public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType)
                    throws java.security.cert.CertificateException{

            }

            @Override
            public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType)
                    throws java.security.cert.CertificateException{

            }
        };

        sslContext.init(null, new TrustManager[] { tm }, null);
    }

    @Override
    public Socket createSocket(Socket socket, String host, int port, boolean autoClose) throws IOException,
            UnknownHostException{
        return sslContext.getSocketFactory().createSocket(socket, host, port, autoClose);
    }

    @Override
    public Socket createSocket() throws IOException{
        return sslContext.getSocketFactory().createSocket();
    }
}