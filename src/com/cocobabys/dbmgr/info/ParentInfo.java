package com.cocobabys.dbmgr.info;

import java.io.File;

import org.json.JSONException;
import org.json.JSONObject;

import com.cocobabys.bean.RelationInfo;
import com.cocobabys.dbmgr.DataMgr;
import com.cocobabys.utils.DataUtils;
import com.cocobabys.utils.Utils;

import android.text.TextUtils;
import android.util.Log;

public class ParentInfo{
    private static final String PARENT_HEAD_ICON = "parent_head_icon";

    public static final String  ID               = "_id";
    // 服务器端的id
    public static String        PARENT_ID        = "parent_id";
    public static String        PARENT_NAME      = "name";
    public static String        PHONE            = "phone";
    public static String        PORTRAIT         = "portrait";
    public static String        MEMBER_STATUS    = "member_status";
    public static String        CARD_NUMBER      = "card";
    public static String        RELATIONSHIP     = "relationship";
    public static String        TIMESTAMP        = "timestamp";
    public static String        INTERNAL_ID      = "internal_id";

    private String              parent_id        = "";
    private String              name             = "";
    private String              phone            = "";
    private String              portrait         = "";
    private String              card             = "";
    private String              relationship     = "";
    private int                 member_status    = -1;
    // 服务器端内部id
    private int                 internal_id      = -1;
    private long                timestamp        = -1;

    private int                 id               = 0;

    public int getId(){
        return id;
    }

    public void setId(int id){
        this.id = id;
    }

    public int getInternal_id(){
        return internal_id;
    }

    public void setInternal_id(int internal_id){
        this.internal_id = internal_id;
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

    public String getCard(){
        return card;
    }

    public void setCard(String card){
        this.card = card;
    }

    public int getMember_status(){
        return member_status;
    }

    public void setMember_status(int member_status){
        this.member_status = member_status;
    }

    public String getPortrait(){
        return portrait;
    }

    public void setPortrait(String portrait){
        this.portrait = portrait;
    }

    public String getRelationship(){
        return relationship;
    }

    public void setRelationship(String relationship){
        this.relationship = relationship;
    }

    public long getTimestamp(){
        return timestamp;
    }

    public void setTimestamp(long timestamp){
        this.timestamp = timestamp;
    }

    public static String getParentLocalIconPath(String parentid){
        String dir = Utils.getSDCardPicRootPath() + File.separator + PARENT_HEAD_ICON + File.separator;

        Utils.mkDirs(dir);
        String url = dir + parentid;
        Log.d("DDD", "getParentLocalIconPath url=" + url);
        return url;
    }

    public static ParentInfo parseFromRelationship(JSONObject jsonObject) throws JSONException{
        ParentInfo info = new ParentInfo();

        JSONObject parentJson = jsonObject.getJSONObject("parent");

        info.setInternal_id(parentJson.getInt("id"));
        info.setCard(jsonObject.getString(CARD_NUMBER));
        info.setMember_status(parentJson.getInt(MEMBER_STATUS));
        info.setParent_id(parentJson.getString(PARENT_ID));
        info.setName(parentJson.getString(PARENT_NAME));
        info.setPhone(parentJson.getString(PHONE));
        info.setPortrait(parentJson.getString(PORTRAIT));
        info.setTimestamp(parentJson.getLong(TIMESTAMP));
        info.setRelationship(jsonObject.getString(RELATIONSHIP));
        return info;
    }

    public static ParentInfo parseFromSender(JSONObject parentJson) throws JSONException{
        ParentInfo info = new ParentInfo();

        info.setInternal_id(parentJson.getInt("uid"));
        info.setMember_status(parentJson.getInt(MEMBER_STATUS));
        info.setParent_id(parentJson.getString(PARENT_ID));
        info.setName(parentJson.getString(PARENT_NAME));
        info.setPhone(parentJson.getString(PHONE));
        info.setPortrait(parentJson.getString(PORTRAIT));
        info.setTimestamp(parentJson.getLong(TIMESTAMP));
        return info;
    }

    // public static String toJsonStr(ParentInfo info) throws JSONException{
    // JSONObject object = new JSONObject();
    //
    // object.put("birthday", "");
    // object.put("company", "");
    // object.put("gender",0);
    // object.put(PARENT_ID,info.getParent_id());
    // object.put(MEMBER_STATUS,info.getMember_status());
    // object.put(PARENT_NAME,info.getName());
    // object.put(PHONE,info.getPhone());
    // object.put(PORTRAIT,info.getPortrait());
    // object.put("school_id",DataMgr.getInstance().getSchoolID());
    //
    //
    // info.setMember_status(parentJson.getInt(MEMBER_STATUS));
    // info.setParent_id(parentJson.getString(PARENT_ID));
    // info.setName(parentJson.getString(PARENT_NAME));
    // info.setPhone(parentJson.getString(PHONE));
    // info.setPortrait(parentJson.getString(PORTRAIT));
    // info.setTimestamp(parentJson.getLong(TIMESTAMP));
    // return info;
    // }

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
        ParentInfo other = (ParentInfo)obj;
        if(parent_id == null){
            if(other.parent_id != null)
                return false;
        } else if(!parent_id.equals(other.parent_id))
            return false;
        return true;
    }

    // 不同的小孩关系可能不一样，有可能是爸爸，叔叔
    public String getFixedRelationShip(String childid){
        RelationInfo relationInfo = DataUtils.getRelationInfo(childid);

        String relation = relationInfo.getRelationship();

        if(TextUtils.isEmpty(relation)){
            relation = relationship;
        }
        return relation;
    }

    // 根据约定，拼接出融云im对应的userid
    public String getIMUserid(){
        String schoolID = DataMgr.getInstance().getSchoolID();
        String id = "p_" + schoolID + "_Some(" + internal_id + ")_" + phone;
        return id;
    }

    @Override
    public String toString(){
        return "ParentInfo [parent_id=" + parent_id + ", name=" + name + ", phone=" + phone + ", portrait=" + portrait
                + ", internal_id=" + internal_id + ", id=" + id + "]";
    }

}
