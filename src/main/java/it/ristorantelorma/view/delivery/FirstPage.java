package it.ristorantelorma.view.delivery;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.Serializable;
import java.util.function.Consumer;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;

import it.ristorantelorma.view.authentication.LoginPage;
import it.ristorantelorma.view.authentication.RegisterPage;

/**
 * Represent the main window with the options for login and register actions.
 */
public class FirstPage implements Serializable {

    public static final long serialVersionUID = 960465863L;
    private static final Color BUTTON_BACKGROUND_COLOR = new Color(60, 179, 113); // Verde chiaro
    private static final Color BUTTON_BACKGROUND_HOVER_COLOR = new Color(34, 139, 34); // Verde scuro
    private static final Color BUTTON_TEXT_COLOR = Color.WHITE;
    private static final int BUTTON_FONT_SIZE = 16;
    private static final Dimension BUTTON_DIMENSION = new Dimension(200, 50);
    private static final int BUTTON_SPACING = 10;
    private static final int PADDING = 10;
    private static final int WINDOW_WIDTH = 400;
    private static final int WINDOW_HEIGHT = 300;

    private final JButton loginButton;
    private final JButton registerButton;
    private final JFrame mainFrame;

    /**
     * FirstPage constructor, initialize the main window and its components.
     */
    public FirstPage() {
        this.mainFrame = new JFrame("Ristorante LorMa");
        this.loginButton = createButton("Login");
        this.registerButton = createButton("Registrati");
        initializeFrame();
        initializeUI();
    }

    /**
     * Initialize the basic properties of the main frame.
     */
    private void initializeFrame() {
        this.mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.mainFrame.setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        this.mainFrame.setLocationRelativeTo(null);
        this.mainFrame.setResizable(false);
    }

    /**
     * Update the content of the main frame using the given consumer.
     * @param consumer
     */
    private void freshPane(final Consumer<Container> consumer) {
        final Container cp = this.mainFrame.getContentPane();
        cp.removeAll();
        cp.validate();
        cp.repaint();
        consumer.accept(cp);
        this.mainFrame.pack();
    }

    /**
     * Initialize the user interface.
     */
    private void initializeUI() {
        freshPane(container -> {
            final var padding = BorderFactory.createEmptyBorder(PADDING, PADDING, PADDING, PADDING);
            ((JComponent) container).setBorder(padding);
            container.setLayout(new BorderLayout());
            container.setBackground(Color.WHITE);

            final JPanel buttonPanel = new JPanel();
            buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
            buttonPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
            buttonPanel.setBackground(Color.WHITE);

            buttonPanel.add(Box.createVerticalGlue()); // Space for the centered buttons
            buttonPanel.add(createAlignedButtonPanel(loginButton));
            buttonPanel.add(Box.createRigidArea(new Dimension(0, BUTTON_SPACING))); // Space between the buttons
            buttonPanel.add(createAlignedButtonPanel(registerButton));
            buttonPanel.add(Box.createVerticalGlue()); // Space for the centered buttons

            container.add(buttonPanel, BorderLayout.CENTER);
        });

        // Resize the window
        this.mainFrame.setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
    }

    /**
     * Create a custom button with predefined style and handlers.
     * @param text the text to show on the button
     * @return the custom JButton
     */
    private JButton createButton(final String text) {
        final JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.PLAIN, BUTTON_FONT_SIZE));
        button.setBackground(BUTTON_BACKGROUND_COLOR);
        button.setForeground(BUTTON_TEXT_COLOR);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setPreferredSize(BUTTON_DIMENSION);

        // Hover effect
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(final MouseEvent evt) {
                button.setBackground(BUTTON_BACKGROUND_HOVER_COLOR);
            }

            @Override
            public void mouseExited(final MouseEvent evt) {
                button.setBackground(BUTTON_BACKGROUND_COLOR);
            }
        });

        // Click handlers
        button.addActionListener(e -> {
            if ("Login".equals(text)) {
                handleLoginButtonClick();
            } else if ("Registrati".equals(text)) {
                handleRegisterButtonClick();
            }
        });

        return button;
    }

    /**
     * Create a custom centered button.
     * @param button
     * @return the JPanel with the centered button
     */
    private JPanel createAlignedButtonPanel(final JButton button) {
        final JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        panel.setBackground(Color.WHITE);
        panel.add(Box.createHorizontalGlue());
        panel.add(button);
        panel.add(Box.createHorizontalGlue());
        return panel;
    }

    /**
     * Handle the login button.
     */
    private void handleLoginButtonClick() {
        this.hide();
        new LoginPage(this).show();
    }

    /**
     * Handle the register button.
     */
    private void handleRegisterButtonClick() {
        this.hide();
        new RegisterPage(this).show();
    }

    /**
     * show this window.
     */
    public void show() {
        this.mainFrame.setVisible(true);
    }

    /**
     * Hide this window.
     */
    public void hide() {
        this.mainFrame.setVisible(false);
    }
}
