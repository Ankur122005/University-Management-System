package in.mak;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
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

public class StudentFrame extends JFrame {

    private CardLayout mainCardLayout;
    private JPanel mainContainer;

    // Logged-in session tracking
    private String loggedInRegNo = "";

    // Login Form Fields
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JTextField captchaInputField;
    private CaptchaPanel captchaDisplayPanel;

    // Dashboard Navigation
    private CardLayout dashboardCardLayout;
    private JPanel dashboardContentPanel;

    // Dynamic UI Panels & Models
    private JPanel studentDetailsCardPanel;
    private DefaultTableModel attendanceTableModel;
    private DefaultTableModel feeTableModel;
    private DefaultTableModel coursesTableModel;
    private DefaultTableModel timetableTableModel;
    private DefaultTableModel marksTableModel;
    private DefaultTableModel completedCoursesTableModel;
    private DefaultTableModel studentRequestsTableModel;
    private JTable studentRequestsTable;

    // Dynamic CGPA Labels
    private JLabel cgpaValLbl;
    private JLabel creditsValLbl;
    private JLabel standingValLbl;

    // UTOP Color Palette
    private static final Color PRIMARY_BLUE = new Color(13, 110, 253);
    private static final Color HEADER_NAV_BLUE = new Color(24, 43, 73);
    private static final Color BG_GRAY = new Color(245, 247, 250);
    private static final Color BORDER_GRAY = new Color(205, 210, 218);
    private static final Color SUCCESS_GREEN = new Color(25, 135, 84);
    private static final Color DANGER_RED = new Color(220, 53, 69);

    public StudentFrame() {
        setTitle("Student Portal - UTOP");
        setSize(1100, 740);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        mainCardLayout = new CardLayout();
        mainContainer = new JPanel(mainCardLayout);

        mainContainer.add(createLoginView(), "LOGIN");
        mainContainer.add(createDashboardView(), "DASHBOARD");

        add(mainContainer);
    }

    // =========================================================================
    // 1. LOGIN VIEW
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

        JLabel titleLabel = new JLabel("UTOP Student Login");
        titleLabel.setFont(new Font("Serif", Font.BOLD, 26));
        titleLabel.setForeground(new Color(30, 30, 30));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        usernameField = createStyledTextField();
        passwordField = createStyledPasswordField();
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

        JButton submitBtn = new JButton("Submit");
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
                openForgotPasswordRequestModal();
            }
        });

        JLabel forgotId = createHoverLink("Forgot LoginId");
        forgotId.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                JOptionPane.showMessageDialog(StudentFrame.this, "Your Login ID is your university Registration Number (e.g., 25BCE11174).\nContact administration if you cannot retrieve it.", "Forgot ID", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        linksPanel.add(forgotPass);
        linksPanel.add(new JLabel("|"));
        linksPanel.add(forgotId);

        card.add(titleLabel);
        card.add(Box.createRigidArea(new Dimension(0, 20)));
        card.add(createInputGroup("Username / Reg No", usernameField));
        card.add(Box.createRigidArea(new Dimension(0, 12)));
        card.add(createInputGroup("Password", passwordField));
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

    private void openForgotPasswordRequestModal() {
        JDialog dialog = new JDialog(this, "Submit Password Reset Request", true);
        dialog.setSize(420, 280);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridLayout(3, 2, 10, 12));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));

        JTextField regField = new JTextField();
        JPasswordField newPassField = new JPasswordField();

        panel.add(new JLabel("Your Reg No:"));
        panel.add(regField);
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
            String reg = regField.getText().trim();
            String desiredPass = new String(newPassField.getPassword()).trim();

            if (reg.isEmpty() || desiredPass.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Please enter your Registration Number and Desired New Password.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String insertSql = "INSERT INTO system_requests (user_type, user_id, request_type, request_details) VALUES ('STUDENT', ?, 'FORGOT_PASSWORD', ?)";
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(insertSql)) {

                stmt.setString(1, reg);
                stmt.setString(2, desiredPass);
                stmt.executeUpdate();

                JOptionPane.showMessageDialog(dialog, "Password reset request submitted successfully!\nAdmin will approve your request shortly.", "Request Sent", JOptionPane.INFORMATION_MESSAGE);
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

    // --- JDBC AUTHENTICATION ---
    private void processLogin() {
        String user = usernameField.getText().trim();
        String pass = new String(passwordField.getPassword()).trim();
        String enteredCaptcha = captchaInputField.getText().trim();

        if (user.isEmpty() || pass.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter both Username and Password.", "Authentication Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!enteredCaptcha.equalsIgnoreCase(captchaDisplayPanel.getCaptchaText())) {
            JOptionPane.showMessageDialog(this, "Incorrect CAPTCHA entered. Please try again.", "Authentication Error", JOptionPane.ERROR_MESSAGE);
            captchaDisplayPanel.regenerateCaptcha();
            captchaInputField.setText("");
            return;
        }

        String sql = "SELECT reg_no FROM students WHERE reg_no = ? AND password_hash = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, user);
            stmt.setString(2, pass);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                loggedInRegNo = rs.getString("reg_no");
                loadDashboardDataFromDB();
                mainCardLayout.show(mainContainer, "DASHBOARD");
            } else {
                JOptionPane.showMessageDialog(this, "Invalid Registration Number or Password.", "Access Denied", JOptionPane.ERROR_MESSAGE);
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database Connection Error: " + ex.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    // =========================================================================
    // 2. DASHBOARD VIEW
    // =========================================================================
    private JPanel createDashboardView() {
        JPanel dashboard = new JPanel(new BorderLayout());

        JPanel navHeader = new JPanel(new BorderLayout());
        navHeader.setBackground(HEADER_NAV_BLUE);
        navHeader.setPreferredSize(new Dimension(950, 55));
        navHeader.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        JLabel portalTitle = new JLabel("UTOP - Student Portal");
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
            loggedInRegNo = "";
            usernameField.setText("");
            passwordField.setText("");
            captchaInputField.setText("");
            captchaDisplayPanel.regenerateCaptcha();
            mainCardLayout.show(mainContainer, "LOGIN");
        });

        navHeader.add(portalTitle, BorderLayout.WEST);
        navHeader.add(logoutBtn, BorderLayout.EAST);

        JPanel sideMenu = new JPanel(new GridLayout(9, 1, 0, 8));
        sideMenu.setBackground(new Color(238, 242, 246));
        sideMenu.setPreferredSize(new Dimension(210, 0));
        sideMenu.setBorder(BorderFactory.createEmptyBorder(15, 10, 15, 10));

        dashboardCardLayout = new CardLayout();
        dashboardContentPanel = new JPanel(dashboardCardLayout);

        dashboardContentPanel.add(createStudentDetailsPanel(), "DETAILS");
        dashboardContentPanel.add(createTimetablePanel(), "TIMETABLE");
        dashboardContentPanel.add(createCoursesPanel(), "COURSES");
        dashboardContentPanel.add(createCompletedCoursesPanel(), "COMPLETED_COURSES");
        dashboardContentPanel.add(createMarksPanel(), "MARKS");
        dashboardContentPanel.add(createAttendancePanel(), "ATTENDANCE");
        dashboardContentPanel.add(createCGPAPanel(), "CGPA");
        dashboardContentPanel.add(createFeePanel(), "FEES");
        dashboardContentPanel.add(createStudentRequestsPanel(), "REQUESTS");

        sideMenu.add(createSidebarButton("Student Details", "DETAILS"));
        sideMenu.add(createSidebarButton("Class Timetable", "TIMETABLE"));
        sideMenu.add(createSidebarButton("Registered Courses", "COURSES"));
        sideMenu.add(createSidebarButton("Course History", "COMPLETED_COURSES"));
        sideMenu.add(createSidebarButton("Marks & Grades", "MARKS"));
        sideMenu.add(createSidebarButton("Attendance View", "ATTENDANCE"));
        sideMenu.add(createSidebarButton("CGPA & Academic Status", "CGPA"));
        sideMenu.add(createSidebarButton("Fee Payment", "FEES"));
        sideMenu.add(createSidebarButton("Service Requests", "REQUESTS"));

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

    // --- STUDENT DETAILS PANEL ---
    private JPanel createStudentDetailsPanel() {
        JPanel panel = createBaseSectionPanel("Student Profile & Details");

        studentDetailsCardPanel = new JPanel(new GridLayout(7, 2, 15, 12));
        studentDetailsCardPanel.setBackground(new Color(248, 249, 250));
        studentDetailsCardPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_GRAY, 1),
                BorderFactory.createEmptyBorder(20, 25, 20, 25)
        ));

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.add(studentDetailsCardPanel, BorderLayout.NORTH);

        panel.add(wrapper, BorderLayout.CENTER);
        return panel;
    }

    // --- TIMETABLE PANEL (6-SLOT MATRIX) ---
    private JPanel createTimetablePanel() {
        JPanel panel = createBaseSectionPanel("My Class Timetable (Enrolled Slots)");

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

    // --- ENROLLED COURSES PANEL ---
    private JPanel createCoursesPanel() {
        JPanel panel = createBaseSectionPanel("Current Enrolled Courses");
        String[] columns = {"Course Code", "Course Title", "Credits", "Semester", "Slot"};
        coursesTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable table = new JTable(coursesTableModel);
        table.setRowHeight(28);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    // --- COMPLETED COURSES PANEL ---
    private JPanel createCompletedCoursesPanel() {
        JPanel panel = createBaseSectionPanel("Completed Course History & Grades");
        String[] columns = {"Course Code", "Course Title", "Semester", "Credits", "Grade Earned"};
        completedCoursesTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable table = new JTable(completedCoursesTableModel);
        table.setRowHeight(28);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    // --- MARKS PANEL ---
    private JPanel createMarksPanel() {
        JPanel panel = createBaseSectionPanel("Continuous Assessment Marks");
        String[] columns = {"Course Code", "Course Title", "Assessment Name", "Max Marks", "Marks Scored"};
        marksTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable table = new JTable(marksTableModel);
        table.setRowHeight(28);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createAttendancePanel() {
        JPanel panel = createBaseSectionPanel("Attendance Summary");
        String[] columns = {"Course Code", "Course Title", "Attended", "Total Classes", "Percentage"};
        attendanceTableModel = new DefaultTableModel(columns, 0);
        JTable table = new JTable(attendanceTableModel);
        table.setRowHeight(28);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    // --- DYNAMIC CGPA & ACADEMIC PERFORMANCE PANEL ---
    private JPanel createCGPAPanel() {
        JPanel panel = createBaseSectionPanel("Academic Performance & Cumulative CGPA");
        JPanel card = new JPanel(new GridLayout(3, 1, 10, 10));
        card.setBackground(new Color(245, 248, 255));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(PRIMARY_BLUE, 1),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));

        cgpaValLbl = new JLabel("Cumulative GPA (CGPA): Calculating...");
        cgpaValLbl.setFont(new Font("SansSerif", Font.BOLD, 22));
        cgpaValLbl.setForeground(HEADER_NAV_BLUE);

        creditsValLbl = new JLabel("Total Credits Completed: Calculating...");
        creditsValLbl.setFont(new Font("SansSerif", Font.PLAIN, 16));

        standingValLbl = new JLabel("Academic Standing: Calculating...");
        standingValLbl.setFont(new Font("SansSerif", Font.PLAIN, 16));

        card.add(cgpaValLbl);
        card.add(creditsValLbl);
        card.add(standingValLbl);

        panel.add(card, BorderLayout.NORTH);
        return panel;
    }

    private JPanel createFeePanel() {
        JPanel panel = createBaseSectionPanel("Fee Payment Status & Intimation");

        JPanel intimationBanner = new JPanel(new BorderLayout(10, 0));
        intimationBanner.setBackground(new Color(255, 243, 205));
        intimationBanner.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(255, 224, 138), 1),
                BorderFactory.createEmptyBorder(12, 15, 12, 15)
        ));

        JLabel alertIcon = new JLabel("🔔 ");
        alertIcon.setFont(new Font("SansSerif", Font.BOLD, 18));

        JLabel intimationText = new JLabel("<html><b>Fees Intimation Notice:</b> Please clear any pending fee balance promptly to avoid late fine charges.</html>");
        intimationText.setFont(new Font("SansSerif", Font.PLAIN, 13));
        intimationText.setForeground(new Color(102, 77, 3));

        intimationBanner.add(alertIcon, BorderLayout.WEST);
        intimationBanner.add(intimationText, BorderLayout.CENTER);

        String[] columns = {"Description", "Amount", "Due Date", "Status", "Action / Receipt"};
        feeTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable table = new JTable(feeTableModel);
        table.setRowHeight(32);

        table.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                String text = (value == null) ? "" : value.toString();
                if ("Pay Now".equalsIgnoreCase(text)) {
                    JButton btn = new JButton("Pay Now");
                    btn.setFont(new Font("SansSerif", Font.BOLD, 12));
                    btn.setBackground(PRIMARY_BLUE);
                    btn.setForeground(Color.WHITE);
                    btn.setOpaque(true);
                    btn.setContentAreaFilled(true);
                    btn.setBorderPainted(false);
                    return btn;
                }
                JLabel lbl = new JLabel(text, SwingConstants.CENTER);
                lbl.setFont(new Font("SansSerif", Font.PLAIN, 13));
                return lbl;
            }
        });

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                int col = table.columnAtPoint(e.getPoint());

                if (col == 4 && "Pay Now".equals(table.getValueAt(row, col))) {
                    String feeDesc = table.getValueAt(row, 0).toString();
                    String feeAmount = table.getValueAt(row, 1).toString();
                    openPaymentModal(feeDesc, feeAmount, feeTableModel, row);
                }
            }
        });

        JPanel centerPanel = new JPanel(new BorderLayout(0, 15));
        centerPanel.setOpaque(false);
        centerPanel.add(intimationBanner, BorderLayout.NORTH);
        centerPanel.add(new JScrollPane(table), BorderLayout.CENTER);

        panel.add(centerPanel, BorderLayout.CENTER);
        return panel;
    }

    private void openPaymentModal(String description, String amount, DefaultTableModel model, int rowIndex) {
        JDialog payDialog = new JDialog(this, "UTOP Online Fee Gateway", true);
        payDialog.setSize(380, 260);
        payDialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridLayout(5, 1, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("Confirm Payment", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 16));

        JLabel descLbl = new JLabel("Item: " + description);
        JLabel amtLbl = new JLabel("Total Amount: " + amount);
        amtLbl.setFont(new Font("SansSerif", Font.BOLD, 14));

        JButton confirmBtn = new JButton("Process Payment");
        confirmBtn.setBackground(new Color(25, 135, 84));
        confirmBtn.setForeground(Color.WHITE);
        confirmBtn.setFont(new Font("SansSerif", Font.BOLD, 13));
        confirmBtn.setOpaque(true);
        confirmBtn.setContentAreaFilled(true);
        confirmBtn.setBorderPainted(false);
        confirmBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        confirmBtn.addActionListener(e -> {
            String receiptNo = "UTOP-" + (100000 + new Random().nextInt(900000));

            String updateSql = "UPDATE fee_records SET status = 'PAID', receipt_no = ? WHERE student_reg_no = ? AND description = ?";
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(updateSql)) {

                stmt.setString(1, receiptNo);
                stmt.setString(2, loggedInRegNo);
                stmt.setString(3, description);
                stmt.executeUpdate();

                JOptionPane.showMessageDialog(payDialog,
                        "Payment Successful!\nTransaction ID: " + receiptNo,
                        "Payment Complete",
                        JOptionPane.INFORMATION_MESSAGE);

                model.setValueAt("PAID", rowIndex, 3);
                model.setValueAt(receiptNo, rowIndex, 4);

            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(payDialog, "Failed to record payment in DB: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }

            payDialog.dispose();
        });

        panel.add(title);
        panel.add(descLbl);
        panel.add(amtLbl);
        panel.add(Box.createRigidArea(new Dimension(0, 5)));
        panel.add(confirmBtn);

        payDialog.add(panel);
        payDialog.setVisible(true);
    }

    // --- STUDENT SERVICE REQUESTS & PROFILE MODIFICATIONS PANEL ---
    private JPanel createStudentRequestsPanel() {
        JPanel panel = createBaseSectionPanel("Service Requests & Profile Correction Desk");

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);

        JLabel infoLbl = new JLabel("Submit or cancel pending requests (passwords, contacts, inquiries).");
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
        deleteRequestBtn.addActionListener(e -> deleteSelectedRequest());

        JButton newRequestBtn = new JButton("+ New Service Request");
        newRequestBtn.setBackground(PRIMARY_BLUE);
        newRequestBtn.setForeground(Color.WHITE);
        newRequestBtn.setFont(new Font("SansSerif", Font.BOLD, 13));
        newRequestBtn.setOpaque(true);
        newRequestBtn.setContentAreaFilled(true);
        newRequestBtn.setBorderPainted(false);
        newRequestBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        newRequestBtn.addActionListener(e -> openNewRequestModal());

        btnPanel.add(deleteRequestBtn);
        btnPanel.add(newRequestBtn);

        topBar.add(infoLbl, BorderLayout.WEST);
        topBar.add(btnPanel, BorderLayout.EAST);

        String[] cols = {"Req ID", "Request Type", "Details / Proposed Change", "Submission Date", "Status"};
        studentRequestsTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        studentRequestsTable = new JTable(studentRequestsTableModel);
        studentRequestsTable.setRowHeight(28);
        studentRequestsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JPanel wrapper = new JPanel(new BorderLayout(0, 15));
        wrapper.setOpaque(false);
        wrapper.add(topBar, BorderLayout.NORTH);
        wrapper.add(new JScrollPane(studentRequestsTable), BorderLayout.CENTER);

        panel.add(wrapper, BorderLayout.CENTER);
        return panel;
    }

    private void deleteSelectedRequest() {
        int selectedRow = studentRequestsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a request from the table to cancel.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int requestId = Integer.parseInt(studentRequestsTableModel.getValueAt(selectedRow, 0).toString());
        String status = studentRequestsTableModel.getValueAt(selectedRow, 4).toString();

        if (!"PENDING".equalsIgnoreCase(status)) {
            JOptionPane.showMessageDialog(this, "Only PENDING requests can be canceled.\nThis request has already been " + status + ".", "Action Not Allowed", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to delete Request ID #" + requestId + "?",
                "Confirm Cancellation",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            String deleteSql = "DELETE FROM system_requests WHERE request_id = ? AND user_id = ? AND status = 'PENDING'";
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(deleteSql)) {

                stmt.setInt(1, requestId);
                stmt.setString(2, loggedInRegNo);
                int rowsAffected = stmt.executeUpdate();

                if (rowsAffected > 0) {
                    JOptionPane.showMessageDialog(this, "Request #" + requestId + " has been successfully canceled.", "Request Canceled", JOptionPane.INFORMATION_MESSAGE);
                    loadStudentRequestsFromDB();
                } else {
                    JOptionPane.showMessageDialog(this, "Could not delete the request. It might have already been processed.", "Error", JOptionPane.ERROR_MESSAGE);
                }

            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Database Error: " + ex.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        }
    }

    private void openNewRequestModal() {
        JDialog dialog = new JDialog(this, "Submit New Service Request", true);
        dialog.setSize(460, 320);
        dialog.setLocationRelativeTo(this);

        JPanel form = new JPanel(new GridLayout(3, 2, 10, 12));
        form.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));

        JComboBox<String> reqTypeCombo = new JComboBox<>(new String[]{
                "CHANGE_PASSWORD",
                "CHANGE_CONTACT",
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
                JOptionPane.showMessageDialog(dialog, "Please fill in the modification value or inquiry message.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String sql = "INSERT INTO system_requests (user_type, user_id, request_type, request_details) VALUES ('STUDENT', ?, ?, ?)";
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setString(1, loggedInRegNo);
                stmt.setString(2, type);
                stmt.setString(3, detailVal);
                stmt.executeUpdate();

                JOptionPane.showMessageDialog(dialog, "Service request submitted successfully!", "Submitted", JOptionPane.INFORMATION_MESSAGE);
                loadStudentRequestsFromDB();
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
    // 3. DYNAMIC DATABASE FETCHING HELPERS & REAL CGPA CALCULATION
    // =========================================================================
    private void loadDashboardDataFromDB() {
        loadProfileFromDB();
        loadAttendanceFromDB();
        loadEnrollmentsAndTimetable();
        loadCompletedCoursesFromDB();
        loadEnrolledMarksFromDB();
        loadFeeRecordsFromDB();
        loadStudentRequestsFromDB();
        calculateAndDisplayRealCGPA();
    }

    private void loadProfileFromDB() {
        studentDetailsCardPanel.removeAll();
        String profileSql = "SELECT full_name, reg_no, contact_no, parent_name, branch, school, residency_status FROM students WHERE reg_no = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(profileSql)) {

            stmt.setString(1, loggedInRegNo);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String[][] details = {
                        {"Student Name:", rs.getString("full_name")},
                        {"Registration No:", rs.getString("reg_no")},
                        {"Contact Number:", rs.getString("contact_no")},
                        {"Parent's Name:", rs.getString("parent_name")},
                        {"Branch / Discipline:", rs.getString("branch")},
                        {"School:", rs.getString("school")},
                        {"Residency Status:", rs.getString("residency_status")}
                };

                for (String[] row : details) {
                    JLabel label = new JLabel(row[0]);
                    label.setFont(new Font("SansSerif", Font.BOLD, 14));
                    label.setForeground(HEADER_NAV_BLUE);

                    JLabel val = new JLabel(row[1] != null ? row[1] : "N/A");
                    val.setFont(new Font("SansSerif", Font.PLAIN, 14));

                    studentDetailsCardPanel.add(label);
                    studentDetailsCardPanel.add(val);
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        studentDetailsCardPanel.revalidate();
        studentDetailsCardPanel.repaint();
    }

    private void calculateAndDisplayRealCGPA() {
        double totalWeightedGradePoints = 0.0;
        int totalCreditsCompleted = 0;

        String completedSql = "SELECT credits_earned, grade FROM completed_courses WHERE reg_no = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(completedSql)) {

            stmt.setString(1, loggedInRegNo);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int credits = rs.getInt("credits_earned");
                String grade = rs.getString("grade");
                double gradePoint = convertGradeToPoint(grade);

                totalWeightedGradePoints += (credits * gradePoint);
                totalCreditsCompleted += credits;
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        String currentMarksSql = "SELECT c.credits, m.scored_marks, m.max_marks " +
                "FROM marks m " +
                "JOIN courses c ON m.course_code = c.course_code " +
                "WHERE m.student_reg_no = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(currentMarksSql)) {

            stmt.setString(1, loggedInRegNo);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int credits = rs.getInt("credits");
                double scored = rs.getDouble("scored_marks");
                double max = rs.getDouble("max_marks");

                if (max > 0) {
                    double percentage = (scored / max) * 100.0;
                    double gradePoint = convertPercentageToPoint(percentage);
                    totalWeightedGradePoints += (credits * gradePoint);
                    totalCreditsCompleted += credits;
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        double calculatedCGPA = totalCreditsCompleted > 0 ? (totalWeightedGradePoints / totalCreditsCompleted) : 0.0;

        String updateStudentSql = "UPDATE students SET cgpa = ?, credits_earned = ? WHERE reg_no = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(updateStudentSql)) {
            stmt.setDouble(1, calculatedCGPA);
            stmt.setInt(2, totalCreditsCompleted);
            stmt.setString(3, loggedInRegNo);
            stmt.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        if (cgpaValLbl != null) {
            cgpaValLbl.setText(String.format("Cumulative GPA (CGPA): %.2f", calculatedCGPA));
        }
        if (creditsValLbl != null) {
            creditsValLbl.setText("Total Credits Completed: " + totalCreditsCompleted + " / 160");
        }
        if (standingValLbl != null) {
            String standing = calculatedCGPA >= 9.0 ? "First Class with Distinction" :
                    (calculatedCGPA >= 8.0 ? "First Class" :
                            (calculatedCGPA >= 6.0 ? "Second Class" : "Pass"));
            standingValLbl.setText("Academic Standing: " + standing);
        }
    }

    private double convertGradeToPoint(String grade) {
        if (grade == null) return 0.0;
        switch (grade.trim().toUpperCase()) {
            case "S": return 10.0;
            case "A": return 9.0;
            case "B": return 8.0;
            case "C": return 7.0;
            case "D": return 6.0;
            case "E": return 5.0;
            default: return 0.0;
        }
    }

    private double convertPercentageToPoint(double pct) {
        if (pct >= 90.0) return 10.0;
        if (pct >= 80.0) return 9.0;
        if (pct >= 70.0) return 8.0;
        if (pct >= 60.0) return 7.0;
        if (pct >= 50.0) return 6.0;
        if (pct >= 40.0) return 5.0;
        return 0.0;
    }

    private void loadEnrollmentsAndTimetable() {
        if (coursesTableModel == null) return;
        coursesTableModel.setRowCount(0);

        List<String[]> enrolledList = new ArrayList<>();
        String enrollSql = "SELECT e.course_code, c.course_title, c.credits, e.semester, e.slot " +
                "FROM enrollments e " +
                "JOIN courses c ON e.course_code = c.course_code " +
                "WHERE e.reg_no = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(enrollSql)) {

            stmt.setString(1, loggedInRegNo);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String code = rs.getString("course_code");
                String title = rs.getString("course_title");
                int credits = rs.getInt("credits");
                String sem = rs.getString("semester");
                String slot = rs.getString("slot");

                coursesTableModel.addRow(new Object[]{code, title, credits, sem, slot});
                enrolledList.add(new String[]{code, slot});
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        buildStudentTimetable(enrolledList);
    }

    private void buildStudentTimetable(List<String[]> enrolledList) {
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

        for (String[] item : enrolledList) {
            String code = item[0];
            String slot = (item[1] != null) ? item[1] : "A1";
            String label = code + " (" + slot + ")";

            if (slot.contains("A1")) { grid[0][1] = label; grid[3][1] = label; }
            if (slot.contains("B1")) { grid[0][2] = label; grid[3][2] = label; }
            if (slot.contains("C1")) { grid[0][3] = label; grid[3][3] = label; }
            if (slot.contains("D1")) { grid[0][5] = label; grid[3][5] = label; }
            if (slot.contains("E1")) { grid[0][6] = label; grid[3][6] = label; }
            if (slot.contains("F1")) { grid[1][1] = label; grid[4][1] = label; }
            if (slot.contains("G1")) { grid[1][2] = label; grid[4][2] = label; }
            if (slot.contains("A2")) { grid[1][3] = label; grid[4][3] = label; }
            if (slot.contains("B2")) { grid[1][5] = label; grid[4][5] = label; }
            if (slot.contains("C2")) { grid[1][6] = label; grid[4][6] = label; }
            if (slot.contains("D2")) { grid[2][1] = label; }
            if (slot.contains("E2")) { grid[2][2] = label; }
            if (slot.contains("F2")) { grid[2][3] = label; }
            if (slot.contains("G2")) { grid[2][5] = label; }
            if (slot.contains("TA1") || slot.contains("TE1")) { grid[2][6] = label; }
        }

        for (String[] row : grid) {
            timetableTableModel.addRow(row);
        }
    }

    private void loadCompletedCoursesFromDB() {
        if (completedCoursesTableModel == null) return;
        completedCoursesTableModel.setRowCount(0);

        String sql = "SELECT cc.course_code, c.course_title, cc.semester, cc.credits_earned, cc.grade " +
                "FROM completed_courses cc " +
                "JOIN courses c ON cc.course_code = c.course_code " +
                "WHERE cc.reg_no = ? " +
                "ORDER BY cc.semester DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, loggedInRegNo);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                completedCoursesTableModel.addRow(new Object[]{
                        rs.getString("course_code"),
                        rs.getString("course_title"),
                        rs.getString("semester"),
                        rs.getInt("credits_earned"),
                        rs.getString("grade")
                });
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void loadEnrolledMarksFromDB() {
        if (marksTableModel == null) return;
        marksTableModel.setRowCount(0);

        String marksSql = "SELECT m.course_code, c.course_title, m.assessment_type, m.max_marks, m.scored_marks " +
                "FROM marks m " +
                "JOIN courses c ON m.course_code = c.course_code " +
                "JOIN enrollments e ON m.course_code = e.course_code AND m.student_reg_no = e.reg_no " +
                "WHERE m.student_reg_no = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(marksSql)) {

            stmt.setString(1, loggedInRegNo);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                marksTableModel.addRow(new Object[]{
                        rs.getString("course_code"),
                        rs.getString("course_title"),
                        rs.getString("assessment_type"),
                        rs.getDouble("max_marks"),
                        rs.getDouble("scored_marks")
                });
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void loadAttendanceFromDB() {
        attendanceTableModel.setRowCount(0);
        String attSql = "SELECT c.course_code, c.course_title, a.classes_attended, a.total_classes " +
                "FROM attendance a JOIN courses c ON a.course_code = c.course_code WHERE a.student_reg_no = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(attSql)) {

            stmt.setString(1, loggedInRegNo);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int attended = rs.getInt("classes_attended");
                int total = rs.getInt("total_classes");
                double pct = total > 0 ? ((double) attended / total) * 100.0 : 0.0;

                attendanceTableModel.addRow(new Object[]{
                        rs.getString("course_code"),
                        rs.getString("course_title"),
                        attended,
                        total,
                        String.format("%.1f%%", pct)
                });
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void loadFeeRecordsFromDB() {
        feeTableModel.setRowCount(0);
        String feeSql = "SELECT description, amount, due_date, status, receipt_no FROM fee_records WHERE student_reg_no = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(feeSql)) {

            stmt.setString(1, loggedInRegNo);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String status = rs.getString("status");
                String receipt = rs.getString("receipt_no");
                String actionOrReceipt = "PAID".equalsIgnoreCase(status) ? (receipt != null ? receipt : "N/A") : "Pay Now";

                feeTableModel.addRow(new Object[]{
                        rs.getString("description"),
                        "₹ " + String.format("%,.2f", rs.getDouble("amount")),
                        rs.getString("due_date"),
                        status,
                        actionOrReceipt
                });
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void loadStudentRequestsFromDB() {
        if (studentRequestsTableModel == null) return;
        studentRequestsTableModel.setRowCount(0);

        String sql = "SELECT request_id, request_type, request_details, created_at, status " +
                "FROM system_requests WHERE user_type = 'STUDENT' AND user_id = ? ORDER BY created_at DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, loggedInRegNo);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                studentRequestsTableModel.addRow(new Object[]{
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
            StudentFrame frame = new StudentFrame();
            frame.setVisible(true);
        });
    }
}
