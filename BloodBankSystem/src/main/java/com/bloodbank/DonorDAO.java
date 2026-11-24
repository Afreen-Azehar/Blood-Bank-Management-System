package com.bloodbank;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/** Database operations for donors. */
public class DonorDAO {

    public void registerDonor(String name, int age, String gender, String bloodGroup,
                              String phone, String city, String state) {
        if (age < 18 || age > 65) {
            System.out.println("Donor age must be between 18 and 65.");
            return;
        }
        String sql = "INSERT INTO donors (name, age, gender, blood_group, phone, city, state) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setInt(2, age);
            ps.setString(3, gender);
            ps.setString(4, bloodGroup);
            ps.setString(5, phone);
            ps.setString(6, city);
            ps.setString(7, state);
            ps.executeUpdate();
            System.out.println("Donor registered successfully.");
        } catch (SQLException e) {
            System.out.println("Could not register donor: " + e.getMessage());
        }
    }

    /** Lists donors of a blood group. Phone numbers are not shown, to protect donor privacy. */
    public void listDonorsByGroup(String bloodGroup) {
        String sql = "SELECT donor_id, name, city, state, last_donation_date "
                   + "FROM donors WHERE blood_group = ? ORDER BY state, city";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, bloodGroup);
            try (ResultSet rs = ps.executeQuery()) {
                System.out.printf("%-5s %-25s %-15s %-18s %-15s%n",
                        "ID", "NAME", "CITY", "STATE", "LAST DONATION");
                System.out.println("-".repeat(82));
                boolean found = false;
                while (rs.next()) {
                    found = true;
                    Date last = rs.getDate("last_donation_date");
                    System.out.printf("%-5d %-25s %-15s %-18s %-15s%n",
                            rs.getInt("donor_id"), rs.getString("name"), rs.getString("city"),
                            rs.getString("state"), last == null ? "Never" : last.toString());
                }
                if (!found) {
                    System.out.println("No donors found for " + bloodGroup + ".");
                }
            }
        } catch (SQLException e) {
            System.out.println("Could not read donors: " + e.getMessage());
        }
    }
}
