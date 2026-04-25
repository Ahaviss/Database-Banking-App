# Banking App - Ahaviss

A comprehensive Command Line Interface (CLI) banking system designed with a focus on **Defensive Programming** and **Object-Oriented Design**. This project simulates a professional financial environment with tiered access and secure transaction logic.

---

## 🚀 Project Overview

This project is a multi-layered banking application that manages complex financial operations. It features a robust role hierarchy, extensive input validation, and a modular architecture spread across multiple packages.

### Key Features
* **Role Hierarchy & Permissions:**
    * **User:** Standard operations (Withdraw, Deposit, Transfers).
    * **Admin:** System oversight; can edit account details and view metadata.
    * **Owner:** Superuser status; full authority to add, edit, and delete accounts.
* **Core Functions:** Secure internal transfers between accounts.
    * Real-time balance updates and transaction logging.
    * Account status management (e.g., `ACTIVE` vs. `LOCKED`).
* **Input Validation:** Use of **Regular Expressions (Regex)** and conditional logic to prevent malformed data and system crashes.

---

## 🛠 Technical Implementation

This project serves as a practical application of advanced Java concepts:

* **OOP Principles:** Demonstrates **Composition**, **Encapsulation**, and **Object Identity**.
* **Generics:** Implemented for flexible data handling and storage.
* **Scale:** Consists of **~1,500-1,800 lines of code** (excluding comments) organized into a clean, multi-package structure.
* **Defensive Coding:** Heavy emphasis on preventing `Exceptions` through proactive data checking.

---

## 💻 Getting Started

### Prerequisites
* **Java Development Kit (JDK) 23** (recommended for compatibility) as before JDK 22 it will give a compile-time error in AccountLockedException.java.
* An IDE such as **IntelliJ IDEA**.

### How to Run
Because the project uses a multi-package structure, running it via an IDE is the most straightforward method:

1. Clone the repository or download the source code.
2. Open the project folder in **IntelliJ IDEA**.
3. Navigate to the `Main.java` file and click **Run**.

#### How to Change Owner Password
To change the owner password:
1. Login as admin using tempUsername@123 for username and tempPassword@123 for password.
2. Go into "owner panel" and "edit owner account".
3. Type in the current password as specified before.
4. Type in your new username and password.

---

## Updates

* MAJOR: Added File I/O and saving data.
* MAJOR: Added basic SHA-256 password hashing.
* Added a killswitch in owner panel.
* Data is now saved with shutdown hooks.
* Removed hardcoded owner password and username.
* Added custom exceptions instead of magic numbers.
* MAJOR: Added audit logs for the owner and is saved using File I/O.
* Added control flow to avoid System.exit(0).
* MAJOR: Added time-based account lockouts instead of permanent lockouts
* MAJOR:
* a) Moved build tool to Maven.
* b) Using JSON via Jackson to save data instead of serialization
* c) Hashing passwords now includes a salt.
* d) JSON is encrypted with AES-256.
---

*Developed by Ahaviss - 2026*
