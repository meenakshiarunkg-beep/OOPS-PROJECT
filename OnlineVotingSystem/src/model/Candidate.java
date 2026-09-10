package model;

/**
 * Candidate
 * ----------
 * Private fields + public getters/setters = encapsulation.
 */
public class Candidate {

    private int candidateId;
    private String name;
    private String party;

    public Candidate(int candidateId, String name, String party) {
        this.candidateId = candidateId;
        this.name = name;
        this.party = party;
    }

    public int getCandidateId() {
        return candidateId;
    }

    public String getName() {
        return name;
    }

    public String getParty() {
        return party;
    }

    @Override
    public String toString() {
        return name + " (" + party + ")";
    }
}
