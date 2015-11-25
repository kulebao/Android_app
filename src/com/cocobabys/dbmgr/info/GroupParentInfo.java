package com.cocobabys.dbmgr.info;

import com.cocobabys.dbmgr.DataMgr;

public class GroupParentInfo{
    public static final String ID          = "_id";
    // 服务器端的id
    public static String       PARENT_ID   = "parent_id";
    public static String       PARENT_NAME = "name";
    public static String       PHONE       = "phone";
    public static String       PORTRAIT    = "portrait";
    public static String       TIMESTAMP   = "timestamp";

    public static String       INTERNAL_ID = "internal_id";
    private String             parent_id   = "";
    private String             name        = "";
    private String             phone       = "";
    private String             portrait    = "";
    private long               timestamp   = -1;
    // 本地数据库id
    private int                local_id    = 0;
    // 服务器端内部id
    private int                id          = 0;

    private String             relationship;

    public String getRelationship(){
        return relationship;
    }

    public void setRelationship(String relationship){
        this.relationship = relationship;
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

    public String getParent_id(){
        return parent_id;
    }

    public void setParent_id(String parent_id){
        this.parent_id = parent_id;
    }

    public String getName(){
        return name;
    }

    public void setName(String name){
        this.name = name;
    }

    public String getPhone(){
        return phone;
    }

    public void setPhone(String phone){
        this.phone = phone;
    }

    public String getPortrait(){
        return portrait;
    }

    public void setPortrait(String portrait){
        this.portrait = portrait;
    }

    public long getTimestamp(){
        return timestamp;
    }

    public void setTimestamp(long timestamp){
        this.timestamp = timestamp;
    }

    @Override
    public int hashCode(){
        final int prime = 31;
        int result = 1;
        result = prime * result + ((parent_id == null) ? 0 : parent_id.hashCode());
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
        GroupParentInfo other = (GroupParentInfo)obj;
        if(parent_id == null){
            if(other.parent_id != null)
                return false;
        } else if(!parent_id.equals(other.parent_id))
            return false;
        return true;
    }

    // 根据约定，拼接出融云im对应的userid
    public String getIMUserid(){
        String schoolID = DataMgr.getInstance().getSchoolID();
        String imuserid = "p_" + schoolID + "_Some(" + id + ")_" + phone;
        return imuserid;
    }

    @Override
    public String toString(){
        return "ParentInfo [parent_id=" + parent_id + ", name=" + name + ", phone=" + phone + ", portrait=" + portrait
                + ", internal_id=" + id;
    }

}
