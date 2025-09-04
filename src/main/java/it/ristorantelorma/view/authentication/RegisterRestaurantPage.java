package it.ristorantelorma.view.authentication;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.Optional;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import it.ristorantelorma.controller.PasswordManager;
import it.ristorantelorma.model.DatabaseConnectionManager;
import it.ristorantelorma.model.Restaurant;
import it.ristorantelorma.model.Result;
import it.ristorantelorma.model.user.RestaurantUser;
import it.ristorantelorma.model.user.Role;
import it.ristorantelorma.model.user.User;
import it.ristorantelorma.view.FirstPage;
import it.ristorantelorma.view.ViewUtils.Form;

/**
 * Represent the registration of a restaurant.
 */
public final class RegisterRestaurantPage {

    private static final String ERROR_WINDOW_TITLE = "Errore";
    private static final int WINDOW_WIDTH = 500;
    private static final int WINDOW_HEIGHT = 700;

    private final JFrame mainFrame;
    private final FirstPage parentPage;
    private final RegisterPage registerPage;

    // User fields
    private final JTextField usernameField = new JTextField(15);
    private final JTextField nameField = new JTextField(15);
    private final JTextField surnameField = new JTextField(15);
    private final JTextField passwordField = new JTextField(15);
    private final JTextField streetField = new JTextField(15);
    private final JTextField houseNumberField = new JTextField(15);
    private final JTextField cityField = new JTextField(15);
    private final JTextField phoneField = new JTextField(15);
    private final JTextField emailField = new JTextField(15);

    // Restaurant fields
    private final JTextField restaurantNameField = new JTextField(15);
    private final JTextField partitaIVAField = new JTextField(15);
    private final JTextField openingField = new JTextField(15); // format: HH:mm
    private final JTextField closingField = new JTextField(15); // format: HH:mm

    /**
     * Original constructor, for compatibility.
     * @param parentPage
     * @param registerPage
     */
    public RegisterRestaurantPage(final FirstPage parentPage, final RegisterPage registerPage) {
        this(parentPage, registerPage, "", "", "", "", "", "", "", "", "");
    }

    /**
     * @param parentPage
     * @param registerPage
     * @param username
     * @param nome
     * @param cognome
     * @param password
     * @param via
     * @param civico
     * @param citta
     * @param telefono
     * @param email
     */
    @SuppressFBWarnings(
        value = "EI_EXPOSE_REP",
        justification = "parentPage and registerPage can freely be modified by others without issues"
    )
    public RegisterRestaurantPage(
        final FirstPage parentPage, final RegisterPage registerPage,
        final String username, final String nome, final String cognome,
        final String password, final String via, final String civico,
        final String citta, final String telefono, final String email
    ) {
        this.parentPage = parentPage;
        this.registerPage = registerPage;
        this.mainFrame = new JFrame("Registrazione Ristorante");

        // Set the values and make the fields unmutable
        usernameField.setText(username);
        usernameField.setEditable(false);
        nameField.setText(nome);
        nameField.setEditable(false);
        surnameField.setText(cognome);
        surnameField.setEditable(false);
        passwordField.setText(password);
        passwordField.setEditable(false);
        streetField.setText(via);
        streetField.setEditable(false);
        houseNumberField.setText(civico);
        houseNumberField.setEditable(false);
        cityField.setText(citta);
        cityField.setEditable(false);
        phoneField.setText(telefono);
        phoneField.setEditable(false);
        emailField.setText(email);
        emailField.setEditable(false);

        initializeUI();
    }

    private void initializeUI() {
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        mainFrame.setLocationRelativeTo(null);
        mainFrame.setResizable(false);

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

        // Campi specifici ristorante
        form.addField("Nome Attività:", restaurantNameField);
        form.addField("Partita IVA:", partitaIVAField);
        form.addField("Ora Apertura (HH:mm):", openingField);
        form.addField("Ora Chiusura (HH:mm):", closingField);

        // Bottoni
        final JPanel buttonPanel = new JPanel();
        final JButton registerButton = new JButton("Registra Ristorante");
        final JButton backButton = new JButton("Back");
        buttonPanel.add(registerButton);
        buttonPanel.add(backButton);

        form.addCenterComponent(buttonPanel);

        mainFrame.add(form);

        registerButton.addActionListener(e -> handleRegisterRestaurant());
        backButton.addActionListener(e -> {
            this.hide();
            registerPage.show();
        });
    }

    private void handleRegisterRestaurant() {
        final String username = usernameField.getText().trim();
        final String name = nameField.getText().trim();
        final String surname = surnameField.getText().trim();
        final String password = passwordField.getText().trim();
        final String street = streetField.getText().trim();
        final String houseNumber = houseNumberField.getText().trim();
        final String city = cityField.getText().trim();
        final String phone = phoneField.getText().trim();
        final String email = emailField.getText().trim();

        final String restaurantName = restaurantNameField.getText().trim();
        final String partitaIVA = partitaIVAField.getText().trim();
        final String openingTime = openingField.getText().trim();
        final String closingTime = closingField.getText().trim();

        if (
            username.isEmpty() || name.isEmpty() || surname.isEmpty() || password.isEmpty()
            || street.isEmpty() || houseNumber.isEmpty() || city.isEmpty() || phone.isEmpty() || email.isEmpty()
            || restaurantName.isEmpty() || partitaIVA.isEmpty() || openingTime.isEmpty() || closingTime.isEmpty()) {
            JOptionPane.showMessageDialog(
                mainFrame,
                "Compila tutti i campi!",
                ERROR_WINDOW_TITLE,
                JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        final Connection conn = DatabaseConnectionManager.getInstance().getConnection();
        final Result<Optional<User>> resUser = User.DAO.find(conn, username);
        if (!resUser.isSuccess()) {
            JOptionPane.showMessageDialog(
                mainFrame,
                "Errore nella ricerca del utente.\n" + resUser.getErrorMessage(),
                ERROR_WINDOW_TITLE,
                JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        final RestaurantUser restaurantUser;
        if (resUser.getValue().isEmpty()) {
            // If the user do not exists
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
            final Result<User> resInsert = User.DAO.insert(
                conn, name, surname, username, hashedPassword, phone,
                email, city, street, houseNumber, Role.RESTAURANT
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
            restaurantUser = (RestaurantUser) resInsert.getValue();
        } else {
            // If the user already exists
            final User tmpUser = resUser.getValue().get();
            if (!(tmpUser instanceof RestaurantUser)) {
                try (PreparedStatement updateStmt = conn.prepareStatement(
                        "UPDATE utenti SET ruolo = ? WHERE username = ?")) {
                    updateStmt.setString(1, "ristorante");
                    updateStmt.setString(2, username);
                    updateStmt.executeUpdate();
                } catch (SQLException e) {
                    JOptionPane.showMessageDialog(
                        mainFrame,
                        "Errore durante la registrazione: " + e.getMessage(),
                        ERROR_WINDOW_TITLE,
                        JOptionPane.ERROR_MESSAGE
                    );
                    return;
                }
            }
            restaurantUser = (RestaurantUser) tmpUser;
        }

        final Timestamp openingTs = parseTime(openingTime);
        final Timestamp closingTs = parseTime(closingTime);
        if (openingTs == null || closingTs == null) {
            JOptionPane.showMessageDialog(
                mainFrame,
                "Formato ora non valido. Usa HH:mm.",
                ERROR_WINDOW_TITLE,
                JOptionPane.ERROR_MESSAGE
            );
            return;
        }
        final Result<Restaurant> restResult = Restaurant.DAO.insert(
            conn, restaurantUser, restaurantName, partitaIVA, openingTs, closingTs
        );
        if (!restResult.isSuccess()) {
            JOptionPane.showMessageDialog(
                mainFrame,
                "Errore inserimento ristorante: " + restResult.getErrorMessage(),
                ERROR_WINDOW_TITLE,
                JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        JOptionPane.showMessageDialog(
            mainFrame,
            "Registrazione ristorante avvenuta con successo!",
            "Successo",
            JOptionPane.INFORMATION_MESSAGE
        );
        this.hide();
        parentPage.show();
    }

    private Timestamp parseTime(final String timeStr) {
        try {
            final LocalTime lt = LocalTime.parse(timeStr);
            return Timestamp.valueOf(LocalDate.now().atTime(lt));
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    /**
     * Show the window.
     */
    public void show() {
        this.mainFrame.setVisible(true);
    }

    /**
     * Hide the window.
     */
    public void hide() {
        this.mainFrame.setVisible(false);
    }
}
