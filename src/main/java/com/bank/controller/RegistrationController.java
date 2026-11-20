package com.bank.controller;

import com.bank.dao.CustomerDAO;
import com.bank.model.Customer;
import com.bank.model.EmploymentDetails;
import com.bank.model.IndividualCustomer;
import com.bank.model.OrganisationCustomer;
import com.bank.util.BankException;
import com.bank.util.Validate;
import java.security.SecureRandom;
import java.time.LocalDate;

//Validates registration requests (FR19) and creates individual or organisation customers.
public class RegistrationController {
    private static final int MIN_PASSWORD_LENGTH = 8;
    private static final SecureRandom RANDOM = new SecureRandom();
    private final CustomerDAO customerDAO;

    public RegistrationController(CustomerDAO customerDAO) {
        this.customerDAO = customerDAO;
    }

    public Customer registerIndividual(String username, String password, String firstName, String surname,
                                       String idNumber, String address, String contactDetails,
                                       String employerName, String employerAddress) {
        String user = checkUsernameAndPassword(username, password);
        String first = Validate.required(firstName, "first name");
        String last = Validate.required(surname, "surname");
        String id = Validate.required(idNumber, "ID number");
        String addr = Validate.required(address, "address");
        String contact = Validate.required(contactDetails, "contact details");
        if (customerDAO.idNumberExists(id)) {
            throw new BankException("Registration rejected: that ID number is already registered.");
        }
        EmploymentDetails employment = parseEmployment(employerName, employerAddress);

        IndividualCustomer customer = new IndividualCustomer(nextCustomerId(), user,
                Customer.hashPassword(password), addr, contact, LocalDate.now(), first, last, id);
        customer.setEmploymentDetails(employment);
        customerDAO.insert(customer);
        return customer;
    }

    public Customer registerOrganisation(String username, String password, String organisationName,
                                         String registrationNumber, LocalDate registrationDate, String address,
                                         String contactDetails, String contactPerson) {
        String user = checkUsernameAndPassword(username, password);
        String name = Validate.required(organisationName, "organisation name");
        String regNo = Validate.required(registrationNumber, "registration number");
        if (registrationDate == null) {
            throw new BankException("Missing required detail: date of registration.");
        }
        String addr = Validate.required(address, "address");
        String contact = Validate.required(contactDetails, "contact details");
        String person = Validate.required(contactPerson, "contact person");
        if (customerDAO.registrationNumberExists(regNo)) {
            throw new BankException("Registration rejected: that registration number is already registered.");
        }
        OrganisationCustomer customer = new OrganisationCustomer(nextCustomerId(), user,
                Customer.hashPassword(password), addr, contact, LocalDate.now(), name, regNo,
                registrationDate, person);
        customerDAO.insert(customer);
        return customer;
    }

    private String checkUsernameAndPassword(String username, String password) {
        String user = Validate.required(username, "username").toLowerCase();
        if (password == null || password.isEmpty()) {
            throw new BankException("Missing required detail: password.");
        }
        if (password.length() < MIN_PASSWORD_LENGTH) {
            throw new BankException("The password must be at least " + MIN_PASSWORD_LENGTH + " characters long.");
        }
        if (customerDAO.usernameExists(user)) {
            throw new BankException("Registration rejected: that username is already in use.");
        }
        return user;
    }

    private EmploymentDetails parseEmployment(String name, String address) {
        boolean hasName = name != null && !name.isBlank();
        boolean hasAddress = address != null && !address.isBlank();
        if (hasName != hasAddress) {
            throw new BankException("Provide both the employer's name and address, or leave both blank.");
        }
        return hasName ? new EmploymentDetails(name.trim(), address.trim()) : null;
    }

    private String nextCustomerId() {
        String id;
        do {
            id = "C" + String.format("%08d", RANDOM.nextInt(100_000_000));
        } while (customerDAO.customerIdExists(id));
        return id;
    }
}
