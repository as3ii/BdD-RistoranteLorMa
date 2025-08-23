package it.ristorantelorma.view.customer;

import java.awt.FlowLayout;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Optional;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import it.ristorantelorma.model.DatabaseConnectionManager;
import it.ristorantelorma.model.Restaurant;
import it.ristorantelorma.model.Result;
import it.ristorantelorma.model.Review;
import it.ristorantelorma.model.Vote;
import it.ristorantelorma.model.user.ClientUser;

/**
 * Review dialog window.
 */
public final class ReviewDialog extends JDialog {

    public static final long serialVersionUID = 811249015L; // Random
    private static final String ERROR_WINDOW_TITLE = "Errore";
    private static final int WINDOW_WIDTH = 400;
    private static final int WINDOW_HEIGHT = 300;

    /**
     * @param parent
     * @param restaurantName
     * @param username
     */
    public ReviewDialog(final JFrame parent, final String restaurantName, final String username) {
        super(parent, "Recensione ristorante", true);
        setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        setLocationRelativeTo(parent);

        final JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        final JLabel nameLabel = new JLabel("Ristorante: " + restaurantName);
        panel.add(nameLabel);

        final JPanel votePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        votePanel.add(new JLabel("Voto:"));
        final JComboBox<Vote> voteComboBox = new JComboBox<>(Vote.values());
        votePanel.add(voteComboBox);
        panel.add(votePanel);

        panel.add(new JLabel("Recensione:"));
        final JTextArea reviewArea = new JTextArea(5, 30);
        final JScrollPane scrollPane = new JScrollPane(reviewArea);
        panel.add(scrollPane);

        final JButton saveButton = new JButton("Salva recensione");
        panel.add(saveButton);

        saveButton.addActionListener(e -> {
            final Vote vote = (Vote) voteComboBox.getSelectedItem();
            final String comment = reviewArea.getText();

            try (Connection conn = DatabaseConnectionManager.getInstance().getConnection()) {
                final Result<Optional<ClientUser>> userResult = ClientUser.DAO.find(conn, username);
                if (!userResult.isSuccess()) {
                    JOptionPane.showMessageDialog(
                        this,
                        "Errore nella ricerca del cliente: " + userResult.getErrorMessage(),
                        ERROR_WINDOW_TITLE,
                        JOptionPane.ERROR_MESSAGE
                    );
                    return;
                } else if (userResult.getValue().isEmpty()) {
                    JOptionPane.showMessageDialog(
                        this,
                        "Il cliente " + username + " non esiste.",
                        ERROR_WINDOW_TITLE,
                        JOptionPane.ERROR_MESSAGE
                    );
                    return;
                }
                final ClientUser user = userResult.getValue().get();

                final Result<Optional<Restaurant>> restaurantResult = Restaurant.DAO.find(conn, restaurantName);
                if (!restaurantResult.isSuccess()) {
                    JOptionPane.showMessageDialog(
                       this,
                       "Errore nella ricerca del ristorante: " + restaurantResult.getErrorMessage(),
                       ERROR_WINDOW_TITLE,
                       JOptionPane.ERROR_MESSAGE
                    );
                    return;
                } else if (restaurantResult.getValue().isEmpty()) {
                    JOptionPane.showMessageDialog(
                       this,
                       "Il ristorante " + restaurantName + " non esiste.",
                       ERROR_WINDOW_TITLE,
                       JOptionPane.ERROR_MESSAGE
                    );
                    return;
                }
                final Restaurant restaurant = restaurantResult.getValue().get();

                final Result<Review> reviewResult = Review.DAO.insert(
                    conn,
                    restaurant,

                    Timestamp.valueOf(LocalDateTime.now()),
                    vote,
                    comment.isEmpty() ? Optional.empty() : Optional.of(comment),
                    user
                );
                if (!reviewResult.isSuccess()) {
                    JOptionPane.showMessageDialog(
                       this,
                       "Errore nel inserimento della recensione: " + reviewResult.getErrorMessage(),
                       ERROR_WINDOW_TITLE,
                       JOptionPane.ERROR_MESSAGE
                    );
                    return;
                }
                JOptionPane.showMessageDialog(this, "Recensione salvata!");
                dispose();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(
                    this,
                    "Errore nel salvataggio: " + ex.getMessage(),
                    ERROR_WINDOW_TITLE,
                    JOptionPane.ERROR_MESSAGE
                );
            }
        });

        setContentPane(panel);
    }
}
