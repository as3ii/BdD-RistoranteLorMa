package it.ristorantelorma.view.customer;

import java.awt.Component;
import java.awt.FlowLayout;
import java.sql.Connection;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Optional;

import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
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
public final class ReviewDialog {

    private static final String ERROR_WINDOW_TITLE = "Errore";
    private static final int WINDOW_WIDTH = 400;
    private static final int WINDOW_HEIGHT = 300;

    private final JDialog dialog;

    /**
     * @param parent
     * @param restaurantName
     * @param username
     */
    public ReviewDialog(final JFrame parent, final String restaurantName, final String username) {
        dialog = new JDialog(parent, "Recensione ristorante", true);
        dialog.setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        dialog.setLocationRelativeTo(parent);

        final JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        final JLabel nameLabel = new JLabel("Ristorante: " + restaurantName);
        panel.add(nameLabel);

        final JPanel votePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        votePanel.add(new JLabel("Voto:"));
        final JComboBox<Vote> voteComboBox = new JComboBox<>(Vote.values());
        voteComboBox.setRenderer(
            new DefaultListCellRenderer() {
                @Override
                public Component getListCellRendererComponent(final JList<?> list, final Object value,
                        final int index, final boolean isSelected, final boolean cellHasFocus) {
                    final JLabel label = (JLabel) super.getListCellRendererComponent(
                        list, value, index, isSelected, cellHasFocus
                    );
                    if (value != null) {
                        label.setText(String.valueOf(((Vote) value).getValue()));
                    }
                    return label;
                }
}
        );
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

            final Connection conn = DatabaseConnectionManager.getInstance().getConnection();
            final Result<Optional<ClientUser>> userResult = ClientUser.DAO.find(conn, username);
            if (!userResult.isSuccess()) {
                JOptionPane.showMessageDialog(
                    dialog,
                    "Errore nella ricerca del cliente: " + userResult.getErrorMessage(),
                    ERROR_WINDOW_TITLE,
                    JOptionPane.ERROR_MESSAGE
                );
                return;
            } else if (userResult.getValue().isEmpty()) {
                JOptionPane.showMessageDialog(
                    dialog,
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
                    dialog,
                    "Errore nella ricerca del ristorante: " + restaurantResult.getErrorMessage(),
                    ERROR_WINDOW_TITLE,
                    JOptionPane.ERROR_MESSAGE
                );
                return;
            } else if (restaurantResult.getValue().isEmpty()) {
                JOptionPane.showMessageDialog(
                    dialog,
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
                    dialog,
                    "Errore nel inserimento della recensione: " + reviewResult.getErrorMessage(),
                    ERROR_WINDOW_TITLE,
                    JOptionPane.ERROR_MESSAGE
                );
                return;
            }
            JOptionPane.showMessageDialog(dialog, "Recensione salvata!");
            dialog.dispose();
        });

        dialog.setContentPane(panel);
    }

    /**
     * Shows or hides the Dialog depending on the value of parameter b.
     * @see JDialog#setVisible(boolean)
     * @param b if true makes the dialog visible, otherwise hides the dialog
     */
    public void setVisible(final boolean b) {
        dialog.setVisible(b);
    }
}
