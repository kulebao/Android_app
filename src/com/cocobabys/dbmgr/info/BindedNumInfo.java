package com.cocobabys.dbmgr.info;

public class BindedNumInfo {
    public static final String ID = "_id";
    public static final String PHONE_NUM = "phone_num";
    public static final String NICKNAME = "nickname";

    private String phone_num = "";
    private String nickname = "";
    private int id = 0;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPhone_num() {
        return phone_num;
    }

    public void setPhone_num(String phone_num) {
        this.phone_num = phone_num;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    @Override
    public String toString() {
        return phone_num + "\n" + nickname;
    }
}
