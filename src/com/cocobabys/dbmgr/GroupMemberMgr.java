package com.cocobabys.dbmgr;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.cocobabys.bean.IMExpandInfo;
import com.cocobabys.dbmgr.info.ChildInfo;
import com.cocobabys.dbmgr.info.GroupChildInfo;
import com.cocobabys.dbmgr.info.GroupParentInfo;
import com.cocobabys.dbmgr.info.RelationshipInfo;

public class GroupMemberMgr {
	private SqliteHelper dbHelper;

	public GroupMemberMgr(SqliteHelper dbHelper) {
		this.dbHelper = dbHelper;
	}

	void addGroupInfo(String content) throws Exception {
		JSONArray array = new JSONArray(content);
		List<GroupChildInfo> groupChildInfos = new ArrayList<>();
		List<GroupParentInfo> groupParentInfos = new ArrayList<>();
		List<RelationshipInfo> relationshipInfos = new ArrayList<>();

		for (int i = 0; i < array.length(); i++) {
			JSONObject jsonObject = array.getJSONObject(i);
			String parent = jsonObject.getString("parent");
			GroupParentInfo parentInfo = com.alibaba.fastjson.JSON.parseObject(parent, GroupParentInfo.class);

			String child = jsonObject.getString("child");
			GroupChildInfo childInfo = com.alibaba.fastjson.JSON.parseObject(child, GroupChildInfo.class);

			String relationship = jsonObject.getString("relationship");
			RelationshipInfo relationshipInfo = new RelationshipInfo();
			relationshipInfo.setChild_id(childInfo.getChild_id());
			relationshipInfo.setParent_id(parentInfo.getParent_id());
			relationshipInfo.setRelationship(relationship);

			groupChildInfos.add(childInfo);
			groupParentInfos.add(parentInfo);
			relationshipInfos.add(relationshipInfo);
		}

		addImpl(groupChildInfos, groupParentInfos, relationshipInfos);

	}

	private void addImpl(List<GroupChildInfo> groupChildInfos, List<GroupParentInfo> groupParentInfos,
			List<RelationshipInfo> relationshipInfos) {
		SQLiteDatabase writableDatabase = dbHelper.getWritableDatabase();
		writableDatabase.beginTransaction(); // 手动设置开始事务
		try {

			for (GroupChildInfo groupChildInfo : groupChildInfos) {
				ContentValues values = buildGroupChildInfo(groupChildInfo);
				writableDatabase.insertWithOnConflict(SqliteHelper.GROUP_CHILDREN_INFO_TAB, null, values,
						SQLiteDatabase.CONFLICT_REPLACE);
			}

			for (GroupParentInfo groupParentInfo : groupParentInfos) {
				ContentValues values = buildGroupParentInfo(groupParentInfo);
				writableDatabase.insertWithOnConflict(SqliteHelper.GROUP_PARENT_INFO_TAB, null, values,
						SQLiteDatabase.CONFLICT_REPLACE);
			}

			for (RelationshipInfo relationshipInfo : relationshipInfos) {
				ContentValues values = buildRelationship(relationshipInfo);
				writableDatabase.insertWithOnConflict(SqliteHelper.RELATIONSHIP_INFO_TAB, null, values,
						SQLiteDatabase.CONFLICT_REPLACE);
			}

			// 数据插入操作循环
			writableDatabase.setTransactionSuccessful(); // 设置事务处理成功，不设置会自动回滚不提交
		} finally {
			writableDatabase.endTransaction(); // 处理完成
		}
	}

	private ContentValues buildGroupChildInfo(GroupChildInfo info) {
		ContentValues values = new ContentValues();
		values.put(GroupChildInfo.CHILD_ID, info.getChild_id());
		values.put(GroupChildInfo.CLASS_ID, info.getClass_id());
		values.put(GroupChildInfo.CLASS_NAME, info.getClass_name());
		values.put(GroupChildInfo.GENDER, info.getGender());
		values.put(GroupChildInfo.INTERNAL_ID, info.getId());
		values.put(GroupChildInfo.NAME, info.getName());
		values.put(GroupChildInfo.NICK, info.getNick());
		values.put(GroupChildInfo.PORTRAIT, info.getPortrait());
		values.put(GroupChildInfo.TIMESTAMP, info.getTimestamp());
		return values;
	}

	private ContentValues buildGroupParentInfo(GroupParentInfo info) {
		ContentValues values = new ContentValues();
		values.put(GroupParentInfo.INTERNAL_ID, info.getId());
		values.put(GroupParentInfo.PARENT_ID, info.getParent_id());
		values.put(GroupParentInfo.PARENT_NAME, info.getName());
		values.put(GroupParentInfo.PHONE, info.getPhone());
		values.put(GroupParentInfo.PORTRAIT, info.getPortrait());
		values.put(GroupParentInfo.TIMESTAMP, info.getTimestamp());
		return values;
	}

	private ContentValues buildRelationship(RelationshipInfo info) {
		ContentValues values = new ContentValues();
		values.put(RelationshipInfo.CHILD_ID, info.getChild_id());
		values.put(RelationshipInfo.PARENT_ID, info.getParent_id());
		values.put(RelationshipInfo.RELATIONSHIP, info.getRelationship());
		return values;
	}

	List<IMExpandInfo> getClassMemberInfo(String classid) {
		List<IMExpandInfo> infos = new ArrayList<>();
		List<GroupChildInfo> GroupChildInfos = getGroupChildInfoByClassid(classid);

		for (GroupChildInfo groupChildInfo : GroupChildInfos) {
			IMExpandInfo imExpandInfo = new IMExpandInfo();
			imExpandInfo.setChildInfo(groupChildInfo);

			List<RelationshipInfo> relationshipInfos = getRelationshipInfoByChildid(groupChildInfo.getChild_id());
			List<GroupParentInfo> parentInfos = new ArrayList<>();

			for (RelationshipInfo relationshipInfo : relationshipInfos) {
				GroupParentInfo groupParentInfo = getGroupParentInfo(relationshipInfo.getParent_id());
				if (groupParentInfo != null) {
					groupParentInfo.setRelationship(relationshipInfo.getRelationship());
					parentInfos.add(groupParentInfo);
				}
			}

			imExpandInfo.setGroupParentInfoList(parentInfos);
			infos.add(imExpandInfo);
		}

		return infos;
	}

	List<GroupChildInfo> getGroupChildInfoByClassid(String classid) {
		List<GroupChildInfo> list = new ArrayList<GroupChildInfo>();
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		Cursor cursor = db.rawQuery("SELECT * FROM " + SqliteHelper.GROUP_CHILDREN_INFO_TAB + " WHERE "
				+ GroupChildInfo.CLASS_ID + " ='" + classid + "'", null);
		try {
			cursor.moveToFirst();
			while (!cursor.isAfterLast() && (cursor.getString(1) != null)) {
				GroupChildInfo info = getGroupChildInfoByCursor(cursor);
				list.add(info);
				cursor.moveToNext();
			}
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}

		return list;
	}

	List<RelationshipInfo> getRelationshipInfoByChildid(String childid) {
		List<RelationshipInfo> list = new ArrayList<RelationshipInfo>();
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		Cursor cursor = db.rawQuery("SELECT * FROM " + SqliteHelper.RELATIONSHIP_INFO_TAB + " WHERE "
				+ RelationshipInfo.CHILD_ID + " ='" + childid + "'", null);
		try {
			cursor.moveToFirst();
			while (!cursor.isAfterLast() && (cursor.getString(1) != null)) {
				RelationshipInfo info = getRelationshipInfoByCursor(cursor);
				list.add(info);
				cursor.moveToNext();
			}
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}

		return list;
	}

	GroupParentInfo getGroupParentInfo(String parentid) {
		GroupParentInfo groupParentInfo = null;
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		Cursor cursor = db.rawQuery("SELECT * FROM " + SqliteHelper.GROUP_PARENT_INFO_TAB + " WHERE "
				+ GroupParentInfo.PARENT_ID + " ='" + parentid + "'", null);
		try {
			cursor.moveToFirst();
			while (!cursor.isAfterLast() && (cursor.getString(0) != null)) {
				groupParentInfo = getGroupParentInfoByCursor(cursor);
				break;
			}
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}

		return groupParentInfo;
	}

	List<GroupParentInfo> getAllGroupParentsInfo() {
		List<GroupParentInfo> list = new ArrayList<>();
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		Cursor cursor = db.rawQuery("SELECT * FROM " + SqliteHelper.GROUP_PARENT_INFO_TAB, null);
		try {
			cursor.moveToFirst();
			while (!cursor.isAfterLast() && (cursor.getString(0) != null)) {
				GroupParentInfo groupParentInfo = getGroupParentInfoByCursor(cursor);
				list.add(groupParentInfo);
				cursor.moveToNext();
			}
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}

		return list;
	}

	private GroupParentInfo getGroupParentInfoByCursor(Cursor cursor) {
		GroupParentInfo info = new GroupParentInfo();

		info.setLocal_id(cursor.getInt(0));
		info.setName(cursor.getString(1));
		info.setId(cursor.getInt(2));
		info.setParent_id(cursor.getString(3));
		info.setPhone(cursor.getString(4));
		info.setPortrait(cursor.getString(5));
		info.setTimestamp(cursor.getLong(6));

		return info;
	}

	private GroupChildInfo getGroupChildInfoByCursor(Cursor cursor) {
		GroupChildInfo info = new GroupChildInfo();

		info.setLocal_id(cursor.getInt(0));
		info.setName(cursor.getString(1));
		info.setClass_name(cursor.getString(2));
		info.setPortrait(cursor.getString(3));
		info.setTimestamp(cursor.getLong(4));
		info.setClass_id(cursor.getString(5));
		info.setNick(cursor.getString(6));
		info.setGender(cursor.getInt(7));
		info.setId(cursor.getInt(8));
		info.setChild_id(cursor.getString(9));
		return info;
	}

	private RelationshipInfo getRelationshipInfoByCursor(Cursor cursor) {
		RelationshipInfo info = new RelationshipInfo();

		info.setId(cursor.getInt(0));
		info.setChild_id(cursor.getString(1));
		info.setParent_id(cursor.getString(2));
		info.setRelationship(cursor.getString(3));
		return info;
	}
}
