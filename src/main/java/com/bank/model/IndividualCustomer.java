package com.bank.model;

import java.time.LocalDate;

public class IndividualCustomer extends Customer {
    private final String firstName;
    private final String surname;
    private final String idNumber;
    private EmploymentDetails employmentDetails; // 0..1

    public IndividualCustomer(String customerId, String username, String passwordHash,
                              String address, String contactDetails, LocalDate dateRegistered,
                              String firstName, String surname, String idNumber) {
        super(customerId, username, passwordHash, address, contactDetails, dateRegistered);
        this.firstName = firstName;
        this.surname = surname;
        this.idNumber = idNumber;
    }

    @Override
    public String getDisplayName() { return firstName + " " + surname; }

    public boolean isEmployed() {
        return employmentDetails != null && employmentDetails.isComplete();
    }

    public EmploymentDetails getEmploymentDetails() { return employmentDetails; }
    public void setEmploymentDetails(EmploymentDetails details) { this.employmentDetails = details; }

    public String getFirstName() { return firstName; }
    public String getSurname() { return surname; }
    public String getIdNumber() { return idNumber; }
}
