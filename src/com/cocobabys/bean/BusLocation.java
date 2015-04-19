package com.cocobabys.bean;

public class BusLocation {
	private int status = 0;
	private int school_id = 0;
	private long timestamp = 0;
	private String driver_id = "";
	private String child_id = "";
	private String address = "";
	private double latitude = 0.0;
	private double longitude = 0.0;
	private double direction = 0.0;
	private double radius = 0.0;

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
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

	public double getDirection() {
		return direction;
	}

	public void setDirection(double direction) {
		this.direction = direction;
	}

	public double getRadius() {
		return radius;
	}

	public void setRadius(double radius) {
		this.radius = radius;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public int getSchool_id() {
		return school_id;
	}

	public void setSchool_id(int school_id) {
		this.school_id = school_id;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public String getDriver_id() {
		return driver_id;
	}

	public void setDriver_id(String driver_id) {
		this.driver_id = driver_id;
	}

	public String getChild_id() {
		return child_id;
	}

	public void setChild_id(String child_id) {
		this.child_id = child_id;
	}

	@Override
	public String toString() {
		return "BusLocation [status=" + status + ", school_id=" + school_id
				+ ", timestamp=" + timestamp + ", driver_id=" + driver_id
				+ ", child_id=" + child_id + ", address=" + address
				+ ", latitude=" + latitude + ", longitude=" + longitude
				+ ", direction=" + direction + ", radius=" + radius + "]";
	}

}
