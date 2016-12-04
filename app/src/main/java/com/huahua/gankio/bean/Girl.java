package com.huahua.gankio.bean;

import java.io.Serializable;

/**
 * Created by Administrator on 2016/8/17.
 */

public class Girl implements Serializable{

    private String id;
    private String url;  // 图片地址
    private int page;   // 页数
    private String desc;

    public Girl(String id, String desc, String url) {
        this.id = id;
        this.desc = desc;
        this.url = url;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    @Override
    public String toString() {
        return "Girl{" +
                "id='" + id + '\'' +
                ", url='" + url + '\'' +
                ", page=" + page +
                ", desc='" + desc + '\'' +
                '}';
    }
}


















