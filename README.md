# 🎓 University Management System (UMS)

A modern desktop-based **Two-Tier Architecture** University Management System developed in Java (Swing & AWT) with MySQL relational database persistence. The system automates campus operations and record-keeping with specialized, role-tailored portals for Administrators, Students, Employees, and Parents.

---

## 🏗️ Architectural Overview (2-Tier Architecture)

The system follows a direct **Client-Server (Two-Tier)** architectural pattern:

```
+-------------------------------------------------------------+
|                        TIER 1                               |
|               Client Layer (Presentation & UI)              |
|   • Java Swing GUI (Main, Admin, Student, Employee, Parent) |
|   • Custom Color Palette, Layouts & Table Models            |
|   • Client-side validation & local interaction rules        |
+-------------------------------------------------------------+
                              │
                              │  JDBC Driver (TCP/IP Port 3306)
                              │  Direct PreparedStatements / SQL
                              ▼
+-------------------------------------------------------------+
|                        TIER 2                               |
|               Data Layer (Database Management)              |
|   • Relational Database (MySQL)                             |
|   • Tables for Users, Courses, Fees, Marks & Notifications  |
|   • ACID-compliant persistent storage                       |
+-------------------------------------------------------------+
```

* **Tier 1 (Client Application):** Handles the presentation interface and event-driven logic (`JFrame`, `JTable`, `DefaultTableModel`, `JComboBox`) across each dedicated user portal.
* **Tier 2 (Database Layer):** MySQL server managing persistent institutional data, schema enforcement, and query handling via `DBConnection`.

---


##  DBMS Schema

The System uses the Following **Database Schema** :

```
                                    +-------------------+
                                    | Tables_in_ums_db  |
                                    +-------------------+
                                    | admin_users       |
                                    | attendance        |
                                    | completed_courses |
                                    | courses           |
                                    | enrollments       |
                                    | faculty           |
                                    | fee_records       |
                                    | marks             |
                                    | parents           |
                                    | students          |
                                    | system_requests   |
                                    +-------------------+
 Admin_user                                   
+---------------+--------------+------+-----+---------+-------+
| Field         | Type         | Null | Key | Default | Extra |
+---------------+--------------+------+-----+---------+-------+
| admin_id      | varchar(50)  | NO   | PRI | NULL    |       |
| password_hash | varchar(255) | NO   |     | NULL    |       |
| full_name     | varchar(100) | NO   |     | NULL    |       |
+---------------+--------------+------+-----+---------+-------+

attendance Table
+------------------+-------------+------+-----+---------+----------------+
| Field            | Type        | Null | Key | Default | Extra          |
+------------------+-------------+------+-----+---------+----------------+
| attendance_id    | int         | NO   | PRI | NULL    | auto_increment |
| student_reg_no   | varchar(20) | YES  | MUL | NULL    |                |
| course_code      | varchar(15) | YES  | MUL | NULL    |                |
| classes_attended | int         | YES  |     | 0       |                |
| total_classes    | int         | YES  |     | 0       |                |
+------------------+-------------+------+-----+---------+----------------+

completed_courses Table
+----------------+-------------+------+-----+---------+----------------+
| Field          | Type        | Null | Key | Default | Extra          |
+----------------+-------------+------+-----+---------+----------------+
| id             | int         | NO   | PRI | NULL    | auto_increment |
| reg_no         | varchar(20) | NO   | MUL | NULL    |                |
| course_code    | varchar(20) | NO   | MUL | NULL    |                |
| semester       | varchar(20) | NO   |     | NULL    |                |
| grade          | varchar(5)  | NO   |     | NULL    |                |
| credits_earned | int         | NO   |     | NULL    |                |
+----------------+-------------+------+-----+---------+----------------+


courses Table
+----------------+--------------+------+-----+---------+-------+
| Field          | Type         | Null | Key | Default | Extra |
+----------------+--------------+------+-----+---------+-------+
| course_code    | varchar(15)  | NO   | PRI | NULL    |       |
| course_title   | varchar(100) | NO   |     | NULL    |       |
| credits        | int          | NO   |     | NULL    |       |
| school         | varchar(100) | YES  |     | NULL    |       |
| faculty_emp_id | varchar(20)  | YES  | MUL | NULL    |       |
+----------------+--------------+------+-----+---------+-------+

enrollments Table
+-----------------+-------------+------+-----+-------------------+-------------------+
| Field           | Type        | Null | Key | Default           | Extra             |
+-----------------+-------------+------+-----+-------------------+-------------------+
| enrollment_id   | int         | NO   | PRI | NULL              | auto_increment    |
| reg_no          | varchar(20) | NO   | MUL | NULL              |                   |
| course_code     | varchar(20) | NO   | MUL | NULL              |                   |
| semester        | varchar(20) | YES  |     | Fall 2026         |                   |
| slot            | varchar(10) | YES  |     | NULL              |                   |
| enrollment_date | timestamp   | YES  |     | CURRENT_TIMESTAMP | DEFAULT_GENERATED |
+-----------------+-------------+------+-----+-------------------+-------------------+

faculty Table
+---------------+--------------+------+-----+---------+-------+
| Field         | Type         | Null | Key | Default | Extra |
+---------------+--------------+------+-----+---------+-------+
| emp_id        | varchar(20)  | NO   | PRI | NULL    |       |
| full_name     | varchar(100) | NO   |     | NULL    |       |
| password_hash | varchar(255) | NO   |     | NULL    |       |
| designation   | varchar(50)  | YES  |     | NULL    |       |
| school        | varchar(100) | YES  |     | NULL    |       |
| email         | varchar(100) | YES  | UNI | NULL    |       |
+---------------+--------------+------+-----+---------+-------+

fee_records Table
+----------------+------------------------+------+-----+---------+----------------+
| Field          | Type                   | Null | Key | Default | Extra          |
+----------------+------------------------+------+-----+---------+----------------+
| fee_id         | int                    | NO   | PRI | NULL    | auto_increment |
| student_reg_no | varchar(20)            | YES  | MUL | NULL    |                |
| description    | varchar(100)           | NO   |     | NULL    |                |
| amount         | decimal(10,2)          | NO   |     | NULL    |                |
| due_date       | date                   | NO   |     | NULL    |                |
| status         | enum('PAID','PENDING') | YES  |     | PENDING |                |
| receipt_no     | varchar(50)            | YES  |     | NULL    |                |
+----------------+------------------------+------+-----+---------+----------------+

marks Table
+-----------------+--------------+------+-----+---------+----------------+
| Field           | Type         | Null | Key | Default | Extra          |
+-----------------+--------------+------+-----+---------+----------------+
| mark_id         | int          | NO   | PRI | NULL    | auto_increment |
| student_reg_no  | varchar(20)  | YES  | MUL | NULL    |                |
| course_code     | varchar(15)  | YES  | MUL | NULL    |                |
| assessment_type | varchar(50)  | YES  |     | NULL    |                |
| max_marks       | int          | NO   |     | NULL    |                |
| scored_marks    | decimal(5,2) | NO   |     | NULL    |                |
+-----------------+--------------+------+-----+---------+----------------+

parents Table
+----------------------+--------------+------+-----+---------+-------+
| Field                | Type         | Null | Key | Default | Extra |
+----------------------+--------------+------+-----+---------+-------+
| student_reg_no       | varchar(20)  | NO   | PRI | NULL    |       |
| parent_password_hash | varchar(255) | NO   |     | NULL    |       |
+----------------------+--------------+------+-----+---------+-------+

system_requests Table
+-----------------+--------------------------------------------+------+-----+-------------------+-------------------+
| Field           | Type                                       | Null | Key | Default           | Extra             |
+-----------------+--------------------------------------------+------+-----+-------------------+-------------------+
| request_id      | int                                        | NO   | PRI | NULL              | auto_increment    |
| user_type       | enum('STUDENT','FACULTY','PARENT','STAFF') | NO   |     | NULL              |                   |
| user_id         | varchar(30)                                | NO   |     | NULL              |                   |
| request_type    | varchar(50)                                | NO   |     | NULL              |                   |
| request_details | text                                       | NO   |     | NULL              |                   |
| status          | enum('PENDING','APPROVED','REJECTED')      | YES  |     | PENDING           |                   |
| created_at      | timestamp                                  | YES  |     | CURRENT_TIMESTAMP | DEFAULT_GENERATED |
+-----------------+--------------------------------------------+------+-----+-------------------+-------------------+

students Table
+------------------+---------------------------------+------+-----+-------------+-------+
| Field            | Type                            | Null | Key | Default     | Extra |
+------------------+---------------------------------+------+-----+-------------+-------+
| reg_no           | varchar(20)                     | NO   | PRI | NULL        |       |
| full_name        | varchar(100)                    | NO   |     | NULL        |       |
| password_hash    | varchar(255)                    | NO   |     | NULL        |       |
| contact_no       | varchar(15)                     | YES  |     | NULL        |       |
| parent_name      | varchar(100)                    | YES  |     | NULL        |       |
| branch           | varchar(50)                     | YES  |     | NULL        |       |
| school           | varchar(100)                    | YES  |     | NULL        |       |
| residency_status | enum('Day Scholar','Hosteller') | YES  |     | Day Scholar |       |
| cgpa             | decimal(3,2)                    | YES  |     | 0.00        |       |
| credits_earned   | int                             | YES  |     | 0           |       |
+------------------+---------------------------------+------+-----+-------------+-------+
```

---


## 🖼️ Application Dashboards

### 1. Main Dashboard & Entry
> Central gateway to navigate between administrative, student, faculty, and parent portals.

![Main Dashboard](Images/Main_Portal.png)

---

### 2. Administrator Portal
> Complete administrative oversight for fee tracking, notifications, course additions, and user records.

![Admin Portal](Images/Admin_Portal(1).png)
![Admin Portal](Images/Admin_Portal(8).png)
![Admin Portal](Images/Admin_Portal(7).png)
![Admin Portal](Images/Admin_Portal(6).png)
![Admin Portal](Images/Admin_Portal(5).png)
![Admin Portal](Images/Admin_Portal(4).png)
![Admin Portal](Images/Admin_Portal(3).png)
![Admin Portal](Images/Admin_Portal(2).png)

---

### 3. Student Portal
> Self-service portal for enrolled students to view course registrations, attendance, semester grades, and dues.

![Student Portal](Images/Student_Portal(3).png)
![Student Portal](Images/Student_Portal(2).png)
![Student Portal](Images/Student_Portal(1).png)
![Student Portal](Images/Student_Portal(4).png)



---

### 4. Employee / Faculty Portal
> Dedicated interface for faculty and staff to update marks, manage class attendance, and view departmental circulars.

![Employee Portal](Images/Employee_Portal(4).png)
![Employee Portal](Images/Employee_Portal(3).png)
![Employee Portal](Images/Employee_Portal(1).png)
![Employee Portal](Images/Employee_Portal(2).png)
![Employee Portal](Images/Employee_Portal(5).png)
![Employee Portal](Images/Employee_Portal(6).png)

---

### 5. Parent Portal
> Dedicated interface for enrolled student's Parent to view Student Performance including their attendance, Semester grades and mark, Registered Courses and dues of Their Ward.

![Parent Portal](Images/Parent_Portal(1).png)
![Parent Portal](Images/Parent_Portal(2).png)
![Parent Portal](Images/Parent_Portal(3).png)
![Parent Portal](Images/Parent_Portal(4).png)
![Parent Portal](Images/Parent_Portal(5).png)
![Parent Portal](Images/Parent_Portal(6).png)
![Parent Portal](Images/Parent_Portal(7).png)
![Parent Portal](Images/Parent_Portal(8).png)

> ---

## ✨ Key Features & Portals

- **Admin Portal (`AdminFrame`):** Central management of faculty records, parent communication, student enrollments, fee structures, and campus-wide notifications.
- **Student Portal (`StudentFrame`):** View registered subjects, internal marks, attendance summaries, and pending fee status.
- **Employee Portal (`EmployeeFrame`):** Faculty toolset for inputting student marks, viewing schedules, and logging course attendance.
- **Parent Portal (`ParentFrame`):** Direct progress tracking, fee receipts, and official institutional announcements.
- **Database Central (`DBConnection`):** Connection pool handling optimized SQL queries and transactional integrity across all frames.

---

## 🛠️ Technology Stack

| Layer | Technology |
| :--- | :--- |
| **Language** | Java (JDK 21 / 24) |
| **UI Framework** | Java Swing & AWT (`javax.swing.*`, `java.awt.*`) |
| **Database** | MySQL Server |
| **Database Driver** | MySQL Connector/J (`JDBC`) |
| **IDE** | IntelliJ IDEA |

---

## 📂 Project Structure

```text
University Management System/
├── Images/
│   ├── main.png                # Main Dashboard Preview
│   ├── admin.png               # Administrator Portal Preview
│   ├── student.png             # Student Portal Preview
│   └── employee.png            # Employee Portal Preview
├── Project_Files/
│   ├── MySQL Queries Used      # SQL scripts & database schema
│   ├── src/
│   │   └── main/
│   │       ├── java/
│   │       │   └── in/
│   │       │       └── mak/
│   │       │           ├── AdminFrame.java     # Administrator Portal GUI & logic
│   │       │           ├── DBConnection.java   # MySQL JDBC Connection handler
│   │       │           ├── EmployeeFrame.java  # Faculty / Staff Portal
│   │       │           ├── Main.java           # Application Entry Point
│   │       │           ├── ParentFrame.java    # Parent Portal interface
│   │       │           └── StudentFrame.java   # Student Portal interface
│   │       └── resources/                      # Icons, fonts & static UI assets
│   └── pom.xml                                 # Build & dependency configuration
└── README.md
```

---

## ⚙️ Setup & Installation

### 1. Database Configuration
1. Open your MySQL client and execute the scripts located in `Project_Files/MySQL Queries Used`:
   ```sql
   CREATE DATABASE IF NOT EXISTS university_management_system;
   USE university_management_system;
   ```
2. Verify database connection credentials inside `in/mak/DBConnection.java`:
   ```java
   private static final String URL = "jdbc:mysql://localhost:3306/university_management_system";
   private static final String USER = "your_mysql_username";
   private static final String PASSWORD = "your_mysql_password";
   ```

### 2. Running in IntelliJ IDEA
1. Open IntelliJ IDEA and select the `Project_Files` directory (or root project directory).
2. Ensure Project SDK is configured: **File → Project Structure → Project → SDK (Java 21/24)**.
3. Open `src/main/java/in/mak/Main.java`.
4. Right-click and select **Run 'Main'** (or press `Shift + F10`).

---

## 📜 License
Distributed under the MIT License.
