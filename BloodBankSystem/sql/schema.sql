-- =====================================================
-- Blood Bank Management System : Oracle schema + sample data
-- Run this script in SQL Developer / SQL*Plus before running the Java app
-- =====================================================

-- (Optional) run these first if you want to recreate the tables
-- DROP TABLE blood_requests CASCADE CONSTRAINTS;
-- DROP TABLE blood_stock    CASCADE CONSTRAINTS;
-- DROP TABLE donors         CASCADE CONSTRAINTS;
-- DROP TABLE blood_banks    CASCADE CONSTRAINTS;

CREATE TABLE blood_banks (
    bank_id  NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name     VARCHAR2(100) NOT NULL,
    city     VARCHAR2(50)  NOT NULL,
    state    VARCHAR2(50)  NOT NULL,
    phone    VARCHAR2(15)
);

CREATE TABLE donors (
    donor_id            NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name                VARCHAR2(100) NOT NULL,
    age                 NUMBER(3)     NOT NULL CHECK (age BETWEEN 18 AND 65),
    gender              CHAR(1)       NOT NULL CHECK (gender IN ('M','F','O')),
    blood_group         VARCHAR2(3)   NOT NULL
                        CHECK (blood_group IN ('A+','A-','B+','B-','AB+','AB-','O+','O-')),
    phone               VARCHAR2(15),
    city                VARCHAR2(50)  NOT NULL,
    state               VARCHAR2(50)  NOT NULL,
    last_donation_date  DATE
);

CREATE TABLE blood_stock (
    stock_id         NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    bank_id          NUMBER NOT NULL REFERENCES blood_banks(bank_id),
    blood_group      VARCHAR2(3) NOT NULL
                     CHECK (blood_group IN ('A+','A-','B+','B-','AB+','AB-','O+','O-')),
    units            NUMBER NOT NULL CHECK (units >= 0),
    collection_date  DATE DEFAULT SYSDATE,
    expiry_date      DATE NOT NULL
);

CREATE TABLE blood_requests (
    request_id    NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    patient_name  VARCHAR2(100) NOT NULL,
    hospital      VARCHAR2(100),
    blood_group   VARCHAR2(3)   NOT NULL,
    units         NUMBER        NOT NULL CHECK (units > 0),
    city          VARCHAR2(50)  NOT NULL,
    state         VARCHAR2(50)  NOT NULL,
    status        VARCHAR2(15)  DEFAULT 'PENDING'
                  CHECK (status IN ('PENDING','FULFILLED')),
    request_date  DATE DEFAULT SYSDATE
);

-- ---------------- Sample data (dummy) ----------------
INSERT INTO blood_banks (name, city, state, phone) VALUES ('Guntur Central Blood Bank', 'Guntur', 'Andhra Pradesh', '9000000001');
INSERT INTO blood_banks (name, city, state, phone) VALUES ('Vijayawada Red Cross Blood Bank', 'Vijayawada', 'Andhra Pradesh', '9000000002');
INSERT INTO blood_banks (name, city, state, phone) VALUES ('Hyderabad City Blood Bank', 'Hyderabad', 'Telangana', '9000000003');
INSERT INTO blood_banks (name, city, state, phone) VALUES ('Chennai Lifeline Blood Bank', 'Chennai', 'Tamil Nadu', '9000000004');

INSERT INTO blood_stock (bank_id, blood_group, units, expiry_date) VALUES (1, 'O+',  10, SYSDATE + 20);
INSERT INTO blood_stock (bank_id, blood_group, units, expiry_date) VALUES (1, 'A+',   5, SYSDATE + 15);
INSERT INTO blood_stock (bank_id, blood_group, units, expiry_date) VALUES (2, 'B+',   8, SYSDATE + 25);
INSERT INTO blood_stock (bank_id, blood_group, units, expiry_date) VALUES (2, 'O-',   3, SYSDATE + 10);
INSERT INTO blood_stock (bank_id, blood_group, units, expiry_date) VALUES (3, 'AB-',  4, SYSDATE + 30);
INSERT INTO blood_stock (bank_id, blood_group, units, expiry_date) VALUES (3, 'O+',  12, SYSDATE + 18);
INSERT INTO blood_stock (bank_id, blood_group, units, expiry_date) VALUES (4, 'B-',   6, SYSDATE + 22);
INSERT INTO blood_stock (bank_id, blood_group, units, expiry_date) VALUES (4, 'A-',   2, SYSDATE + 12);

INSERT INTO donors (name, age, gender, blood_group, phone, city, state, last_donation_date)
VALUES ('Ravi Kumar', 28, 'M', 'O+', '9111111111', 'Guntur', 'Andhra Pradesh', SYSDATE - 200);
INSERT INTO donors (name, age, gender, blood_group, phone, city, state, last_donation_date)
VALUES ('Sneha Reddy', 24, 'F', 'B+', '9222222222', 'Vijayawada', 'Andhra Pradesh', SYSDATE - 30);

COMMIT;

