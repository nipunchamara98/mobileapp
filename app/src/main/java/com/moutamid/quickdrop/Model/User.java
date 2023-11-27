package com.moutamid.quickdrop.Model;

public class User {

    private String id;
    private String fullname;
    private String email;
    private String phone;
    private String password;
    private String location;
    private String imageUrl;

    public User(){

    }

    public User(String id,String fullname, String email, String phone, String password, String location,
                String imageUrl) {
        this.id = id;
        this.fullname = fullname;
        this.email = email;
        this.phone = phone;
        this.password = password;
        this.location = location;
        this.imageUrl = imageUrl;
    }

    public User(String id, String fullname, String email, String phone,String location, String imageUrl) {
        this.id = id;
        this.fullname = fullname;
        this.email = email;
        this.phone = phone;
        this.location = location;
        this.imageUrl = imageUrl;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }


    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}
