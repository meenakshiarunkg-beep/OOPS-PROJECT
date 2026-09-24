package com.voting.model;

public class Candidate {

    private int candidateId;
    private String fullName;
    private String party;
    private String positionName;
    private String photoPath; // may be null -> UI falls back to an initials avatar
    private int voteCount;

    public Candidate() {
    }

    public Candidate(int candidateId, String fullName, String party, String positionName,
                      String photoPath, int voteCount) {
        this.candidateId = candidateId;
        this.fullName = fullName;
        this.party = party;
        this.positionName = positionName;
        this.photoPath = photoPath;
        this.voteCount = voteCount;
    }

    public int getCandidateId() {
        return candidateId;
    }

    public void setCandidateId(int candidateId) {
        this.candidateId = candidateId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getParty() {
        return party;
    }

    public void setParty(String party) {
        this.party = party;
    }

    public String getPositionName() {
        return positionName;
    }

    public void setPositionName(String positionName) {
        this.positionName = positionName;
    }

    public String getPhotoPath() {
        return photoPath;
    }

    public void setPhotoPath(String photoPath) {
        this.photoPath = photoPath;
    }

    public int getVoteCount() {
        return voteCount;
    }

    public void setVoteCount(int voteCount) {
        this.voteCount = voteCount;
    }

    @Override
    public String toString() {
        return fullName + " (" + party + ")";
    }
}
