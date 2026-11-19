# Integration Test Report

**Student:** Lone Miso Kufuna (cse25-205)  **Module:** CSE202  **Date run:** ____

## Approach

`BankingIntegrationTest` exercises the real controllers, domain model, DAOs and an in-memory H2 database
(no mocks). Each test starts at a controller call, exactly as a JavaFX screen would make it, and then
reads the database back to confirm the stored state. The database is reset before every test.
`ModelSmokeJUnitTest` runs the 26 model checks from `ModelSmokeTest` (OOP rules, interest, the interest payment state machine).

Run with `mvn clean test`. Paste the final summary of that command in the Results section.

## Test cases

| ID | User flow tested | Expected outcome | Requirement | Result |
|---|---|---|---|---|
| IT01 | Register, then log in | Login succeeds, display name correct | FR1, FR2 | |
| IT02 | Wrong password, unknown user, empty fields | Same generic error each time | FR17 | |
| IT03 | Inspect stored password | Stored as the 64-character SHA-256 hash, not plain text | NFR Security | |
| IT04 | Duplicate username, duplicate ID, missing field | Rejected with the cause named | FR19 | |
| IT05 | Open Savings, deposit 250.50 | DB balance 350.50, history has 2 entries, newest first | FR8, FR12 | |
| IT06 | Deposit "", "abc", 0, -5, 1.005 | All rejected, DB balance unchanged | FR10 | |
| IT07 | Withdraw from Savings | Rejected, balance unchanged | FR5 | |
| IT08 | Investment with 499.99, blank, 500.00 | First two rejected, 500.00 accepted | FR6 | |
| IT09 | Withdraw 100 then 500.01 from Investment of 600 | First works (500.00 left), second rejected | FR9, FR10 | |
| IT10 | Cheque for unemployed, employed, organisation | Only the employed individual succeeds | FR3, FR7 | |
| IT11 | Open 2 Savings and 1 Investment | Customer holds 3 accounts | FR4 | |
| IT12 | Customer B uses Customer A's account | Rejected, A's balance unchanged | NFR Security | |
| IT13 | Use accounts after logout | Rejected | FR2, FR18 | |
| IT14 | Idle 4 min, then 10 min | Valid at 4, ended at 10 | NFR Security | |
| IT15 | Run interest twice for one month, then next month | 0.50 and 50.00 once; second run credits nothing; Cheque none | FR11, FR14 | |
| IT16 | Load sample data twice, log in as sample user | 10 customers, 17 accounts, loaded once | Brief: sample records | |

## Results

```
(paste the "Tests run: 17, Failures: 0, Errors: 0, Skipped: 0" summary from mvn clean test here)
```

## Observations and limitations

- The JavaFX screens are not driven by an automated GUI robot. They contain no logic, so the tests start at
  the controller. Walk through login, deposit, withdraw and history by hand and note it here.
- Interest scheduling (day 1 of each month) is covered by running `InterestController` directly.
