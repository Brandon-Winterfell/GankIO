package com.huahua.gankio.application;

import android.app.Application;
import android.content.Context;
import android.support.annotation.NonNull;

import com.huahua.gankio.network.CacheInterceptor;
import com.huahua.gankio.picasso.OkHttp3Downloader;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.OkHttpClient;

/**
 * Created by Administrator on 2016/7/15.
 */
public class MyApplication extends Application {

    // 离线数据缓存文件夹名字
    private static final String OKHTTP3_CACHE = "okhttp3-cache";

    // picasso实例
    private static Picasso mPicasso = null;

    // okhttp3实例
    private static OkHttpClient myOkHttp3Client = null;


    @Override
    public void onCreate() {
        super.onCreate();

        CacheInterceptor cacheInterceptor = new CacheInterceptor(getApplicationContext());

        // 初始化okhttp3client
        myOkHttp3Client = new OkHttpClient
                .Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .cache(getCache())
                .addInterceptor(cacheInterceptor)
                .addNetworkInterceptor(cacheInterceptor)
                .build();

        // 初始化Picasso
        setUpPicasso();
    }

    // 默认okhttp构造没有添加缓存
    private void setUpPicasso() {
        Picasso picasso = new Picasso.Builder(this)
                .downloader(new OkHttp3Downloader(myOkHttp3Client))
                .build();
        Picasso.setSingletonInstance(picasso);
        // 红色：代表从网络下载的图片
        // 绿色：代表从内存中加载的图片
        // 深蓝色：代表从磁盘缓存加载的图片
        // picasso.setIndicatorsEnabled(true);
        mPicasso = picasso;
    }

    /**
     * 得到30M大小的Cache目录
     * @return
     */
    @NonNull
    private Cache getCache() {
        return new Cache(
                createExternalCacheDir(this, OKHTTP3_CACHE),
                30*1024*1024);
    }

    /**
     * 在内置SD卡，本应用包下创建私有的cache目录
     *
     * @param context
     * @param cacheName
     * @return
     */
    private File createExternalCacheDir(Context context, String cacheName) {
        File cacheFile = new File(context.getExternalCacheDir(), cacheName);

        if (!cacheFile.exists()) {
            //noinspection ResultOfMethodCallIgnored
            cacheFile.mkdirs();
        }

        return cacheFile;
    }

    /**
     * 给整个app调用的Picasso单例
     * @return
     */
    public static Picasso getmPicasso() {
        return mPicasso;
    }

    /**
     * 给整个app调用的OkHttpClient单例
     * @return
     */
    public static OkHttpClient getOkHttp3Client() {
        return myOkHttp3Client;
    }

}















