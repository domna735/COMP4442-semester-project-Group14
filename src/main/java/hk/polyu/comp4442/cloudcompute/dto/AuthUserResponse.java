package hk.polyu.comp4442.cloudcompute.dto;

import hk.polyu.comp4442.cloudcompute.entity.AppUser;

public class AuthUserResponse {

    private Long id;
    private String username;
    private String email;
    private String role;

    public AuthUserResponse() {
    }

    public AuthUserResponse(AppUser user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.email = user.getEmail();
        this.role = user.getRole();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
