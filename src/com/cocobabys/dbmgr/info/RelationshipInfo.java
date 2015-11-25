package com.cocobabys.dbmgr.info;

public class RelationshipInfo{
    public static final String ID           = "_id";

    public static String       PARENT_ID    = "parent_id";
    public static String       CHILD_ID     = "child_id";
    public static String       RELATIONSHIP = "relationship";

    private String             relationship = "";
    private String             parent_id    = "";
    private String             child_id     = "";
    private int                id           = 0;

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

    public String getChild_id(){
        return child_id;
    }

    public void setChild_id(String child_id){
        this.child_id = child_id;
    }

    public String getRelationship(){
        return relationship;
    }

    public void setRelationship(String relationship){
        this.relationship = relationship;
    }

}
