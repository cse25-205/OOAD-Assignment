package com.bank.model;

public class EmploymentDetails {
    private final String employerName;
    private final String employerAddress;

    public EmploymentDetails(String employerName, String employerAddress) {
        this.employerName = employerName;
        this.employerAddress = employerAddress;
    }

    public String getEmployerName() { return employerName; }
    public String getEmployerAddress() { return employerAddress; }

    // True only if both the employer name and address are filled in.
    public boolean isComplete() {
        return employerName != null && !employerName.trim().isEmpty()
            && employerAddress != null && !employerAddress.trim().isEmpty();
    }
}
