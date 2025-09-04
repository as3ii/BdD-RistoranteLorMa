package it.ristorantelorma.view.restaurant;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.sql.Connection;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import it.ristorantelorma.model.DatabaseConnectionManager;
import it.ristorantelorma.model.Restaurant;
import it.ristorantelorma.model.Result;
import it.ristorantelorma.view.ViewUtils.Form;

/**
 * Restaurant management window.
 */
public final class RestaurantManagementPage {

    private static final String ERROR_WINDOW_TITLE = "Errore";
    private static final int WINDOW_WIDTH = 400;
    private static final int WINDOW_HEIGHT = 400;
    private static final Dimension FIELD_DIMENSION = new Dimension(100, 30);
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    private final JFrame frame;
    private final JTextField userField;
    private final JTextField restaurantNameField;
    private final JTextField vatIDField;
    private final JTextField openingTimeField;
    private final JTextField closingTimeField;

    /**
     * @param username of the restaurant owner.
     */
    public RestaurantManagementPage(final String username) {
        frame = new JFrame("RestaurantManagementPage");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        frame.setLocationRelativeTo(null);
        frame.setLayout(new BorderLayout());

        userField = new JTextField();
        userField.setPreferredSize(FIELD_DIMENSION);
        userField.setEnabled(false);
        restaurantNameField = new JTextField();
        restaurantNameField.setPreferredSize(FIELD_DIMENSION);
        restaurantNameField.setEnabled(false);
        vatIDField = new JTextField();
        vatIDField.setPreferredSize(FIELD_DIMENSION);
        vatIDField.setEnabled(false);
        openingTimeField = new JTextField();
        openingTimeField.setPreferredSize(FIELD_DIMENSION);
        closingTimeField = new JTextField();
        closingTimeField.setPreferredSize(FIELD_DIMENSION);

        final Connection conn = DatabaseConnectionManager.getInstance().getConnection();
        final Result<Optional<Restaurant>> result = Restaurant.DAO.findByUsername(conn, username);
        if (!result.isSuccess()) {
            JOptionPane.showMessageDialog(
                frame,
                "Errore nella ricerca del ristorante: " + result.getErrorMessage(),
                ERROR_WINDOW_TITLE,
                JOptionPane.ERROR_MESSAGE
            );
            return;
        }
        if (result.getValue().isEmpty()) {
            JOptionPane.showMessageDialog(
                frame,
                "Ristorante appartenente al utente " + username + " non trovato",
                ERROR_WINDOW_TITLE,
                JOptionPane.ERROR_MESSAGE
            );
            return;
        }
        final Restaurant restaurant = result.getValue().get();

        userField.setText(restaurant.getUser().getUsername());
        restaurantNameField.setText(restaurant.getRestaurantName());
        vatIDField.setText(restaurant.getVatNumber());
        openingTimeField.setText(restaurant.getOpeningTime().toLocalDateTime().toLocalTime().format(TIME_FORMATTER));
        closingTimeField.setText(restaurant.getClosingTime().toLocalDateTime().toLocalTime().format(TIME_FORMATTER));

        final Form form = new Form();

        form.addField("Utente:", userField);
        form.addField("Nome ristorante:", restaurantNameField);
        form.addField("P.IVA:", vatIDField);
        form.addField("Orario apertura:", openingTimeField);
        form.addField("Orario chiusura:", closingTimeField);

        final JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
        buttonPanel.add(Box.createHorizontalGlue());

        final JButton updateButton = new JButton("Update");
        updateButton.addActionListener(e -> {
            final Timestamp openingTime = Timestamp.valueOf(
                LocalDateTime.parse(openingTimeField.getText(), TIME_FORMATTER)
            );
            final Timestamp closingTime = Timestamp.valueOf(
                LocalDateTime.parse(closingTimeField.getText(), TIME_FORMATTER)
            );
            final Result<Restaurant> resRestaurant = Restaurant.DAO.updateTime(conn, restaurant, openingTime, closingTime);
            if (!resRestaurant.isSuccess()) {
                JOptionPane.showMessageDialog(
                    frame,
                    "Errore nell'aggiornamento del ristorante: " + resRestaurant.getErrorMessage(),
                    ERROR_WINDOW_TITLE,
                    JOptionPane.ERROR_MESSAGE
                );
                return;
            }
            JOptionPane.showMessageDialog(
                frame,
                "Orari ristorante aggiornati con successo",
                "Successo",
                JOptionPane.INFORMATION_MESSAGE
            );
        });
        buttonPanel.add(updateButton);
        buttonPanel.add(Box.createHorizontalGlue());

        final JButton menuButton = new JButton("Change Menu");
        menuButton.addActionListener(e -> {
            new MenuEditPage(restaurant).setVisible(true);
        });
        buttonPanel.add(menuButton);
        buttonPanel.add(Box.createHorizontalGlue());

        form.addCenterComponent(buttonPanel);

        frame.add(form, BorderLayout.CENTER);
    }

    /**
     * Shows or hides the Window depending on the value of parameter b.
     * @see JFrame#setVisible(boolean)
     * @param b if true makes the window visible, otherwise hides the window
     */
    public void setVisible(final boolean b) {
        frame.setVisible(b);
    }
}
