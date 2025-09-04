package it.ristorantelorma.view.deliveryman;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;

import it.ristorantelorma.model.Result;
import it.ristorantelorma.model.order.AcceptedOrder;
import it.ristorantelorma.model.order.DeliveredOrder;
import it.ristorantelorma.model.order.ReadyOrder;
import it.ristorantelorma.model.user.DeliverymanUser;

/**
 * Deliveryman-specific page to handle order acceptance and delivery.
 */
public final class DeliverymanPage {

    private static final String ERROR_WINDOW_TITLE = "Errore";
    private static final int MAIN_WINDOW_WIDTH = 400;
    private static final int MAIN_WINDOW_HEIGHT = 400;
    private static final int DELIVERY_WINDOW_WIDTH = 500;
    private static final int DELIVERY_WINDOW_HEIGHT = 300;
    private static final int FONT_SIZE = 16;
    private static final int TABLE_ROW_HEIGHT = 28;
    private static final String HTML_NEWLINE = "<br>";
    private static final Dimension BUTTON_DIMENSION = new Dimension(160, 30);
    private static final int EMPTY_GAP_HEIGHT = 10;

    private final JButton showOrdersButton;
    private final JButton viewAcceptedButton;
    private final JFrame frame;

    /**
     * @param conn
     * @param username
     */
    public DeliverymanPage(final Connection conn, final String username) {
        frame = new JFrame("DeliveryDB");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(MAIN_WINDOW_WIDTH, MAIN_WINDOW_HEIGHT);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);

        frame.setLayout(new BorderLayout());

        final JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setBackground(Color.WHITE);

        showOrdersButton = createButton("Show available orders");
        viewAcceptedButton = createButton("View Accepted");

        centerPanel.add(Box.createVerticalStrut(EMPTY_GAP_HEIGHT * 2));
        centerPanel.add(showOrdersButton);
        centerPanel.add(Box.createVerticalStrut(EMPTY_GAP_HEIGHT));
        centerPanel.add(viewAcceptedButton);

        frame.add(centerPanel, BorderLayout.CENTER);

        showOrdersButton.addActionListener(e -> {
            final Collection<ReadyOrder> readyOrders;
            final Result<Collection<ReadyOrder>> result = ReadyOrder.DAO.list(conn);
            if (result.isSuccess()) {
                readyOrders = result.getValue();
            } else {
                JOptionPane.showMessageDialog(
                    frame,
                    "Errore nel recupero degli ordini: " + result.getErrorMessage(),
                    ERROR_WINDOW_TITLE,
                    JOptionPane.ERROR_MESSAGE
                );
                return;
            }

            final String[][] data = new String[readyOrders.size()][4];
            int i = 0;
            final List<ReadyOrder> readyOrdersList = new ArrayList<>(readyOrders);
            for (final ReadyOrder order : readyOrders) {
                data[i][0] = String.valueOf(order.getId());
                data[i][1] = order.getClient().getUsername();
                data[i][2] = order.getRestaurant().getRestaurantName();
                data[i][3] = String.valueOf(order.getFoodRequested().size());
                i++;
            }
            final String[] columns = { "ID Ordine", "Cliente", "Ristorante", "N. Piatti" };

            final JTable table = new JTable(data, columns);
            table.setEnabled(true);
            table.setRowSelectionAllowed(true);
            table.setShowGrid(false);
            table.setFont(new Font("Dialog", Font.PLAIN, FONT_SIZE));
            table.setRowHeight(TABLE_ROW_HEIGHT);

            table.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(final MouseEvent evt) {
                    final int row = table.rowAtPoint(evt.getPoint());
                    if (row >= 0) {
                        final ReadyOrder order = readyOrdersList.get(row);
                        final var client = order.getClient();
                        final StringBuilder items = new StringBuilder();
                        for (final var entry : order.getFoodRequested().entrySet()) {
                            items.append("- ").append(entry.getKey().getName())
                                 .append(" x ").append(entry.getValue()).append(HTML_NEWLINE);
                        }
                        final String details = "<html><b>Ristorante:</b> "
                            + order.getRestaurant().getRestaurantName() + HTML_NEWLINE
                            + "<b>Cliente:</b> " + client.getUsername() + HTML_NEWLINE
                            + "<b>Città:</b> " + client.getCity() + HTML_NEWLINE
                            + "<b>Via:</b> " + client.getStreet() + HTML_NEWLINE
                            + "<b>N. Civico:</b> " + client.getHouseNumber() + HTML_NEWLINE
                            + "<b>Piatti ordinati:</b>" + HTML_NEWLINE
                            + items + HTML_NEWLINE
                            + "<b>Compenso:</b> $" + order.getShippingRate() + "</html>";

                        final int choice = JOptionPane.showConfirmDialog(
                            frame,
                            details + "Sei sicuro di voler accettare l'ordine?",
                            "Accetta Ordine",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.QUESTION_MESSAGE
                        );
                        if (choice == JOptionPane.YES_OPTION) {
                            final Timestamp now = new Timestamp(System.currentTimeMillis());
                            final Optional<DeliverymanUser> optDeliveryman = DeliverymanUser.DAO.find(conn, username).getValue();
                            if (optDeliveryman.isEmpty()) {
                                JOptionPane.showMessageDialog(
                                    frame,
                                    "Deliveryman non trovato!",
                                    ERROR_WINDOW_TITLE,
                                    JOptionPane.ERROR_MESSAGE
                                );
                                return;
                            }
                            final DeliverymanUser deliveryman = optDeliveryman.get();
                            final var acceptResult = AcceptedOrder.DAO.from(conn, order, now, deliveryman);
                            if (acceptResult.isSuccess()) {
                                JOptionPane.showMessageDialog(
                                    frame,
                                    "Ordine accettato!",
                                    "Conferma",
                                    JOptionPane.INFORMATION_MESSAGE
                                );
                                SwingUtilities.getWindowAncestor(table).dispose(); // Close the orders window
                                showOrdersButton.doClick(); // Reopen the updated window
                            } else {
                                JOptionPane.showMessageDialog(
                                    frame,
                                    "Errore nell'accettazione: " + acceptResult.getErrorMessage(),
                                    ERROR_WINDOW_TITLE,
                                    JOptionPane.ERROR_MESSAGE
                                );
                            }
                        }
                    }
                }
            });

            final JDialog dialog = new JDialog(frame, "Ordini disponibili", true);
            dialog.setSize(DELIVERY_WINDOW_WIDTH, DELIVERY_WINDOW_HEIGHT);
            dialog.setLocationRelativeTo(frame);

            final JScrollPane scrollPane = new JScrollPane(table);
            dialog.add(scrollPane);
            dialog.setVisible(true);
        });

        viewAcceptedButton.addActionListener(e -> {
            final Collection<AcceptedOrder> acceptedOrders;
            final Result<Collection<AcceptedOrder>> result = AcceptedOrder.DAO.list(conn);
            if (result.isSuccess()) {
                // Filter the orders based on the deliveryman username
                acceptedOrders = result
                    .getValue()
                    .stream()
                    .filter(order -> order.getDeliveryman().getUsername().equals(username))
                    .toList();
            } else {
                JOptionPane.showMessageDialog(
                    frame,
                    "Errore nel recupero degli ordini accettati: " + result.getErrorMessage(),
                    ERROR_WINDOW_TITLE,
                    JOptionPane.ERROR_MESSAGE
                );
                return;
            }

            final String[] columns = { "ID Ordine", "Cliente", "Ristorante", "N. Piatti", "Accettato il" };
            final String[][] data = new String[acceptedOrders.size()][columns.length];
            int i = 0;
            final List<AcceptedOrder> acceptedOrdersList = new ArrayList<>(acceptedOrders);
            for (final AcceptedOrder order : acceptedOrders) {
                data[i][0] = String.valueOf(order.getId());
                data[i][1] = order.getClient().getUsername();
                data[i][2] = order.getRestaurant().getRestaurantName();
                data[i][3] = String.valueOf(order.getFoodRequested().size());
                data[i][4] = order.getAcceptanceTime().toString();
                i++;
            }

            final JTable table = new JTable(data, columns);
            table.setEnabled(true);
            table.setRowSelectionAllowed(true);
            table.setShowGrid(false);
            table.setFont(new Font("Dialog", Font.PLAIN, FONT_SIZE));
            table.setRowHeight(TABLE_ROW_HEIGHT);

            table.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(final MouseEvent evt) {
                    final int row = table.rowAtPoint(evt.getPoint());
                    if (row >= 0) {
                        final AcceptedOrder order = acceptedOrdersList.get(row);
                        final var client = order.getClient();
                        final StringBuilder items = new StringBuilder();
                        for (final var entry : order.getFoodRequested().entrySet()) {
                            items.append("- ").append(entry.getKey().getName())
                                 .append(" x ").append(entry.getValue()).append(HTML_NEWLINE);
                        }
                        final String details = "<html><b>Ristorante:</b> "
                            + order.getRestaurant().getRestaurantName() + HTML_NEWLINE
                            + "<b>Cliente:</b> " + client.getUsername() + HTML_NEWLINE
                            + "<b>Città:</b> " + client.getCity() + HTML_NEWLINE
                            + "<b>Via:</b> " + client.getStreet() + HTML_NEWLINE
                            + "<b>N. Civico:</b> " + client.getHouseNumber() + HTML_NEWLINE
                            + "<b>Piatti ordinati:</b>" + HTML_NEWLINE
                            + items + HTML_NEWLINE
                            + "<b>Accettato il:</b> " + order.getAcceptanceTime() + HTML_NEWLINE
                            + "<b>Compenso:</b> $" + order.getShippingRate() + "</html>";

                        final int scelta = JOptionPane.showConfirmDialog(
                            frame,
                            details + HTML_NEWLINE + "Segna come consegnato?",
                            "Consegna Ordine",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.QUESTION_MESSAGE
                        );
                        if (scelta == JOptionPane.YES_OPTION) {
                            final Timestamp now = Timestamp.valueOf(LocalDateTime.now());
                            final var deliverResult = DeliveredOrder.DAO.from(conn, order, now);
                            if (deliverResult.isSuccess()) {
                                // Accredit the compensation to the deliveryman
                                final DeliverymanUser deliveryman = order.getDeliveryman();
                                final Result<DeliverymanUser> resUpdate = DeliverymanUser.DAO.updateCredit(
                                    conn,
                                    deliveryman,
                                    deliveryman.getCredit().add(order.getShippingRate())
                                );
                                if (!resUpdate.isSuccess()) {
                                    JOptionPane.showMessageDialog(
                                        frame,
                                        "Errore nell'accredito del compenso al fattorino: " + resUpdate.getErrorMessage(),
                                        ERROR_WINDOW_TITLE,
                                        JOptionPane.ERROR_MESSAGE
                                    );
                                    return;
                                }
                                JOptionPane.showMessageDialog(
                                    frame,
                                    "Ordine consegnato!",
                                    "Conferma",
                                    JOptionPane.INFORMATION_MESSAGE
                                );
                                SwingUtilities.getWindowAncestor(table).dispose();
                                viewAcceptedButton.doClick();
                            } else {
                                JOptionPane.showMessageDialog(
                                    null,
                                    "Errore nella consegna: " + deliverResult.getErrorMessage(),
                                    ERROR_WINDOW_TITLE,
                                    JOptionPane.ERROR_MESSAGE
                                );
                            }
                        }
                    }
                }
            });

            final JDialog dialog = new JDialog(frame, "Ordini da consegnare", true);
            dialog.setSize(DELIVERY_WINDOW_WIDTH, DELIVERY_WINDOW_HEIGHT);
            dialog.setLocationRelativeTo(frame);

            final JScrollPane scrollPane = new JScrollPane(table);
            dialog.add(scrollPane);
            dialog.setVisible(true);
        });
    }

    private JButton createButton(final String text) {
        final JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.PLAIN, FONT_SIZE - 4));
        button.setPreferredSize(BUTTON_DIMENSION);
        button.setFocusPainted(false);
        return button;
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
