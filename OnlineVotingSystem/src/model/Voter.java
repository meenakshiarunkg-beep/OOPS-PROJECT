package model;

/**
 * Voter extends User.
 * Adds the fields that only a voter has: their real name, an ID from the
 * database, whether an admin has approved them, and whether they've voted.
 */
public class Voter extends User {

    private int voterId;
    private String name;
    private boolean approved;
    private boolean hasVoted;

    public Voter(int voterId, String name, String username, String password,
                 boolean approved, boolean hasVoted) {
        super(username, password); // sets up the shared User fields
        this.voterId = voterId;
        this.name = name;
        this.approved = approved;
        this.hasVoted = hasVoted;
    }

    public int getVoterId() {
        return voterId;
    }

    public String getName() {
        return name;
    }

    public boolean isApproved() {
        return approved;
    }

    public boolean hasVoted() {
        return hasVoted;
    }
}
