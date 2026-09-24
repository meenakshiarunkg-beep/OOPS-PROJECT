package model;

/**
 * Admin extends User.
 * An admin doesn't need has_voted or approved - it's a different kind of
 * user built from the same base class. This is the pairing to mention for
 * inheritance/polymorphism: User -> Voter and User -> Admin behave
 * differently even though they share the same parent.
 */
public class Admin extends User {

    private int adminId;

    public Admin(int adminId, String username, String password) {
        super(username, password);
        this.adminId = adminId;
    }

    public int getAdminId() {
        return adminId;
    }
}
