package com.huahua.gankio.bean;

import java.io.Serializable;

/**
 * Created by Administrator on 2016/9/18.
 */

public class AndroidBean implements Serializable{
    private String id;
    private String url;  // 图片地址
    private int page;   // 页数
    private String desc;
    private String publishedAt;
    private String who;

    public AndroidBean(String id, String url, String desc, String publishedAt, String who) {
        this.id = id;
        this.url = url;
        this.desc = desc;
        this.publishedAt = publishedAt;
        this.who = who;
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

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getPublishedAt() {
        return publishedAt.substring(0, 19).replace("T", " ");
    }

    public void setPublishedAt(String publishedAt) {
        this.publishedAt = publishedAt;
    }

    public String getWho() {
        return who;
    }

    public void setWho(String who) {
        this.who = who;
    }

    @Override
    public String toString() {
        return "AndroidBean{" +
                "id='" + id + '\'' +
                ", url='" + url + '\'' +
                ", desc='" + desc + '\'' +
                ", publishedAt='" + publishedAt + '\'' +
                ", who='" + who + '\'' +
                '}';
    }
}



















