package com.voting.dao;

import com.voting.db.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class VoteDAO {

    private final VoterDAO voterDAO = new VoterDAO();
    private final CandidateDAO candidateDAO = new CandidateDAO();
    private final ElectionDAO electionDAO = new ElectionDAO();

    public enum VoteResult {
        SUCCESS, ALREADY_VOTED, VOTING_CLOSED, ERROR
    }

    /**
     * Casts one vote inside a single database transaction:
     *   1. Voting must currently be open.
     *   2. The voter row is locked (SELECT ... FOR UPDATE) and re-checked
     *      so two clicks / two windows can never both succeed.
     *   3. A vote record is inserted, the candidate's tally is bumped,
     *      and the voter is flagged as having voted.
     * Everything commits together, or nothing does.
     */
    public VoteResult castVote(int voterId, int candidateId) {
        if (!electionDAO.isVotingOpen()) {
            return VoteResult.VOTING_CLOSED;
        }

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            String lockSql = "SELECT has_voted FROM voters WHERE voter_id = ? FOR UPDATE";
            try (PreparedStatement ps = conn.prepareStatement(lockSql)) {
                ps.setInt(1, voterId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        conn.rollback();
                        return VoteResult.ERROR;
                    }
                    if (rs.getBoolean("has_voted")) {
                        conn.rollback();
                        return VoteResult.ALREADY_VOTED;
                    }
                }
            }

            String insertVoteSql = "INSERT INTO votes (voter_id, candidate_id) VALUES (?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(insertVoteSql)) {
                ps.setInt(1, voterId);
                ps.setInt(2, candidateId);
                ps.executeUpdate();
            }

            candidateDAO.incrementVote(conn, candidateId);
            voterDAO.markVoted(conn, voterId);

            conn.commit();
            return VoteResult.SUCCESS;

        } catch (SQLException e) {
            e.printStackTrace();
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ignored) {
                }
            }
            return VoteResult.ERROR;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException ignored) {
                }
            }
        }
    }
}
