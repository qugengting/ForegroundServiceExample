package com.common.library.net.api;

import com.common.library.net.bean.Qrcode;

import retrofit2.http.GET;
import retrofit2.http.Query;
import rx.Observable;


public interface ServiceAPI {
//    app_id:r7nbnvfakxikpfrk   app_secret:NUs3UXFBNkpiaDlLTkUyUjJJQmlCQT09
//    qrcode/create/single?content=你好&size=500&type=0
    @GET("qrcode/create/single")
    Observable<Qrcode> qrcode(@Query("app_id") String app_id, @Query("app_secret") String app_secret,
                              @Query("content") String content, @Query("size") String size,
                              @Query("type") String type);

}
