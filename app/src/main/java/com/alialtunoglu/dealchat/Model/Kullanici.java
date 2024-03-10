package com.alialtunoglu.dealchat.Model;

public class Kullanici {
    String id,username,fullname,imageUrl,bio,durum;

    public Kullanici(String id, String username, String fullname, String imageUrl, String bio, String durum) {
        this.id = id;
        this.username = username;
        this.fullname = fullname;
        this.imageUrl = imageUrl;
        this.bio = bio;
        this.durum = durum;
    }

    public Kullanici() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getDurum() {
        return durum;
    }

    public void setDurum(String durum) {
        this.durum = durum;
    }
}
