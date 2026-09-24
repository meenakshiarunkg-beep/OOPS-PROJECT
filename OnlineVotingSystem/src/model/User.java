package model;

/**
 * User
 * -----
 * Base class for anyone who logs in: a Voter or an Admin.
 * Both share a username and password, so that common data lives here
 * instead of being duplicated - this is INHERITANCE.
 * The fields are private with public getters/setters - this is ENCAPSULATION.
 */
public class User {

    private String username;
    private String password;

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
