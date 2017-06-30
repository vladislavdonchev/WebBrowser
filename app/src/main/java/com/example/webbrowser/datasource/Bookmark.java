package com.example.webbrowser.datasource;


import java.util.Date;

/**
 * Created by username on 29/06/2017.
 */

public class Bookmark {
    private long ID;
    private String title;
    private String url;
    private Date timestamp;

    public long getID() {
        return ID;
    }

    public void setID(long ID) {
        this.ID = ID;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
}
