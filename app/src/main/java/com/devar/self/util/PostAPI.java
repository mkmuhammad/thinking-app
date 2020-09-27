package com.devar.self.util;

import android.app.Application;

public class PostAPI extends Application {
    private String username;
    private String userId;
    private static PostAPI instance;

    public static PostAPI getInstance(){
        //this part makes this class a singleton
        if (instance  == null){
            instance = new PostAPI();
        }
        return instance;
    }

    public PostAPI() {
    }



    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
