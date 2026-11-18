-- Banking system schema (portable SQL: H2, MySQL, SQLite).
-- Unique columns support FR19 (no duplicate username / ID number / registration number)
-- and FR14 (one interest payment per account per month).

CREATE TABLE IF NOT EXISTS customer (
    customer_id          VARCHAR(20)   PRIMARY KEY,
    customer_type        VARCHAR(12)   NOT NULL,            -- INDIVIDUAL or ORGANISATION
    username             VARCHAR(50)   NOT NULL UNIQUE,
    password_hash        VARCHAR(64)   NOT NULL,
    address              VARCHAR(200)  NOT NULL,
    contact_details      VARCHAR(100)  NOT NULL,
    date_registered      DATE          NOT NULL,
    first_name           VARCHAR(50),
    surname              VARCHAR(50),
    id_number            VARCHAR(20)   UNIQUE,
    organisation_name    VARCHAR(100),
    registration_number  VARCHAR(30)   UNIQUE,
    registration_date    DATE,
    contact_person       VARCHAR(100)
);

CREATE TABLE IF NOT EXISTS employment_details (
    customer_id       VARCHAR(20)   PRIMARY KEY,
    employer_name     VARCHAR(100)  NOT NULL,
    employer_address  VARCHAR(200)  NOT NULL,
    FOREIGN KEY (customer_id) REFERENCES customer (customer_id)
);

CREATE TABLE IF NOT EXISTS account (
    account_number  VARCHAR(20)    PRIMARY KEY,
    account_type    VARCHAR(12)    NOT NULL,                -- SAVINGS, INVESTMENT or CHEQUE
    branch          VARCHAR(50)    NOT NULL,
    balance         DECIMAL(15,2)  NOT NULL,
    customer_id     VARCHAR(20)    NOT NULL,
    FOREIGN KEY (customer_id) REFERENCES customer (customer_id)
);

CREATE TABLE IF NOT EXISTS bank_transaction (
    seq               BIGINT         AUTO_INCREMENT NOT NULL,  -- keeps history in insertion order
    transaction_id    VARCHAR(40)    PRIMARY KEY,
    account_number    VARCHAR(20)    NOT NULL,
    type              VARCHAR(12)    NOT NULL,              -- DEPOSIT, WITHDRAWAL or INTEREST
    amount            DECIMAL(15,2)  NOT NULL,
    transaction_date  TIMESTAMP      NOT NULL,
    balance_after     DECIMAL(15,2)  NOT NULL,
    reference         VARCHAR(100),
    FOREIGN KEY (account_number) REFERENCES account (account_number)
);

CREATE TABLE IF NOT EXISTS interest_payment (
    payment_id      VARCHAR(40)    PRIMARY KEY,
    account_number  VARCHAR(20)    NOT NULL,
    period          VARCHAR(7)     NOT NULL,                -- e.g. 2026-11
    amount          DECIMAL(15,2)  NOT NULL,
    status          VARCHAR(10)    NOT NULL,                -- PENDING, CREDITED or FAILED
    transaction_id  VARCHAR(40),
    FOREIGN KEY (account_number) REFERENCES account (account_number),
    FOREIGN KEY (transaction_id) REFERENCES bank_transaction (transaction_id),
    UNIQUE (account_number, period)
);
