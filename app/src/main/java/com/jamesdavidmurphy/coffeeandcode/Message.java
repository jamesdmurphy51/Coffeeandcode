package com.jamesdavidmurphy.coffeeandcode;

public class Message {

    private String userName, content;
    //private boolean left;

    public Message(){
    }

    public Message(String userName, String content) {
        this.userName = userName;
        this.content=content;
        //this.left=left;
    }

    public String getUserName() {return userName;}
    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getContent() {return content;}
    public void setContent(String content) {
        this.content = content;
    }

    //public boolean isLeft() {return left;}
    //public void setLeft(boolean left) {this.left = left;}
}
