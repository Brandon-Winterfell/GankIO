package com.huahua.gankio.network;

import android.os.Handler;
import android.os.Looper;

import com.huahua.gankio.application.MyApplication;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * OKhttp工具封装类
 *
 * 参考：http://www.cnblogs.com/huolongluo/p/5924976.html
 * Created by Administrator on 2016/10/10.
 */
public class OkhttpManager {

    private OkHttpClient mClient;
    private static OkhttpManager okhttpManager;
    // 主线程的handler，在主线程进行更新UI的操作
    private Handler mHandler;

    /**
     * 单例模式 OkhttpManager实例
     * @return
     */
    private static OkhttpManager getInstance() {
        if(okhttpManager == null) {
            okhttpManager = new OkhttpManager();
        }
        return okhttpManager;
    }

    /**
     * 构造方法
     */
    private OkhttpManager() {
        // 得到整个app的单例OKHttp3的实例
        mClient = MyApplication.getOkHttp3Client();
        // 利用主线程(UI线程)的Looper将更新UI的操作切换到主线程执行 因为子线程不能更新UI
        mHandler = new Handler(Looper.getMainLooper());
    }


    //************* 内部逻辑处理方法 *************/

    /**
     * 同步的get方法
     * @param url
     * @return  response 二进制类型的数据 没做特别的处理
     * @throws IOException
     */
    private Response p_getSync(String url) throws IOException {
        Request request = new Request.Builder().url(url).build();
        Response response = mClient.newCall(request).execute();
        return response;
    }

    /**
     * 同步的Get方法 返回String类型的数据
     * @param url
     * @return
     * @throws IOException
     */
    private String p_getSyncAsString(String url) throws IOException {
        return p_getSync(url).body().string();
    }

    /**
     * 异步的Get方法
     * @param url
     * @param dataCallBack
     */
    private void p_getAsync(String url, final IDataCallBack dataCallBack) {
        final Request request = new Request.Builder().url(url).build();
        mClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                deliverDataFailure(request, e, dataCallBack);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                String result = response.body().string();
                deliverDataSuccess(result, dataCallBack);
            }
        });
    }

    private void p_postAsync(String url, Map<String, String> params,
                             final IDataCallBack dataCallBack) {
        // 构造请求Body
        RequestBody requestBody = null;
        if (params == null) {
            params = new HashMap<String, String>();
        }
        FormBody.Builder builder = new FormBody.Builder();
        for(Map.Entry<String, String> entry : params.entrySet()) {
            String key = entry.getKey();
            String value = null;
            if (entry.getValue() == null) {
                value = "";
            } else {
                value = entry.getValue();
            }
            builder.add(key, value);
        }
        requestBody = builder.build();

        //
        final Request request = new Request.Builder().url(url).post(requestBody).build();
        mClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                deliverDataFailure(request, e, dataCallBack);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String result = response.body().string();
                deliverDataSuccess(result, dataCallBack);
            }
        });
    }

    //************** 数据分发的方法 ***************/

    private void deliverDataFailure(final Request request, final IOException e, final IDataCallBack dataCallBack) {
        mHandler.post(new Runnable() { // 切换到主线程进行更新UI的操作
            @Override
            public void run() {
                if (dataCallBack != null) {
                    dataCallBack.requestFailure(request, e);
                }
            }
        });
    }

    private void deliverDataSuccess(final String result, final IDataCallBack dataCallBack) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if(dataCallBack != null) {
                    dataCallBack.requestSuccess(result);
                }
            }
        });
    }

    //************* 对外公布的方法  ***************/

    /**
     * 同步GET，返回Response类型数据
     *
     * @param url
     * @return
     * @throws IOException
     */
    public static Response getSync(String url) throws IOException {
        return getInstance().p_getSync(url);
    }

    /**
     * 同步GET，返回String类型数据
     * 和上面getSync方法只是返回的数据类型不同而已
     *
     * @param url
     * @return
     * @throws IOException
     */
    public static String getSyncAsString(String url) throws IOException {
        return getInstance().p_getSyncAsString(url);
    }

    /**
     * 异步GET 调用方法
     *
     * @param url
     * @param dataCallBack
     */
    public static void getAsync(String url, IDataCallBack dataCallBack) {
        getInstance().p_getAsync(url, dataCallBack);
    }

    /**
     * POST提交表单 调用方法
     *
     * @param url
     * @param params
     * @param dataCallBack
     */
    public static void postAsync(String url, Map<String, String> params,
                                 IDataCallBack dataCallBack) {
        getInstance().p_postAsync(url, params, dataCallBack);
    }
}

























