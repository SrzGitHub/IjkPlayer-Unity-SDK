package com.letinvr.play.model;

import java.io.Serializable;

/**
 * Created by KID on 2018/1/16.
 */

public class VideoModel implements Serializable {
    //视频id
    private int id;
    //大片名字
    private String name;
    //封面图片
    private String imgUrl;
    //视频地址
    private String videoUrl;

    public VideoModel(int id, String name, String imgUrl, String videoUrl) {
        this.id = id;
        this.name = name;
        this.imgUrl = imgUrl;
        this.videoUrl = videoUrl;
    }
    public VideoModel(){}

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }
}
