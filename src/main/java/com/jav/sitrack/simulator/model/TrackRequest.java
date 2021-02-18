package com.jav.sitrack.simulator.model;

public class TrackRequest {
    
    private String loginCode;
    private String reportDate;
    private String reportType;
    private Double latitude;
    private Double longitude;
    private Double gpsDop;
    
    private Integer header;
    private Double speed;
    private Double speedLabel;
    private Integer gpsSatelites;
    private String text;
    private String textLabel;

    

    public String getLoginCode() {
        return loginCode;
    }

    public void setLoginCode(String loginCode) {
        this.loginCode = loginCode;
    }

    public String getReportDate() {
        return reportDate;
    }

    public void setReportDate(String reportDate) {
        this.reportDate = reportDate;
    }

    public String getReportType() {
        return reportType;
    }

    public void setReportType(String reportType) {
        this.reportType = reportType;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Double getGpsDop() {
        return gpsDop;
    }

    public void setGpsDop(Double gpsDop) {
        this.gpsDop = gpsDop;
    }

    public Integer getHeader() {
        return header;
    }

    public void setHeader(Integer header) {
        this.header = header;
    }

    public Double getSpeed() {
        return speed;
    }

    public void setSpeed(Double speed) {
        this.speed = speed;
    }

    public Double getSpeedLabel() {
        return speedLabel;
    }

    public void setSpeedLabel(Double speedLabel) {
        this.speedLabel = speedLabel;
    }

    public Integer getGpsSatelites() {
        return gpsSatelites;
    }

    public void setGpsSatelites(Integer gpsSatelites) {
        this.gpsSatelites = gpsSatelites;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getTextLabel() {
        return textLabel;
    }

    public void setTextLabel(String textLabel) {
        this.textLabel = textLabel;
    }

    @Override
    public String toString() {
        return "TrackRequest [gpsDop=" + gpsDop + ", gpsSatelites=" + gpsSatelites + ", header=" + header
                + ", latitude=" + latitude + ", loginCode=" + loginCode + ", longitude=" + longitude + ", reportDate="
                + reportDate + ", reportType=" + reportType + ", speed=" + speed + ", speedLabel=" + speedLabel
                + ", text=" + text + ", textLabel=" + textLabel + "]";
    }

    public TrackRequest(String loginCode, String reportDate, String reportType, Double latitude, Double longitude,
            Double gpsDop, Integer header, Double speed, Double speedLabel, Integer gpsSatelites, String text,
            String textLabel) {
        this.loginCode = loginCode;
        this.reportDate = reportDate;
        this.reportType = reportType;
        this.latitude = latitude;
        this.longitude = longitude;
        this.gpsDop = gpsDop;
        this.header = header;
        this.speed = speed;
        this.speedLabel = speedLabel;
        this.gpsSatelites = gpsSatelites;
        this.text = text;
        this.textLabel = textLabel;
    }

    public TrackRequest() {

    }

}
