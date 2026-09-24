package com.voting.dao;

import com.voting.db.DBConnection;
import com.voting.model.Candidate;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CandidateDAO {

    public boolean addCandidate(String fullName, String party, String positionName, String photoPath) {
        String sql = "INSERT INTO candidates (full_name, party, position_name, photo_path) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, fullName);
            ps.setString(2, party);
            ps.setString(3, positionName);
            ps.setString(4, photoPath);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Removes a candidate. Because votes.candidate_id has ON DELETE CASCADE,
     * any votes already cast for this candidate are removed too - the
     * calling UI is responsible for warning the admin about that first.
     */
    public boolean removeCandidate(int candidateId) {
        String sql = "DELETE FROM candidates WHERE candidate_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, candidateId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateCandidate(int candidateId, String fullName, String party,
                                    String positionName, String photoPath) {
        String sql = "UPDATE candidates SET full_name = ?, party = ?, position_name = ?, photo_path = ? " +
                     "WHERE candidate_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, fullName);
            ps.setString(2, party);
            ps.setString(3, positionName);
            ps.setString(4, photoPath);
            ps.setInt(5, candidateId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Candidate> findAll() {
        List<Candidate> list = new ArrayList<>();
        String sql = "SELECT * FROM candidates ORDER BY vote_count DESC, full_name";
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public Candidate findById(int candidateId) {
        String sql = "SELECT * FROM candidates WHERE candidate_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, candidateId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    void incrementVote(Connection conn, int candidateId) throws SQLException {
        String sql = "UPDATE candidates SET vote_count = vote_count + 1 WHERE candidate_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, candidateId);
            ps.executeUpdate();
        }
    }

    private Candidate mapRow(ResultSet rs) throws SQLException {
        return new Candidate(
                rs.getInt("candidate_id"),
                rs.getString("full_name"),
                rs.getString("party"),
                rs.getString("position_name"),
                rs.getString("photo_path"),
                rs.getInt("vote_count")
        );
    }
}
