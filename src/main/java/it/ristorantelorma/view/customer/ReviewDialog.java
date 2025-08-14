package it.ristorantelorma.view.customer;

import it.ristorantelorma.model.DatabaseConnectionManager;
import it.ristorantelorma.model.Restaurant;
import it.ristorantelorma.model.Review;
import it.ristorantelorma.model.Vote;
import it.ristorantelorma.model.user.ClientUser;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.Timestamp;
import java.util.Optional;

public class ReviewDialog extends JDialog {
    public ReviewDialog(JFrame parent, String restaurantName, String username) {
        super(parent, "Recensione ristorante", true);
        setSize(400, 300);
        setLocationRelativeTo(parent);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JLabel nameLabel = new JLabel("Ristorante: " + restaurantName);
        panel.add(nameLabel);

        JPanel votePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        votePanel.add(new JLabel("Voto:"));
        JComboBox<Vote> voteComboBox = new JComboBox<>(Vote.values());
        votePanel.add(voteComboBox);
        panel.add(votePanel);

        panel.add(new JLabel("Recensione:"));
        JTextArea reviewArea = new JTextArea(5, 30);
        JScrollPane scrollPane = new JScrollPane(reviewArea);
        panel.add(scrollPane);

        JButton saveButton = new JButton("Salva recensione");
        panel.add(saveButton);

        saveButton.addActionListener(e -> {
            Vote voto = (Vote) voteComboBox.getSelectedItem();
            String commento = reviewArea.getText();

            try (Connection conn = DatabaseConnectionManager.getInstance().getConnection()) {
                // Recupera ClientUser e Restaurant
                var userResult = ClientUser.DAO.find(conn, username);
                var restaurantResult = Restaurant.DAO.find(conn, restaurantName);

                if (!userResult.isSuccess() || userResult.getValue() == null || userResult.getValue().isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Utente non trovato o errore DB: " + userResult.getErrorMessage());
                    return;
                }
                if (!restaurantResult.isSuccess() || restaurantResult.getValue() == null || restaurantResult.getValue().isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Ristorante non trovato o errore DB: " + restaurantResult.getErrorMessage());
                    return;
                }

                ClientUser user = userResult.getValue().get();
                Restaurant restaurant = restaurantResult.getValue().get();

                Review.DAO.insert(
                    conn,
                    restaurant,
                    new Timestamp(System.currentTimeMillis()),
                    voto, // enum direttamente
                    commento.isEmpty() ? Optional.empty() : Optional.of(commento),
                    user
                );
                JOptionPane.showMessageDialog(this, "Recensione salvata!");
                dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Errore nel salvataggio: " + ex.getMessage());
            }
        });

        setContentPane(panel);
    }
}
