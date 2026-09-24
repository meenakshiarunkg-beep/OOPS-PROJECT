package com.voting.model;

public class AdminUser {

    private int adminId;
    private String username;

    public AdminUser() {
    }

    public AdminUser(int adminId, String username) {
        this.adminId = adminId;
        this.username = username;
    }

    public int getAdminId() {
        return adminId;
    }

    public void setAdminId(int adminId) {
        this.adminId = adminId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
