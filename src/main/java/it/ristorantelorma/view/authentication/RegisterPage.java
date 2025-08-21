package it.ristorantelorma.view.authentication;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import it.ristorantelorma.view.delivery.FirstPage;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.function.Consumer;
import javax.swing.JOptionPane;

import it.ristorantelorma.model.DatabaseConnectionManager;
import it.ristorantelorma.model.Queries;

/**
 * Rappresenta la pagina di registrazione dell'applicazione.
 */
public class RegisterPage {

    private static final Color BUTTON_BACKGROUND_COLOR = new Color(60, 179, 113); // Verde chiaro
    private static final Color BUTTON_BACKGROUND_HOVER_COLOR = new Color(34, 139, 34); // Verde scuro
    private static final Color BUTTON_TEXT_COLOR = Color.WHITE;
    private static final int BUTTON_FONT_SIZE = 14;
    private static final Dimension BUTTON_DIMENSION = new Dimension(120, 35);
    private static final int PADDING = 20;

    private final JFrame mainFrame;
    private final FirstPage parentPage;
    private final JTextField usernameField;
    private final JTextField nomeField;
    private final JTextField cognomeField;
    private final JTextField passwordField;
    private final JTextField viaField;
    private final JTextField civicoField;
    private final JTextField cittaField;
    private final JTextField telefonoField;
    private final JTextField emailField;
    private final JTextField creditoField;
    private final JCheckBox deliveryManCheckBox;
    private JButton registerButton; // aggiungi questo campo

    /**
     * Costruttore di RegisterPage.
     *
     * @param parentPage la pagina parent che ha aperto questa pagina
     */
    public RegisterPage(final FirstPage parentPage) {
        this.parentPage = parentPage;
        this.mainFrame = new JFrame("Registrazione - Ristorante LorMa");
        this.usernameField = new JTextField(15);
        this.nomeField = new JTextField(15);
        this.cognomeField = new JTextField(15);
        this.passwordField = new JTextField(15);
        this.viaField = new JTextField(15);
        this.civicoField = new JTextField(15);
        this.cittaField = new JTextField(15);
        this.telefonoField = new JTextField(15);
        this.emailField = new JTextField(15);
        this.creditoField = new JTextField(15);
        this.deliveryManCheckBox = new JCheckBox("Registrati come fattorino");
        initializeFrame();
        initializeUI();
    }

    /**
     * Inizializza le proprietà base del frame principale.
     */
    private void initializeFrame() {
        this.mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.mainFrame.setSize(500, 600);
        this.mainFrame.setLocationRelativeTo(null);
        this.mainFrame.setResizable(false);
    }

    /**
     * Aggiorna il contenuto del frame principale.
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
     * Inizializza l'interfaccia utente per la pagina di registrazione.
     */
    private void initializeUI() {
        freshPane(container -> {
            final var padding = BorderFactory.createEmptyBorder(PADDING, PADDING, PADDING, PADDING);
            ((JComponent) container).setBorder(padding);
            container.setLayout(new BorderLayout());
            container.setBackground(Color.WHITE);

            // Pannello principale per il form
            final JPanel formPanel = new JPanel(new GridBagLayout());
            formPanel.setBackground(Color.WHITE);
            final GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(10, 10, 10, 10);

            // Aggiunge i campi del form
            addFormField(formPanel, gbc, "Username:", usernameField, 0);
            addFormField(formPanel, gbc, "Nome:", nomeField, 1);
            addFormField(formPanel, gbc, "Cognome:", cognomeField, 2);
            addFormField(formPanel, gbc, "Password:", passwordField, 3);
            addFormField(formPanel, gbc, "Via:", viaField, 4);
            addFormField(formPanel, gbc, "Civico:", civicoField, 5);
            addFormField(formPanel, gbc, "Città:", cittaField, 6);
            addFormField(formPanel, gbc, "Telefono:", telefonoField, 7);
            addFormField(formPanel, gbc, "Email:", emailField, 8);
            addFormField(formPanel, gbc, "Credito:", creditoField, 9);

            // Checkbox per delivery man
            gbc.gridx = 0;
            gbc.gridy = 10;
            gbc.gridwidth = 2;
            gbc.anchor = GridBagConstraints.CENTER;
            formPanel.add(deliveryManCheckBox, gbc);

            // Pannello per i bottoni
            final JPanel buttonPanel = createButtonPanel();
            gbc.gridx = 0;
            gbc.gridy = 11;
            gbc.gridwidth = 2;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            formPanel.add(buttonPanel, gbc);

            container.add(formPanel, BorderLayout.CENTER);
        });

        this.mainFrame.setSize(500, 700);

        // Aggiungi i listener per abilitare/disabilitare il bottone Register
        DocumentListener docListener = new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { updateRegisterButtonState(); }
            public void removeUpdate(DocumentEvent e) { updateRegisterButtonState(); }
            public void changedUpdate(DocumentEvent e) { updateRegisterButtonState(); }
        };
        usernameField.getDocument().addDocumentListener(docListener);
        nomeField.getDocument().addDocumentListener(docListener);
        cognomeField.getDocument().addDocumentListener(docListener);
        passwordField.getDocument().addDocumentListener(docListener);
        viaField.getDocument().addDocumentListener(docListener);
        civicoField.getDocument().addDocumentListener(docListener);
        cittaField.getDocument().addDocumentListener(docListener);
        telefonoField.getDocument().addDocumentListener(docListener);
        emailField.getDocument().addDocumentListener(docListener);
        creditoField.getDocument().addDocumentListener(docListener);
        deliveryManCheckBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                updateRegisterButtonState();
            }
        });

        updateRegisterButtonState();
    }

    /**
     * Aggiunge un campo del form al pannello.
     */
    private void addFormField(final JPanel panel, final GridBagConstraints gbc,
                             final String labelText, final JTextField field, final int row) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.EAST;
        final JLabel label = new JLabel(labelText);
        label.setFont(new Font("SansSerif", Font.PLAIN, 14));
        panel.add(label, gbc);

        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(field, gbc);
    }

    /**
     * Crea il pannello contenente i bottoni.
     */
    private JPanel createButtonPanel() {
        final JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        panel.setBackground(Color.WHITE);

        this.registerButton = createButton("Register"); // usa il campo
        final JButton resetButton = createButton("Reset");
        final JButton backButton = createButton("Back");

        panel.add(Box.createHorizontalGlue());
        panel.add(registerButton);
        panel.add(Box.createRigidArea(new Dimension(10, 0)));
        panel.add(resetButton);
        panel.add(Box.createRigidArea(new Dimension(10, 0)));
        panel.add(backButton);
        panel.add(Box.createHorizontalGlue());

        return panel;
    }

    /**
     * Crea un bottone con styling personalizzato.
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

        // Effetto hover
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
            }
        });

        return button;
    }

    /**
     * Abilita il bottone Register solo se tutti i campi sono riempiti.
     */
    private void updateRegisterButtonState() {
        boolean allFilled =
            !usernameField.getText().trim().isEmpty() &&
            !nomeField.getText().trim().isEmpty() &&
            !cognomeField.getText().trim().isEmpty() &&
            !passwordField.getText().trim().isEmpty() &&
            !viaField.getText().trim().isEmpty() &&
            !civicoField.getText().trim().isEmpty() &&
            !cittaField.getText().trim().isEmpty() &&
            !telefonoField.getText().trim().isEmpty() &&
            !emailField.getText().trim().isEmpty() &&
            !creditoField.getText().trim().isEmpty();
        if (registerButton != null) {
            registerButton.setEnabled(allFilled);
        }
    }

    /**
     * Gestisce il click del bottone Register.
     */
    private void handleRegisterButtonClick() {
        // Raccogli i dati dai campi
        String username = usernameField.getText().trim();
        String nome = nomeField.getText().trim();
        String cognome = cognomeField.getText().trim();
        String password = passwordField.getText().trim();
        String via = viaField.getText().trim();
        String civico = civicoField.getText().trim();
        String citta = cittaField.getText().trim();
        String telefono = telefonoField.getText().trim();
        String email = emailField.getText().trim();
        double credito = 0.0;
        try {
            credito = Double.parseDouble(creditoField.getText().trim());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(mainFrame, "Credito non valido.", "Errore", JOptionPane.ERROR_MESSAGE);
            return;
        }
        boolean isDeliveryMan = deliveryManCheckBox.isSelected();
        String ruolo = isDeliveryMan ? "fattorino" : "cliente";

        try (Connection conn = DatabaseConnectionManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(Queries.INSERT_USER)) {

            stmt.setString(1, nome);
            stmt.setString(2, cognome);
            stmt.setString(3, username);
            stmt.setString(4, password);
            stmt.setString(5, telefono);
            stmt.setString(6, email);
            stmt.setString(7, citta);
            stmt.setString(8, via);
            stmt.setString(9, civico);
            stmt.setDouble(10, credito);
            stmt.setString(11, ruolo);

            int rows = stmt.executeUpdate();
            if (rows > 0) {
                JOptionPane.showMessageDialog(mainFrame, "Registrazione avvenuta con successo!", "Successo", JOptionPane.INFORMATION_MESSAGE);
                handleResetButtonClick();
            } else {
                JOptionPane.showMessageDialog(mainFrame, "Registrazione fallita.", "Errore", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(mainFrame, "Errore durante la registrazione: " + ex.getMessage(), "Errore", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    /**
     * Gestisce il click del bottone Reset.
     */
    private void handleResetButtonClick() {
        usernameField.setText("");
        nomeField.setText("");
        cognomeField.setText("");
        passwordField.setText("");
        viaField.setText("");
        civicoField.setText("");
        cittaField.setText("");
        telefonoField.setText("");
        emailField.setText("");
        creditoField.setText("");
        deliveryManCheckBox.setSelected(false);
    }

    /**
     * Gestisce il click del bottone Back.
     */
    private void handleBackButtonClick() {
        this.hide();
        this.parentPage.show();
    }

    /**
     * Mostra la finestra di registrazione.
     */
    public void show() {
        this.mainFrame.setVisible(true);
    }

    /**
     * Nasconde la finestra di registrazione.
     */
    public void hide() {
        this.mainFrame.setVisible(false);
    }

    /**
     * Metodo main per testare la RegisterPage.
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new RegisterPage(null).show();
        });
    }
}
