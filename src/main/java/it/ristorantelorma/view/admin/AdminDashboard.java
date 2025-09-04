package it.ristorantelorma.view.admin;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.sql.Connection;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import it.ristorantelorma.model.DatabaseConnectionManager;
import it.ristorantelorma.model.Food;
import it.ristorantelorma.model.FoodType;
import it.ristorantelorma.model.Restaurant;
import it.ristorantelorma.model.Result;
import it.ristorantelorma.model.Review;
import it.ristorantelorma.model.user.DeliverymanUser;

/**
 * Administration interface.
 */
public final class AdminDashboard {

    private static final String ERROR_WINDOW_TITLE = "Errore";
    private static final String MOST_POPULAR_CUISINE_TYPE_LABEL = "Most popular cuisine type";
    private static final String WORST_RESTAURANT_LABEL = "Worst restaurant";
    private static final String TOP_DISH_LABEL = "Top dish";
    private static final String BEST_RESTAURANT_LABEL = "Best restaurant";
    private static final String BEST_DELIVERER_LABEL = "Best deliverer";
    private static final int DASHBOARD_WIDTH = 800;
    private static final int DASHBOARD_HEIGHT = 600;
    private static final int REVIEWS_WIDTH = 600;
    private static final int REVIEWS_HEIGHT = 400;
    private static final int COMBO_WIDTH = 200;
    private static final int COMBO_HEIGHT = 30;

    private final JFrame frame;
    private final JComboBox<Restaurant> restaurantComboBox;

    /**
     * Constructor for the admin dashboard.
     */
    public AdminDashboard() {
        frame = new JFrame("DeliveryDB");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(DASHBOARD_WIDTH, DASHBOARD_HEIGHT);
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

        // Load restaurants from DB
        final Connection conn = DatabaseConnectionManager.getInstance().getConnection();
        final Result<Collection<Restaurant>> resRestaurant = Restaurant.DAO.list(conn);
        if (!resRestaurant.isSuccess()) {
            JOptionPane.showMessageDialog(
                frame,
                "Errore nella raccolta della lista ristoranti.\n" + resRestaurant.getErrorMessage(),
                ERROR_WINDOW_TITLE,
                JOptionPane.ERROR_MESSAGE
            );
            restaurantComboBox = null;
            return;
        }
        final Collection<Restaurant> restaurants = resRestaurant.getValue();

        restaurantComboBox = new JComboBox<>(restaurants.toArray(Restaurant[]::new));
        restaurantComboBox.setRenderer(
            new DefaultListCellRenderer() {
                @Override
                public Component getListCellRendererComponent(final JList<?> list, final Object value,
                        final int index, final boolean isSelected, final boolean cellHasFocus) {
                    final JLabel label = (JLabel) super.getListCellRendererComponent(
                        list, value, index, isSelected, cellHasFocus
                    );
                    if (value != null) {
                        label.setText(((Restaurant) value).getRestaurantName());
                    }
                    return label;
                }
            }
        );
        restaurantComboBox.setMaximumSize(new Dimension(COMBO_WIDTH, COMBO_HEIGHT));
        centerPanel.add(restaurantComboBox);
        centerPanel.add(Box.createVerticalGlue());

        frame.add(centerPanel, BorderLayout.CENTER);

        // Bottom panel with buttons
        final JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        final JButton topDishButton = new JButton(TOP_DISH_LABEL);
        topDishButton.addActionListener(e -> {
            final Result<Entry<Food, Integer>> result = Food.DAO.getMostPurchased(conn);
            if (!result.isSuccess()) {
                JOptionPane.showMessageDialog(
                    frame,
                    "Errore nella ricerca della vivanda più acquistata: " + result.getErrorMessage(),
                    ERROR_WINDOW_TITLE,
                    JOptionPane.ERROR_MESSAGE
                );
                return;
            }
            final Food food = result.getValue().getKey();
            final int count = result.getValue().getValue();
            JOptionPane.showMessageDialog(
                frame,
                "Vivanda più acquistata: " + food.getName()
                    + "\nRistorante: " + food.getRestaurant().getRestaurantName()
                    + "\nQuantità totale: " + count,
                TOP_DISH_LABEL,
                JOptionPane.INFORMATION_MESSAGE
            );
        });
        bottomPanel.add(topDishButton);

        final JButton mostPopularCuisineButton = new JButton(MOST_POPULAR_CUISINE_TYPE_LABEL);
        mostPopularCuisineButton.addActionListener(e -> {
            final Result<Entry<FoodType, Integer>> result = FoodType.DAO.getMostPurchased(conn);
            if (!result.isSuccess()) {
                JOptionPane.showMessageDialog(
                    frame,
                    "Errore nella ricerca della tipologia di cucina più acquistata: " + result.getErrorMessage(),
                    ERROR_WINDOW_TITLE,
                    JOptionPane.ERROR_MESSAGE
                );
                return;
            }
            final FoodType foodType = result.getValue().getKey();
            final String info = "Tipologia di cucina più acquistata: " + foodType.getName()
                + "\nTotale piatti acquistati: " + result.getValue().getValue();
            JOptionPane.showMessageDialog(
                frame,
                info,
                MOST_POPULAR_CUISINE_TYPE_LABEL,
                JOptionPane.INFORMATION_MESSAGE
            );
        });
        bottomPanel.add(mostPopularCuisineButton);

        final JButton worstRestaurantsButton = new JButton(WORST_RESTAURANT_LABEL);
        worstRestaurantsButton.addActionListener(e -> {
            final Result<Entry<Restaurant, Float>> result = Restaurant.DAO.getTopByNegativeReviews(conn);
            if (!result.isSuccess()) {
                JOptionPane.showMessageDialog(
                    frame,
                    "Errore nella ricerca del ristorante con le peggiori recensioni: " + result.getErrorMessage(),
                    ERROR_WINDOW_TITLE,
                    JOptionPane.ERROR_MESSAGE
                );
                return;
            }
            final String info = "Ristorante con più recensioni negative:"
                + "\nNome: " + result.getValue().getKey().getRestaurantName()
                + "\nMedia voti: " + result.getValue().getValue();
            JOptionPane.showMessageDialog(
                frame,
                info,
                WORST_RESTAURANT_LABEL,
                JOptionPane.INFORMATION_MESSAGE
            );
        });
        bottomPanel.add(worstRestaurantsButton);

        // Button Best restaurant
        final JButton bestRestaurantButton = new JButton(BEST_RESTAURANT_LABEL);
        bestRestaurantButton.addActionListener(e -> {
            final Result<Entry<Restaurant, Integer>> result = Restaurant.DAO.getTopByOrderCount(conn);
            if (!result.isSuccess()) {
                JOptionPane.showMessageDialog(
                    frame,
                    "Errore nella ricerca del ristorante con più ordini: " + result.getErrorMessage(),
                    ERROR_WINDOW_TITLE,
                    JOptionPane.ERROR_MESSAGE
                );
                return;
            }
            final String info = "Ristorante con più ordini:\n"
                    + "Nome: " + result.getValue().getKey().getRestaurantName() + "\n"
                    + "Numero ordini: " + result.getValue().getValue();
            JOptionPane.showMessageDialog(
                frame,
                info,
                BEST_RESTAURANT_LABEL,
                JOptionPane.INFORMATION_MESSAGE
            );
        });
        bottomPanel.add(bestRestaurantButton);

        // Button Best deliverer
        final JButton bestDelivererButton = new JButton(BEST_DELIVERER_LABEL);
        bestDelivererButton.addActionListener(e -> {
            final Result<Entry<DeliverymanUser, Integer>> result = DeliverymanUser.DAO.getTopByDeliveryCount(conn);
            if (!result.isSuccess()) {
                JOptionPane.showMessageDialog(
                    frame,
                    "Errore nella ricerca del fattorino con più ordini consegnati: " + result.getErrorMessage(),
                    ERROR_WINDOW_TITLE,
                    JOptionPane.ERROR_MESSAGE
                );
                return;
            }
            final DeliverymanUser deliveryman = result.getValue().getKey();
            final String info = "Miglior fattorino:\n"
                + "Username: " + deliveryman.getUsername() + "\n"
                + "Nome: " + deliveryman.getName() + "\n"
                + "Cognome: " + deliveryman.getSurname() + "\n"
                + "Numero ordini consegnati: " + result.getValue().getValue();
            JOptionPane.showMessageDialog(
                frame,
                info,
                BEST_DELIVERER_LABEL,
                JOptionPane.INFORMATION_MESSAGE
            );
        });
        bottomPanel.add(bestDelivererButton);
        frame.add(bottomPanel, BorderLayout.SOUTH);

        // Listener for the button View Reviews
        viewReviewsButton.addActionListener(e -> {
            final Restaurant selectedRestaurant = (Restaurant) restaurantComboBox.getSelectedItem();
            if (selectedRestaurant != null) {
                new ReviewsWindow(selectedRestaurant).setVisible(true);
            }
        });
    }

    /**
     * Finestra che mostra le recensioni di un ristorante.
     */
    private static class ReviewsWindow {

        private final JFrame frame;
        private final JTable table;
        private final DefaultTableModel tableModel;
        private final Map<Integer, Review> reviews;

        ReviewsWindow(final Restaurant restaurant) {
            frame = new JFrame("Recensioni di " + restaurant.getRestaurantName());
            frame.setSize(REVIEWS_WIDTH, REVIEWS_HEIGHT);
            frame.setLocationRelativeTo(null);
            frame.setLayout(new BorderLayout());

            // Columns: codice, utente, data, voto, commento
            final String[] columns = {"Codice", "Utente", "Data", "Voto", "Commento"};
            tableModel = new DefaultTableModel(columns, 0) {
                @Override
                public boolean isCellEditable(final int row, final int column) {
                    return false;
                }
            };
            table = new JTable(tableModel);
            table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            frame.add(new JScrollPane(table), BorderLayout.CENTER);

            // Button to delete a review
            final JButton deleteButton = new JButton("Elimina recensione");
            deleteButton.addActionListener(e -> deleteSelectedReview());
            final JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            buttonPanel.add(deleteButton);
            frame.add(buttonPanel, BorderLayout.SOUTH);

            // Load reviews from DB
            final Connection conn = DatabaseConnectionManager.getInstance().getConnection();
            final Result<Collection<Review>> resReviews = Review.DAO.list(conn, restaurant);
            if (!resReviews.isSuccess()) {
                JOptionPane.showMessageDialog(
                    frame,
                    "Errore nella raccolta della lista ristoranti.\n" + resReviews.getErrorMessage(),
                    ERROR_WINDOW_TITLE,
                    JOptionPane.ERROR_MESSAGE
                );
            }
            reviews = resReviews.getValue().stream().collect(Collectors.toMap(Review::getId, e -> e));
            for (final Review r : reviews.values().stream().sorted(Comparator.comparing(Review::getId)).toList()) {
                final Object[] row = {
                    r.getId(),
                    r.getUser().getUsername(),
                    r.getDate().toLocalDateTime().format(
                        DateTimeFormatter.ofPattern("dd/mm/yyyy HH:mm")
                    ),
                    r.getVote().getValue(),
                    r.getComment().orElse("")
                };
                tableModel.addRow(row);
            }
        }

        private void deleteSelectedReview() {
            final int selectedRow = table.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(
                    frame,
                    "Seleziona una recensione da eliminare."
                );
                return;
            }
            final int id = (int) tableModel.getValueAt(selectedRow, 0);
            final int confirm = JOptionPane.showConfirmDialog(
                frame,
                "Vuoi eliminare la recensione selezionata?",
                "Conferma eliminazione",
                JOptionPane.YES_NO_OPTION
            );
            if (confirm != JOptionPane.YES_OPTION) {
                return;
            }
            final Connection conn = DatabaseConnectionManager.getInstance().getConnection();
            final Result<?> resDel = Review.DAO.delete(conn, reviews.get(id));
            if (!resDel.isSuccess()) {
                JOptionPane.showMessageDialog(
                    frame,
                   "Errore: recensione non trovata."
                );
            } else {
                JOptionPane.showMessageDialog(frame, "Recensione eliminata.");
                reviews.remove(id);
                table.removeRowSelectionInterval(selectedRow, selectedRow);
            }
        }

        public void setVisible(final boolean v) {
            frame.setVisible(v);
        }
    }

    /**
     * Set visibility of the admin interface.
     * @param v
     */
    public void setVisible(final boolean v) {
        frame.setVisible(v);
    }
}
