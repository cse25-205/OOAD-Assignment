# Integration Test Report

**Student:** Lone Kufuna  **Registration number:** ____  
**Module:** CSE202 Object Oriented Analysis & Design with Java  **Date run:** 20 November 2026

## Approach

`BankingIntegrationTest` exercises the real controllers, domain model, DAOs and an in-memory H2 database
(no mocks). Each test starts at a controller call, exactly as a JavaFX screen would make it, and then
reads the database back to confirm the stored state. The database is reset before every test.
`ModelSmokeJUnitTest` runs the 26 model checks from `ModelSmokeTest` (OOP rules, interest, the interest payment state machine).

Run with `mvn clean test`. The same result was obtained in IntelliJ IDEA on Windows and in a fresh GitHub Codespace
(Java 21).

## Test cases

| ID | User flow tested | Expected outcome | Requirement | Result |
|---|---|---|---|---|
| IT01 | Register, then log in | Login succeeds, display name correct | FR1, FR2 | Pass |
| IT02 | Wrong password, unknown user, empty fields | Same generic error each time | FR17 | Pass |
| IT03 | Inspect stored password | Stored as the 64-character SHA-256 hash, not plain text | NFR Security | Pass |
| IT04 | Duplicate username, duplicate ID, missing field | Rejected with the cause named | FR19 | Pass |
| IT05 | Open Savings, deposit 250.50 | DB balance 350.50, history has 2 entries, newest first | FR8, FR12 | Pass |
| IT06 | Deposit "", "abc", 0, -5, 1.005 | All rejected, DB balance unchanged | FR10 | Pass |
| IT07 | Withdraw from Savings | Rejected, balance unchanged | FR5 | Pass |
| IT08 | Investment with 499.99, blank, 500.00 | First two rejected, 500.00 accepted | FR6 | Pass |
| IT09 | Withdraw 100 then 500.01 from Investment of 600 | First works (500.00 left), second rejected | FR9, FR10 | Pass |
| IT10 | Cheque for unemployed, employed, organisation | Only the employed individual succeeds | FR3, FR7 | Pass |
| IT11 | Open 2 Savings and 1 Investment | Customer holds 3 accounts | FR4 | Pass |
| IT12 | Customer B uses Customer A's account | Rejected, A's balance unchanged | NFR Security | Pass |
| IT13 | Use accounts after logout | Rejected | FR2, FR18 | Pass |
| IT14 | Idle 4 min, then 10 min | Valid at 4, ended at 10 | NFR Security | Pass |
| IT15 | Run interest twice for one month, then next month | 0.50 and 50.00 once; second run credits nothing; Cheque none | FR11, FR14 | Pass |
| IT16 | Load sample data twice, log in as sample user | 10 customers, 17 accounts, loaded once | Brief: sample records | Pass |

## Results

```
Tests run: 17, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

16 integration tests (IT01 to IT16) plus 1 test (`ModelSmokeJUnitTest`) that runs the 26 model checks.
All passed. No defects were found in this test round.

## Observations and limitations

- The JavaFX screens are not driven by an automated GUI robot. They contain no logic, so the tests start at
  the controller. They were checked by hand instead (see the checklist below).
- Interest scheduling (day 1 of each month) is covered by running `InterestController` directly.

## Manual walkthrough of the GUI

Tick each item once you have done it on the running application.

- [ ] Registered a new individual and a new organisation, then logged in as each
- [ ] Wrong password and unknown username both show the same message
- [ ] Opened Savings, Investment (400.00 refused, 500.00 accepted) and Cheque accounts
- [ ] Deposit and withdrawal updated the balance; withdrawal from Savings was refused; overdraft was refused
- [ ] History tab lists transactions newest first
- [ ] Interest button credited once; pressing it a second time credited nothing
- [ ] Password eye icon shows and hides the typed password
- [ ] Theme drop-down switches between Botswana Blue and Dark Mode
- [ ] Log out returns to the login screen
