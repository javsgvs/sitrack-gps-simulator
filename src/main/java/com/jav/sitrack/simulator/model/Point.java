package com.jav.sitrack.simulator.model;

/**
 * Denotes a point on the globe.
 */
public class Point {
	private Double latitude;
	private Double longitude;
	private Double altitude;

	public Point(Double latitude, Double longitude, Double altitude) {
		super();
		this.latitude = latitude;
		this.longitude = longitude;
		this.altitude = altitude;
	}

	public Point(Double latitude, Double longitude) {
		super();
		this.latitude = latitude;
		this.longitude = longitude;
		this.altitude = null;
	}

	/**
	 * @return the latitude
	 */
	public Double getLatitude() {
		return latitude;
	}

	/**
	 * @param latitude the latitude to set
	 */
	public void setLatitude(Double latitude) {
		this.latitude = latitude;
	}

	/**
	 * @return the longitude
	 */
	public Double getLongitude() {
		return longitude;
	}

	/**
	 * @param longitude the longitude to set
	 */
	public void setLongitude(Double longitude) {
		this.longitude = longitude;
	}

	/**
	 * @return the altitude
	 */
	public Double getAltitude() {
		return altitude;
	}

	/**
	 * @param altitude the altitude to set
	 */
	public void setAltitude(Double altitude) {
		this.altitude = altitude;
	}

	@Override
	public String toString() {
		return "Point [lat/lang:" + latitude + "," + longitude + ", altitude=" + altitude + "]";
	}


}
