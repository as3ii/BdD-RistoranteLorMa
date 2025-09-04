package it.ristorantelorma.view.authentication;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.sql.Connection;
import java.util.function.Consumer;
import javax.swing.JOptionPane;
import it.ristorantelorma.controller.PasswordManager;
import it.ristorantelorma.model.DatabaseConnectionManager;
import it.ristorantelorma.model.Result;
import it.ristorantelorma.model.user.ClientUser;
import it.ristorantelorma.model.user.Role;
import it.ristorantelorma.model.user.User;
import it.ristorantelorma.view.FirstPage;
import it.ristorantelorma.view.ViewUtils;
import it.ristorantelorma.view.ViewUtils.Form;

/**
 * Represent the registration page.
 */
public class RegisterPage {

    private static final Color BUTTON_BACKGROUND_COLOR = new Color(60, 179, 113); // Verde chiaro
    private static final Color BUTTON_BACKGROUND_HOVER_COLOR = new Color(34, 139, 34); // Verde scuro
    private static final Color BUTTON_TEXT_COLOR = Color.WHITE;
    private static final int FONT_SIZE = 14;
    private static final Dimension BUTTON_DIMENSION = new Dimension(120, 35);
    private static final Dimension BUTTON_RISTORANTE_DIMENSION = new Dimension(220, 35);
    private static final int PADDING = 20;
    private static final int TEXT_FIELD_COLUMNS = 15;
    private static final String ERROR_WINDOW_TITLE = "Errore";
    private static final int WINDOW_WIDTH = 800;
    private static final int WINDOW_HEIGHT = 700;

    private final JFrame mainFrame;
    private final FirstPage parentPage;
    private final JTextField usernameField;
    private final JTextField nameField;
    private final JTextField surnameField;
    private final JTextField passwordField;
    private final JTextField streetField;
    private final JTextField houseNumberField;
    private final JTextField cityField;
    private final JTextField phoneField;
    private final JTextField emailField;
    private final JTextField creditField;
    private final JCheckBox deliveryManCheckBox;
    private final JCheckBox adminCheckBox;
    private JButton registerButton; // aggiungi questo campo

    /**
     * @param parentPage the parent page that opened this page
     */
    public RegisterPage(final FirstPage parentPage) {
        this.parentPage = parentPage;
        this.mainFrame = new JFrame("Registrazione - Ristorante LorMa");
        this.usernameField = new JTextField(TEXT_FIELD_COLUMNS);
        this.nameField = new JTextField(TEXT_FIELD_COLUMNS);
        this.surnameField = new JTextField(TEXT_FIELD_COLUMNS);
        this.passwordField = new JTextField(TEXT_FIELD_COLUMNS);
        this.streetField = new JTextField(TEXT_FIELD_COLUMNS);
        this.houseNumberField = new JTextField(TEXT_FIELD_COLUMNS);
        this.cityField = new JTextField(TEXT_FIELD_COLUMNS);
        this.phoneField = new JTextField(TEXT_FIELD_COLUMNS);
        this.emailField = new JTextField(TEXT_FIELD_COLUMNS);
        this.creditField = new JTextField(TEXT_FIELD_COLUMNS);
    this.deliveryManCheckBox = new JCheckBox("Registrati come fattorino");
    this.adminCheckBox = new JCheckBox("Registrati come amministratore");
        initializeFrame();
        initializeUI();
    }

    /**
     * Initialize the properties of the main frame.
     */
    private void initializeFrame() {
        this.mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.mainFrame.setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        this.mainFrame.setLocationRelativeTo(null);
        this.mainFrame.setResizable(false);
    }

    /**
     * Update the content of the main frame.
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
     * Initialize the user interface for the registration page.
     */
    private void initializeUI() {
        freshPane(container -> {
            final var padding = BorderFactory.createEmptyBorder(PADDING, PADDING, PADDING, PADDING);
            ((JComponent) container).setBorder(padding);
            container.setLayout(new BorderLayout());
            container.setBackground(Color.WHITE);

            final Form form = new Form();

            form.addField("Username:", usernameField);
            form.addField("Nome:", nameField);
            form.addField("Cognome:", surnameField);
            form.addField("Password:", passwordField);
            form.addField("Via:", streetField);
            form.addField("Civico:", houseNumberField);
            form.addField("Città:", cityField);
            form.addField("Telefono:", phoneField);
            form.addField("Email:", emailField);
            form.addField("Credito:", creditField);

            // Checkbox for delivery man
            form.addCenterComponent(deliveryManCheckBox);

            // Checkbox for admin
            form.addCenterComponent(adminCheckBox);

            final JPanel buttonPanel = createButtonPanel();
            form.addCenterComponent(buttonPanel);

            container.add(form, BorderLayout.CENTER);
        });

        this.mainFrame.setSize(WINDOW_WIDTH, WINDOW_HEIGHT);

        // Listeners for enablement of the register button
        final DocumentListener docListener = new DocumentListener() {
            @Override
            public void insertUpdate(final DocumentEvent e) {
                updateRegisterButtonState();
            }
            @Override
            public void removeUpdate(final DocumentEvent e) {
                updateRegisterButtonState();
            }
            @Override
            public void changedUpdate(final DocumentEvent e) {
                updateRegisterButtonState();
            }
        };
        usernameField.getDocument().addDocumentListener(docListener);
        nameField.getDocument().addDocumentListener(docListener);
        surnameField.getDocument().addDocumentListener(docListener);
        passwordField.getDocument().addDocumentListener(docListener);
        streetField.getDocument().addDocumentListener(docListener);
        houseNumberField.getDocument().addDocumentListener(docListener);
        cityField.getDocument().addDocumentListener(docListener);
        phoneField.getDocument().addDocumentListener(docListener);
        emailField.getDocument().addDocumentListener(docListener);
        creditField.getDocument().addDocumentListener(docListener);
        deliveryManCheckBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(final ItemEvent e) {
                updateRegisterButtonState();
            }
        });
        adminCheckBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(final ItemEvent e) {
                updateRegisterButtonState();
            }
        });

        updateRegisterButtonState();
    }

    /**
     * Create the button panel.
     * @return the new button panel
     */
    private JPanel createButtonPanel() {
        final JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        panel.setBackground(Color.WHITE);

        this.registerButton = createButton("Register"); // usa il campo
        final JButton resetButton = createButton("Reset");
        final JButton backButton = createButton("Back");
        final JButton registerRestaurantButton = createButton("Registrati come Ristorante");
        registerRestaurantButton.setPreferredSize(BUTTON_RISTORANTE_DIMENSION);

        panel.add(Box.createRigidArea(new Dimension(10, 0)));
        panel.add(registerButton);
        panel.add(Box.createRigidArea(new Dimension(10, 0)));
        panel.add(resetButton);
        panel.add(Box.createRigidArea(new Dimension(10, 0)));
        panel.add(backButton);
        panel.add(Box.createRigidArea(new Dimension(10, 0)));
        panel.add(registerRestaurantButton);
        panel.add(Box.createHorizontalGlue());

        return panel;
    }

    /**
     * Create a custom button.
     * @param text
     * @return the new custom button
     */
    private JButton createButton(final String text) {
        final JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.PLAIN, FONT_SIZE));
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

        // Action listeners
        button.addActionListener(e -> {
            switch (text) {
                case "Register":
                    handleRegisterButtonClick();
                    break;
                case "Reset":
                    handleResetButtonClick();
                    break;
                case "Back":
                    handleBackButtonClick();
                    break;
                case "Registrati come Ristorante":
                    handleRegisterRestaurantButtonClick();
                    break;
                default:
                    // Do nothing
            }
        });

        return button;
    }

    /**
     * Enable the register button only if all the fields are filled.
     */
    private void updateRegisterButtonState() {
        final boolean allFilled =
            !ViewUtils.isBlank(usernameField.getText())
            && !ViewUtils.isBlank(nameField.getText())
            && !ViewUtils.isBlank(surnameField.getText())
            && !ViewUtils.isBlank(passwordField.getText())
            && !ViewUtils.isBlank(streetField.getText())
            && !ViewUtils.isBlank(houseNumberField.getText())
            && !ViewUtils.isBlank(cityField.getText())
            && !ViewUtils.isBlank(phoneField.getText())
            && !ViewUtils.isBlank(emailField.getText())
            && !ViewUtils.isBlank(creditField.getText());
        if (registerButton != null) {
            registerButton.setEnabled(allFilled);
        }
    }

    /**
     * Handle the click on the register button.
     */
    private void handleRegisterButtonClick() {
        final String username = usernameField.getText().trim();
        final String name = nameField.getText().trim();
        final String surname = surnameField.getText().trim();
        final String password = passwordField.getText().trim();
        final String street = streetField.getText().trim();
        final String houseNumber = houseNumberField.getText().trim();
        final String city = cityField.getText().trim();
        final String phone = phoneField.getText().trim();
        final String email = emailField.getText().trim();
        final BigDecimal credit;
        try {
            credit = new BigDecimal(creditField.getText().trim());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(
                mainFrame,
                "Credito non valido.",
                ERROR_WINDOW_TITLE,
                JOptionPane.ERROR_MESSAGE
            );
            return;
        }
        final boolean isDeliveryMan = deliveryManCheckBox.isSelected();

        final boolean isAdmin = adminCheckBox.isSelected();
        final Role role;
        if (isAdmin) {
            role = Role.ADMIN;
        } else if (isDeliveryMan) {
            role = Role.DELIVERYMAN;
        } else {
            role = Role.CLIENT;
        }
        final String hashedPassword = PasswordManager.newEncodedPassword(password);
        if (hashedPassword == null) {
            JOptionPane.showMessageDialog(
                mainFrame,
                "Errore durante la cifratura della password",
                ERROR_WINDOW_TITLE,
                JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        final Connection conn = DatabaseConnectionManager.getInstance().getConnection();
        final Result<User> resInsert = User.DAO.insert(
            conn, name, surname, username, hashedPassword, phone,
            email, city, street, houseNumber, role
        );
        if (!resInsert.isSuccess()) {
            JOptionPane.showMessageDialog(
                mainFrame,
                "Registrazione fallita.\n" + resInsert.getErrorMessage(),
                ERROR_WINDOW_TITLE,
                JOptionPane.ERROR_MESSAGE
            );
            return;
        }
        if (role == Role.CLIENT) {
            final ClientUser user = (ClientUser) resInsert.getValue();
            final Result<ClientUser> resCredit = ClientUser.DAO.updateCredit(conn, user, credit);
            if (!resCredit.isSuccess()) {
                JOptionPane.showMessageDialog(
                    mainFrame,
                    "Registrazione fallita.\n" + resCredit.getErrorMessage(),
                    ERROR_WINDOW_TITLE,
                    JOptionPane.ERROR_MESSAGE
                );
                return;
            }
        }
        JOptionPane.showMessageDialog(
            mainFrame,
            "Registrazione avvenuta con successo!",
            "Successo",
            JOptionPane.INFORMATION_MESSAGE
        );
        handleResetButtonClick();
    }

    /**
     * Handle click on the reset button.
     */
    private void handleResetButtonClick() {
        usernameField.setText("");
        nameField.setText("");
        surnameField.setText("");
        passwordField.setText("");
        streetField.setText("");
        houseNumberField.setText("");
        cityField.setText("");
        phoneField.setText("");
        emailField.setText("");
        creditField.setText("");
        deliveryManCheckBox.setSelected(false);
        adminCheckBox.setSelected(false);
    }

    /**
     * Handle click on the back button.
     */
    private void handleBackButtonClick() {
        this.hide();
        this.parentPage.show();
    }

    /**
     * Hancle click on the button "Registrati come Ristorante".
     */
    private void handleRegisterRestaurantButtonClick() {
        this.hide();
        new RegisterRestaurantPage(
            this.parentPage,
            this,
            usernameField.getText().trim(),
            nameField.getText().trim(),
            surnameField.getText().trim(),
            passwordField.getText().trim(),
            streetField.getText().trim(),
            houseNumberField.getText().trim(),
            cityField.getText().trim(),
            phoneField.getText().trim(),
            emailField.getText().trim()
        ).show();
    }

    /**
     * Show registration window.
     */
    public void show() {
        this.mainFrame.setVisible(true);
    }

    /**
     * Hide registation window.
     */
    public void hide() {
        this.mainFrame.setVisible(false);
    }
}
