package com.moutamid.quickdrop.Model;

public class Reviews {

    private String id;
    private String userId;
    private float rating;
    private String feedback;
    private String riderId;
    private String tripId;

    public Reviews(){

    }

    public Reviews(String id, String userId, float rating, String feedback, String riderId,String tripId) {
        this.id = id;
        this.userId = userId;
        this.rating = rating;
        this.feedback = feedback;
        this.riderId = riderId;
        this.tripId = tripId;
    }

    public String getTripId() {
        return tripId;
    }

    public void setTripId(String tripId) {
        this.tripId = tripId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public String getFeedback() {
        return feedback;
    }

    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }

    public String getRiderId() {
        return riderId;
    }

    public void setRiderId(String riderId) {
        this.riderId = riderId;
    }
}
