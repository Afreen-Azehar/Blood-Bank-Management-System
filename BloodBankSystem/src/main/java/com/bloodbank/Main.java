package com.bloodbank;

import java.util.Scanner;


public class Main {

    private static final Scanner sc = new Scanner(System.in);

    private static final BloodBankDAO bankDAO = new BloodBankDAO();
    private static final DonorDAO donorDAO = new DonorDAO();
    private static final StockDAO stockDAO = new StockDAO();
    private static final RequestDAO requestDAO = new RequestDAO();

    public static void main(String[] args) {
        System.out.println("====================================");
        System.out.println("   BLOOD BANK MANAGEMENT SYSTEM");
        System.out.println("====================================");

        boolean running = true;
        while (running) {
            printMenu();
            int choice = readInt("Enter your choice: ");
            System.out.println();
            switch (choice) {
                case 1 -> addBank();
                case 2 -> bankDAO.listBanks();
                case 3 -> registerDonor();
                case 4 -> listDonors();
                case 5 -> recordDonation();
                case 6 -> searchAvailability();
                case 7 -> requestBlood();
                case 8 -> stockDAO.viewAllStock();
                case 9 -> requestDAO.listRequests();
                case 0 -> {
                    System.out.println("Thank you. Goodbye!");
                    running = false;
                }
                default -> System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    private static void printMenu() {
        System.out.println("\n------------- MENU -------------");
        System.out.println("1. Add blood bank");
        System.out.println("2. View blood banks");
        System.out.println("3. Register donor");
        System.out.println("4. View donors by blood group");
        System.out.println("5. Record a donation (adds stock)");
        System.out.println("6. Search blood availability (by present address)");
        System.out.println("7. Request blood");
        System.out.println("8. View all stock");
        System.out.println("9. View all requests");
        System.out.println("0. Exit");
        System.out.println("--------------------------------");
    }

    private static void addBank() {
        String name = readText("Blood bank name: ");
        String city = readText("City: ");
        String state = readText("State: ");
        String phone = readText("Phone: ");
        bankDAO.addBank(name, city, state, phone);
    }

    private static void registerDonor() {
        String name = readText("Donor name: ");
        int age = readInt("Age (18-65): ");
        String gender = readText("Gender (M/F/O): ").toUpperCase();
        if (!gender.equals("M") && !gender.equals("F") && !gender.equals("O")) {
            System.out.println("Gender must be M, F or O.");
            return;
        }
        String group = readGroup("Blood group (A+, A-, B+, B-, AB+, AB-, O+, O-): ");
        String phone = readText("Phone: ");
        String city = readText("City: ");
        String state = readText("State: ");
        donorDAO.registerDonor(name, age, gender, group, phone, city, state);
    }

    private static void listDonors() {
        String group = readGroup("Blood group to search: ");
        donorDAO.listDonorsByGroup(group);
    }

    private static void recordDonation() {
        int donorId = readInt("Donor ID: ");
        int bankId = readInt("Blood bank ID: ");
        int units = readInt("Units donated: ");
        stockDAO.recordDonation(donorId, bankId, units);
    }

    private static void searchAvailability() {
        String group = readGroup("Blood group needed: ");
        String city = readText("Your present city: ");
        String state = readText("Your present state: ");
        stockDAO.searchAvailability(group, city, state);
    }

    private static void requestBlood() {
        String patient = readText("Patient name: ");
        String hospital = readText("Hospital name: ");
        String group = readGroup("Blood group needed: ");
        int units = readInt("Units needed: ");
        String city = readText("City: ");
        String state = readText("State: ");
        requestDAO.requestBlood(patient, hospital, group, units, city, state);
    }

    // ---------------- input helpers ----------------

    private static String readText(String prompt) {
        String value;
        do {
            System.out.print(prompt);
            value = sc.nextLine().trim();
        } while (value.isEmpty());
        return value;
    }

    private static int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = sc.nextLine().trim();
            try {
                return Integer.parseInt(line);
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
            }
        }
    }

    private static String readGroup(String prompt) {
        while (true) {
            String group = Validator.normalizeGroup(readText(prompt));
            if (Validator.isValidGroup(group)) {
                return group;
            }
            System.out.println("Invalid blood group. Use A+, A-, B+, B-, AB+, AB-, O+ or O-.");
        }
    }
}
