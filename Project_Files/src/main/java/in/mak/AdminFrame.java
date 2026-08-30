package in.mak;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
// This is the admin portal.
public class AdminFrame extends JFrame {

    // --- DATABASE CONFIGURATION ---
    private static final String DB_URL = "jdbc:mysql://localhost:3306/vtop_db?useSSL=false&allowPublicKeyRetrieval=true";
    private static final String DB_USER = "root";     // Adjust if needed
    private static final String DB_PASS = "admin321";   // Adjust to your MySQL password

    private CardLayout mainCardLayout;
    private JPanel mainContainer;

    // Login Form Fields
    private JTextField adminIdField;
    private JPasswordField adminPasswordField;
    private JTextField captchaInputField;
    private CaptchaPanel captchaDisplayPanel;

    // Dashboard Navigation
    private CardLayout dashboardCardLayout;
    private JPanel dashboardContentPanel;

    // Dynamic Labels for System Metrics
    private JLabel totalStudentsVal;
    private JLabel totalFacultyVal;
    private JLabel feeCollectionVal;
    private JLabel dbStatusVal;

    // Tables and Models
    private JTable studentTable;
    private JTable facultyTable;
    private DefaultTableModel studentTableModel;
    private DefaultTableModel facultyTableModel;
    private DefaultTableModel parentTableModel;
    private DefaultTableModel feeTableModel;
    private DefaultTableModel courseTableModel;
    private DefaultTableModel notificationsTableModel;

    private JComboBox<String> requestFilterCombo;

    // UTOP Color Palette
    private static final Color PRIMARY_BLUE = new Color(13, 110, 253);
    private static final Color HEADER_NAV_BLUE = new Color(24, 43, 73);
    private static final Color BG_GRAY = new Color(245, 247, 250);
    private static final Color BORDER_GRAY = new Color(205, 210, 218);
    private static final Color SUCCESS_GREEN = new Color(25, 135, 84);
    private static final Color DANGER_RED = new Color(220, 53, 69);

    public AdminFrame() {
        setTitle("Administrator Portal - UTOP System Management");
        setSize(1120, 740);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        mainCardLayout = new CardLayout();
        mainContainer = new JPanel(mainCardLayout);

        mainContainer.add(createLoginView(), "LOGIN");
        mainContainer.add(createDashboardView(), "DASHBOARD");

        add(mainContainer);
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
    }

    // =========================================================================
    // 1. ADMIN LOGIN VIEW
    // =========================================================================
    private JPanel createLoginView() {
        JPanel backgroundPanel = new JPanel(new GridBagLayout());
        backgroundPanel.setBackground(BG_GRAY);

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_GRAY, 1),
                BorderFactory.createEmptyBorder(25, 35, 25, 35)
        ));
        card.setBackground(Color.WHITE);
        card.setPreferredSize(new Dimension(420, 520));

        JLabel titleLabel = new JLabel("UTOP Admin Login");
        titleLabel.setFont(new Font("Serif", Font.BOLD, 24));
        titleLabel.setForeground(new Color(30, 30, 30));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        adminIdField = createStyledTextField();
        adminPasswordField = createStyledPasswordField();
        captchaInputField = createStyledTextField();

        captchaDisplayPanel = new CaptchaPanel();

        JButton refreshBtn = new JButton("↻");
        refreshBtn.setFont(new Font("SansSerif", Font.BOLD, 18));
        refreshBtn.setBackground(new Color(15, 120, 70));
        refreshBtn.setForeground(Color.WHITE);
        refreshBtn.setOpaque(true);
        refreshBtn.setContentAreaFilled(true);
        refreshBtn.setBorderPainted(false);
        refreshBtn.setFocusPainted(false);
        refreshBtn.setPreferredSize(new Dimension(45, 38));
        refreshBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        refreshBtn.addActionListener(e -> captchaDisplayPanel.regenerateCaptcha());

        JPanel captchaBox = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        captchaBox.setOpaque(false);
        captchaBox.setAlignmentX(Component.CENTER_ALIGNMENT);
        captchaBox.add(captchaDisplayPanel);
        captchaBox.add(refreshBtn);

        JButton submitBtn = new JButton("Admin Sign In");
        submitBtn.setFont(new Font("SansSerif", Font.BOLD, 15));
        submitBtn.setBackground(PRIMARY_BLUE);
        submitBtn.setForeground(Color.WHITE);
        submitBtn.setOpaque(true);
        submitBtn.setContentAreaFilled(true);
        submitBtn.setBorderPainted(false);
        submitBtn.setFocusPainted(false);
        submitBtn.setMaximumSize(new Dimension(350, 42));
        submitBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        submitBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        submitBtn.addActionListener(e -> processLogin());

        JPanel linksPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        linksPanel.setOpaque(false);
        JLabel forgotPass = createHoverLink("Reset Credentials");
        linksPanel.add(forgotPass);

        card.add(titleLabel);
        card.add(Box.createRigidArea(new Dimension(0, 20)));
        card.add(createInputGroup("Admin ID / Username", adminIdField));
        card.add(Box.createRigidArea(new Dimension(0, 12)));
        card.add(createInputGroup("Master Password", adminPasswordField));
        card.add(Box.createRigidArea(new Dimension(0, 15)));
        card.add(captchaBox);
        card.add(Box.createRigidArea(new Dimension(0, 12)));
        card.add(createInputGroup("Enter CAPTCHA shown above", captchaInputField));
        card.add(Box.createRigidArea(new Dimension(0, 20)));
        card.add(submitBtn);
        card.add(Box.createRigidArea(new Dimension(0, 15)));
        card.add(linksPanel);

        backgroundPanel.add(card);
        return backgroundPanel;
    }

    private JPanel createInputGroup(String labelText, JComponent inputComponent) {
        JPanel group = new JPanel(new BorderLayout(0, 4));
        group.setOpaque(false);
        group.setMaximumSize(new Dimension(350, 60));

        JLabel lbl = new JLabel(labelText);
        lbl.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lbl.setForeground(new Color(90, 90, 90));

        group.add(lbl, BorderLayout.NORTH);
        group.add(inputComponent, BorderLayout.CENTER);
        return group;
    }

    private JTextField createStyledTextField() {
        JTextField tf = new JTextField();
        tf.setFont(new Font("SansSerif", Font.PLAIN, 14));
        tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_GRAY, 1),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
        return tf;
    }

    private JPasswordField createStyledPasswordField() {
        JPasswordField pf = new JPasswordField();
        pf.setFont(new Font("SansSerif", Font.PLAIN, 14));
        pf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_GRAY, 1),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
        return pf;
    }

    private JLabel createHoverLink(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("SansSerif", Font.PLAIN, 12));
        label.setForeground(PRIMARY_BLUE);
        label.setCursor(new Cursor(Cursor.HAND_CURSOR));
        label.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                label.setText("<html><u>" + text + "</u></html>");
            }
            @Override
            public void mouseExited(MouseEvent e) {
                label.setText(text);
            }
        });
        return label;
    }

    private void processLogin() {
        String adminId = adminIdField.getText().trim();
        String pass = new String(adminPasswordField.getPassword()).trim();
        String enteredCaptcha = captchaInputField.getText().trim();

        if (adminId.isEmpty() || pass.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter Admin ID and Master Password.", "Authentication Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!enteredCaptcha.equalsIgnoreCase(captchaDisplayPanel.getCaptchaText())) {
            JOptionPane.showMessageDialog(this, "Incorrect CAPTCHA code. Please try again.", "Authentication Error", JOptionPane.ERROR_MESSAGE);
            captchaDisplayPanel.regenerateCaptcha();
            captchaInputField.setText("");
            return;
        }

        try (Connection conn = getConnection()) {
            String query = "SELECT * FROM admin_users WHERE admin_id = ? AND password_hash = ?";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, adminId);
            pstmt.setString(2, pass);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                loadAllAdminDataFromDB();
                mainCardLayout.show(mainContainer, "DASHBOARD");
            } else {
                JOptionPane.showMessageDialog(this, "Invalid Admin ID or Password.", "Authentication Failed", JOptionPane.ERROR_MESSAGE);
                captchaDisplayPanel.regenerateCaptcha();
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database Connection Error:\n" + ex.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // =========================================================================
    // 2. ADMIN DASHBOARD VIEW
    // =========================================================================
    private JPanel createDashboardView() {
        JPanel dashboard = new JPanel(new BorderLayout());

        JPanel navHeader = new JPanel(new BorderLayout());
        navHeader.setBackground(HEADER_NAV_BLUE);
        navHeader.setPreferredSize(new Dimension(1020, 55));
        navHeader.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        JLabel portalTitle = new JLabel("UTOP - System Administration Console");
        portalTitle.setForeground(Color.WHITE);
        portalTitle.setFont(new Font("SansSerif", Font.BOLD, 18));

        JButton logoutBtn = new JButton("Logout System");
        logoutBtn.setFont(new Font("SansSerif", Font.BOLD, 13));
        logoutBtn.setBackground(DANGER_RED);
        logoutBtn.setForeground(Color.WHITE);
        logoutBtn.setOpaque(true);
        logoutBtn.setContentAreaFilled(true);
        logoutBtn.setBorderPainted(false);
        logoutBtn.setFocusPainted(false);
        logoutBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        logoutBtn.addActionListener(e -> {
            adminIdField.setText("");
            adminPasswordField.setText("");
            captchaInputField.setText("");
            captchaDisplayPanel.regenerateCaptcha();
            mainCardLayout.show(mainContainer, "LOGIN");
        });

        navHeader.add(portalTitle, BorderLayout.WEST);
        navHeader.add(logoutBtn, BorderLayout.EAST);

        // Sidebar Navigation
        JPanel sideMenu = new JPanel(new GridLayout(7, 1, 0, 8));
        sideMenu.setBackground(new Color(238, 242, 246));
        sideMenu.setPreferredSize(new Dimension(220, 0));
        sideMenu.setBorder(BorderFactory.createEmptyBorder(15, 10, 15, 10));

        dashboardCardLayout = new CardLayout();
        dashboardContentPanel = new JPanel(dashboardCardLayout);

        dashboardContentPanel.add(createSystemOverviewPanel(), "OVERVIEW");
        dashboardContentPanel.add(createManageStudentsPanel(), "STUDENTS");
        dashboardContentPanel.add(createManageFacultyPanel(), "FACULTY");
        dashboardContentPanel.add(createManageParentsPanel(), "PARENTS");
        dashboardContentPanel.add(createCourseAdminPanel(), "COURSES_ADMIN");
        dashboardContentPanel.add(createNotificationsPanel(), "NOTIFICATIONS");
        dashboardContentPanel.add(createFeeReconciliationPanel(), "FEES_ADMIN");

        sideMenu.add(createSidebarButton("System Overview", "OVERVIEW"));
        sideMenu.add(createSidebarButton("Manage Students", "STUDENTS"));
        sideMenu.add(createSidebarButton("Manage Faculty", "FACULTY"));
        sideMenu.add(createSidebarButton("Manage Parents", "PARENTS"));
        sideMenu.add(createSidebarButton("Course Catalog", "COURSES_ADMIN"));
        sideMenu.add(createSidebarButton("Notifications / Requests", "NOTIFICATIONS"));
        sideMenu.add(createSidebarButton("Fee Approvals", "FEES_ADMIN"));

        dashboard.add(navHeader, BorderLayout.NORTH);
        dashboard.add(sideMenu, BorderLayout.WEST);
        dashboard.add(dashboardContentPanel, BorderLayout.CENTER);

        return dashboard;
    }

    private JButton createSidebarButton(String title, String cardKey) {
        JButton btn = new JButton(title);
        btn.setFont(new Font("SansSerif", Font.PLAIN, 13));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setFocusPainted(false);
        btn.addActionListener(e -> {
            dashboardCardLayout.show(dashboardContentPanel, cardKey);
            loadAllAdminDataFromDB();
        });
        return btn;
    }

    // --- 1. SYSTEM OVERVIEW METRICS ---
    private JPanel createSystemOverviewPanel() {
        JPanel panel = createBaseSectionPanel("System Metrics & Statistics");

        JPanel metricsGrid = new JPanel(new GridLayout(2, 2, 20, 20));
        metricsGrid.setOpaque(false);

        totalStudentsVal = new JLabel("...", SwingConstants.LEFT);
        totalFacultyVal = new JLabel("...", SwingConstants.LEFT);
        feeCollectionVal = new JLabel("...", SwingConstants.LEFT);
        dbStatusVal = new JLabel("ACTIVE / ONLINE", SwingConstants.LEFT);

        metricsGrid.add(createMetricCard("Total Enrolled Students", totalStudentsVal, PRIMARY_BLUE));
        metricsGrid.add(createMetricCard("Active Faculty Members", totalFacultyVal, SUCCESS_GREEN));
        metricsGrid.add(createMetricCard("Fee Collection Rate", feeCollectionVal, new Color(255, 193, 7)));
        metricsGrid.add(createMetricCard("Database Status", dbStatusVal, new Color(108, 117, 125)));

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.add(metricsGrid, BorderLayout.NORTH);

        panel.add(wrapper, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createMetricCard(String title, JLabel valueLabel, Color accentColor) {
        JPanel card = new JPanel(new BorderLayout(0, 10));
        card.setBackground(new Color(248, 249, 250));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 5, 0, 0, accentColor),
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(BORDER_GRAY, 1),
                        BorderFactory.createEmptyBorder(18, 20, 18, 20)
                )
        ));

        JLabel tLbl = new JLabel(title);
        tLbl.setFont(new Font("SansSerif", Font.PLAIN, 14));
        tLbl.setForeground(new Color(100, 100, 100));

        valueLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        valueLabel.setForeground(HEADER_NAV_BLUE);

        card.add(tLbl, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        return card;
    }

    // --- 2. MANAGE STUDENTS ---
    private JPanel createManageStudentsPanel() {
        JPanel panel = createBaseSectionPanel("Student Records & Academic Performance");

        JPanel topControlBar = new JPanel(new BorderLayout(10, 0));
        topControlBar.setOpaque(false);

        JLabel hintLbl = new JLabel("💡 Tip: Double-click row for student performance analytics.");
        hintLbl.setFont(new Font("SansSerif", Font.ITALIC, 12));
        hintLbl.setForeground(new Color(100, 100, 100));

        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonsPanel.setOpaque(false);

        JButton deleteStudentBtn = new JButton("🗑 Delete Selected");
        deleteStudentBtn.setFont(new Font("SansSerif", Font.BOLD, 13));
        deleteStudentBtn.setBackground(DANGER_RED);
        deleteStudentBtn.setForeground(Color.WHITE);
        deleteStudentBtn.setOpaque(true);
        deleteStudentBtn.setContentAreaFilled(true);
        deleteStudentBtn.setBorderPainted(false);
        deleteStudentBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        deleteStudentBtn.addActionListener(e -> deleteSelectedStudent());

        JButton addStudentBtn = new JButton("+ Add New Student");
        addStudentBtn.setFont(new Font("SansSerif", Font.BOLD, 13));
        addStudentBtn.setBackground(PRIMARY_BLUE);
        addStudentBtn.setForeground(Color.WHITE);
        addStudentBtn.setOpaque(true);
        addStudentBtn.setContentAreaFilled(true);
        addStudentBtn.setBorderPainted(false);
        addStudentBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        addStudentBtn.addActionListener(e -> openAddStudentModal());

        buttonsPanel.add(deleteStudentBtn);
        buttonsPanel.add(addStudentBtn);

        topControlBar.add(hintLbl, BorderLayout.WEST);
        topControlBar.add(buttonsPanel, BorderLayout.EAST);

        String[] columns = {"Reg No", "Student Name", "Branch", "School", "Residency", "CGPA", "Credits Earned", "Contact"};
        studentTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        studentTable = new JTable(studentTableModel);
        studentTable.setRowHeight(30);
        studentTable.setCursor(new Cursor(Cursor.HAND_CURSOR));

        studentTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = studentTable.rowAtPoint(e.getPoint());
                    if (row >= 0) {
                        String regNo = studentTable.getValueAt(row, 0).toString();
                        String name = studentTable.getValueAt(row, 1).toString();
                        openStudentPerformanceModal(regNo, name);
                    }
                }
            }
        });

        JPanel wrapper = new JPanel(new BorderLayout(0, 15));
        wrapper.setOpaque(false);
        wrapper.add(topControlBar, BorderLayout.NORTH);
        wrapper.add(new JScrollPane(studentTable), BorderLayout.CENTER);

        panel.add(wrapper, BorderLayout.CENTER);
        return panel;
    }

    private void deleteSelectedStudent() {
        int selectedRow = studentTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a student from the table to remove.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String regNo = studentTable.getValueAt(selectedRow, 0).toString();
        String studentName = studentTable.getValueAt(selectedRow, 1).toString();

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to permanently remove student:\n" + studentName + " (" + regNo + ")\n\nThis will also remove all associated enrollments, fee, and attendance records.",
                "Confirm Student Removal",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection conn = getConnection()) {
                String deleteSql = "DELETE FROM students WHERE reg_no = ?";
                try (PreparedStatement pstmt = conn.prepareStatement(deleteSql)) {
                    pstmt.setString(1, regNo);
                    pstmt.executeUpdate();
                }
                JOptionPane.showMessageDialog(this, "Student " + regNo + " has been successfully removed.", "Removed", JOptionPane.INFORMATION_MESSAGE);
                loadStudentsFromDatabase();
                loadParentsFromDatabase();
                loadMetricsFromDatabase();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error deleting student: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void openStudentPerformanceModal(String regNo, String studentName) {
        JDialog dialog = new JDialog(this, "Performance Analytics: " + studentName + " (" + regNo + ")", true);
        dialog.setSize(680, 460);
        dialog.setLocationRelativeTo(this);

        JPanel main = new JPanel(new BorderLayout(0, 15));
        main.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel headerCard = new JPanel(new GridLayout(2, 2, 15, 10));
        headerCard.setBackground(new Color(245, 248, 255));
        headerCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(PRIMARY_BLUE, 1),
                BorderFactory.createEmptyBorder(12, 15, 12, 15)
        ));

        JLabel cgpaLbl = new JLabel("CGPA: --");
        cgpaLbl.setFont(new Font("SansSerif", Font.BOLD, 16));
        cgpaLbl.setForeground(HEADER_NAV_BLUE);

        JLabel creditsLbl = new JLabel("Credits Earned: --");
        creditsLbl.setFont(new Font("SansSerif", Font.BOLD, 14));

        JLabel residencyLbl = new JLabel("Residency: --");
        residencyLbl.setFont(new Font("SansSerif", Font.PLAIN, 13));

        JLabel overallAttLbl = new JLabel("Overall Attendance: Calculating...");
        overallAttLbl.setFont(new Font("SansSerif", Font.BOLD, 14));
        overallAttLbl.setForeground(SUCCESS_GREEN);

        headerCard.add(cgpaLbl);
        headerCard.add(creditsLbl);
        headerCard.add(residencyLbl);
        headerCard.add(overallAttLbl);

        try (Connection conn = getConnection()) {
            String sql = "SELECT cgpa, credits_earned, residency_status FROM students WHERE reg_no = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, regNo);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                cgpaLbl.setText("Cumulative CGPA: " + rs.getDouble("cgpa"));
                creditsLbl.setText("Credits Earned: " + rs.getInt("credits_earned") + " / 160");
                residencyLbl.setText("Residency Status: " + rs.getString("residency_status"));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        String[] cols = {"Course Code", "Course Title", "Attended", "Total Classes", "Attendance %"};
        DefaultTableModel attModel = new DefaultTableModel(cols, 0);
        JTable attTable = new JTable(attModel);
        attTable.setRowHeight(26);

        int totalAtt = 0;
        int totalClasses = 0;

        try (Connection conn = getConnection()) {
            String sql = "SELECT a.course_code, c.course_title, a.classes_attended, a.total_classes " +
                    "FROM attendance a " +
                    "JOIN courses c ON a.course_code = c.course_code " +
                    "WHERE a.student_reg_no = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, regNo);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int att = rs.getInt("classes_attended");
                int tot = rs.getInt("total_classes");
                double pct = tot > 0 ? ((double) att / tot) * 100.0 : 0.0;
                totalAtt += att;
                totalClasses += tot;

                attModel.addRow(new Object[]{
                        rs.getString("course_code"),
                        rs.getString("course_title"),
                        att,
                        tot,
                        String.format("%.1f%%", pct)
                });
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        double aggregatePct = totalClasses > 0 ? ((double) totalAtt / totalClasses) * 100.0 : 100.0;
        overallAttLbl.setText("Overall Attendance: " + String.format("%.1f%%", aggregatePct));

        main.add(headerCard, BorderLayout.NORTH);
        main.add(new JScrollPane(attTable), BorderLayout.CENTER);

        dialog.add(main);
        dialog.setVisible(true);
    }

    private void openAddStudentModal() {
        JDialog dialog = new JDialog(this, "Add New Student Record", true);
        dialog.setSize(400, 440);
        dialog.setLocationRelativeTo(this);

        JPanel form = new JPanel(new GridLayout(7, 2, 10, 12));
        form.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));

        JTextField nameF = new JTextField();
        JTextField regF = new JTextField();
        JTextField branchF = new JTextField("CSE");
        JTextField schoolF = new JTextField("SCSE");
        JComboBox<String> residencyF = new JComboBox<>(new String[]{"Day Scholar", "Hosteller"});
        JTextField contactF = new JTextField();

        form.add(new JLabel("Full Name:"));
        form.add(nameF);
        form.add(new JLabel("Registration No:"));
        form.add(regF);
        form.add(new JLabel("Branch:"));
        form.add(branchF);
        form.add(new JLabel("School:"));
        form.add(schoolF);
        form.add(new JLabel("Residency Status:"));
        form.add(residencyF);
        form.add(new JLabel("Contact Number:"));
        form.add(contactF);

        JButton saveBtn = new JButton("Save Record");
        saveBtn.setBackground(SUCCESS_GREEN);
        saveBtn.setForeground(Color.WHITE);
        saveBtn.setFont(new Font("SansSerif", Font.BOLD, 13));
        saveBtn.setOpaque(true);
        saveBtn.setContentAreaFilled(true);
        saveBtn.setBorderPainted(false);

        saveBtn.addActionListener(e -> {
            String name = nameF.getText().trim();
            String reg = regF.getText().trim();
            String branch = branchF.getText().trim();
            String school = schoolF.getText().trim();
            String residency = residencyF.getSelectedItem().toString();
            String contact = contactF.getText().trim();

            if (name.isEmpty() || reg.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Please fill required fields (Name and Registration No).", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try (Connection conn = getConnection()) {
                String sql = "INSERT INTO students (reg_no, full_name, password_hash, branch, school, residency_status, contact_no) VALUES (?, ?, 'student123', ?, ?, ?, ?)";
                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, reg);
                pstmt.setString(2, name);
                pstmt.setString(3, branch);
                pstmt.setString(4, school);
                pstmt.setString(5, residency);
                pstmt.setString(6, contact);
                pstmt.executeUpdate();

                JOptionPane.showMessageDialog(dialog, "New Student added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadStudentsFromDatabase();
                dialog.dispose();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(dialog, "Database Insert Error:\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        form.add(new JLabel(""));
        form.add(saveBtn);

        dialog.add(form);
        dialog.setVisible(true);
    }

    // --- 3. MANAGE FACULTY ---
    private JPanel createManageFacultyPanel() {
        JPanel panel = createBaseSectionPanel("Faculty Directory & Academic Performance");

        JPanel topControlBar = new JPanel(new BorderLayout(10, 0));
        topControlBar.setOpaque(false);

        JLabel hintLbl = new JLabel("💡 Tip: Double-click row to evaluate passing rate and courses taught.");
        hintLbl.setFont(new Font("SansSerif", Font.ITALIC, 12));
        hintLbl.setForeground(new Color(100, 100, 100));

        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonsPanel.setOpaque(false);

        JButton deleteFacultyBtn = new JButton("🗑 Delete Selected");
        deleteFacultyBtn.setFont(new Font("SansSerif", Font.BOLD, 13));
        deleteFacultyBtn.setBackground(DANGER_RED);
        deleteFacultyBtn.setForeground(Color.WHITE);
        deleteFacultyBtn.setOpaque(true);
        deleteFacultyBtn.setContentAreaFilled(true);
        deleteFacultyBtn.setBorderPainted(false);
        deleteFacultyBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        deleteFacultyBtn.addActionListener(e -> deleteSelectedFaculty());

        JButton addFacultyBtn = new JButton("+ Add New Faculty");
        addFacultyBtn.setFont(new Font("SansSerif", Font.BOLD, 13));
        addFacultyBtn.setBackground(PRIMARY_BLUE);
        addFacultyBtn.setForeground(Color.WHITE);
        addFacultyBtn.setOpaque(true);
        addFacultyBtn.setContentAreaFilled(true);
        addFacultyBtn.setBorderPainted(false);
        addFacultyBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        addFacultyBtn.addActionListener(e -> openAddFacultyModal());

        buttonsPanel.add(deleteFacultyBtn);
        buttonsPanel.add(addFacultyBtn);

        topControlBar.add(hintLbl, BorderLayout.WEST);
        topControlBar.add(buttonsPanel, BorderLayout.EAST);

        String[] columns = {"Emp ID", "Faculty Name", "Designation", "School / Department", "Email"};
        facultyTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        facultyTable = new JTable(facultyTableModel);
        facultyTable.setRowHeight(30);
        facultyTable.setCursor(new Cursor(Cursor.HAND_CURSOR));

        facultyTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = facultyTable.rowAtPoint(e.getPoint());
                    if (row >= 0) {
                        String empId = facultyTable.getValueAt(row, 0).toString();
                        String facultyName = facultyTable.getValueAt(row, 1).toString();
                        openFacultyPerformanceModal(empId, facultyName);
                    }
                }
            }
        });

        JPanel wrapper = new JPanel(new BorderLayout(0, 15));
        wrapper.setOpaque(false);
        wrapper.add(topControlBar, BorderLayout.NORTH);
        wrapper.add(new JScrollPane(facultyTable), BorderLayout.CENTER);

        panel.add(wrapper, BorderLayout.CENTER);
        return panel;
    }

    private void deleteSelectedFaculty() {
        int selectedRow = facultyTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a faculty member from the table to remove.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String empId = facultyTable.getValueAt(selectedRow, 0).toString();
        String facultyName = facultyTable.getValueAt(selectedRow, 1).toString();

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to remove faculty member:\n" + facultyName + " (" + empId + ")?\n\nNote: This will unassign this faculty member from their allocated courses.",
                "Confirm Faculty Removal",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection conn = getConnection()) {
                String unassignSql = "UPDATE courses SET faculty_emp_id = NULL WHERE faculty_emp_id = ?";
                try (PreparedStatement unassignStmt = conn.prepareStatement(unassignSql)) {
                    unassignStmt.setString(1, empId);
                    unassignStmt.executeUpdate();
                }

                String deleteSql = "DELETE FROM faculty WHERE emp_id = ?";
                try (PreparedStatement pstmt = conn.prepareStatement(deleteSql)) {
                    pstmt.setString(1, empId);
                    pstmt.executeUpdate();
                }

                JOptionPane.showMessageDialog(this, "Faculty member " + facultyName + " has been successfully removed.", "Removed", JOptionPane.INFORMATION_MESSAGE);
                loadFacultyFromDatabase();
                loadCoursesFromDatabase();
                loadMetricsFromDatabase();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error deleting faculty: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void openFacultyPerformanceModal(String empId, String facultyName) {
        JDialog dialog = new JDialog(this, "Faculty Teaching Analytics: " + facultyName + " (" + empId + ")", true);
        dialog.setSize(700, 460);
        dialog.setLocationRelativeTo(this);

        JPanel main = new JPanel(new BorderLayout(0, 15));
        main.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel headerCard = new JPanel(new GridLayout(2, 2, 15, 10));
        headerCard.setBackground(new Color(245, 248, 255));
        headerCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(PRIMARY_BLUE, 1),
                BorderFactory.createEmptyBorder(12, 15, 12, 15)
        ));

        JLabel passRateLbl = new JLabel("Overall Passing Rate: Calculating...");
        passRateLbl.setFont(new Font("SansSerif", Font.BOLD, 16));
        passRateLbl.setForeground(SUCCESS_GREEN);

        JLabel totalCoursesLbl = new JLabel("Allotted Courses: --");
        totalCoursesLbl.setFont(new Font("SansSerif", Font.BOLD, 14));

        JLabel evaluatedStudentsLbl = new JLabel("Students Evaluated: --");
        evaluatedStudentsLbl.setFont(new Font("SansSerif", Font.PLAIN, 13));

        JLabel statusLbl = new JLabel("Teaching Effectiveness: Excellent");
        statusLbl.setFont(new Font("SansSerif", Font.PLAIN, 13));

        headerCard.add(passRateLbl);
        headerCard.add(totalCoursesLbl);
        headerCard.add(evaluatedStudentsLbl);
        headerCard.add(statusLbl);

        String[] cols = {"Course Code", "Course Title", "Total Evaluated", "Passed (>= 50%)", "Course Pass Rate %"};
        DefaultTableModel model = new DefaultTableModel(cols, 0);
        JTable table = new JTable(model);
        table.setRowHeight(26);

        int totalEvaluatedOverall = 0;
        int totalPassedOverall = 0;
        int courseCount = 0;

        try (Connection conn = getConnection()) {
            String sql = "SELECT c.course_code, c.course_title, " +
                    "COUNT(m.scored_marks) AS evaluated_count, " +
                    "SUM(CASE WHEN (m.scored_marks / m.max_marks) >= 0.5 THEN 1 ELSE 0 END) AS passed_count " +
                    "FROM courses c " +
                    "LEFT JOIN marks m ON c.course_code = m.course_code " +
                    "WHERE c.faculty_emp_id = ? " +
                    "GROUP BY c.course_code, c.course_title";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, empId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                courseCount++;
                int evaluated = rs.getInt("evaluated_count");
                int passed = rs.getInt("passed_count");
                double rate = evaluated > 0 ? ((double) passed / evaluated) * 100.0 : 100.0;

                totalEvaluatedOverall += evaluated;
                totalPassedOverall += passed;

                model.addRow(new Object[]{
                        rs.getString("course_code"),
                        rs.getString("course_title"),
                        evaluated,
                        passed,
                        String.format("%.1f%%", rate)
                });
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        double overallPassRate = totalEvaluatedOverall > 0 ? ((double) totalPassedOverall / totalEvaluatedOverall) * 100.0 : 100.0;
        passRateLbl.setText("Overall Passing Rate: " + String.format("%.1f%%", overallPassRate));
        totalCoursesLbl.setText("Allotted Courses: " + courseCount);
        evaluatedStudentsLbl.setText("Total Evaluated Records: " + totalEvaluatedOverall);

        main.add(headerCard, BorderLayout.NORTH);
        main.add(new JScrollPane(table), BorderLayout.CENTER);

        dialog.add(main);
        dialog.setVisible(true);
    }

    private void openAddFacultyModal() {
        JDialog dialog = new JDialog(this, "Add New Faculty Member", true);
        dialog.setSize(400, 380);
        dialog.setLocationRelativeTo(this);

        JPanel form = new JPanel(new GridLayout(5, 2, 10, 12));
        form.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));

        JTextField nameF = new JTextField();
        JTextField empIdF = new JTextField();
        JComboBox<String> desigF = new JComboBox<>(new String[]{"Assistant Professor", "Associate Professor", "Professor"});
        JTextField schoolF = new JTextField("SCSE");
        JTextField emailF = new JTextField();

        form.add(new JLabel("Faculty Name:"));
        form.add(nameF);
        form.add(new JLabel("Employee ID:"));
        form.add(empIdF);
        form.add(new JLabel("Designation:"));
        form.add(desigF);
        form.add(new JLabel("School / Dept:"));
        form.add(schoolF);
        form.add(new JLabel("Official Email:"));
        form.add(emailF);

        JButton saveBtn = new JButton("Register Faculty");
        saveBtn.setBackground(SUCCESS_GREEN);
        saveBtn.setForeground(Color.WHITE);
        saveBtn.setFont(new Font("SansSerif", Font.BOLD, 13));
        saveBtn.setOpaque(true);
        saveBtn.setContentAreaFilled(true);
        saveBtn.setBorderPainted(false);

        saveBtn.addActionListener(e -> {
            String name = nameF.getText().trim();
            String empId = empIdF.getText().trim();
            String desig = desigF.getSelectedItem().toString();
            String school = schoolF.getText().trim();
            String email = emailF.getText().trim();

            if (name.isEmpty() || empId.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Please fill required fields (Name and Employee ID).", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try (Connection conn = getConnection()) {
                String sql = "INSERT INTO faculty (emp_id, full_name, password_hash, designation, school, email) VALUES (?, ?, 'faculty123', ?, ?, ?)";
                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, empId);
                pstmt.setString(2, name);
                pstmt.setString(3, desig);
                pstmt.setString(4, school);
                pstmt.setString(5, email);
                pstmt.executeUpdate();

                JOptionPane.showMessageDialog(dialog, "New Faculty registered successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadFacultyFromDatabase();
                dialog.dispose();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(dialog, "Database Insert Error:\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        JPanel btnWrapper = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnWrapper.setOpaque(false);
        btnWrapper.add(saveBtn);

        JPanel wrapper = new JPanel(new BorderLayout(0, 10));
        wrapper.add(form, BorderLayout.CENTER);
        wrapper.add(btnWrapper, BorderLayout.SOUTH);

        dialog.add(wrapper);
        dialog.setVisible(true);
    }

    // --- 4. MANAGE PARENTS ---
    private JPanel createManageParentsPanel() {
        JPanel panel = createBaseSectionPanel("Parent Records & Guardian Directory");

        JPanel topControlBar = new JPanel(new BorderLayout());
        topControlBar.setOpaque(false);

        JButton addParentBtn = new JButton("+ Add Parent Account");
        addParentBtn.setFont(new Font("SansSerif", Font.BOLD, 13));
        addParentBtn.setBackground(PRIMARY_BLUE);
        addParentBtn.setForeground(Color.WHITE);
        addParentBtn.setOpaque(true);
        addParentBtn.setContentAreaFilled(true);
        addParentBtn.setBorderPainted(false);
        addParentBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        addParentBtn.addActionListener(e -> openAddParentModal());

        topControlBar.add(addParentBtn, BorderLayout.EAST);

        String[] columns = {"Student Reg No", "Student Name", "Parent / Guardian Name", "Contact Number"};
        parentTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(parentTableModel);
        table.setRowHeight(30);

        JPanel wrapper = new JPanel(new BorderLayout(0, 15));
        wrapper.setOpaque(false);
        wrapper.add(topControlBar, BorderLayout.NORTH);
        wrapper.add(new JScrollPane(table), BorderLayout.CENTER);

        panel.add(wrapper, BorderLayout.CENTER);
        return panel;
    }

    private void openAddParentModal() {
        JDialog dialog = new JDialog(this, "Register Parent / Guardian Login", true);
        dialog.setSize(400, 260);
        dialog.setLocationRelativeTo(this);

        JPanel form = new JPanel(new GridLayout(3, 2, 10, 12));
        form.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));

        JTextField studentRegF = new JTextField();
        JPasswordField passwordF = new JPasswordField("parent123");

        form.add(new JLabel("Student Reg No:"));
        form.add(studentRegF);
        form.add(new JLabel("Initial Password:"));
        form.add(passwordF);

        JButton saveBtn = new JButton("Save Parent Access");
        saveBtn.setBackground(SUCCESS_GREEN);
        saveBtn.setForeground(Color.WHITE);
        saveBtn.setFont(new Font("SansSerif", Font.BOLD, 13));
        saveBtn.setOpaque(true);
        saveBtn.setContentAreaFilled(true);
        saveBtn.setBorderPainted(false);

        saveBtn.addActionListener(e -> {
            String sReg = studentRegF.getText().trim();
            String pwd = new String(passwordF.getPassword()).trim();

            if (sReg.isEmpty() || pwd.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Please enter Student Registration Number.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try (Connection conn = getConnection()) {
                String sql = "INSERT INTO parents (student_reg_no, parent_password_hash) VALUES (?, ?)";
                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, sReg);
                pstmt.setString(2, pwd);
                pstmt.executeUpdate();

                JOptionPane.showMessageDialog(dialog, "Parent login record created successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadParentsFromDatabase();
                dialog.dispose();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(dialog, "Database Insert Error:\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        form.add(new JLabel(""));
        form.add(saveBtn);

        dialog.add(form);
        dialog.setVisible(true);
    }

    // --- 5. COURSE CATALOG ADMIN ---
    private JPanel createCourseAdminPanel() {
        JPanel panel = createBaseSectionPanel("Course Catalog & Department Management");

        String[] columns = {"Course Code", "Course Title", "School", "Credits", "Allocated Instructor", "Enrolled Students", "View Enrolled"};
        courseTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(courseTableModel);
        table.setRowHeight(32);

        table.getColumnModel().getColumn(6).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                JLabel lbl = new JLabel("➜ View", SwingConstants.CENTER);
                lbl.setFont(new Font("SansSerif", Font.BOLD, 13));
                lbl.setForeground(PRIMARY_BLUE);
                lbl.setCursor(new Cursor(Cursor.HAND_CURSOR));
                return lbl;
            }
        });

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                int col = table.columnAtPoint(e.getPoint());
                if (col == 6 && row >= 0) {
                    String courseCode = table.getValueAt(row, 0).toString();
                    String courseTitle = table.getValueAt(row, 1).toString();
                    openEnrolledStudentsModal(courseCode, courseTitle);
                }
            }
        });

        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private void openEnrolledStudentsModal(String courseCode, String courseTitle) {
        JDialog dialog = new JDialog(this, "Enrolled Students: " + courseCode + " (" + courseTitle + ")", true);
        dialog.setSize(680, 400);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        String[] cols = {"Reg No", "Student Name", "Branch", "Semester", "Slot"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(model);
        table.setRowHeight(28);

        String sql = "SELECT s.reg_no, s.full_name, s.branch, e.semester, e.slot " +
                "FROM enrollments e " +
                "JOIN students s ON e.reg_no = s.reg_no " +
                "WHERE e.course_code = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, courseCode);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getString("reg_no"),
                        rs.getString("full_name"),
                        rs.getString("branch"),
                        rs.getString("semester"),
                        rs.getString("slot")
                });
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        dialog.add(panel);
        dialog.setVisible(true);
    }

    // --- 6. NOTIFICATIONS & REQUESTS APPROVAL DESK ---
    private JPanel createNotificationsPanel() {
        JPanel panel = createBaseSectionPanel("System Notifications & User Change Approvals Desk");

        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 5));
        topBar.setOpaque(false);

        requestFilterCombo = new JComboBox<>(new String[]{"All Requests", "STUDENT", "FACULTY", "PARENT", "STAFF"});
        requestFilterCombo.addActionListener(e -> loadRequestsFromDatabase());

        topBar.add(new JLabel("Filter by User Role:"));
        topBar.add(requestFilterCombo);

        String[] cols = {"Req ID", "User Role", "User ID / Reg No", "Request Type", "Details / Request Message", "Date", "Status", "Action"};
        notificationsTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };

        JTable table = new JTable(notificationsTableModel);
        table.setRowHeight(32);

        table.getColumnModel().getColumn(7).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                JButton btn = new JButton("Review ➜");
                btn.setFont(new Font("SansSerif", Font.BOLD, 11));
                btn.setBackground(PRIMARY_BLUE);
                btn.setForeground(Color.WHITE);
                btn.setOpaque(true);
                btn.setContentAreaFilled(true);
                btn.setBorderPainted(false);
                return btn;
            }
        });

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                int col = table.columnAtPoint(e.getPoint());
                if (col == 7 && row >= 0) {
                    int reqId = Integer.parseInt(table.getValueAt(row, 0).toString());
                    String userRole = table.getValueAt(row, 1).toString();
                    String userId = table.getValueAt(row, 2).toString();
                    String reqType = table.getValueAt(row, 3).toString();
                    String reqDetails = table.getValueAt(row, 4).toString();
                    String status = table.getValueAt(row, 6).toString();
                    openRequestReviewDialog(reqId, userRole, userId, reqType, reqDetails, status);
                }
            }
        });

        JPanel wrapper = new JPanel(new BorderLayout(0, 10));
        wrapper.setOpaque(false);
        wrapper.add(topBar, BorderLayout.NORTH);
        wrapper.add(new JScrollPane(table), BorderLayout.CENTER);

        panel.add(wrapper, BorderLayout.CENTER);
        return panel;
    }

    private void openRequestReviewDialog(int reqId, String role, String userId, String reqType, String reqDetails, String currentStatus) {
        JDialog dialog = new JDialog(this, "Review Request #" + reqId + " (" + reqType + ")", true);
        dialog.setSize(530, 430);
        dialog.setLocationRelativeTo(this);

        JPanel mainPanel = new JPanel(new BorderLayout(0, 15));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel infoCard = new JPanel(new GridLayout(4, 2, 10, 8));
        infoCard.setBackground(new Color(245, 248, 255));
        infoCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(PRIMARY_BLUE, 1),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));

        infoCard.add(new JLabel("Request ID:"));
        infoCard.add(new JLabel("#" + reqId));
        infoCard.add(new JLabel("Requester Role:"));
        infoCard.add(new JLabel(role));
        infoCard.add(new JLabel("User ID / Reg No:"));
        infoCard.add(new JLabel(userId));
        infoCard.add(new JLabel("Current Status:"));
        JLabel statusLbl = new JLabel(currentStatus);
        statusLbl.setFont(new Font("SansSerif", Font.BOLD, 12));
        statusLbl.setForeground("APPROVED".equalsIgnoreCase(currentStatus) ? SUCCESS_GREEN : ("REJECTED".equalsIgnoreCase(currentStatus) ? DANGER_RED : Color.DARK_GRAY));
        infoCard.add(statusLbl);

        JPanel contentPanel = new JPanel(new BorderLayout(0, 5));
        contentPanel.setOpaque(false);

        boolean isDirectUpdate = reqType.toUpperCase().contains("PASSWORD") ||
                reqType.toUpperCase().contains("NAME") ||
                reqType.toUpperCase().contains("EMAIL") ||
                reqType.toUpperCase().contains("CONTACT") ||
                reqType.toUpperCase().contains("PHONE");

        JLabel contentTitle = new JLabel(isDirectUpdate ? "Requested Field Modification:" : "Written Inquiry / Message Content:");
        contentTitle.setFont(new Font("SansSerif", Font.BOLD, 13));

        JTextArea detailArea = new JTextArea(reqDetails);
        detailArea.setLineWrap(true);
        detailArea.setWrapStyleWord(true);
        detailArea.setEditable(false);
        detailArea.setFont(new Font("SansSerif", Font.PLAIN, 13));
        detailArea.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        contentPanel.add(contentTitle, BorderLayout.NORTH);
        contentPanel.add(new JScrollPane(detailArea), BorderLayout.CENTER);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionPanel.setOpaque(false);

        JButton approveBtn = new JButton(isDirectUpdate ? "Approve & Apply" : "Resolve (Approve)");
        approveBtn.setBackground(SUCCESS_GREEN);
        approveBtn.setForeground(Color.WHITE);
        approveBtn.setFont(new Font("SansSerif", Font.BOLD, 13));
        approveBtn.setOpaque(true);
        approveBtn.setContentAreaFilled(true);
        approveBtn.setBorderPainted(false);
        approveBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JButton declineBtn = new JButton("Decline");
        declineBtn.setBackground(DANGER_RED);
        declineBtn.setForeground(Color.WHITE);
        declineBtn.setFont(new Font("SansSerif", Font.BOLD, 13));
        declineBtn.setOpaque(true);
        declineBtn.setContentAreaFilled(true);
        declineBtn.setBorderPainted(false);
        declineBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        if ("APPROVED".equalsIgnoreCase(currentStatus) || "REJECTED".equalsIgnoreCase(currentStatus)) {
            approveBtn.setEnabled(false);
            declineBtn.setEnabled(false);
        }

        approveBtn.addActionListener(e -> {
            if (isDirectUpdate) {
                applyApprovedChanges(role, userId, reqType, reqDetails);
            }
            updateRequestStatus(reqId, "APPROVED");
            JOptionPane.showMessageDialog(dialog, "Request #" + reqId + " has been approved successfully.", "Approved", JOptionPane.INFORMATION_MESSAGE);
            dialog.dispose();
        });

        declineBtn.addActionListener(e -> {
            updateRequestStatus(reqId, "REJECTED");
            JOptionPane.showMessageDialog(dialog, "Request #" + reqId + " has been declined.", "Declined", JOptionPane.INFORMATION_MESSAGE);
            dialog.dispose();
        });

        actionPanel.add(declineBtn);
        actionPanel.add(approveBtn);

        mainPanel.add(infoCard, BorderLayout.NORTH);
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        mainPanel.add(actionPanel, BorderLayout.SOUTH);

        dialog.add(mainPanel);
        dialog.setVisible(true);
    }

    // --- UNIVERSAL DATABASE MODIFICATION DISPATCHER (STUDENT, FACULTY, PARENT, STAFF) ---
    private void applyApprovedChanges(String role, String userId, String reqType, String requestedValue) {
        String targetValue = requestedValue.trim();

        // 1. If combined with a pipe delimiter (e.g., "112233 | Reason: Forgot..."), take the first part
        if (targetValue.contains("|")) {
            targetValue = targetValue.split("\\|")[0].trim();
        }

        // 2. If parenthetical notes exist (e.g., "112233 (Reason:...)"), take before parenthesis
        if (targetValue.contains("(")) {
            targetValue = targetValue.split("\\(")[0].trim();
        }

        // 3. If prefixed with a label like "Password: 112233" or "Contact: 98765", strip the label before colon
        if (targetValue.contains(":")) {
            String[] parts = targetValue.split(":", 2);
            targetValue = parts[1].trim();
        }

        try (Connection conn = getConnection()) {
            String upperType = reqType.toUpperCase();
            String upperRole = role.toUpperCase();

            // 1. Password Modification (All Roles)
            if (upperType.contains("PASSWORD")) {
                String sql;
                if ("STUDENT".equals(upperRole)) {
                    sql = "UPDATE students SET password_hash = ? WHERE reg_no = ?";
                } else if ("FACULTY".equals(upperRole)) {
                    sql = "UPDATE faculty SET password_hash = ? WHERE emp_id = ?";
                } else if ("PARENT".equals(upperRole)) {
                    sql = "UPDATE parents SET parent_password_hash = ? WHERE student_reg_no = ?";
                } else {
                    sql = "UPDATE admin_users SET password_hash = ? WHERE admin_id = ?";
                }

                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, targetValue);
                    stmt.setString(2, userId);
                    stmt.executeUpdate();
                }
            }
            // 2. Name Modification
            else if (upperType.contains("NAME")) {
                if ("STUDENT".equals(upperRole)) {
                    String sql = "UPDATE students SET full_name = ? WHERE reg_no = ?";
                    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                        stmt.setString(1, targetValue);
                        stmt.setString(2, userId);
                        stmt.executeUpdate();
                    }
                } else if ("FACULTY".equals(upperRole)) {
                    String sql = "UPDATE faculty SET full_name = ? WHERE emp_id = ?";
                    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                        stmt.setString(1, targetValue);
                        stmt.setString(2, userId);
                        stmt.executeUpdate();
                    }
                } else if ("PARENT".equals(upperRole)) {
                    String sql = "UPDATE students SET parent_name = ? WHERE reg_no = ?";
                    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                        stmt.setString(1, targetValue);
                        stmt.setString(2, userId);
                        stmt.executeUpdate();
                    }
                }
            }
            // 3. Email Modification
            else if (upperType.contains("EMAIL")) {
                if ("FACULTY".equals(upperRole)) {
                    String sql = "UPDATE faculty SET email = ? WHERE emp_id = ?";
                    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                        stmt.setString(1, targetValue);
                        stmt.setString(2, userId);
                        stmt.executeUpdate();
                    }
                }
            }
            // 4. Contact Number Modification
            else if (upperType.contains("CONTACT") || upperType.contains("PHONE")) {
                if ("STUDENT".equals(upperRole) || "PARENT".equals(upperRole)) {
                    String sql = "UPDATE students SET contact_no = ? WHERE reg_no = ?";
                    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                        stmt.setString(1, targetValue);
                        stmt.setString(2, userId);
                        stmt.executeUpdate();
                    }
                }
            }

            JOptionPane.showMessageDialog(this, "Successfully updated " + reqType + " to '" + targetValue + "' for " + role + " (" + userId + ")!");

            // Refresh all table models
            loadStudentsFromDatabase();
            loadFacultyFromDatabase();
            loadParentsFromDatabase();

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Failed to apply database update:\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private void updateRequestStatus(int reqId, String newStatus) {
        String sql = "UPDATE system_requests SET status = ? WHERE request_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, newStatus);
            stmt.setInt(2, reqId);
            stmt.executeUpdate();
            loadRequestsFromDatabase();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    // --- 7. FEE RECONCILIATION & ADD NEW FEE DUE ---
    private JPanel createFeeReconciliationPanel() {
        JPanel panel = createBaseSectionPanel("System Fee Approvals & Intimations");

        JPanel topControlBar = new JPanel(new BorderLayout());
        topControlBar.setOpaque(false);

        JLabel infoLbl = new JLabel("Manage student semester fee dues, hostel records, and transaction reconciliations.");
        infoLbl.setFont(new Font("SansSerif", Font.PLAIN, 12));
        infoLbl.setForeground(new Color(100, 100, 100));

        JButton addFeeBtn = new JButton("+ Issue New Fee Due");
        addFeeBtn.setFont(new Font("SansSerif", Font.BOLD, 13));
        addFeeBtn.setBackground(SUCCESS_GREEN);
        addFeeBtn.setForeground(Color.WHITE);
        addFeeBtn.setOpaque(true);
        addFeeBtn.setContentAreaFilled(true);
        addFeeBtn.setBorderPainted(false);
        addFeeBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        addFeeBtn.addActionListener(e -> openAddNewFeeModal());

        topControlBar.add(infoLbl, BorderLayout.WEST);
        topControlBar.add(addFeeBtn, BorderLayout.EAST);

        String[] columns = {"Receipt No", "Reg No", "Description", "Amount", "Due Date", "Status"};
        feeTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(feeTableModel);
        table.setRowHeight(28);

        JPanel wrapper = new JPanel(new BorderLayout(0, 15));
        wrapper.setOpaque(false);
        wrapper.add(topControlBar, BorderLayout.NORTH);
        wrapper.add(new JScrollPane(table), BorderLayout.CENTER);

        panel.add(wrapper, BorderLayout.CENTER);
        return panel;
    }

    private void openAddNewFeeModal() {
        JDialog dialog = new JDialog(this, "Issue New Fee Invoice to Student(s)", true);
        dialog.setSize(460, 400);
        dialog.setLocationRelativeTo(this);

        JPanel form = new JPanel(new GridLayout(6, 2, 10, 12));
        form.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));

        JComboBox<String> targetType = new JComboBox<>(new String[]{"All Enrolled Students", "Individual Student"});
        JComboBox<String> studentSelector = new JComboBox<>();
        studentSelector.setEnabled(false);

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT reg_no, full_name FROM students ORDER BY reg_no")) {
            while (rs.next()) {
                studentSelector.addItem(rs.getString("reg_no") + " - " + rs.getString("full_name"));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        targetType.addActionListener(e -> {
            boolean isIndividual = targetType.getSelectedIndex() == 1;
            studentSelector.setEnabled(isIndividual);
        });

        JTextField descField = new JTextField("Tuition Fee - Fall 2026");
        JTextField amountField = new JTextField("150000.00");
        JTextField dueDateField = new JTextField("2026-09-30");

        form.add(new JLabel("Assign Fee To:"));
        form.add(targetType);
        form.add(new JLabel("Select Specific Student:"));
        form.add(studentSelector);
        form.add(new JLabel("Fee Description:"));
        form.add(descField);
        form.add(new JLabel("Amount (₹):"));
        form.add(amountField);
        form.add(new JLabel("Due Date (YYYY-MM-DD):"));
        form.add(dueDateField);

        JButton saveBtn = new JButton("Generate Fee Invoice");
        saveBtn.setBackground(SUCCESS_GREEN);
        saveBtn.setForeground(Color.WHITE);
        saveBtn.setFont(new Font("SansSerif", Font.BOLD, 13));
        saveBtn.setOpaque(true);
        saveBtn.setContentAreaFilled(true);
        saveBtn.setBorderPainted(false);

        saveBtn.addActionListener(e -> {
            String desc = descField.getText().trim();
            String amtStr = amountField.getText().trim();
            String dueDate = dueDateField.getText().trim();

            if (desc.isEmpty() || amtStr.isEmpty() || dueDate.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Please fill in all fee details.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            double amount;
            try {
                amount = Double.parseDouble(amtStr);
            } catch (NumberFormatException nfe) {
                JOptionPane.showMessageDialog(dialog, "Invalid amount format.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try (Connection conn = getConnection()) {
                List<String> targetRegNos = new ArrayList<>();

                if (targetType.getSelectedIndex() == 0) {
                    try (Statement stmt = conn.createStatement();
                         ResultSet rs = stmt.executeQuery("SELECT reg_no FROM students")) {
                        while (rs.next()) {
                            targetRegNos.add(rs.getString("reg_no"));
                        }
                    }
                } else {
                    if (studentSelector.getSelectedItem() == null) {
                        JOptionPane.showMessageDialog(dialog, "No student selected.", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    String selected = studentSelector.getSelectedItem().toString();
                    targetRegNos.add(selected.split(" - ")[0].trim());
                }

                if (targetRegNos.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "No target students found.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                String insertSql = "INSERT INTO fee_records (receipt_no, student_reg_no, description, amount, due_date, status) VALUES (?, ?, ?, ?, ?, 'PENDING')";
                try (PreparedStatement pstmt = conn.prepareStatement(insertSql)) {
                    Random rand = new Random();
                    for (String regNo : targetRegNos) {
                        String tempReceipt = "UTOP-DUE-" + (100000 + rand.nextInt(900000));
                        pstmt.setString(1, tempReceipt);
                        pstmt.setString(2, regNo);
                        pstmt.setString(3, desc);
                        pstmt.setDouble(4, amount);
                        pstmt.setDate(5, Date.valueOf(dueDate));
                        pstmt.addBatch();
                    }
                    pstmt.executeBatch();
                }

                JOptionPane.showMessageDialog(dialog, "Successfully generated and assigned fee due to " + targetRegNos.size() + " student(s)!", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadFeeRecordsFromDatabase();
                loadMetricsFromDatabase();
                dialog.dispose();

            } catch (SQLException | IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(dialog, "Error adding fee record:\n" + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        form.add(new JLabel(""));
        form.add(saveBtn);

        dialog.add(form);
        dialog.setVisible(true);
    }

    private JPanel createBaseSectionPanel(String sectionTitle) {
        JPanel panel = new JPanel(new BorderLayout(0, 15));
        panel.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));
        panel.setBackground(Color.WHITE);

        JLabel header = new JLabel(sectionTitle);
        header.setFont(new Font("SansSerif", Font.BOLD, 20));
        header.setForeground(HEADER_NAV_BLUE);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, BORDER_GRAY));

        panel.add(header, BorderLayout.NORTH);
        return panel;
    }

    // =========================================================================
    // DATABASE DATA SYNCHRONIZATION HELPERS
    // =========================================================================
    private void loadAllAdminDataFromDB() {
        loadMetricsFromDatabase();
        loadStudentsFromDatabase();
        loadFacultyFromDatabase();
        loadParentsFromDatabase();
        loadCoursesFromDatabase();
        loadRequestsFromDatabase();
        loadFeeRecordsFromDatabase();
    }

    private void loadMetricsFromDatabase() {
        try (Connection conn = getConnection()) {
            if (dbStatusVal != null) {
                dbStatusVal.setText("ACTIVE / ONLINE");
                dbStatusVal.setForeground(SUCCESS_GREEN);
            }

            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM students")) {
                if (rs.next() && totalStudentsVal != null) {
                    totalStudentsVal.setText(String.format("%,d", rs.getInt(1)));
                }
            }

            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM faculty")) {
                if (rs.next() && totalFacultyVal != null) {
                    totalFacultyVal.setText(String.format("%,d", rs.getInt(1)));
                }
            }

            String feeSql = "SELECT " +
                    "SUM(CASE WHEN status = 'PAID' THEN amount ELSE 0 END) AS paid_amt, " +
                    "SUM(amount) AS total_amt FROM fee_records";
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(feeSql)) {
                if (rs.next() && feeCollectionVal != null) {
                    double paid = rs.getDouble("paid_amt");
                    double total = rs.getDouble("total_amt");
                    double rate = (total > 0) ? (paid / total) * 100.0 : 100.0;
                    feeCollectionVal.setText(String.format("%.1f%%", rate));
                }
            }
        } catch (SQLException ex) {
            if (dbStatusVal != null) {
                dbStatusVal.setText("DISCONNECTED");
                dbStatusVal.setForeground(Color.RED);
            }
            System.err.println("Error loading metrics: " + ex.getMessage());
        }
    }

    private void loadStudentsFromDatabase() {
        if (studentTableModel == null) return;
        studentTableModel.setRowCount(0);
        try (Connection conn = getConnection()) {
            String query = "SELECT reg_no, full_name, branch, school, residency_status, cgpa, credits_earned, contact_no FROM students";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            while (rs.next()) {
                studentTableModel.addRow(new Object[]{
                        rs.getString("reg_no"),
                        rs.getString("full_name"),
                        rs.getString("branch"),
                        rs.getString("school"),
                        rs.getString("residency_status"),
                        rs.getDouble("cgpa"),
                        rs.getInt("credits_earned"),
                        rs.getString("contact_no")
                });
            }
        } catch (SQLException ex) {
            System.err.println("Error fetching students: " + ex.getMessage());
        }
    }

    private void loadFacultyFromDatabase() {
        if (facultyTableModel == null) return;
        facultyTableModel.setRowCount(0);
        try (Connection conn = getConnection()) {
            String query = "SELECT emp_id, full_name, designation, school, email FROM faculty";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            while (rs.next()) {
                facultyTableModel.addRow(new Object[]{
                        rs.getString("emp_id"),
                        rs.getString("full_name"),
                        rs.getString("designation"),
                        rs.getString("school"),
                        rs.getString("email")
                });
            }
        } catch (SQLException ex) {
            System.err.println("Error fetching faculty: " + ex.getMessage());
        }
    }

    private void loadParentsFromDatabase() {
        if (parentTableModel == null) return;
        parentTableModel.setRowCount(0);
        try (Connection conn = getConnection()) {
            String query = "SELECT p.student_reg_no, s.full_name AS student_name, " +
                    "s.parent_name, s.contact_no " +
                    "FROM parents p " +
                    "JOIN students s ON p.student_reg_no = s.reg_no";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            while (rs.next()) {
                parentTableModel.addRow(new Object[]{
                        rs.getString("student_reg_no"),
                        rs.getString("student_name"),
                        rs.getString("parent_name"),
                        rs.getString("contact_no")
                });
            }
        } catch (SQLException ex) {
            System.err.println("Error fetching parents: " + ex.getMessage());
        }
    }

    private void loadCoursesFromDatabase() {
        if (courseTableModel == null) return;
        courseTableModel.setRowCount(0);
        try (Connection conn = getConnection()) {
            String query = "SELECT c.course_code, c.course_title, c.school, c.credits, " +
                    "COALESCE(f.full_name, 'Not Assigned') AS instructor_name, " +
                    "COUNT(e.reg_no) AS enrolled_count " +
                    "FROM courses c " +
                    "LEFT JOIN faculty f ON c.faculty_emp_id = f.emp_id " +
                    "LEFT JOIN enrollments e ON c.course_code = e.course_code " +
                    "GROUP BY c.course_code, c.course_title, c.school, c.credits, f.full_name";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            while (rs.next()) {
                courseTableModel.addRow(new Object[]{
                        rs.getString("course_code"),
                        rs.getString("course_title"),
                        rs.getString("school"),
                        rs.getInt("credits"),
                        rs.getString("instructor_name"),
                        rs.getInt("enrolled_count") + " Students",
                        "➜"
                });
            }
        } catch (SQLException ex) {
            System.err.println("Error fetching courses: " + ex.getMessage());
        }
    }

    private void loadRequestsFromDatabase() {
        if (notificationsTableModel == null) return;
        notificationsTableModel.setRowCount(0);

        String filter = (requestFilterCombo != null && requestFilterCombo.getSelectedItem() != null)
                ? requestFilterCombo.getSelectedItem().toString()
                : "All Requests";

        String sql = "SELECT request_id, user_type, user_id, request_type, request_details, created_at, status FROM system_requests";
        if (!"All Requests".equalsIgnoreCase(filter)) {
            sql += " WHERE user_type = ?";
        }
        sql += " ORDER BY created_at DESC";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            if (!"All Requests".equalsIgnoreCase(filter)) {
                stmt.setString(1, filter);
            }

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                notificationsTableModel.addRow(new Object[]{
                        rs.getInt("request_id"),
                        rs.getString("user_type"),
                        rs.getString("user_id"),
                        rs.getString("request_type"),
                        rs.getString("request_details"),
                        rs.getString("created_at"),
                        rs.getString("status"),
                        "Process"
                });
            }
        } catch (SQLException ex) {
            System.err.println("Error fetching requests: " + ex.getMessage());
        }
    }

    private void loadFeeRecordsFromDatabase() {
        if (feeTableModel == null) return;
        feeTableModel.setRowCount(0);
        try (Connection conn = getConnection()) {
            String query = "SELECT receipt_no, student_reg_no, description, amount, due_date, status FROM fee_records";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            while (rs.next()) {
                feeTableModel.addRow(new Object[]{
                        rs.getString("receipt_no"),
                        rs.getString("student_reg_no"),
                        rs.getString("description"),
                        "₹ " + rs.getBigDecimal("amount"),
                        rs.getDate("due_date"),
                        rs.getString("status")
                });
            }
        } catch (SQLException ex) {
            System.err.println("Error fetching fee records: " + ex.getMessage());
        }
    }

    // =========================================================================
    // 3. CAPTCHA GRAPHICS PANEL
    // =========================================================================
    private static class CaptchaPanel extends JPanel {
        private String captchaCode;
        private final Random random = new Random();

        public CaptchaPanel() {
            setPreferredSize(new Dimension(170, 38));
            setBorder(BorderFactory.createLineBorder(BORDER_GRAY, 1));
            regenerateCaptcha();
        }

        public void regenerateCaptcha() {
            String chars = "ABCDEFGHJKMNPQRSTUVWXYZ23456789";
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 6; i++) {
                sb.append(chars.charAt(random.nextInt(chars.length())));
            }
            captchaCode = sb.toString();
            repaint();
        }

        public String getCaptchaText() {
            return captchaCode;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2d.setColor(new Color(240, 240, 240));
            g2d.fillRect(0, 0, getWidth(), getHeight());

            g2d.setColor(new Color(180, 180, 180));
            for (int i = 0; i < 5; i++) {
                int x1 = random.nextInt(getWidth());
                int y1 = random.nextInt(getHeight());
                int x2 = random.nextInt(getWidth());
                int y2 = random.nextInt(getHeight());
                g2d.drawLine(x1, y1, x2, y2);
            }

            g2d.setFont(new Font("Serif", Font.BOLD | Font.ITALIC, 22));
            g2d.setColor(new Color(140, 20, 20));

            FontMetrics fm = g2d.getFontMetrics();
            int charWidth = getWidth() / (captchaCode.length() + 1);

            for (int i = 0; i < captchaCode.length(); i++) {
                char ch = captchaCode.charAt(i);
                double angle = (random.nextDouble() - 0.5) * 0.4;

                AffineTransform originalTransform = g2d.getTransform();
                int x = (i + 1) * charWidth - 5;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;

                g2d.rotate(angle, x, y);
                g2d.drawString(String.valueOf(ch), x, y);
                g2d.setTransform(originalTransform);
            }

            g2d.dispose();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            AdminFrame frame = new AdminFrame();
            frame.setVisible(true);
        });
    }
}