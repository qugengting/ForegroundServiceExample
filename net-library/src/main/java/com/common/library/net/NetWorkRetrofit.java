package com.common.library.net;

import android.content.Context;

import com.google.gson.GsonBuilder;
import com.common.library.net.api.ServiceAPI;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.X509TrustManager;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by xuruibin on 2020/11/6.
 * 描述：
 */

public class NetWorkRetrofit {
    public static final String BASE_URL = "https://www.mxnzp.com/api/";

    private static OkHttpClient.Builder httpClient;

    private static Retrofit.Builder builder = new Retrofit.Builder().baseUrl(BASE_URL).addCallAdapterFactory(
            RxJavaCallAdapterFactory.create()).addConverterFactory(GsonConverterFactory.create(new GsonBuilder().setLenient().create()));

    private NetWorkRetrofit() {

    }

    public void init(Context context) {
        httpClient = new OkHttpClient.Builder()
                .retryOnConnectionFailure(true)
                .connectTimeout(1000 * 3, TimeUnit.MILLISECONDS);
    }

    private static class geneInstance {
        private static final NetWorkRetrofit INSTANCE = new NetWorkRetrofit();
    }

    public static NetWorkRetrofit getInstance() {
        return geneInstance.INSTANCE;
    }

    public ServiceAPI getServiceAPI() {
        setNoCertificates();
        Retrofit retrofit = builder.client(httpClient.build()).build();
        return retrofit.create(ServiceAPI.class);
    }

    public static void setNoCertificates() {
        try {
            SSLContext sc = SSLContext.getInstance("SSL");
            X509TrustManager trustManager = new X509TrustManager() {
                @Override
                public void checkClientTrusted(X509Certificate[] chain, String authType) {

                }

                @Override
                public void checkServerTrusted(X509Certificate[] chain, String authType) {

                }

                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }
            };
            X509TrustManager[] trustManagers = new X509TrustManager[]{
                    trustManager
            };
            sc.init(null, trustManagers, new SecureRandom());
            httpClient.sslSocketFactory(sc.getSocketFactory(), trustManager).hostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            }).build();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }
    }
}
