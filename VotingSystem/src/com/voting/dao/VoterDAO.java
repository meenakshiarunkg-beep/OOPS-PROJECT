package com.voting.dao;

import com.voting.db.DBConnection;
import com.voting.model.Voter;
import com.voting.util.PasswordUtil;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class VoterDAO {

    private static final int MAX_FAILED_ATTEMPTS = 3;

    /** Registers a brand-new voter. Returns null on success, or an error message. */
   public String register(String fullName, String voterCode, String email, LocalDate dob,
                        String plainPassword, String securityQuestion, String plainAnswer) {

    // Check the voter is at least 18 years old
    if (dob == null || dob.plusYears(18).isAfter(LocalDate.now())) {
        return "Age limit not met. You must be at least 18 years old to register.";
    }

    if (findByCode(voterCode) != null) return "A voter with this Voter ID already exists.";

        String pwdSalt = PasswordUtil.generateSalt();
        String pwdHash = PasswordUtil.hash(plainPassword, pwdSalt);
        String ansSalt = PasswordUtil.generateSalt();
        // normalise the answer (trim + lowercase) before hashing so capitalisation
        // doesn't matter when the voter answers it again later
        String ansHash = PasswordUtil.hash(plainAnswer.trim().toLowerCase(), ansSalt);

        String sql = "INSERT INTO voters " +
                "(full_name, voter_code, email, dob, password_hash, salt, " +
                " security_question, security_answer_hash, security_answer_salt) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, fullName);
            ps.setString(2, voterCode);
            ps.setString(3, email);
            ps.setDate(4, Date.valueOf(dob));
            ps.setString(5, pwdHash);
            ps.setString(6, pwdSalt);
            ps.setString(7, securityQuestion);
            ps.setString(8, ansHash);
            ps.setString(9, ansSalt);
            ps.executeUpdate();
            return null;
        } catch (SQLException e) {
            e.printStackTrace();
            return "Registration failed: " + e.getMessage();
        }
    }

    public Voter findByCode(String voterCode) {
        return findByColumn("voter_code", voterCode);
    }

    public Voter findByEmail(String email) {
        return findByColumn("email", email);
    }

    /** Accepts either a Voter ID or an email address - used by login & forgot-password. */
    public Voter findByCodeOrEmail(String identifier) {
        Voter v = findByCode(identifier);
        return (v != null) ? v : findByEmail(identifier);
    }

    private Voter findByColumn(String column, String value) {
        String sql = "SELECT * FROM voters WHERE " + column + " = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, value);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Authenticates a voter for normal login.
     * Returns a status via the AuthResult wrapper so the UI can show the
     * right message (wrong password vs. account locked vs. success).
     */
    public AuthResult authenticate(String identifier, String plainPassword) {
        Voter voter = findByCodeOrEmail(identifier);
        if (voter == null) return AuthResult.notFound();
        if (voter.isLocked()) return AuthResult.locked();

        if (PasswordUtil.verify(plainPassword, voter.getSalt(), voter.getPasswordHash())) {
            resetFailedAttempts(voter.getVoterId());
            return AuthResult.success(voter);
        } else {
            registerFailedAttempt(voter);
            return AuthResult.wrongPassword();
        }
    }

    private void registerFailedAttempt(Voter voter) {
        int attempts = voter.getFailedAttempts() + 1;
        boolean lock = attempts >= MAX_FAILED_ATTEMPTS;
        String sql = "UPDATE voters SET failed_attempts = ?, is_locked = ? WHERE voter_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, attempts);
            ps.setBoolean(2, lock);
            ps.setInt(3, voter.getVoterId());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void resetFailedAttempts(int voterId) {
        String sql = "UPDATE voters SET failed_attempts = 0 WHERE voter_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, voterId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean verifyDob(Voter voter, LocalDate enteredDob) {
        return voter.getDob() != null && voter.getDob().isEqual(enteredDob);
    }

    public boolean verifySecurityAnswer(Voter voter, String plainAnswer) {
        String normalised = plainAnswer == null ? "" : plainAnswer.trim().toLowerCase();
        return PasswordUtil.verify(normalised, voter.getSecurityAnswerSalt(), voter.getSecurityAnswerHash());
    }

    /** Used by the forgot-password wizard once every step has been verified. */
    public boolean resetPassword(int voterId, String newPlainPassword) {
        String salt = PasswordUtil.generateSalt();
        String hash = PasswordUtil.hash(newPlainPassword, salt);
        String sql = "UPDATE voters SET password_hash = ?, salt = ?, failed_attempts = 0, is_locked = FALSE " +
                     "WHERE voter_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, hash);
            ps.setString(2, salt);
            ps.setInt(3, voterId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void markVoted(Connection conn, int voterId) throws SQLException {
        String sql = "UPDATE voters SET has_voted = TRUE WHERE voter_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, voterId);
            ps.executeUpdate();
        }
    }

    // ----------------------- Admin-facing operations -----------------------

    public List<Voter> findAll() {
        List<Voter> list = new ArrayList<>();
        String sql = "SELECT * FROM voters ORDER BY full_name";
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /** Used by the forgot-password wizard when too many identity checks fail in a row. */
    public boolean lockAccount(int voterId) {
        String sql = "UPDATE voters SET is_locked = TRUE WHERE voter_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, voterId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean unlockVoter(int voterId) {
        String sql = "UPDATE voters SET is_locked = FALSE, failed_attempts = 0 WHERE voter_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, voterId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteVoter(int voterId) {
        String sql = "DELETE FROM voters WHERE voter_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, voterId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private Voter mapRow(ResultSet rs) throws SQLException {
        Voter v = new Voter();
        v.setVoterId(rs.getInt("voter_id"));
        v.setFullName(rs.getString("full_name"));
        v.setVoterCode(rs.getString("voter_code"));
        v.setEmail(rs.getString("email"));
        Date dob = rs.getDate("dob");
        v.setDob(dob != null ? dob.toLocalDate() : null);
        v.setPasswordHash(rs.getString("password_hash"));
        v.setSalt(rs.getString("salt"));
        v.setSecurityQuestion(rs.getString("security_question"));
        v.setSecurityAnswerHash(rs.getString("security_answer_hash"));
        v.setSecurityAnswerSalt(rs.getString("security_answer_salt"));
        v.setHasVoted(rs.getBoolean("has_voted"));
        v.setLocked(rs.getBoolean("is_locked"));
        v.setFailedAttempts(rs.getInt("failed_attempts"));
        return v;
    }

    /** Small result wrapper so the UI can distinguish every failure case. */
    public static final class AuthResult {
        public enum Status { SUCCESS, NOT_FOUND, WRONG_PASSWORD, LOCKED }

        private final Status status;
        private final Voter voter;

        private AuthResult(Status status, Voter voter) {
            this.status = status;
            this.voter = voter;
        }

        static AuthResult success(Voter v) { return new AuthResult(Status.SUCCESS, v); }
        static AuthResult notFound() { return new AuthResult(Status.NOT_FOUND, null); }
        static AuthResult wrongPassword() { return new AuthResult(Status.WRONG_PASSWORD, null); }
        static AuthResult locked() { return new AuthResult(Status.LOCKED, null); }

        public Status getStatus() { return status; }
        public Voter getVoter() { return voter; }
    }
}
