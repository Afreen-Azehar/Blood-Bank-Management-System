package com.bloodbank;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** Database operations for blood requests. */
public class RequestDAO {

    /**
     * Requests blood from blood banks in the patient's city.
     * Uses the earliest-expiry stock first (FEFO). Stock deduction and the request record
     * are saved in ONE transaction, so stock never goes wrong if something fails.
     */
    public void requestBlood(String patient, String hospital, String group,
                             int units, String city, String state) {
        if (units <= 0) {
            System.out.println("Units must be greater than zero.");
            return;
        }
        Connection con = null;
        try {
            con = DBConnection.getConnection();
            con.setAutoCommit(false);

            // Lock the matching stock rows (earliest expiry first)
            String selectSql = "SELECT s.stock_id, s.units FROM blood_stock s "
                    + "JOIN blood_banks b ON s.bank_id = b.bank_id "
                    + "WHERE s.blood_group = ? AND s.units > 0 AND s.expiry_date > SYSDATE "
                    + "AND UPPER(b.city) = UPPER(?) "
                    + "ORDER BY s.expiry_date FOR UPDATE OF s.units";

            List<int[]> rows = new ArrayList<>();
            int available = 0;
            try (PreparedStatement ps = con.prepareStatement(selectSql)) {
                ps.setString(1, group);
                ps.setString(2, city);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        int stockId = rs.getInt("stock_id");
                        int stockUnits = rs.getInt("units");
                        rows.add(new int[]{stockId, stockUnits});
                        available += stockUnits;
                    }
                }
            }

            String status;
            if (available >= units) {
                int remaining = units;
                try (PreparedStatement up = con.prepareStatement(
                        "UPDATE blood_stock SET units = units - ? WHERE stock_id = ?")) {
                    for (int[] row : rows) {
                        if (remaining == 0) break;
                        int take = Math.min(remaining, row[1]);
                        up.setInt(1, take);
                        up.setInt(2, row[0]);
                        up.executeUpdate();
                        remaining -= take;
                    }
                }
                status = "FULFILLED";
            } else {
                status = "PENDING";
            }

            try (PreparedStatement ins = con.prepareStatement(
                    "INSERT INTO blood_requests (patient_name, hospital, blood_group, units, city, state, status) "
                  + "VALUES (?, ?, ?, ?, ?, ?, ?)")) {
                ins.setString(1, patient);
                ins.setString(2, hospital);
                ins.setString(3, group);
                ins.setInt(4, units);
                ins.setString(5, city);
                ins.setString(6, state);
                ins.setString(7, status);
                ins.executeUpdate();
            }

            con.commit();

            if ("FULFILLED".equals(status)) {
                System.out.println("Request FULFILLED: " + units + " unit(s) of " + group
                        + " reserved from blood banks in " + city + ".");
            } else {
                System.out.println("Only " + available + " unit(s) of " + group + " available in " + city
                        + ". Request saved as PENDING.");
                System.out.println("Tip: use option 6 to search other cities and states.");
            }

        } catch (SQLException e) {
            try {
                if (con != null) con.rollback();
            } catch (SQLException ignored) {
            }
            System.out.println("Request failed, changes rolled back: " + e.getMessage());
        } finally {
            try {
                if (con != null) con.close();
            } catch (SQLException ignored) {
            }
        }
    }

    public void listRequests() {
        String sql = "SELECT request_id, patient_name, hospital, blood_group, units, city, status, request_date "
                   + "FROM blood_requests ORDER BY request_date DESC";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            System.out.printf("%-5s %-20s %-22s %-6s %-6s %-12s %-10s %-12s%n",
                    "ID", "PATIENT", "HOSPITAL", "GROUP", "UNITS", "CITY", "STATUS", "DATE");
            System.out.println("-".repeat(102));
            boolean found = false;
            while (rs.next()) {
                found = true;
                System.out.printf("%-5d %-20s %-22s %-6s %-6d %-12s %-10s %-12s%n",
                        rs.getInt("request_id"), rs.getString("patient_name"), rs.getString("hospital"),
                        rs.getString("blood_group"), rs.getInt("units"), rs.getString("city"),
                        rs.getString("status"), rs.getDate("request_date").toString());
            }
            if (!found) {
                System.out.println("No requests found.");
            }
        } catch (SQLException e) {
            System.out.println("Could not read requests: " + e.getMessage());
        }
    }
}
