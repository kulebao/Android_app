package com.cocobabys.bean;

import java.util.ArrayList;
import java.util.List;

import android.text.TextUtils;

public class BusinessInfo {

	private int id;
	private int agent_id;
	private String title;
	private String address;
	private String contact;
	private String time_span;
	private String detail;
	private long updated_at;
	private PublishState publishing;
	private Location location = new Location();
	private List<Logos> logos = new ArrayList<Logos>();

	public Location getLocation() {
		return location;
	}

	public void setLocation(Location location) {
		this.location = location;
	}

	public List<Logos> getLogos() {
		return logos;
	}

	public void setLogos(List<Logos> logos) {
		this.logos = logos;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getAgent_id() {
		return agent_id;
	}

	public void setAgent_id(int agent_id) {
		this.agent_id = agent_id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getContact() {
		return contact;
	}

	public void setContact(String contact) {
		this.contact = contact;
	}

	public String getTime_span() {
		return time_span;
	}

	public void setTime_span(String time_span) {
		this.time_span = time_span;
	}

	public String getDetail() {
		return detail;
	}

	public void setDetail(String detail) {
		this.detail = detail;
	}

	public long getUpdated_at() {
		return updated_at;
	}

	public void setUpdated_at(long updated_at) {
		this.updated_at = updated_at;
	}

	public PublishState getPublishing() {
		return publishing;
	}

	public void setPublishing(PublishState publishing) {
		this.publishing = publishing;
	}

	public BusinessInfo() {
		super();
	}

	public List<String> getLogoList() {
		List<String> list = new ArrayList<String>();

		for (Logos logo : logos) {
			if (!TextUtils.isEmpty(logo.getUrl())) {
				list.add(logo.getUrl());
			}
		}

		return list;
	}

	public static class PublishState {
		private int publish_status;
		private long published_at;

		public int getPublish_status() {
			return publish_status;
		}

		public void setPublish_status(int publish_status) {
			this.publish_status = publish_status;
		}

		public long getPublished_at() {
			return published_at;
		}

		public void setPublished_at(long published_at) {
			this.published_at = published_at;
		}

	}

	public static class Location {
		private double latitude = -1;
		private double longitude = -1;

		public boolean isValid() {
			return latitude > 0 && longitude > 0;
		}

		public double getLatitude() {
			return latitude;
		}

		public void setLatitude(double latitude) {
			this.latitude = latitude;
		}

		public double getLongitude() {
			return longitude;
		}

		public void setLongitude(double longitude) {
			this.longitude = longitude;
		}
	}

	public static class Logos {

		private String url = "";

		public String getUrl() {
			return url;
		}

		public void setUrl(String url) {
			this.url = url;
		}

	}

}