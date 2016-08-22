package com.abcmmee.tieba.utils;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * 封装OkHttp，用重载。
 * 这里代码有点多，如果支持Java也支持可选参数的话，就可以缩写成一个方法
 * 用@Nullable注释的话表示有点强迫，因为如果传入参数不需要的话要传入null值
 * 不知道还有没更好的办法-_-||
 */
public class HttpUtils {

    /**
     * 发送GET请求
     */
    public static Response sendGetRequest(String url) throws IOException {
        Request request = getRequest(url, null, null);
        OkHttpClient client = getOkHttpClient(null);
        return client.newCall(request).execute();
    }

    /**
     * 发送GET请求
     */
    public static Response sendGetRequest(String url, MyCookieJar cookieJar) throws IOException {
        Request request = getRequest(url, null, null);
        OkHttpClient client = getOkHttpClient(cookieJar);
        return client.newCall(request).execute();
    }

    /**
     * 发送POST请求
     */
    public static Response sendPostRequest(String url, MyCookieJar cookieJar, RequestBody requestBody) throws IOException {
        Request request = getRequest(url, requestBody, null);
        OkHttpClient client = getOkHttpClient(cookieJar);
        return client.newCall(request).execute();
    }

    /**
     * 发送POST请求
     */
    public static Response sendPostRequest(String url, MyCookieJar cookieJar, RequestBody requestBody, Headers headers) throws IOException {
        Request request = getRequest(url, requestBody, headers);
        OkHttpClient client = getOkHttpClient(cookieJar);
        return client.newCall(request).execute();
    }


    private static Request getRequest(String url, RequestBody requestBody, Headers headers) {
        Request.Builder requestBuilder = new Request.Builder();
        requestBuilder.url(url);

        // OkHttp默认请求头中Accept-Encoding使用gzip，这里改成identity
        requestBuilder.header("Accept-Encoding", "identity");

        if (requestBody != null)
            requestBuilder.post(requestBody);
        if (headers != null)
            requestBuilder.headers(headers);

        return requestBuilder.build();
    }

    private static OkHttpClient getOkHttpClient(MyCookieJar cookieJar) {
        OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder();
        if (cookieJar != null)
            clientBuilder.cookieJar(cookieJar);

        // 调试时忽略https证书认证
        try {
            X509TrustManager x509 = new X509TrustManager() {
                @Override
                public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {

                }

                @Override
                public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {

                }

                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }
            };
            TrustManager[] tm = new TrustManager[]{x509};
            SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, tm, new SecureRandom());
            SSLSocketFactory factory = sslContext.getSocketFactory();
            clientBuilder.sslSocketFactory(factory, x509);
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            e.printStackTrace();
        }

        clientBuilder.hostnameVerifier(new HostnameVerifier() {
            @Override
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        });

        return clientBuilder.build();
    }
}
