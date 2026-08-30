# 🎓 University Management System (UMS) — Version 2.0 🚀

[![Java Version](https://img.shields.io/badge/Java-24-orange.svg)](https://www.oracle.com/java/)
[![Architecture](https://img.shields.io/badge/Architecture-2--Tier%20Client--Server-blue.svg)]()
[![Database](https://img.shields.io/badge/Database-MySQL-blue.svg)](https://www.mysql.com/)
[![License: MIT](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)
[![Release](https://img.shields.io/badge/Release-v2.0.0-success.svg)]()

A robust, enterprise-grade desktop **Two-Tier Architecture** University Management System developed in Java (Swing/AWT) with MySQL relational database integration. Version 2.0 builds upon the core foundation of v1.0, introducing enhanced UI ergonomics, role-isolated portals, advanced query filtering, automated calculations, and expanded data models.

---

## 🌟 What's New in Version 2.0

* **Enhanced UI & Color Palette:** Re-engineered with custom brand gradients, responsive component borders, and flat design aesthetics.
* **Granular Role Segregation:** Dedicated, isolated modules for **Administrator**, **Student**, **Employee / Faculty**, and **Parent** interactions.
* **Dynamic Table Models & Filtering:** Integrated combo-box and search filters (`requestFilterCombo`, custom `DefaultTableModel` instances) for fast data retrieval.
* **Automated Record Processing:** Streamlined automated fee receipts, pending payment ledgers, and GPA/marks aggregations.
* **Optimized Database Connections:** Enhanced connection lifecycle management in `DBConnection` using sanitized `PreparedStatement` queries to prevent SQL injections.

---

## 🏗️ Two-Tier System Architecture

```
+-----------------------------------------------------------------------+
|                                TIER 1                                 |
|                 Client Application & Presentation Layer               |
|                                                                       |
|   ┌───────────────────────────────────────────────────────────────┐   |
|   │                        Main Dashboard                         │   |
|   └───────────────┬───────────────┬───────────────┬───────────────┘   |
|                   │               │               │                   |
|           ┌───────▼──────┐ ┌──────▼──────┐ ┌──────▼──────┐ ┌──────▼─┐ |
|           │  AdminFrame  │ │StudentFrame │ │EmployeeFrame│ │ParentF │ |
|           └───────┬──────┘ └──────┬──────┘ └──────┬──────┘ └──────┬─┘ |
|                   └───────────────┼───────────────┘               │   |
|                                   ▼                                   |
|                        in.mak.DBConnection                            |
+--------------------------------───┬───────────────────────────────────+
                                    │
                                    │ JDBC Driver (TCP/IP Port 3306)
                                    │ PreparedStatements & Batch Updates
                                    ▼
+-----------------------------------------------------------------------+
|                                TIER 2                                 |
|                       Data Persistence Layer                          |
|                                                                       |
|   ┌───────────────────────────────────────────────────────────────┐   |
|   │                    MySQL Relational Database                  │   |
|   │  • Faculty & Staff Directory    • Student Academic Records    │   |
|   │  • Course Offerings & Credits   • Fee Accounts & Receipts     │   |
|   │  • Parent Profiles & Contacts   • Campus Announcements        │   |
|   └───────────────────────────────────────────────────────────────┘   |
+-----------------------------------------------------------------------+
```

---

## 🖼️ Application Dashboards

### 1. Main Navigation Dashboard
> Central launchpad routing users to role-specific interfaces with verified credentials.

![Main Dashboard](Images/main.png)

---

### 2. Administrator Portal (`AdminFrame`)
> Full administrative governance for student enrollments, fee structures, faculty allotments, course catalogs, and circulars.

![Administrator Portal](Images/admin.png)

---

### 3. Student Portal (`StudentFrame`)
> Student self-service console for tracking semester courses, attendance statistics, exam grades, and outstanding dues.

![Student Portal](Images/student.png)

---

### 4. Employee / Faculty Portal (`EmployeeFrame`)
> Faculty workstation for grading submissions, attendance logging, and departmental circular monitoring.

![Employee Portal](Images/employee.png)

---

## 📂 Project Structure

```text
University-Management-System/
├── Images/
│   ├── main.png                # Main Dashboard Screenshot
│   ├── admin.png               # Admin Portal Screenshot
│   ├── student.png             # Student Portal Screenshot
│   └── employee.png            # Employee Portal Screenshot
├── Project_Files/
│   ├── MySQL Queries Used      # Schema definitions, DDL/DML SQL scripts
│   ├── src/
│   │   └── main/
│   │       ├── java/
│   │       │   └── in/
│   │       │       └── mak/
│   │       │           ├── AdminFrame.java     # Administrator Portal GUI & logic
│   │       │           ├── DBConnection.java   # MySQL JDBC connection manager
│   │       │           ├── EmployeeFrame.java  # Faculty / Employee Portal
│   │       │           ├── Main.java           # Application Bootstrap & Hub UI
│   │       │           ├── ParentFrame.java    # Parent Portal interface
│   │       │           └── StudentFrame.java   # Student Portal interface
│   │       └── resources/                      # Fonts, static assets, and icons
│   └── pom.xml                                 # Maven dependencies & build lifecycle
├── LICENSE
└── README.md
```

---

## 🛠️ Tech Stack & Requirements

* **Language:** Java 24 (compatible with JDK 17+)
* **GUI Engine:** Java Swing (`JFrame`, `JPanel`, `JTable`, `GridBagLayout`, `Custom Gradients`) & AWT
* **Database Engine:** MySQL Server 8.0+
* **Connectivity:** MySQL Connector/J (`com.mysql:mysql-connector-j:9.x`)
* **Build System:** Apache Maven 3.9+
* **IDE:** IntelliJ IDEA 2026.2+

---

## ⚙️ Installation & Setup

### 1. Clone the Repository
```bash
git clone https://github.com/Ankur122005/University-Management-System.git
cd University-Management-System
```

### 2. Database Initialization
1. Start your local MySQL service.
2. Run the SQL initialization scripts located in `Project_Files/MySQL Queries Used` inside MySQL Workbench, CLI, or phpMyAdmin:
   ```sql
   CREATE DATABASE IF NOT EXISTS university_management_system;
   USE university_management_system;
   -- Source the included query scripts to generate schema tables
   ```
3. Update connection credentials in `src/main/java/in/mak/DBConnection.java`:
   ```java
   private static final String URL = "jdbc:mysql://localhost:3306/university_management_system";
   private static final String USER = "your_mysql_username";
   private static final String PASSWORD = "your_mysql_password";
   ```

### 3. Build and Launch

#### Via IntelliJ IDEA:
1. Open the project root folder in IntelliJ IDEA.
2. Ensure the JDK is set to Java 21/24 (**File → Project Structure → Project → SDK**).
3. Open `src/main/java/in/mak/Main.java`.
4. Click **Run** (`Shift + F10`).

#### Via Maven CLI:
```bash
cd Project_Files
mvn clean compile
mvn exec:java -Dexec.mainClass="in.mak.Main"
```

---

## 🗺️ Roadmap & Future Enhancements (v2.x & Beyond)

- [ ] Export reports to PDF/Excel (Fee slips, mark sheets).
- [ ] Direct email notifications via JavaMail API.
- [ ] Password hashing using BCrypt for database credentials.
- [ ] Automated dark mode / dynamic theme toggler.

---

## 🤝 Contributing
1. Fork the repository.
2. Create a feature branch (`git checkout -b feature/NewFeature`).
3. Commit your changes (`git commit -m 'Add NewFeature'`).
4. Push to the branch (`git push origin feature/NewFeature`).
5. Open a Pull Request.

---

## 📜 License
This project is open-source and available under the [MIT License](LICENSE).
