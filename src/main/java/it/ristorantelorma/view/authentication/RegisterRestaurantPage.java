package it.ristorantelorma.view.authentication;

import it.ristorantelorma.model.DatabaseConnectionManager;
import it.ristorantelorma.model.Queries;
import it.ristorantelorma.model.Restaurant;
import it.ristorantelorma.model.Result;
import it.ristorantelorma.model.user.RestaurantUser;
import it.ristorantelorma.view.delivery.FirstPage;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;

public class RegisterRestaurantPage {

    private final JFrame mainFrame;
    private final FirstPage parentPage;
    private final RegisterPage registerPage;

    // Campi utente
    private final JTextField usernameField = new JTextField(15);
    private final JTextField nomeField = new JTextField(15);
    private final JTextField cognomeField = new JTextField(15);
    private final JTextField passwordField = new JTextField(15);
    private final JTextField viaField = new JTextField(15);
    private final JTextField civicoField = new JTextField(15);
    private final JTextField cittaField = new JTextField(15);
    private final JTextField telefonoField = new JTextField(15);
    private final JTextField emailField = new JTextField(15);

    // Campi ristorante
    private final JTextField nomeAttivitaField = new JTextField(15);
    private final JTextField partitaIVAField = new JTextField(15);
    private final JTextField aperturaField = new JTextField(15); // formato: HH:mm
    private final JTextField chiusuraField = new JTextField(15); // formato: HH:mm

    // Costruttore originale per compatibilità
    public RegisterRestaurantPage(final FirstPage parentPage, final RegisterPage registerPage) {
        this(parentPage, registerPage, "", "", "", "", "", "", "", "", "");
    }

    public RegisterRestaurantPage(final FirstPage parentPage, final RegisterPage registerPage,
                                  final String username, final String nome, final String cognome, final String password,
                                  final String via, final String civico, final String citta, final String telefono, final String email) {
        this.parentPage = parentPage;
        this.registerPage = registerPage;
        this.mainFrame = new JFrame("Registrazione Ristorante");

        // Imposta i valori e rendi i campi non modificabili
        usernameField.setText(username);
        usernameField.setEditable(false);
        nomeField.setText(nome);
        nomeField.setEditable(false);
        cognomeField.setText(cognome);
        cognomeField.setEditable(false);
        passwordField.setText(password);
        passwordField.setEditable(false);
        viaField.setText(via);
        viaField.setEditable(false);
        civicoField.setText(civico);
        civicoField.setEditable(false);
        cittaField.setText(citta);
        cittaField.setEditable(false);
        telefonoField.setText(telefono);
        telefonoField.setEditable(false);
        emailField.setText(email);
        emailField.setEditable(false);

        initializeUI();
    }

    private void initializeUI() {
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setSize(500, 700);
        mainFrame.setLocationRelativeTo(null);
        mainFrame.setResizable(false);

        final JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        final GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        int row = 0;
        addFormField(formPanel, gbc, "Username:", usernameField, row++);
        addFormField(formPanel, gbc, "Nome:", nomeField, row++);
        addFormField(formPanel, gbc, "Cognome:", cognomeField, row++);
        addFormField(formPanel, gbc, "Password:", passwordField, row++);
        addFormField(formPanel, gbc, "Via:", viaField, row++);
        addFormField(formPanel, gbc, "Civico:", civicoField, row++);
        addFormField(formPanel, gbc, "Città:", cittaField, row++);
        addFormField(formPanel, gbc, "Telefono:", telefonoField, row++);
        addFormField(formPanel, gbc, "Email:", emailField, row++);

        // Campi specifici ristorante
        addFormField(formPanel, gbc, "Nome Attività:", nomeAttivitaField, row++);
        addFormField(formPanel, gbc, "Partita IVA:", partitaIVAField, row++);
        addFormField(formPanel, gbc, "Ora Apertura (HH:mm):", aperturaField, row++);
        addFormField(formPanel, gbc, "Ora Chiusura (HH:mm):", chiusuraField, row++);

        // Bottoni
        final JPanel buttonPanel = new JPanel();
        final JButton registerButton = new JButton("Registra Ristorante");
        final JButton backButton = new JButton("Back");
        buttonPanel.add(registerButton);
        buttonPanel.add(backButton);

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        formPanel.add(buttonPanel, gbc);

        mainFrame.add(formPanel);

        registerButton.addActionListener(e -> handleRegisterRestaurant());
        backButton.addActionListener(e -> {
            this.hide();
            registerPage.show();
        });
    }

    private void addFormField(final JPanel panel, final GridBagConstraints gbc, final String labelText, final JTextField field, final int row) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.anchor = GridBagConstraints.EAST;
        panel.add(new JLabel(labelText), gbc);

        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(field, gbc);
    }

    private void handleRegisterRestaurant() {
        final String username = usernameField.getText().trim();
        final String nome = nomeField.getText().trim();
        final String cognome = cognomeField.getText().trim();
        final String password = passwordField.getText().trim();
        final String via = viaField.getText().trim();
        final String civico = civicoField.getText().trim();
        final String citta = cittaField.getText().trim();
        final String telefono = telefonoField.getText().trim();
        final String email = emailField.getText().trim();

        final String nomeAttivita = nomeAttivitaField.getText().trim();
        final String partitaIVA = partitaIVAField.getText().trim();
        final String apertura = aperturaField.getText().trim();
        final String chiusura = chiusuraField.getText().trim();

        if (username.isEmpty() || nome.isEmpty() || cognome.isEmpty() || password.isEmpty() ||
            via.isEmpty() || civico.isEmpty() || citta.isEmpty() || telefono.isEmpty() || email.isEmpty() ||
            nomeAttivita.isEmpty() || partitaIVA.isEmpty() || apertura.isEmpty() || chiusura.isEmpty()) {
            JOptionPane.showMessageDialog(mainFrame, "Compila tutti i campi!", "Errore", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try (Connection conn = DatabaseConnectionManager.getInstance().getConnection()) {
            // 1. Inserisci utente come RESTAURANT (se non esiste già)
            try (PreparedStatement checkStmt = conn.prepareStatement("SELECT username FROM utenti WHERE username = ?")) {
                checkStmt.setString(1, username);
                final ResultSet rs = checkStmt.executeQuery();
                if (!rs.next()) {
                    try (PreparedStatement insertStmt = conn.prepareStatement(Queries.INSERT_USER)) {
                        insertStmt.setString(1, nome);
                        insertStmt.setString(2, cognome);
                        insertStmt.setString(3, username);
                        insertStmt.setString(4, password);
                        insertStmt.setString(5, telefono);
                        insertStmt.setString(6, email);
                        insertStmt.setString(7, citta);
                        insertStmt.setString(8, via);
                        insertStmt.setString(9, civico);
                        insertStmt.setDouble(10, 0.0); // credito iniziale a 0
                        insertStmt.setString(11, "ristorante");
                        insertStmt.executeUpdate();
                    }
                } else {
                    // Se esiste già, aggiorna il ruolo
                    try (PreparedStatement updateStmt = conn.prepareStatement(
                            "UPDATE utenti SET ruolo = ? WHERE username = ?")) {
                        updateStmt.setString(1, "ristorante");
                        updateStmt.setString(2, username);
                        updateStmt.executeUpdate();
                    }
                }
            }

            // Ora puoi recuperare l’utente come RestaurantUser
            final Result<java.util.Optional<RestaurantUser>> userResult = RestaurantUser.DAO.find(conn, username);
            if (!userResult.isSuccess() || userResult.getValue().isEmpty()) {
                JOptionPane.showMessageDialog(mainFrame, "Errore nel recupero utente ristorante.", "Errore", JOptionPane.ERROR_MESSAGE);
                return;
            }
            final RestaurantUser restaurantUser = userResult.getValue().get();

            // 2. Inserisci ristorante
            final Timestamp aperturaTs = parseTime(apertura);
            final Timestamp chiusuraTs = parseTime(chiusura);
            if (aperturaTs == null || chiusuraTs == null) {
                JOptionPane.showMessageDialog(mainFrame, "Formato ora non valido. Usa HH:mm.", "Errore", JOptionPane.ERROR_MESSAGE);
                return;
            }
            final Result<Restaurant> restResult = Restaurant.DAO.insert(conn, restaurantUser, nomeAttivita, partitaIVA, aperturaTs, chiusuraTs);
            if (!restResult.isSuccess()) {
                JOptionPane.showMessageDialog(mainFrame, "Errore inserimento ristorante: " + restResult.getErrorMessage(), "Errore", JOptionPane.ERROR_MESSAGE);
                return;
            }

            JOptionPane.showMessageDialog(mainFrame, "Registrazione ristorante avvenuta con successo!", "Successo", JOptionPane.INFORMATION_MESSAGE);
            this.hide();
            parentPage.show();

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(mainFrame, "Errore durante la registrazione: " + ex.getMessage(), "Errore", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private Timestamp parseTime(final String timeStr) {
        try {
            final LocalTime lt = LocalTime.parse(timeStr);
            return Timestamp.valueOf(LocalDate.now().atTime(lt));
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    public void show() {
        this.mainFrame.setVisible(true);
    }

    public void hide() {
        this.mainFrame.setVisible(false);
    }
}
