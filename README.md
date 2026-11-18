# Banking System (CSE202 OOAD with Java)

JavaFX desktop banking application: customers (individuals and organisations) register, open
Savings / Investment / Cheque accounts, deposit, withdraw, view history; monthly interest runs
through the System Scheduler.

## Requirements
- Java 21, Maven 3.9+
- JavaFX 21 and H2 2.2.224 (both downloaded by Maven)

## Build, test and run
    mvn clean test        # 17 tests: 16 integration tests + the model smoke checks
    mvn javafx:run        # starts the GUI

In GitHub Codespaces the GUI needs the desktop-lite feature (see `.devcontainer/devcontainer.json`)
and port 6080: open it from the Ports tab (password `vscode`), then run `mvn javafx:run` there.

Quick model-only check without Maven:

    javac -d out src/main/java/com/bank/model/*.java src/test/java/com/bank/model/ModelSmokeTest.java
    java -cp out com.bank.model.ModelSmokeTest

## Sample data
On first start the app creates `./data/bank` (H2 file database, schema from
`src/main/resources/schema.sql`) and inserts 5 customers, 9 accounts and their transactions.
Every sample user has the password `Bank@2026`.

| Username | Customer | Accounts |
|---|---|---|
| thato | Individual, employed | Savings, Investment, Cheque |
| neo | Individual, not employed | Savings |
| kabo | Individual, employed | Cheque, Investment |
| kalahari | Organisation | Savings, Investment |
| baobab | Organisation | Savings |

## Layers
    com.bank.model       domain classes (the starter model, unchanged)
    com.bank.dao         JDBC: Database, CustomerDAO, AccountDAO, TransactionDAO, InterestPaymentDAO, SampleDataLoader
    com.bank.controller  LoginController, RegistrationController, AccountController, InterestController,
                         InterestScheduler, AppContext (wiring). No JavaFX in here.
    com.bank.view        JavaFX boundary classes: MainApp, LoginView, RegisterView, DashboardView
    com.bank.util        BankException (message shown to the user), Money, Validate

Views call controllers only. Controllers call the model and DAOs. The model knows nothing about either.
The model signals rule violations with IllegalArgumentException / IllegalStateException; the
AccountController turns those into BankExceptions with the same message.

## Business rules and where they live
| Rule | Where |
|---|---|
| Investment opening deposit at least BWP500.00 | `InvestmentAccount` constructor |
| Cheque only for an employed individual | `ChequeAccount` constructor |
| Savings cannot withdraw | `SavingsAccount` does not implement `Withdrawable` |
| Amounts above zero, at most 2 decimals, no overdraft | `Account.requireValidAmount`, `Account.debit` |
| Interest 0.05% Savings, 5% Investment, none for Cheque | `SavingsAccount`, `InvestmentAccount` |
| One interest credit per account per month (FR14) | `Account.applyInterest` and `UNIQUE (account_number, period)` |
| Duplicate username / ID number / registration number (FR19) | `RegistrationController` checks first, schema enforces |
| Hashed passwords, generic login error, 5-minute session | `Customer.hashPassword`, `LoginController` |
| Balance and its transaction saved together | `AccountDAO.applyTransaction`, `applyInterest` (one JDBC transaction) |

## Database
`schema.sql` creates `customer`, `employment_details`, `account`, `bank_transaction` and
`interest_payment`. It is read by `Database.init()` on every start (`CREATE TABLE IF NOT EXISTS`).
