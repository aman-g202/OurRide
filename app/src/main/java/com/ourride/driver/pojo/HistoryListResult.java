package com.ourride.driver.pojo;

public class HistoryListResult {
    private String bookingDate;
    private String bookingTime;
    private String pickFrom;
    private String dropTo;
    private String vehicleNumber;
    private String assignedUser;

    public String getBookingDate() {
        return bookingDate;
    }

    public void setBookingDate(String bookingDate) {
        this.bookingDate = bookingDate;
    }

    public String getBookingTime() {
        return bookingTime;
    }

    public void setBookingTime(String bookingTime) {
        this.bookingTime = bookingTime;
    }

    public String getPickFrom() {
        return pickFrom;
    }

    public void setPickFrom(String pickFrom) {
        this.pickFrom = pickFrom;
    }

    public String getDropTo() {
        return dropTo;
    }

    public void setDropTo(String dropTo) {
        this.dropTo = dropTo;
    }

    public String getVehicleNumber() {
        return vehicleNumber;
    }

    public void setVehicleNumber(String vehicleNumber) {
        this.vehicleNumber = vehicleNumber;
    }

    public String getAssignedUser() {
        return assignedUser;
    }

    public void setAssignedUser(String assignedUser) {
        this.assignedUser = assignedUser;
    }
}
