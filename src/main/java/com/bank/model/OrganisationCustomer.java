package com.bank.model;

import java.time.LocalDate;

public class OrganisationCustomer extends Customer {
    private final String organisationName;
    private final String registrationNumber;
    private final LocalDate registrationDate;
    private final String contactPerson;

    public OrganisationCustomer(String customerId, String username, String passwordHash,
                                String address, String contactDetails, LocalDate dateRegistered,
                                String organisationName, String registrationNumber,
                                LocalDate registrationDate, String contactPerson) {
        super(customerId, username, passwordHash, address, contactDetails, dateRegistered);
        this.organisationName = organisationName;
        this.registrationNumber = registrationNumber;
        this.registrationDate = registrationDate;
        this.contactPerson = contactPerson;
    }

    @Override
    public String getDisplayName() { return organisationName; }

    public String getOrganisationName() { return organisationName; }
    public String getRegistrationNumber() { return registrationNumber; }
    public LocalDate getRegistrationDate() { return registrationDate; }
    public String getContactPerson() { return contactPerson; }
}
