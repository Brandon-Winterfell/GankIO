package com.huahua.gankio.network;

import java.io.IOException;

import okhttp3.Request;

/**
 * 数据回调接口
 *
 * Created by Administrator on 2016/10/10.
 */
public interface IDataCallBack {

    void requestFailure(Request request, IOException e);

    void requestSuccess(String result);

}
