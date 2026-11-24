package com.bloodbank;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

/** Database operations for blood stock, donations and availability search. */
public class StockDAO {

    /**
     * Records a donation: checks donor eligibility, adds units to the blood bank's stock
     * and updates the donor's last donation date. All steps run as ONE transaction.
     */
    public void recordDonation(int donorId, int bankId, int units) {
        if (units <= 0) {
            System.out.println("Units must be greater than zero.");
            return;
        }
        Connection con = null;
        try {
            con = DBConnection.getConnection();
            con.setAutoCommit(false); // start transaction

            // 1. Find the donor
            String bloodGroup;
            String gender;
            Date lastDonation;
            try (PreparedStatement ps = con.prepareStatement(
                    "SELECT blood_group, gender, last_donation_date FROM donors WHERE donor_id = ?")) {
                ps.setInt(1, donorId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        System.out.println("Donor not found.");
                        con.rollback();
                        return;
                    }
                    bloodGroup = rs.getString("blood_group");
                    gender = rs.getString("gender");
                    lastDonation = rs.getDate("last_donation_date");
                }
            }

            // 2. Eligibility check (simplified: 90 days for men, 120 days for women)
            int minGapDays = "F".equals(gender) ? 120 : 90;
            if (lastDonation != null) {
                long days = ChronoUnit.DAYS.between(lastDonation.toLocalDate(), LocalDate.now());
                if (days < minGapDays) {
                    System.out.println("Donor is NOT eligible yet. Last donation was " + days
                            + " days ago; minimum gap is " + minGapDays + " days.");
                    con.rollback();
                    return;
                }
            }

            // 3. Check the blood bank exists
            try (PreparedStatement ps = con.prepareStatement(
                    "SELECT 1 FROM blood_banks WHERE bank_id = ?")) {
                ps.setInt(1, bankId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        System.out.println("Blood bank not found.");
                        con.rollback();
                        return;
                    }
                }
            }

            // 4. Add stock (whole blood is given a simple 35-day shelf life)
            try (PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO blood_stock (bank_id, blood_group, units, collection_date, expiry_date) "
                  + "VALUES (?, ?, ?, SYSDATE, SYSDATE + 35)")) {
                ps.setInt(1, bankId);
                ps.setString(2, bloodGroup);
                ps.setInt(3, units);
                ps.executeUpdate();
            }

            // 5. Update the donor's last donation date
            try (PreparedStatement ps = con.prepareStatement(
                    "UPDATE donors SET last_donation_date = SYSDATE WHERE donor_id = ?")) {
                ps.setInt(1, donorId);
                ps.executeUpdate();
            }

            con.commit();
            System.out.println("Donation recorded: " + units + " unit(s) of " + bloodGroup + " added to stock.");

        } catch (SQLException e) {
            rollbackQuietly(con);
            System.out.println("Donation failed, changes rolled back: " + e.getMessage());
        } finally {
            closeQuietly(con);
        }
    }

    /**
     * Searches availability by blood group and the person's PRESENT ADDRESS (city + state).
     * Order of search: same city -> other cities in the same state -> other states.
     */
    public void searchAvailability(String group, String city, String state) {
        System.out.println("\nSearching for " + group + " near " + city + ", " + state + " ...");

        int found = runSearch("AVAILABLE IN YOUR CITY (" + city + ")",
                "UPPER(b.city) = UPPER(?)", group, city);

        if (found == 0) {
            System.out.println("Not available in " + city + ". Checking other cities in " + state + " ...");
            found = runSearch("AVAILABLE IN YOUR STATE (" + state + ")",
                    "UPPER(b.state) = UPPER(?) AND UPPER(b.city) <> UPPER(?)", group, state, city);
        }

        if (found == 0) {
            System.out.println("Not available in " + state + ". Checking OTHER STATES ...");
            found = runSearch("AVAILABLE IN OTHER STATES",
                    "UPPER(b.state) <> UPPER(?)", group, state);
        }

        if (found == 0) {
            List<String> compatible = Validator.compatibleDonorGroups(group);
            System.out.println("\n" + group + " is not available anywhere right now.");
            System.out.println("A patient with " + group + " can also receive: " + compatible
                    + " (confirm with the doctor before using).");
        }
    }

    /** Runs one search query and prints the blood banks found. Returns how many were found. */
    private int runSearch(String title, String condition, String group, String... params) {
        String sql = "SELECT b.name, b.city, b.state, b.phone, SUM(s.units) AS total_units, "
                   + "MIN(s.expiry_date) AS nearest_expiry "
                   + "FROM blood_stock s JOIN blood_banks b ON s.bank_id = b.bank_id "
                   + "WHERE s.blood_group = ? AND s.units > 0 AND s.expiry_date > SYSDATE AND " + condition + " "
                   + "GROUP BY b.bank_id, b.name, b.city, b.state, b.phone "
                   + "ORDER BY total_units DESC";
        int count = 0;
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, group);
            for (int i = 0; i < params.length; i++) {
                ps.setString(i + 2, params[i]);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    if (count == 0) {
                        System.out.println("\n=== " + title + " ===");
                        System.out.printf("%-35s %-13s %-16s %-12s %-6s %-12s%n",
                                "BLOOD BANK", "CITY", "STATE", "PHONE", "UNITS", "EXPIRES");
                        System.out.println("-".repeat(98));
                    }
                    count++;
                    System.out.printf("%-35s %-13s %-16s %-12s %-6d %-12s%n",
                            rs.getString("name"), rs.getString("city"), rs.getString("state"),
                            rs.getString("phone"), rs.getInt("total_units"),
                            rs.getDate("nearest_expiry").toString());
                }
            }
        } catch (SQLException e) {
            System.out.println("Search failed: " + e.getMessage());
        }
        return count;
    }

    public void viewAllStock() {
        String sql = "SELECT s.stock_id, b.name, b.city, s.blood_group, s.units, s.expiry_date, "
                   + "CASE WHEN s.expiry_date < SYSDATE THEN 'EXPIRED' ELSE 'OK' END AS state "
                   + "FROM blood_stock s JOIN blood_banks b ON s.bank_id = b.bank_id "
                   + "ORDER BY b.name, s.blood_group";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            System.out.printf("%-6s %-33s %-13s %-6s %-6s %-12s %-8s%n",
                    "ID", "BLOOD BANK", "CITY", "GROUP", "UNITS", "EXPIRY", "STATUS");
            System.out.println("-".repeat(92));
            boolean found = false;
            while (rs.next()) {
                found = true;
                System.out.printf("%-6d %-33s %-13s %-6s %-6d %-12s %-8s%n",
                        rs.getInt("stock_id"), rs.getString("name"), rs.getString("city"),
                        rs.getString("blood_group"), rs.getInt("units"),
                        rs.getDate("expiry_date").toString(), rs.getString("state"));
            }
            if (!found) {
                System.out.println("No stock records found.");
            }
        } catch (SQLException e) {
            System.out.println("Could not read stock: " + e.getMessage());
        }
    }

    private void rollbackQuietly(Connection con) {
        try {
            if (con != null) con.rollback();
        } catch (SQLException ignored) {
        }
    }

    private void closeQuietly(Connection con) {
        try {
            if (con != null) con.close();
        } catch (SQLException ignored) {
        }
    }
}
