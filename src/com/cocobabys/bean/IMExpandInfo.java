package com.cocobabys.bean;

import java.util.ArrayList;
import java.util.List;

import com.cocobabys.dbmgr.info.GroupChildInfo;
import com.cocobabys.dbmgr.info.GroupParentInfo;

public class IMExpandInfo{

    private GroupChildInfo        childInfo;
    private List<GroupParentInfo> groupParentInfoList = new ArrayList<>();

    public GroupChildInfo getChildInfo(){
        return childInfo;
    }

    public void setChildInfo(GroupChildInfo childInfo){
        this.childInfo = childInfo;
    }

    public List<GroupParentInfo> getGroupParentInfoList(){
        return groupParentInfoList;
    }

    public void setGroupParentInfoList(List<GroupParentInfo> groupParentInfoList){
        this.groupParentInfoList = groupParentInfoList;
    }

}
