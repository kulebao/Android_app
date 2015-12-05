package com.cocobabys.dbmgr.info;

import android.text.TextUtils;

public class GroupChildInfo{

    public static final String ID          = "_id";
    public static final String NICK        = "child_nick_name";
    public static final String PORTRAIT    = "portrait";
    public static final String CHILD_ID    = "child_id";
    public static final String CLASS_ID    = "class_id";
    public static final String CLASS_NAME  = "class_name";
    public static final String NAME        = "name";
    public static final String GENDER      = "gender";
    public static final String TIMESTAMP   = "timestamp";
    public static final String INTERNAL_ID = "internal_id";

    private long               timestamp   = 0;
    // 本地数据库id
    private int                local_id    = 0;
    // 服务器id
    private int                id          = 0;
    private String             class_id    = "";
    private String             class_name  = "";

    private String             portrait    = "";
    private int                gender      = 0;
    private String             nick        = "";
    // 旧版服务器id
    private String             child_id    = "";
    private String             name        = "";

    public long getTimestamp(){
        return timestamp;
    }

    public void setTimestamp(long timestamp){
        this.timestamp = timestamp;
    }

    public int getLocal_id(){
        return local_id;
    }

    public void setLocal_id(int local_id){
        this.local_id = local_id;
    }

    public int getId(){
        return id;
    }

    public void setId(int id){
        this.id = id;
    }

    public String getClass_id(){
        return class_id;
    }

    public void setClass_id(String class_id){
        this.class_id = class_id;
    }

    public String getClass_name(){
        return class_name;
    }

    public void setClass_name(String class_name){
        this.class_name = class_name;
    }

    public String getPortrait(){
        return portrait;
    }

    public void setPortrait(String portrait){
        this.portrait = portrait;
    }

    public int getGender(){
        return gender;
    }

    public void setGender(int gender){
        this.gender = gender;
    }

    public String getNick(){
        return nick;
    }

    public void setNick(String nick){
        this.nick = nick;
    }

    public String getChild_id(){
        return child_id;
    }

    public void setChild_id(String child_id){
        this.child_id = child_id;
    }

    public String getName(){
        return name;
    }

    public void setName(String name){
        this.name = name;
    }

    public String getDisplayName(){
        return TextUtils.isEmpty(nick) ? name : nick;
    }

    @Override
    public int hashCode(){
        final int prime = 31;
        int result = 1;
        result = prime * result + ((child_id == null) ? 0 : child_id.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj){
        if(this == obj)
            return true;
        if(obj == null)
            return false;
        if(getClass() != obj.getClass())
            return false;
        GroupChildInfo other = (GroupChildInfo)obj;
        if(child_id == null){
            if(other.child_id != null)
                return false;
        } else if(!child_id.equals(other.child_id))
            return false;
        return true;
    }

}
