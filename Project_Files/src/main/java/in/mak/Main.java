package in.mak;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Main extends JFrame {

    public Main() {
        setTitle("UTOP - UMS Bhopal Campus");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 750);
        setLocationRelativeTo(null);
        getContentPane().setBackground(Color.WHITE);
        setLayout(new BorderLayout());

        // ------------------- TOP HEADER -------------------
        JPanel headerPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Blue Header Gradient
                GradientPaint gp = new GradientPaint(
                        0, 0, new Color(15, 65, 125),
                        getWidth(), 0, new Color(30, 120, 190)
                );
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        headerPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 20, 12));
        headerPanel.setPreferredSize(new Dimension(getWidth(), 55));

        JLabel logoLabel = new JLabel("UMS");
        logoLabel.setFont(new Font("SansSerif", Font.BOLD, 22));
        logoLabel.setForeground(Color.WHITE);

        JLabel subLogoLabel = new JLabel("(Bhopal Campus)");
        subLogoLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        subLogoLabel.setForeground(new Color(220, 230, 245));

        headerPanel.add(logoLabel);
        headerPanel.add(subLogoLabel);

        add(headerPanel, BorderLayout.NORTH);

        // ------------------- MAIN CONTENT -------------------
        JPanel mainContent = new JPanel();
        mainContent.setLayout(new BoxLayout(mainContent, BoxLayout.Y_AXIS));
        mainContent.setOpaque(false);
        mainContent.setBorder(new EmptyBorder(30, 40, 20, 40));

        // 1. Title Banner Section
        JLabel titleLabel = new JLabel("UTOP translates to \"University on TOP\"", SwingConstants.CENTER);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 26));
        titleLabel.setForeground(new Color(24, 100, 180));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitleLabel = new JLabel(
                "A digital initiative by the institute facilitating Faculty, Staff, Students, Parents and Alumni to access and process Academics, Research, Supporting services at one common platform.",
                SwingConstants.CENTER
        );
        subtitleLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        subtitleLabel.setForeground(new Color(60, 60, 60));
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        mainContent.add(titleLabel);
        mainContent.add(Box.createRigidArea(new Dimension(0, 10)));
        mainContent.add(subtitleLabel);
        mainContent.add(Box.createRigidArea(new Dimension(0, 35)));

        // 2. Role Cards Section
        JPanel cardsContainer = new JPanel(new GridLayout(1, 4, 20, 0));
        cardsContainer.setOpaque(false);
        cardsContainer.setMaximumSize(new Dimension(1150, 110));
        cardsContainer.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Add 4 Interactive Cards
        cardsContainer.add(createRoleCard("Student", "🎓", new Color(0, 123, 255)));
        cardsContainer.add(createRoleCard("Employee", "👥", new Color(212, 160, 23)));
        cardsContainer.add(createRoleCard("Parent", "👨‍👩‍👧", new Color(40, 167, 69)));
        cardsContainer.add(createRoleCard("Admin", "🎓", new Color(23, 162, 184)));

        mainContent.add(cardsContainer);
        mainContent.add(Box.createRigidArea(new Dimension(0, 40)));

        // 3. Spotlight Box Section (Shifted to Left)
        JPanel spotlightContainer = new JPanel(new BorderLayout());
        spotlightContainer.setOpaque(false);
        spotlightContainer.setMaximumSize(new Dimension(1150, 180));
        spotlightContainer.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel spotlightCard = createSpotlightCard();
        // Shifted to LEFT
        spotlightContainer.add(spotlightCard, BorderLayout.WEST);

        mainContent.add(spotlightContainer);

        JScrollPane scrollPane = new JScrollPane(mainContent);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(Color.WHITE);
        add(scrollPane, BorderLayout.CENTER);
    }

    /**
     * Creates each role card with click event listener
     */
    private JPanel createRoleCard(String title, String iconSymbol, Color themeColor) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);

        card.setBorder(new CompoundBorder(
                BorderFactory.createLineBorder(new Color(225, 225, 225), 1),
                new MatteBorder(3, 0, 0, 0, themeColor)
        ));

        // Center Panel (Icon + Title)
        JPanel contentPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 20));
        contentPanel.setOpaque(false);

        JLabel iconLabel = new JLabel(iconSymbol);
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 32));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        titleLabel.setForeground(themeColor);

        contentPanel.add(iconLabel);
        contentPanel.add(titleLabel);

        // Right side Button
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 25));
        buttonPanel.setOpaque(false);

        JButton actionButton = new JButton("➔");
        actionButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        actionButton.setForeground(Color.WHITE);
        actionButton.setBackground(themeColor);
        actionButton.setFocusPainted(false);
        actionButton.setBorderPainted(false);
        actionButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        actionButton.setPreferredSize(new Dimension(38, 34));

        // Action Listener to close frame and instantiate target class
        actionButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Close current frame
                dispose();

                // Instantiate corresponding class
                switch (title) {
                    case "Student":
                        new StudentFrame().setVisible(true);
                        break;
                    case "Employee":
                        new EmployeeFrame().setVisible(true);
                        break;
                    case "Parent":
                        new ParentFrame().setVisible(true);
                        break;
                    case "Admin":
                        new AdminFrame().setVisible(true);
                        break;
                }
            }
        });

        buttonPanel.add(actionButton);

        card.add(contentPanel, BorderLayout.CENTER);
        card.add(buttonPanel, BorderLayout.EAST);

        return card;
    }

    /**
     * Creates Spotlight Box
     */
    private JPanel createSpotlightCard() {
        JPanel card = new JPanel(new BorderLayout());
        card.setPreferredSize(new Dimension(550, 150));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220), 1));

        // Header Panel
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(248, 248, 248));
        header.setBorder(new CompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(230, 230, 230)),
                new EmptyBorder(8, 12, 8, 12)
        ));

        JLabel title = new JLabel("Spotlight");
        title.setFont(new Font("SansSerif", Font.BOLD, 14));
        title.setForeground(new Color(128, 0, 32));

        JLabel moreLink = new JLabel("More ...");
        moreLink.setFont(new Font("SansSerif", Font.PLAIN, 13));
        moreLink.setForeground(new Color(128, 0, 32));
        moreLink.setCursor(new Cursor(Cursor.HAND_CURSOR));

        header.add(title, BorderLayout.WEST);
        header.add(moreLink, BorderLayout.EAST);

        // List Panel
        JPanel listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setBackground(Color.WHITE);
        listPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        String item1Text = "THE FIRST UNIVERSITY IN INDIA TO INTRODUCE CALTECH (COLLABORATIVE AND ACTIVE LEARNING THROUGH TECHNOLOGY)";
        String item2Text = "THE FIRST PRIVATE UNIVERSITY IN INDIA TO HAVE 100% DOCTORAL FACULTY";

        listPanel.add(createSpotlightItem(item1Text));
        listPanel.add(createSeparator());
        listPanel.add(createSpotlightItem(item2Text));

        card.add(header, BorderLayout.NORTH);
        card.add(listPanel, BorderLayout.CENTER);

        return card;
    }

    private JPanel createSpotlightItem(String text) {
        JPanel item = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        item.setOpaque(false);

        JLabel icon = new JLabel("⚡");
        icon.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 12));
        icon.setForeground(new Color(128, 0, 32));

        JLabel label = new JLabel("<html><body style='width: 450px;'>" + text + "</body></html>");
        label.setFont(new Font("SansSerif", Font.BOLD, 10));
        label.setForeground(new Color(40, 40, 40));

        item.add(icon);
        item.add(label);

        return item;
    }

    private JSeparator createSeparator() {
        JSeparator sep = new JSeparator(JSeparator.HORIZONTAL);
        sep.setForeground(new Color(235, 235, 235));
        sep.setBackground(new Color(235, 235, 235));
        return sep;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}
            new Main().setVisible(true);
        });
    }
}

// =================================================================
// TARGET CLASS PLACEHOLDERS (Replace these with your actual frames)
// =================================================================


