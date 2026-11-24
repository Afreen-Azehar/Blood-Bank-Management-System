package com.bloodbank;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/** Database operations for blood banks. */
public class BloodBankDAO {

    public void addBank(String name, String city, String state, String phone) {
        String sql = "INSERT INTO blood_banks (name, city, state, phone) VALUES (?, ?, ?, ?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setString(2, city);
            ps.setString(3, state);
            ps.setString(4, phone);
            ps.executeUpdate();
            System.out.println("Blood bank added successfully.");
        } catch (SQLException e) {
            System.out.println("Could not add blood bank: " + e.getMessage());
        }
    }

    public void listBanks() {
        String sql = "SELECT bank_id, name, city, state, phone FROM blood_banks ORDER BY state, city";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            System.out.printf("%-5s %-35s %-15s %-18s %-12s%n", "ID", "NAME", "CITY", "STATE", "PHONE");
            System.out.println("-".repeat(90));
            boolean found = false;
            while (rs.next()) {
                found = true;
                System.out.printf("%-5d %-35s %-15s %-18s %-12s%n",
                        rs.getInt("bank_id"), rs.getString("name"), rs.getString("city"),
                        rs.getString("state"), rs.getString("phone"));
            }
            if (!found) {
                System.out.println("No blood banks found.");
            }
        } catch (SQLException e) {
            System.out.println("Could not read blood banks: " + e.getMessage());
        }
    }
}
