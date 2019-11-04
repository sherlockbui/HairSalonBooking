package com.example.hairsalonbooking.Model;

public class MyNotification {
    private String  idBarber, title, content;
    private boolean read;

    public MyNotification() {
    }

    public String getIdBarber() {
        return idBarber;
    }

    public void setIdBarber(String idBarber) {
        this.idBarber = idBarber;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

}
