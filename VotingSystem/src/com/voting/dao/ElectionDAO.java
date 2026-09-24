package com.voting.dao;

import com.voting.db.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class ElectionDAO {

    public boolean isVotingOpen() {
        String sql = "SELECT voting_open FROM election_settings WHERE id = 1";
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) return rs.getBoolean("voting_open");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public String getElectionTitle() {
        String sql = "SELECT election_title FROM election_settings WHERE id = 1";
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) return rs.getString("election_title");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "Election";
    }

    public boolean setVotingOpen(boolean open) {
        String sql = "UPDATE election_settings SET voting_open = ? WHERE id = 1";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBoolean(1, open);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean setElectionTitle(String title) {
        String sql = "UPDATE election_settings SET election_title = ? WHERE id = 1";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, title);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
