package com.bank.dao;

import com.bank.model.Customer;
import com.bank.model.EmploymentDetails;
import com.bank.model.IndividualCustomer;
import com.bank.model.OrganisationCustomer;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** CRUD for customers (table customer) and their optional employment details (table employment_details). */
public class CustomerDAO {
    private static final String SELECT = "SELECT c.customer_id, c.customer_type, c.username, c.password_hash, "
            + "c.address, c.contact_details, c.date_registered, c.first_name, c.surname, c.id_number, "
            + "c.organisation_name, c.registration_number, c.registration_date, c.contact_person, "
            + "e.employer_name, e.employer_address "
            + "FROM customer c LEFT JOIN employment_details e ON e.customer_id = c.customer_id ";

    public void insert(Customer c) {
        Database.execute(conn -> {
            String sql = "INSERT INTO customer (customer_id, customer_type, username, password_hash, address, "
                    + "contact_details, date_registered, first_name, surname, id_number, organisation_name, "
                    + "registration_number, registration_date, contact_person) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, c.getCustomerId());
                ps.setString(3, c.getUsername());
                ps.setString(4, c.getPasswordHash());
                ps.setString(5, c.getAddress());
                ps.setString(6, c.getContactDetails());
                ps.setObject(7, c.getDateRegistered());
                if (c instanceof IndividualCustomer i) {
                    ps.setString(2, "INDIVIDUAL");
                    ps.setString(8, i.getFirstName());
                    ps.setString(9, i.getSurname());
                    ps.setString(10, i.getIdNumber());
                    ps.setNull(11, Types.VARCHAR);
                    ps.setNull(12, Types.VARCHAR);
                    ps.setNull(13, Types.DATE);
                    ps.setNull(14, Types.VARCHAR);
                } else if (c instanceof OrganisationCustomer o) {
                    ps.setString(2, "ORGANISATION");
                    ps.setNull(8, Types.VARCHAR);
                    ps.setNull(9, Types.VARCHAR);
                    ps.setNull(10, Types.VARCHAR);
                    ps.setString(11, o.getOrganisationName());
                    ps.setString(12, o.getRegistrationNumber());
                    ps.setObject(13, o.getRegistrationDate());
                    ps.setString(14, o.getContactPerson());
                }
                ps.executeUpdate();
            }
            if (c instanceof IndividualCustomer i) {
                saveEmployment(conn, i);
            }
            return null;
        });
    }

    public Customer findById(String customerId) { return findWhere("c.customer_id", customerId); }

    public Customer findByUsername(String username) { return findWhere("c.username", username); }

    public List<Customer> findAll() {
        return Database.execute(conn -> {
            List<Customer> list = new ArrayList<>();
            try (PreparedStatement ps = conn.prepareStatement(SELECT + "ORDER BY c.username");
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }
            return list;
        });
    }

    /** Saves changed employment details (a customer's address and contact details are fixed in the model). */
    public void update(Customer c) {
        Database.execute(conn -> {
            if (c instanceof IndividualCustomer i) {
                saveEmployment(conn, i);
            }
            return null;
        });
    }

    /** Deletes a customer together with their accounts, transactions and interest payments. */
    public void delete(String customerId) {
        Database.execute(conn -> {
            String[] statements = {
                "DELETE FROM interest_payment WHERE account_number IN (SELECT account_number FROM account WHERE customer_id=?)",
                "DELETE FROM bank_transaction WHERE account_number IN (SELECT account_number FROM account WHERE customer_id=?)",
                "DELETE FROM account WHERE customer_id=?",
                "DELETE FROM employment_details WHERE customer_id=?",
                "DELETE FROM customer WHERE customer_id=?"
            };
            for (String sql : statements) {
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setString(1, customerId);
                    ps.executeUpdate();
                }
            }
            return null;
        });
    }

    public boolean customerIdExists(String id) { return exists("customer_id", id); }
    public boolean usernameExists(String username) { return exists("username", username); }
    public boolean idNumberExists(String idNumber) { return exists("id_number", idNumber); }
    public boolean registrationNumberExists(String regNumber) { return exists("registration_number", regNumber); }

    public int count() {
        return Database.execute(conn -> {
            try (PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM customer");
                 ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        });
    }

    private void saveEmployment(Connection conn, IndividualCustomer c) throws SQLException {
        EmploymentDetails e = c.getEmploymentDetails();
        if (e != null && e.isComplete()) {
            try (PreparedStatement ps = conn.prepareStatement(
                    "MERGE INTO employment_details (customer_id, employer_name, employer_address) KEY (customer_id) VALUES (?,?,?)")) {
                ps.setString(1, c.getCustomerId());
                ps.setString(2, e.getEmployerName().trim());
                ps.setString(3, e.getEmployerAddress().trim());
                ps.executeUpdate();
            }
        } else {
            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM employment_details WHERE customer_id=?")) {
                ps.setString(1, c.getCustomerId());
                ps.executeUpdate();
            }
        }
    }

    private boolean exists(String column, String value) {
        return Database.execute(conn -> {
            try (PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM customer WHERE " + column + "=?")) {
                ps.setString(1, value);
                try (ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    return rs.getInt(1) > 0;
                }
            }
        });
    }

    private Customer findWhere(String column, String value) {
        return Database.execute(conn -> {
            try (PreparedStatement ps = conn.prepareStatement(SELECT + "WHERE " + column + "=?")) {
                ps.setString(1, value);
                try (ResultSet rs = ps.executeQuery()) {
                    return rs.next() ? map(rs) : null;
                }
            }
        });
    }

    private Customer map(ResultSet rs) throws SQLException {
        String id = rs.getString("customer_id");
        String username = rs.getString("username");
        String hash = rs.getString("password_hash");
        String address = rs.getString("address");
        String contact = rs.getString("contact_details");
        LocalDate registered = rs.getObject("date_registered", LocalDate.class);
        if ("INDIVIDUAL".equals(rs.getString("customer_type"))) {
            IndividualCustomer c = new IndividualCustomer(id, username, hash, address, contact, registered,
                    rs.getString("first_name"), rs.getString("surname"), rs.getString("id_number"));
            String employer = rs.getString("employer_name");
            if (employer != null) {
                c.setEmploymentDetails(new EmploymentDetails(employer, rs.getString("employer_address")));
            }
            return c;
        }
        return new OrganisationCustomer(id, username, hash, address, contact, registered,
                rs.getString("organisation_name"), rs.getString("registration_number"),
                rs.getObject("registration_date", LocalDate.class), rs.getString("contact_person"));
    }
}
