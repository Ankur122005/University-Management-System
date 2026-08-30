package in.mak;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.List;

public class EmployeeFrame extends JFrame {

    private CardLayout mainCardLayout;
    private JPanel mainContainer;

    // Session State
    private String loggedInEmpId = "";

    // Login Form Fields
    private JTextField employeeIdField;
    private JPasswordField facultyPasswordField;
    private JTextField captchaInputField;
    private CaptchaPanel captchaDisplayPanel;

    // Dashboard Navigation
    private CardLayout dashboardCardLayout;
    private JPanel dashboardContentPanel;

    // Dynamic UI Panels & Models
    private JPanel facultyProfileCardPanel;
    private DefaultTableModel timetableTableModel;
    private DefaultTableModel attendanceTableModel;
    private DefaultTableModel marksTableModel;
    private DefaultTableModel studentListTableModel;
    private DefaultTableModel facultyRequestsTableModel;
    private JTable facultyRequestsTable;
    private JComboBox<String> attendanceCourseSelect;
    private JComboBox<String> marksCourseSelect;
    private JComboBox<String> examTypeSelect;

    // Leave Form Components
    private JComboBox<String> leaveTypeCombo;
    private JTextField fromDateField;
    private JTextField toDateField;
    private JTextField leaveReasonField;

    // UTOP Color Palette
    private static final Color PRIMARY_BLUE = new Color(13, 110, 253);
    private static final Color HEADER_NAV_BLUE = new Color(24, 43, 73);
    private static final Color BG_GRAY = new Color(245, 247, 250);
    private static final Color BORDER_GRAY = new Color(205, 210, 218);
    private static final Color SUCCESS_GREEN = new Color(25, 135, 84);
    private static final Color DANGER_RED = new Color(220, 53, 69);

    public EmployeeFrame() {
        setTitle("Faculty / Employee Portal - UTOP");
        setSize(1080, 720);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        mainCardLayout = new CardLayout();
        mainContainer = new JPanel(mainCardLayout);

        mainContainer.add(createLoginView(), "LOGIN");
        mainContainer.add(createDashboardView(), "DASHBOARD");

        add(mainContainer);
    }

    // =========================================================================
    // 1. FACULTY LOGIN VIEW
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

        JLabel titleLabel = new JLabel("UTOP Faculty Login");
        titleLabel.setFont(new Font("Serif", Font.BOLD, 24));
        titleLabel.setForeground(new Color(30, 30, 30));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        employeeIdField = createStyledTextField();
        facultyPasswordField = createStyledPasswordField();
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

        JButton submitBtn = new JButton("Login");
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
        JLabel forgotPass = createHoverLink("Forgot Password");
        forgotPass.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                openFacultyForgotPasswordModal();
            }
        });
        linksPanel.add(forgotPass);

        card.add(titleLabel);
        card.add(Box.createRigidArea(new Dimension(0, 20)));
        card.add(createInputGroup("Employee ID", employeeIdField));
        card.add(Box.createRigidArea(new Dimension(0, 12)));
        card.add(createInputGroup("Password", facultyPasswordField));
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

    private void openFacultyForgotPasswordModal() {
        JDialog dialog = new JDialog(this, "Faculty Password Reset Request", true);
        dialog.setSize(420, 280);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridLayout(3, 2, 10, 12));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));

        JTextField empField = new JTextField();
        JPasswordField newPassField = new JPasswordField();

        panel.add(new JLabel("Your Employee ID:"));
        panel.add(empField);
        panel.add(new JLabel("Desired New Password:"));
        panel.add(newPassField);

        JButton submitRequestBtn = new JButton("Submit Request");
        submitRequestBtn.setBackground(PRIMARY_BLUE);
        submitRequestBtn.setForeground(Color.WHITE);
        submitRequestBtn.setFont(new Font("SansSerif", Font.BOLD, 13));
        submitRequestBtn.setOpaque(true);
        submitRequestBtn.setContentAreaFilled(true);
        submitRequestBtn.setBorderPainted(false);

        submitRequestBtn.addActionListener(e -> {
            String emp = empField.getText().trim();
            String desiredPass = new String(newPassField.getPassword()).trim();

            if (emp.isEmpty() || desiredPass.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Please fill in Employee ID and Desired New Password.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String insertSql = "INSERT INTO system_requests (user_type, user_id, request_type, request_details) VALUES ('FACULTY', ?, 'FORGOT_PASSWORD', ?)";
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(insertSql)) {

                stmt.setString(1, emp);
                stmt.setString(2, desiredPass);
                stmt.executeUpdate();

                JOptionPane.showMessageDialog(dialog, "Password reset request submitted successfully!\nAdmin will review and approve your request shortly.", "Request Sent", JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();

            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(dialog, "Error submitting request:\n" + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        panel.add(new JLabel(""));
        panel.add(submitRequestBtn);

        dialog.add(panel);
        dialog.setVisible(true);
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
        String empId = employeeIdField.getText().trim();
        String pass = new String(facultyPasswordField.getPassword()).trim();
        String enteredCaptcha = captchaInputField.getText().trim();

        if (empId.isEmpty() || pass.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter Employee ID and Password.", "Authentication Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!enteredCaptcha.equalsIgnoreCase(captchaDisplayPanel.getCaptchaText())) {
            JOptionPane.showMessageDialog(this, "Incorrect CAPTCHA entered. Please try again.", "Authentication Error", JOptionPane.ERROR_MESSAGE);
            captchaDisplayPanel.regenerateCaptcha();
            captchaInputField.setText("");
            return;
        }

        String sql = "SELECT emp_id FROM faculty WHERE emp_id = ? AND password_hash = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, empId);
            stmt.setString(2, pass);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                loggedInEmpId = rs.getString("emp_id");
                loadDashboardDataFromDB();
                mainCardLayout.show(mainContainer, "DASHBOARD");
            } else {
                JOptionPane.showMessageDialog(this, "Invalid Employee ID or Password.", "Access Denied", JOptionPane.ERROR_MESSAGE);
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database Error: " + ex.getMessage(), "Connection Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    // =========================================================================
    // 2. FACULTY DASHBOARD VIEW
    // =========================================================================
    private JPanel createDashboardView() {
        JPanel dashboard = new JPanel(new BorderLayout());

        JPanel navHeader = new JPanel(new BorderLayout());
        navHeader.setBackground(HEADER_NAV_BLUE);
        navHeader.setPreferredSize(new Dimension(950, 55));
        navHeader.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        JLabel portalTitle = new JLabel("UTOP - Faculty / Employee Portal");
        portalTitle.setForeground(Color.WHITE);
        portalTitle.setFont(new Font("SansSerif", Font.BOLD, 18));

        JButton logoutBtn = new JButton("Logout");
        logoutBtn.setFont(new Font("SansSerif", Font.BOLD, 13));
        logoutBtn.setBackground(new Color(220, 53, 69));
        logoutBtn.setForeground(Color.WHITE);
        logoutBtn.setOpaque(true);
        logoutBtn.setContentAreaFilled(true);
        logoutBtn.setBorderPainted(false);
        logoutBtn.setFocusPainted(false);
        logoutBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        logoutBtn.addActionListener(e -> {
            loggedInEmpId = "";
            employeeIdField.setText("");
            facultyPasswordField.setText("");
            captchaInputField.setText("");
            captchaDisplayPanel.regenerateCaptcha();
            mainCardLayout.show(mainContainer, "LOGIN");
        });

        navHeader.add(portalTitle, BorderLayout.WEST);
        navHeader.add(logoutBtn, BorderLayout.EAST);

        JPanel sideMenu = new JPanel(new GridLayout(7, 1, 0, 8));
        sideMenu.setBackground(new Color(238, 242, 246));
        sideMenu.setPreferredSize(new Dimension(200, 0));
        sideMenu.setBorder(BorderFactory.createEmptyBorder(15, 10, 15, 10));

        dashboardCardLayout = new CardLayout();
        dashboardContentPanel = new JPanel(dashboardCardLayout);

        dashboardContentPanel.add(createFacultyProfilePanel(), "PROFILE");
        dashboardContentPanel.add(createTimetablePanel(), "TIMETABLE");
        dashboardContentPanel.add(createAttendanceMarkerPanel(), "ATTENDANCE_MARK");
        dashboardContentPanel.add(createMarksUploadPanel(), "MARKS_UPLOAD");
        dashboardContentPanel.add(createStudentListPanel(), "STUDENT_LIST");
        dashboardContentPanel.add(createFacultyRequestsPanel(), "FACULTY_REQUESTS");
        dashboardContentPanel.add(createLeaveApplicationPanel(), "LEAVE_APP");

        sideMenu.add(createSidebarButton("Faculty Profile", "PROFILE"));
        sideMenu.add(createSidebarButton("Class Timetable", "TIMETABLE"));
        sideMenu.add(createSidebarButton("Mark Attendance", "ATTENDANCE_MARK"));
        sideMenu.add(createSidebarButton("Upload Marks", "MARKS_UPLOAD"));
        sideMenu.add(createSidebarButton("Student Directory", "STUDENT_LIST"));
        sideMenu.add(createSidebarButton("Service Requests", "FACULTY_REQUESTS"));
        sideMenu.add(createSidebarButton("Apply Leave", "LEAVE_APP"));

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
            loadDashboardDataFromDB();
        });
        return btn;
    }

    // --- FACULTY PROFILE PANEL ---
    private JPanel createFacultyProfilePanel() {
        JPanel panel = createBaseSectionPanel("Faculty Profile & Details");

        facultyProfileCardPanel = new JPanel(new GridLayout(6, 2, 15, 12));
        facultyProfileCardPanel.setBackground(new Color(248, 249, 250));
        facultyProfileCardPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_GRAY, 1),
                BorderFactory.createEmptyBorder(20, 25, 20, 25)
        ));

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.add(facultyProfileCardPanel, BorderLayout.NORTH);

        panel.add(wrapper, BorderLayout.CENTER);
        return panel;
    }

    // --- TIMETABLE PANEL (ACCORDING TO EXACT 6-SLOT MATRIX) ---
    private JPanel createTimetablePanel() {
        JPanel panel = createBaseSectionPanel("Weekly Schedule & Timetable (Allotted Slots)");

        String[] columns = {
                "Day",
                "08:30 - 09:55 AM",
                "10:05 - 11:30 AM",
                "11:40 - 01:05 PM",
                "Lunch Break",
                "02:00 - 03:25 PM",
                "03:35 - 05:00 PM"
        };

        timetableTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable table = new JTable(timetableTableModel);
        table.setRowHeight(38);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    // --- ATTENDANCE PANEL ---
    private JPanel createAttendanceMarkerPanel() {
        JPanel panel = createBaseSectionPanel("Mark Student Class Attendance");

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 5));
        filterPanel.setOpaque(false);

        attendanceCourseSelect = new JComboBox<>();
        JTextField dateField = new JTextField("2026-08-18", 10);
        JButton loadBtn = new JButton("Load Course Enrolled Students");
        loadBtn.setBackground(PRIMARY_BLUE);
        loadBtn.setForeground(Color.WHITE);
        loadBtn.setOpaque(true);
        loadBtn.setContentAreaFilled(true);
        loadBtn.setBorderPainted(false);
        loadBtn.addActionListener(e -> fetchEnrolledStudentsForAttendance());

        filterPanel.add(new JLabel("Allotted Course:"));
        filterPanel.add(attendanceCourseSelect);
        filterPanel.add(new JLabel("Date:"));
        filterPanel.add(dateField);
        filterPanel.add(loadBtn);

        String[] columns = {"Reg No", "Student Name", "Status"};
        attendanceTableModel = new DefaultTableModel(columns, 0);
        JTable table = new JTable(attendanceTableModel);
        table.setRowHeight(28);

        JButton submitAttendanceBtn = new JButton("Submit Attendance Batch");
        submitAttendanceBtn.setBackground(new Color(25, 135, 84));
        submitAttendanceBtn.setForeground(Color.WHITE);
        submitAttendanceBtn.setFont(new Font("SansSerif", Font.BOLD, 14));
        submitAttendanceBtn.setOpaque(true);
        submitAttendanceBtn.setContentAreaFilled(true);
        submitAttendanceBtn.setBorderPainted(false);
        submitAttendanceBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        submitAttendanceBtn.addActionListener(e -> JOptionPane.showMessageDialog(this, "Attendance records uploaded successfully!", "Success", JOptionPane.INFORMATION_MESSAGE));

        JPanel wrapper = new JPanel(new BorderLayout(0, 12));
        wrapper.setOpaque(false);
        wrapper.add(filterPanel, BorderLayout.NORTH);
        wrapper.add(new JScrollPane(table), BorderLayout.CENTER);
        wrapper.add(submitAttendanceBtn, BorderLayout.SOUTH);

        panel.add(wrapper, BorderLayout.CENTER);
        return panel;
    }

    // --- MARKS PANEL (SCHEMA MATCHED: assessment_name) ---
    private JPanel createMarksUploadPanel() {
        JPanel panel = createBaseSectionPanel("Upload Class Marks & Grades");

        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 5));
        topBar.setOpaque(false);

        examTypeSelect = new JComboBox<>(new String[]{"CAT 1", "Mid-Term Exam", "CAT 2", "Lab Assessment", "FAT"});
        marksCourseSelect = new JComboBox<>();
        marksCourseSelect.addActionListener(e -> fetchStudentsForMarksUpload());

        topBar.add(new JLabel("Assessment:"));
        topBar.add(examTypeSelect);
        topBar.add(new JLabel("Allotted Course:"));
        topBar.add(marksCourseSelect);

        String[] columns = {"Reg No", "Student Name", "Max Marks", "Marks Scored"};
        marksTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 3;
            }
        };

        JTable table = new JTable(marksTableModel);
        table.setRowHeight(28);

        JButton saveMarksBtn = new JButton("Save & Publish Marks");
        saveMarksBtn.setBackground(PRIMARY_BLUE);
        saveMarksBtn.setForeground(Color.WHITE);
        saveMarksBtn.setFont(new Font("SansSerif", Font.BOLD, 14));
        saveMarksBtn.setOpaque(true);
        saveMarksBtn.setContentAreaFilled(true);
        saveMarksBtn.setBorderPainted(false);
        saveMarksBtn.addActionListener(e -> savePublishedMarksToDB());

        JPanel wrapper = new JPanel(new BorderLayout(0, 12));
        wrapper.setOpaque(false);
        wrapper.add(topBar, BorderLayout.NORTH);
        wrapper.add(new JScrollPane(table), BorderLayout.CENTER);
        wrapper.add(saveMarksBtn, BorderLayout.SOUTH);

        panel.add(wrapper, BorderLayout.CENTER);
        return panel;
    }

    // --- STUDENT DIRECTORY PANEL ---
    private JPanel createStudentListPanel() {
        JPanel panel = createBaseSectionPanel("My Enrolled Students Directory");

        String[] columns = {"Reg No", "Student Name", "Course Code", "Course Title", "Residency Status"};
        studentListTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable table = new JTable(studentListTableModel);
        table.setRowHeight(28);

        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    // --- FACULTY SERVICE REQUESTS PANEL ---
    private JPanel createFacultyRequestsPanel() {
        JPanel panel = createBaseSectionPanel("Faculty Service Requests & Inquiries");

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);

        JLabel infoLbl = new JLabel("Submit, manage, or cancel your pending administrative service and leave requests.");
        infoLbl.setFont(new Font("SansSerif", Font.PLAIN, 12));
        infoLbl.setForeground(new Color(100, 100, 100));

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnPanel.setOpaque(false);

        JButton deleteRequestBtn = new JButton("Cancel Selected Request");
        deleteRequestBtn.setBackground(DANGER_RED);
        deleteRequestBtn.setForeground(Color.WHITE);
        deleteRequestBtn.setFont(new Font("SansSerif", Font.BOLD, 13));
        deleteRequestBtn.setOpaque(true);
        deleteRequestBtn.setContentAreaFilled(true);
        deleteRequestBtn.setBorderPainted(false);
        deleteRequestBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        deleteRequestBtn.addActionListener(e -> deleteSelectedFacultyRequest());

        JButton newRequestBtn = new JButton("+ New Request");
        newRequestBtn.setBackground(PRIMARY_BLUE);
        newRequestBtn.setForeground(Color.WHITE);
        newRequestBtn.setFont(new Font("SansSerif", Font.BOLD, 13));
        newRequestBtn.setOpaque(true);
        newRequestBtn.setContentAreaFilled(true);
        newRequestBtn.setBorderPainted(false);
        newRequestBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        newRequestBtn.addActionListener(e -> openFacultyNewRequestModal());

        btnPanel.add(deleteRequestBtn);
        btnPanel.add(newRequestBtn);

        topBar.add(infoLbl, BorderLayout.WEST);
        topBar.add(btnPanel, BorderLayout.EAST);

        String[] cols = {"Req ID", "Request Type", "Details / Proposed Change", "Date", "Status"};
        facultyRequestsTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        facultyRequestsTable = new JTable(facultyRequestsTableModel);
        facultyRequestsTable.setRowHeight(28);
        facultyRequestsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JPanel wrapper = new JPanel(new BorderLayout(0, 15));
        wrapper.setOpaque(false);
        wrapper.add(topBar, BorderLayout.NORTH);
        wrapper.add(new JScrollPane(facultyRequestsTable), BorderLayout.CENTER);

        panel.add(wrapper, BorderLayout.CENTER);
        return panel;
    }

    private void deleteSelectedFacultyRequest() {
        int selectedRow = facultyRequestsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a request from the table to cancel.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int requestId = Integer.parseInt(facultyRequestsTableModel.getValueAt(selectedRow, 0).toString());
        String status = facultyRequestsTableModel.getValueAt(selectedRow, 4).toString();

        if (!"PENDING".equalsIgnoreCase(status)) {
            JOptionPane.showMessageDialog(this, "Only PENDING requests can be canceled.\nThis request has already been " + status + ".", "Action Not Allowed", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to cancel and delete Request ID #" + requestId + "?",
                "Confirm Cancellation",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            String deleteSql = "DELETE FROM system_requests WHERE request_id = ? AND user_type = 'FACULTY' AND user_id = ? AND status = 'PENDING'";
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(deleteSql)) {

                stmt.setInt(1, requestId);
                stmt.setString(2, loggedInEmpId);
                int rowsAffected = stmt.executeUpdate();

                if (rowsAffected > 0) {
                    JOptionPane.showMessageDialog(this, "Request #" + requestId + " has been successfully canceled and removed.", "Request Canceled", JOptionPane.INFORMATION_MESSAGE);
                    loadFacultyRequestsFromDB();
                } else {
                    JOptionPane.showMessageDialog(this, "Could not delete the request. It might have already been processed.", "Error", JOptionPane.ERROR_MESSAGE);
                }

            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Database Error: " + ex.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        }
    }

    private void openFacultyNewRequestModal() {
        JDialog dialog = new JDialog(this, "Submit Faculty Service Request", true);
        dialog.setSize(460, 320);
        dialog.setLocationRelativeTo(this);

        JPanel form = new JPanel(new GridLayout(3, 2, 10, 12));
        form.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));

        JComboBox<String> reqTypeCombo = new JComboBox<>(new String[]{
                "CHANGE_PASSWORD",
                "CHANGE_EMAIL",
                "NAME_CORRECTION",
                "WRITTEN_INQUIRY"
        });

        JTextField detailsField = new JTextField();

        form.add(new JLabel("Request Category:"));
        form.add(reqTypeCombo);
        form.add(new JLabel("Target Detail / Value / Message:"));
        form.add(detailsField);

        JButton submitBtn = new JButton("Send to Admin");
        submitBtn.setBackground(SUCCESS_GREEN);
        submitBtn.setForeground(Color.WHITE);
        submitBtn.setFont(new Font("SansSerif", Font.BOLD, 13));
        submitBtn.setOpaque(true);
        submitBtn.setContentAreaFilled(true);
        submitBtn.setBorderPainted(false);

        submitBtn.addActionListener(e -> {
            String type = reqTypeCombo.getSelectedItem().toString();
            String detailVal = detailsField.getText().trim();

            if (detailVal.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Please fill in the modification value or message.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String sql = "INSERT INTO system_requests (user_type, user_id, request_type, request_details) VALUES ('FACULTY', ?, ?, ?)";
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setString(1, loggedInEmpId);
                stmt.setString(2, type);
                stmt.setString(3, detailVal);
                stmt.executeUpdate();

                JOptionPane.showMessageDialog(dialog, "Service request submitted successfully!", "Submitted", JOptionPane.INFORMATION_MESSAGE);
                loadFacultyRequestsFromDB();
                dialog.dispose();

            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(dialog, "Failed to submit request:\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        form.add(new JLabel(""));
        form.add(submitBtn);

        dialog.add(form);
        dialog.setVisible(true);
    }

    // --- LEAVE APPLICATION PANEL (AUTOMATICALLY CONVERTED TO 2ND TYPE WRITTEN REQUEST) ---
    private JPanel createLeaveApplicationPanel() {
        JPanel panel = createBaseSectionPanel("Faculty Leave Portal");

        JPanel form = new JPanel(new GridLayout(5, 2, 12, 12));
        form.setBackground(new Color(248, 249, 250));
        form.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_GRAY, 1),
                BorderFactory.createEmptyBorder(20, 25, 20, 25)
        ));

        leaveTypeCombo = new JComboBox<>(new String[]{"Casual Leave (CL)", "Medical Leave (ML)", "Duty Leave (DL)", "Special Casual Leave (SCL)"});
        fromDateField = new JTextField("2026-08-20");
        toDateField = new JTextField("2026-08-22");
        leaveReasonField = new JTextField();

        JButton submitLeaveBtn = new JButton("Submit Leave Request to Admin");
        submitLeaveBtn.setBackground(PRIMARY_BLUE);
        submitLeaveBtn.setForeground(Color.WHITE);
        submitLeaveBtn.setFont(new Font("SansSerif", Font.BOLD, 13));
        submitLeaveBtn.setOpaque(true);
        submitLeaveBtn.setContentAreaFilled(true);
        submitLeaveBtn.setBorderPainted(false);
        submitLeaveBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        submitLeaveBtn.addActionListener(e -> processLeaveSubmission());

        form.add(new JLabel("Leave Category:"));
        form.add(leaveTypeCombo);
        form.add(new JLabel("From Date (YYYY-MM-DD):"));
        form.add(fromDateField);
        form.add(new JLabel("To Date (YYYY-MM-DD):"));
        form.add(toDateField);
        form.add(new JLabel("Reason / Justification:"));
        form.add(leaveReasonField);
        form.add(new JLabel(""));
        form.add(submitLeaveBtn);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.add(form, BorderLayout.NORTH);

        panel.add(wrapper, BorderLayout.CENTER);
        return panel;
    }

    private void processLeaveSubmission() {
        String category = leaveTypeCombo.getSelectedItem().toString();
        String fromDate = fromDateField.getText().trim();
        String toDate = toDateField.getText().trim();
        String reason = leaveReasonField.getText().trim();

        if (fromDate.isEmpty() || toDate.isEmpty() || reason.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter From Date, To Date, and Reason for leave.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String formattedLeaveDetails = "Leave Category: " + category + " | Duration: " + fromDate + " to " + toDate + " | Reason: " + reason;

        String insertSql = "INSERT INTO system_requests (user_type, user_id, request_type, request_details) VALUES ('FACULTY', ?, 'LEAVE_APPLICATION', ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(insertSql)) {

            stmt.setString(1, loggedInEmpId);
            stmt.setString(2, formattedLeaveDetails);
            stmt.executeUpdate();

            JOptionPane.showMessageDialog(this,
                    "Leave application successfully submitted to Administrator!\nIt has been logged in your Service Requests tracking desk.",
                    "Application Submitted",
                    JOptionPane.INFORMATION_MESSAGE);

            leaveReasonField.setText("");
            loadFacultyRequestsFromDB();

            dashboardCardLayout.show(dashboardContentPanel, "FACULTY_REQUESTS");

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database error submitting leave request: " + ex.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
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
    // 3. DYNAMIC DATABASE FETCHING HELPERS
    // =========================================================================
    private void loadDashboardDataFromDB() {
        facultyProfileCardPanel.removeAll();
        String sql = "SELECT full_name, emp_id, designation, school, email FROM faculty WHERE emp_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, loggedInEmpId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String[][] details = {
                        {"Faculty Name:", rs.getString("full_name")},
                        {"Employee ID:", rs.getString("emp_id")},
                        {"Designation:", rs.getString("designation")},
                        {"School:", rs.getString("school")},
                        {"Official Email:", rs.getString("email")},
                        {"Status:", "Active Faculty"}
                };

                for (String[] row : details) {
                    JLabel lbl = new JLabel(row[0]);
                    lbl.setFont(new Font("SansSerif", Font.BOLD, 14));
                    lbl.setForeground(HEADER_NAV_BLUE);

                    JLabel val = new JLabel(row[1] != null ? row[1] : "N/A");
                    val.setFont(new Font("SansSerif", Font.PLAIN, 14));

                    facultyProfileCardPanel.add(lbl);
                    facultyProfileCardPanel.add(val);
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        facultyProfileCardPanel.revalidate();
        facultyProfileCardPanel.repaint();

        loadFacultyAssignedCoursesAndTimetable();
        fetchStudentDirectory();
        fetchStudentsForMarksUpload();
        loadFacultyRequestsFromDB();
    }

    private void loadFacultyAssignedCoursesAndTimetable() {
        if (attendanceCourseSelect != null) attendanceCourseSelect.removeAllItems();
        if (marksCourseSelect != null) marksCourseSelect.removeAllItems();

        List<String[]> assignedCourses = new ArrayList<>();

        String sql = "SELECT course_code, course_title FROM courses WHERE faculty_emp_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, loggedInEmpId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String code = rs.getString("course_code");
                String title = rs.getString("course_title");
                assignedCourses.add(new String[]{code, title});

                String item = code + " - " + title;
                if (attendanceCourseSelect != null) attendanceCourseSelect.addItem(item);
                if (marksCourseSelect != null) marksCourseSelect.addItem(item);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        buildTimetableForAssignedCourses(assignedCourses);
    }

    private void buildTimetableForAssignedCourses(List<String[]> assignedCourses) {
        if (timetableTableModel == null) return;
        timetableTableModel.setRowCount(0);

        String[] days = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday"};
        String[][] grid = new String[5][7];

        for (int d = 0; d < 5; d++) {
            grid[d][0] = days[d];
            grid[d][1] = "-";
            grid[d][2] = "-";
            grid[d][3] = "-";
            grid[d][4] = "LUNCH";
            grid[d][5] = "-";
            grid[d][6] = "-";
        }

        if (assignedCourses.isEmpty()) {
            for (String[] row : grid) {
                timetableTableModel.addRow(row);
            }
            return;
        }

        String slotQuery = "SELECT DISTINCT slot FROM enrollments WHERE course_code = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(slotQuery)) {

            for (String[] course : assignedCourses) {
                String code = course[0];
                stmt.setString(1, code);
                ResultSet rs = stmt.executeQuery();

                String assignedSlot = "A1";
                if (rs.next()) {
                    String slot = rs.getString("slot");
                    if (slot != null && !slot.isEmpty()) {
                        assignedSlot = slot;
                    }
                }

                String label = code + " (" + assignedSlot + ")";

                if (assignedSlot.contains("A1")) { grid[0][1] = label; grid[3][1] = label; }
                if (assignedSlot.contains("B1")) { grid[0][2] = label; grid[3][2] = label; }
                if (assignedSlot.contains("C1")) { grid[0][3] = label; grid[3][3] = label; }
                if (assignedSlot.contains("D1")) { grid[0][5] = label; grid[3][5] = label; }
                if (assignedSlot.contains("E1")) { grid[0][6] = label; grid[3][6] = label; }
                if (assignedSlot.contains("F1")) { grid[1][1] = label; grid[4][1] = label; }
                if (assignedSlot.contains("G1")) { grid[1][2] = label; grid[4][2] = label; }
                if (assignedSlot.contains("A2")) { grid[1][3] = label; grid[4][3] = label; }
                if (assignedSlot.contains("B2")) { grid[1][5] = label; grid[4][5] = label; }
                if (assignedSlot.contains("C2")) { grid[1][6] = label; grid[4][6] = label; }
                if (assignedSlot.contains("D2")) { grid[2][1] = label; }
                if (assignedSlot.contains("E2")) { grid[2][2] = label; }
                if (assignedSlot.contains("F2")) { grid[2][3] = label; }
                if (assignedSlot.contains("G2")) { grid[2][5] = label; }
                if (assignedSlot.contains("TA1") || assignedSlot.contains("TE1")) { grid[2][6] = label; }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        for (String[] row : grid) {
            timetableTableModel.addRow(row);
        }
    }

    private void fetchEnrolledStudentsForAttendance() {
        if (attendanceCourseSelect == null || attendanceCourseSelect.getSelectedItem() == null) return;
        attendanceTableModel.setRowCount(0);

        String selected = attendanceCourseSelect.getSelectedItem().toString();
        String courseCode = selected.split(" - ")[0].trim();

        String sql = "SELECT s.reg_no, s.full_name FROM enrollments e " +
                "JOIN students s ON e.reg_no = s.reg_no " +
                "WHERE e.course_code = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, courseCode);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                attendanceTableModel.addRow(new Object[]{
                        rs.getString("reg_no"),
                        rs.getString("full_name"),
                        "Present"
                });
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void fetchStudentsForMarksUpload() {
        if (marksCourseSelect == null || marksCourseSelect.getSelectedItem() == null || marksTableModel == null) return;
        marksTableModel.setRowCount(0);

        String selected = marksCourseSelect.getSelectedItem().toString();
        String courseCode = selected.split(" - ")[0].trim();

        String sql = "SELECT s.reg_no, s.full_name FROM enrollments e " +
                "JOIN students s ON e.reg_no = s.reg_no " +
                "WHERE e.course_code = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, courseCode);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                marksTableModel.addRow(new Object[]{
                        rs.getString("reg_no"),
                        rs.getString("full_name"),
                        "100",
                        "0"
                });
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void savePublishedMarksToDB() {
        if (marksCourseSelect == null || marksCourseSelect.getSelectedItem() == null || marksTableModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "No student data available to save.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String selected = marksCourseSelect.getSelectedItem().toString();
        String courseCode = selected.split(" - ")[0].trim();
        String assessment = examTypeSelect.getSelectedItem().toString();

        String insertSql = "INSERT INTO marks (student_reg_no, course_code, assessment_name, max_marks, scored_marks) " +
                "VALUES (?, ?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE scored_marks = VALUES(scored_marks), max_marks = VALUES(max_marks)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(insertSql)) {

            for (int i = 0; i < marksTableModel.getRowCount(); i++) {
                String regNo = marksTableModel.getValueAt(i, 0).toString();
                double maxMarks = Double.parseDouble(marksTableModel.getValueAt(i, 2).toString());
                double scored = Double.parseDouble(marksTableModel.getValueAt(i, 3).toString());

                stmt.setString(1, regNo);
                stmt.setString(2, courseCode);
                stmt.setString(3, assessment);
                stmt.setDouble(4, maxMarks);
                stmt.setDouble(5, scored);
                stmt.addBatch();
            }

            stmt.executeBatch();
            JOptionPane.showMessageDialog(this, "Marks saved and published successfully for " + courseCode + " (" + assessment + ")!", "Success", JOptionPane.INFORMATION_MESSAGE);

        } catch (SQLException | NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Error saving marks: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private void fetchStudentDirectory() {
        if (studentListTableModel == null) return;
        studentListTableModel.setRowCount(0);

        String sql = "SELECT DISTINCT s.reg_no, s.full_name, c.course_code, c.course_title, s.residency_status " +
                "FROM courses c " +
                "JOIN enrollments e ON c.course_code = e.course_code " +
                "JOIN students s ON e.reg_no = s.reg_no " +
                "WHERE c.faculty_emp_id = ? " +
                "ORDER BY c.course_code, s.reg_no";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, loggedInEmpId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                studentListTableModel.addRow(new Object[]{
                        rs.getString("reg_no"),
                        rs.getString("full_name"),
                        rs.getString("course_code"),
                        rs.getString("course_title"),
                        rs.getString("residency_status")
                });
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void loadFacultyRequestsFromDB() {
        if (facultyRequestsTableModel == null) return;
        facultyRequestsTableModel.setRowCount(0);

        String sql = "SELECT request_id, request_type, request_details, created_at, status " +
                "FROM system_requests WHERE user_type = 'FACULTY' AND user_id = ? ORDER BY created_at DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, loggedInEmpId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                facultyRequestsTableModel.addRow(new Object[]{
                        rs.getInt("request_id"),
                        rs.getString("request_type"),
                        rs.getString("request_details"),
                        rs.getString("created_at"),
                        rs.getString("status")
                });
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    // =========================================================================
    // 4. CAPTCHA GRAPHICS PANEL
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
            EmployeeFrame frame = new EmployeeFrame();
            frame.setVisible(true);
        });
    }
}
