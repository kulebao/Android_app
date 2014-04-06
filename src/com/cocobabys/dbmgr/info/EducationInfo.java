package com.cocobabys.dbmgr.info;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class EducationInfo {
	private static final int DEFAULT_RANK = 3;
	public static final String ID = "_id";
	public static final String SERVER_ID = "server_id";
	public static final String TIMESTAMP = "timestamp";
	public static final String PUBLISHER = "publisher";
	public static final String COMMENTS = "comments";
	public static final String EMOTION = "emotion";
	public static final String DINING = "dining";
	public static final String REST = "rest";
	public static final String ACTIVITY = "activity";
	public static final String EXERCISE = "exercise";
	public static final String SELF_CARE = "self_care";
	public static final String MANNER = "manner";
	public static final String GAME = "game";
	public static final String CHILD_ID = "child_id";

	private int id = 0;
	private int server_id = 0;

	private String publisher = "";
	private String comments = "";
	private int emotion = DEFAULT_RANK;
	private int dining = DEFAULT_RANK;
	private int rest = DEFAULT_RANK;
	private int activity = DEFAULT_RANK;
	private int exercise = DEFAULT_RANK;
	private int self_care = DEFAULT_RANK;
	private int manner = DEFAULT_RANK;
	private int game = DEFAULT_RANK;
	private String child_id = "";
	private long timestamp = 0;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getServer_id() {
		return server_id;
	}

	public void setServer_id(int server_id) {
		this.server_id = server_id;
	}

	public String getPublisher() {
		return publisher;
	}

	public void setPublisher(String publisher) {
		this.publisher = publisher;
	}

	public String getComments() {
		return comments;
	}

	public void setComments(String comments) {
		this.comments = comments;
	}

	public int getEmotion() {
		return emotion;
	}

	public void setEmotion(int emotion) {
		this.emotion = emotion;
	}

	public int getDining() {
		return dining;
	}

	public void setDining(int dining) {
		this.dining = dining;
	}

	public int getRest() {
		return rest;
	}

	public void setRest(int rest) {
		this.rest = rest;
	}

	public int getActivity() {
		return activity;
	}

	public void setActivity(int activity) {
		this.activity = activity;
	}

	public int getExercise() {
		return exercise;
	}

	public void setExercise(int exercise) {
		this.exercise = exercise;
	}

	public int getSelf_care() {
		return self_care;
	}

	public void setSelf_care(int self_care) {
		this.self_care = self_care;
	}

	public int getManner() {
		return manner;
	}

	public void setManner(int manner) {
		this.manner = manner;
	}

	public int getGame() {
		return game;
	}

	public void setGame(int game) {
		this.game = game;
	}

	public String getChild_id() {
		return child_id;
	}

	public void setChild_id(String child_id) {
		this.child_id = child_id;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public static List<EducationInfo> jsonArrayToList(JSONArray array) {
		List<EducationInfo> list = new ArrayList<EducationInfo>();

		try {
			for (int i = 0; i < array.length(); i++) {
				JSONObject jsonObject = array.getJSONObject(i);
				EducationInfo info = parse(jsonObject);
				list.add(info);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return list;
	}

	private static EducationInfo parse(JSONObject jsonObject)
			throws JSONException {
		EducationInfo info = new EducationInfo();
		//注意服务器端给出的key是id
		info.setServer_id(jsonObject.getInt("id"));
		info.setTimestamp(jsonObject.getLong(TIMESTAMP));
		info.setPublisher(jsonObject.getString(PUBLISHER));
		info.setComments(jsonObject.getString(COMMENTS));
		info.setEmotion(jsonObject.getInt(EMOTION));
		info.setDining(jsonObject.getInt(DINING));
		info.setRest(jsonObject.getInt(REST));
		info.setActivity(jsonObject.getInt(ACTIVITY));
		info.setExercise(jsonObject.getInt(EXERCISE));
		info.setSelf_care(jsonObject.getInt(SELF_CARE));
		info.setManner(jsonObject.getInt(MANNER));
		info.setGame(jsonObject.getInt(GAME));
		info.setChild_id(jsonObject.getString(CHILD_ID));
		return info;
	}
}
