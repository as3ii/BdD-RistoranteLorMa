package it.ristorantelorma.view.admin;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.BoxLayout;
import javax.swing.Box;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;

/**
 * Administration interface.
 */
public final class AdminDashboard {
    private final JFrame frame;
    private final JComboBox<String> restaurantComboBox;

    /**
     * Constructor for the admin dashboard.
     */
    public AdminDashboard() {
        frame = new JFrame("DeliveryDB");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        final int windowWidth = 800;
        final int windowHeight = 600;
        frame.setSize(windowWidth, windowHeight);
        frame.setLocationRelativeTo(null);
        frame.setLayout(new BorderLayout());

        // Top panel with Logout
        final JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        final JButton logoutButton = new JButton("Logout");
        topPanel.add(logoutButton);
        frame.add(topPanel, BorderLayout.NORTH);

        // View Reviews and ComboBox in the center
        final JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setBackground(Color.WHITE);
        centerPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        final JButton viewReviewsButton = new JButton("View Reviews");
        viewReviewsButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        final int verticalStrut = 40;
        centerPanel.add(Box.createVerticalStrut(verticalStrut));
        centerPanel.add(viewReviewsButton);
        final int verticalStrutSmall = 10;
        centerPanel.add(Box.createVerticalStrut(verticalStrutSmall));

        final int comboWidth = 200;
        final int comboHeight = 30;
        restaurantComboBox = new JComboBox<>(new String[]{"Osteria dei Sapori"}); // Da popolare dinamicamente
        restaurantComboBox.setMaximumSize(new Dimension(comboWidth, comboHeight));
        centerPanel.add(restaurantComboBox);
        centerPanel.add(Box.createVerticalGlue());

        frame.add(centerPanel, BorderLayout.CENTER);

        // Bottom panel with buttons
        final JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bottomPanel.add(new JButton("Top dish"));
        bottomPanel.add(new JButton("Most popular cuisine type"));
        bottomPanel.add(new JButton("5 most chosen restaurants"));
        bottomPanel.add(new JButton("Worst restaurant"));
        bottomPanel.add(new JButton("Best deliverer"));
        frame.add(bottomPanel, BorderLayout.SOUTH);
    }

    /**
     * Set visibility of the admin interface.
     * @param v
     */
    public void setVisible(final boolean v) {
        frame.setVisible(v);
    }
}
