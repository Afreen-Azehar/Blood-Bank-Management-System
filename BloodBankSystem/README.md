# Blood Bank Management System (Java + JDBC + Oracle)

A console application to manage blood banks, donors, blood stock and emergency blood requests.
Patients can check availability by their **present address** (city, then state, then other states).

## Technologies
Java 17, JDBC (PreparedStatement, transactions), Oracle Database, Maven, IntelliJ IDEA

## Features
1. Add / view blood banks
2. Register donors (age and blood group validation)
3. Record donations with an eligibility check (90 days men, 120 days women) and automatic stock update
4. Search availability by blood group and present address: same city, then same state, then other states
5. Request blood: earliest-expiry stock is used first, inside one database transaction
6. Suggests compatible blood groups when a group is unavailable
7. View stock (with expired units flagged) and all requests

## Setup
1. Open **SQL Developer** (or SQL*Plus), connect to Oracle and run `sql/schema.sql`.
2. Open this folder in **IntelliJ IDEA** (File > Open > select the folder with `pom.xml`).
   Let Maven download the Oracle driver (`ojdbc11`).
3. Edit `src/main/java/com/bloodbank/DBConnection.java`: set `URL`, `USER` and `PASSWORD`.
4. Right-click `Main.java` > **Run 'Main.main()'**.

## Project structure
```
BloodBankSystem
├── pom.xml
├── sql/schema.sql
└── src/main/java/com/bloodbank
    ├── Main.java          (menu)
    ├── DBConnection.java  (JDBC connection)
    ├── BloodBankDAO.java
    ├── DonorDAO.java
    ├── StockDAO.java      (donations, search, stock)
    ├── RequestDAO.java    (blood requests)
    └── Validator.java     (blood group rules)
```

## Notes
- All data is dummy data. Do not store real patient or donor information.
- Eligibility gaps and shelf life are simplified for learning; real systems follow official guidelines.
