package it.ristorantelorma.view.customer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.SpinnerNumberModel;

import it.ristorantelorma.model.DatabaseConnectionManager;
import it.ristorantelorma.model.Food;
import it.ristorantelorma.model.Restaurant;
import it.ristorantelorma.model.Result;
import it.ristorantelorma.model.order.Order;
import it.ristorantelorma.model.order.ReadyOrder;
import it.ristorantelorma.model.order.WaitingOrder;
import it.ristorantelorma.model.user.ClientUser;

/**
 * Food selection menu for an order.
 */
public final class ResMenu {

    private static final String ERROR_WINDOW_TITLE = "Errore";
    private static final int WINDOW_WIDTH = 900;
    private static final int WINDOW_HEIGHT = 600;
    private static final Dimension INFO_PANEL_DIMENSION = new Dimension(300, 400);
    private static final Dimension MENU_PANEL_DIMENSION = new Dimension(500, 400);
    private static final int MAX_QUANTITY = 99;

    private BigDecimal balance;
    private final JFrame frame;

    /**
     * @param restaurantName
     * @param restaurantsPage
     * @param username
     */
    public ResMenu(final String restaurantName, final RestaurantsPage restaurantsPage, final String username) {
        frame = new JFrame("DeliveryDB");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        frame.setLocationRelativeTo(null);

        final Connection conn = DatabaseConnectionManager.getInstance().getConnection();
        final Result<Optional<Restaurant>> resRestaurant = Restaurant.DAO.find(conn, restaurantName);
        if (!resRestaurant.isSuccess()) {
            JOptionPane.showMessageDialog(
                frame,
                "Errore nella ricerca del ristorante.\n" + resRestaurant.getErrorMessage(),
                ERROR_WINDOW_TITLE,
                JOptionPane.ERROR_MESSAGE
            );
            return;
        } else if (resRestaurant.getValue().isEmpty()) {
            JOptionPane.showMessageDialog(
                frame,
                "Il ristorante " + restaurantName + " non esiste.",
                ERROR_WINDOW_TITLE,
                JOptionPane.ERROR_MESSAGE
            );
            return;
        }
        final Restaurant restaurant = resRestaurant.getValue().get();

        final Result<Collection<Food>> resFoods = Food.DAO.list(conn, restaurant);
        if (!resFoods.isSuccess()) {
            JOptionPane.showMessageDialog(
                frame,
                "Errore nella raccolta della lista vivande.\n" + resFoods.getErrorMessage(),
                ERROR_WINDOW_TITLE,
                JOptionPane.ERROR_MESSAGE
            );
            return;
        }
        final List<Food> menuData = new ArrayList<>(resFoods.getValue());

        final Result<Optional<ClientUser>> resOptClient = ClientUser.DAO.find(conn, username);
        if (!resOptClient.isSuccess()) {
            JOptionPane.showMessageDialog(
                frame,
                "Errore nella ricerca del cliente.\n" + resOptClient.getErrorMessage(),
                ERROR_WINDOW_TITLE,
                JOptionPane.ERROR_MESSAGE
            );
            return;
        } else if (resOptClient.getValue().isEmpty()) {
            JOptionPane.showMessageDialog(
                frame,
                "L'utente " + username + " non esiste.",
                ERROR_WINDOW_TITLE,
                JOptionPane.ERROR_MESSAGE
            );
            return;
        }
        final ClientUser client = resOptClient.getValue().get();

        balance = client.getCredit();

        final JPanel mainPanel = new JPanel(new BorderLayout());

        final JPanel menuPanel = new JPanel(new BorderLayout());
        final JPanel menuTablePanel = new JPanel();
        menuTablePanel.setLayout(new BoxLayout(menuTablePanel, BoxLayout.Y_AXIS));

        final JPanel header = new JPanel(new GridLayout(1, 4));
        header.add(new JLabel("Nome"));
        header.add(new JLabel("Nome Vivanda"));
        header.add(new JLabel("Quantità"));
        header.add(new JLabel("Prezzo"));
        menuTablePanel.add(header);

        final JSpinner[] quantitySpinners = new JSpinner[menuData.size()];
        int index = 0;
        for (final Food food : menuData) {
            final JPanel row = new JPanel(new GridLayout(1, 4));
            row.add(new JLabel(food.getName()));
            row.add(new JLabel(food.getRestaurant().getRestaurantName()));
            quantitySpinners[index] = new JSpinner(new SpinnerNumberModel(0, 0, MAX_QUANTITY, 1));
            row.add(quantitySpinners[index]);
            row.add(new JLabel(food.getPrice().toPlainString()));
            menuTablePanel.add(row);
            index++;
        }

        final JScrollPane menuScroll = new JScrollPane(menuTablePanel);
        menuScroll.setPreferredSize(MENU_PANEL_DIMENSION);
        menuPanel.add(menuScroll, BorderLayout.CENTER);

        // Pulsanti Back e Send Order
        final JPanel buttonPanel = new JPanel();
        final JButton backButton = new JButton("Back");
        final JButton sendOrderButton = new JButton("Send Order");
        sendOrderButton.setBackground(Color.RED);
        sendOrderButton.setForeground(Color.WHITE);

        // Implementazione azioni bottoni
        backButton.addActionListener(e -> {
            frame.dispose();
            restaurantsPage.setVisible(true);
        });
        sendOrderButton.addActionListener(e -> {
            final BigDecimal total;
            final Map<Food, Integer> orderedFood = new HashMap<>();
            for (int i = 0; i < quantitySpinners.length; i++) {
                final int qty = (Integer) quantitySpinners[i].getValue();
                if (qty > 0) {
                    orderedFood.put(menuData.get(i), qty);
                }
            }
            total = orderedFood
                    .entrySet()
                    .stream()
                    .map(el -> el.getKey().getPrice().multiply(new BigDecimal(el.getValue())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            if (total.compareTo(balance) > 0) {
                JOptionPane.showMessageDialog(frame, "Saldo insufficiente!");
                return;
            }
            final Result<ClientUser> resClient = ClientUser.DAO.updateCredit(conn, client, balance.subtract(total));
            if (!resClient.isSuccess()) {
                JOptionPane.showMessageDialog(
                    frame,
                    "Errore nell'aggiornamento del bilancio del cliente.\n" + resClient.getErrorMessage(),
                    ERROR_WINDOW_TITLE,
                    JOptionPane.ERROR_MESSAGE
                );
                return;
            }
            balance = balance.subtract(total);

            // Spese di spedizione gestite come valore fisso
            final BigDecimal shippingRate = new BigDecimal("2.5");
            // Sottrae il shippingRate dal credito dell'utente (tabella utenti)
            try (java.sql.PreparedStatement stmt = conn.prepareStatement(
                    "UPDATE utenti SET credito = credito - ? WHERE username = ?;"
                )) {
                stmt.setBigDecimal(1, shippingRate);
                stmt.setString(2, restaurant.getUser().getUsername());
                stmt.executeUpdate();
            } catch (java.sql.SQLException ex) {
                JOptionPane.showMessageDialog(frame, "Errore nell'aggiornamento del credito ristorante: "
                                + ex.getMessage(), ERROR_WINDOW_TITLE, JOptionPane.ERROR_MESSAGE);
                return;
            }

            final Timestamp now = Timestamp.valueOf(LocalDateTime.now());
            final Result<WaitingOrder> resNewOrder = Order.DAO.insert(
                conn, restaurant, now, shippingRate, resClient.getValue(), orderedFood
            );
            if (!resNewOrder.isSuccess()) {
                JOptionPane.showMessageDialog(
                    frame,
                    "Errore nel inserimento dell'ordine.\n" + resNewOrder.getErrorMessage(),
                    ERROR_WINDOW_TITLE,
                    JOptionPane.ERROR_MESSAGE
                );
                return;
            }
            // Conversione dell'ordine in stato pronto
            final Result<ReadyOrder> resOrder = ReadyOrder.DAO.from(conn, resNewOrder.getValue());
            if (!resOrder.isSuccess()) {
                JOptionPane.showMessageDialog(
                    frame,
                    "Errore nel aggiornamento dell'ordine.\n" + resOrder.getErrorMessage(),
                    ERROR_WINDOW_TITLE,
                    JOptionPane.ERROR_MESSAGE
                );
                return;
            }
            final ReadyOrder order = resOrder.getValue();

            final Result<Map<Food, Integer>> resFoodRequested = Order.DAO.insertFoodRequested(
                conn, order.getId(), orderedFood
            );
            if (!resFoodRequested.isSuccess()) {
                JOptionPane.showMessageDialog(
                    frame,
                    "Errore nel inserimento degli elementi dell'ordine.\n" + resFoodRequested.getErrorMessage(),
                    ERROR_WINDOW_TITLE,
                    JOptionPane.ERROR_MESSAGE
                );
                return;
            }

            JOptionPane.showMessageDialog(frame, "Ordine inviato con successo!");
            frame.dispose();
            restaurantsPage.setVisible(true);
        });

        buttonPanel.add(backButton);
        buttonPanel.add(sendOrderButton);
        menuPanel.add(buttonPanel, BorderLayout.SOUTH);

        mainPanel.add(menuPanel, BorderLayout.CENTER);

        // --- DESTRA: Info, riepilogo, saldo ---
        final JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setPreferredSize(INFO_PANEL_DIMENSION);

        final JLabel balanceLabel = new JLabel("Balance: $" + balance.toPlainString());
        final JLabel orderSummaryLabel = new JLabel("Order Summary:");
        final JTextArea orderSummaryArea = new JTextArea(6, 20);
        orderSummaryArea.setEditable(false);
        final JLabel totalLabel = new JLabel("Total: $0.0");

        final JPanel restaurantInfoPanel = new JPanel();
        restaurantInfoPanel.setLayout(new BoxLayout(restaurantInfoPanel, BoxLayout.Y_AXIS));
        restaurantInfoPanel.add(new JLabel("Restaurant Info:"));
        restaurantInfoPanel.add(new JLabel("Restaurant: " + restaurantName));
        restaurantInfoPanel.add(new JLabel("Opening Hours: 12:30 - 22:30"));
        final JButton reviewsButton = new JButton("Reviews");
        reviewsButton.setBackground(Color.BLUE);
        reviewsButton.setForeground(Color.WHITE);

        // Implementazione azione bottone Reviews
        reviewsButton.addActionListener(e -> {
            final ReviewDialog dialog = new ReviewDialog(frame, restaurantName, username);
            dialog.setVisible(true);
        });

        restaurantInfoPanel.add(reviewsButton);

        infoPanel.add(balanceLabel);
        infoPanel.add(orderSummaryLabel);
        infoPanel.add(orderSummaryArea);
        infoPanel.add(totalLabel);
        infoPanel.add(Box.createVerticalStrut(10));
        infoPanel.add(restaurantInfoPanel);

        mainPanel.add(infoPanel, BorderLayout.EAST);

        frame.setContentPane(mainPanel);

        // Dopo aver creato totalLabel:
        for (final JSpinner spinner : quantitySpinners) {
            spinner.addChangeListener(e -> {
                BigDecimal total = BigDecimal.ZERO;
                for (int j = 0; j < quantitySpinners.length; j++) {
                    final int qty = (Integer) quantitySpinners[j].getValue();
                    total = total.add(menuData.get(j).getPrice().multiply(new BigDecimal(qty)));
                }
                totalLabel.setText("Total: $" + total.toPlainString());
            });
        }
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
