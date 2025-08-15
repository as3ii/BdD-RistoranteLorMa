package it.ristorantelorma.view.deliveryman;

import it.ristorantelorma.model.order.ReadyOrder;
import it.ristorantelorma.model.order.AcceptedOrder;
import it.ristorantelorma.model.Result;
import it.ristorantelorma.model.user.DeliverymanUser;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Optional;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

public class DeliverymanPage extends JFrame {
    private final JButton showOrdersButton;
    private final JButton viewAcceptedButton;

    public DeliverymanPage(Connection conn, String username) {
        super("DeliveryDB");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 400);
        setLocationRelativeTo(null);
        setResizable(false);

        setLayout(new BorderLayout());

        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setBackground(Color.WHITE);

        showOrdersButton = createButton("Show available orders");
        viewAcceptedButton = createButton("View Accepted");

        centerPanel.add(Box.createVerticalStrut(20));
        centerPanel.add(showOrdersButton);
        centerPanel.add(Box.createVerticalStrut(10));
        centerPanel.add(viewAcceptedButton);

        add(centerPanel, BorderLayout.CENTER);

        showOrdersButton.addActionListener(e -> {
            // Usa il DAO per ottenere gli ordini "pronto"
            Collection<ReadyOrder> readyOrders;
            var result = ReadyOrder.DAO.list(conn);
            if (result.isSuccess()) {
                readyOrders = result.getValue();
            } else {
                JOptionPane.showMessageDialog(
                    this,
                    "Errore nel recupero degli ordini: " + result.getErrorMessage(),
                    "Errore",
                    JOptionPane.ERROR_MESSAGE
                );
                return;
            }

            // Prepara i dati per la tabella
            String[][] data = new String[readyOrders.size()][4];
            int i = 0;
            ArrayList<ReadyOrder> readyOrdersList = new ArrayList<>(readyOrders); // <--- AGGIUNTA
            for (ReadyOrder order : readyOrders) {
                data[i][0] = String.valueOf(order.getId());
                data[i][1] = order.getClient().getUsername();
                data[i][2] = order.getRestaurant().getRestaurantName();
                data[i][3] = String.valueOf(order.getFoodRequested().size());
                i++;
            }
            String[] columns = { "ID Ordine", "Cliente", "Ristorante", "N. Piatti" };

            JTable table = new JTable(data, columns);
            table.setEnabled(true);
            table.setRowSelectionAllowed(true);
            table.setShowGrid(false);
            table.setFont(new Font("Dialog", Font.PLAIN, 16));
            table.setRowHeight(28);

            table.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseClicked(java.awt.event.MouseEvent evt) {
                    int row = table.rowAtPoint(evt.getPoint());
                    if (row >= 0) {
                        ReadyOrder order = readyOrdersList.get(row);
                        var client = order.getClient();
                        StringBuilder items = new StringBuilder();
                        for (var entry : order.getFoodRequested().entrySet()) {
                            items.append("- ").append(entry.getKey().getName())
                                 .append(" x ").append(entry.getValue()).append("<br>");
                        }
                        String details = "<html><b>Ristorante:</b> " + order.getRestaurant().getRestaurantName() + "<br>"
                            + "<b>Cliente:</b> " + client.getUsername() + "<br>"
                            + "<b>Città:</b> " + client.getCity() + "<br>"
                            + "<b>Via:</b> " + client.getStreet() + "<br>"
                            + "<b>N. Civico:</b> " + client.getHouseNumber() + "<br>"
                            + "<b>Piatti ordinati:</b><br>" + items
                            + "<br><b>Compenso:</b> $" + order.getShippingRate() + "</html>";

                        int scelta = JOptionPane.showConfirmDialog(
                            null,
                            details + "Sei sicuro di voler accettare l'ordine?",
                            "Accetta Ordine",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.QUESTION_MESSAGE
                        );
                        if (scelta == JOptionPane.YES_OPTION) {
                            Timestamp now = new Timestamp(System.currentTimeMillis());
                            Optional<DeliverymanUser> optDeliveryman = DeliverymanUser.DAO.find(conn, username).getValue();
                            if (optDeliveryman.isEmpty()) {
                                JOptionPane.showMessageDialog(null, "Deliveryman non trovato!", "Errore", JOptionPane.ERROR_MESSAGE);
                                return;
                            }
                            DeliverymanUser deliveryman = optDeliveryman.get();
                            var acceptResult = AcceptedOrder.DAO.from(conn, order, now, deliveryman);
                            if (acceptResult.isSuccess()) {
                                JOptionPane.showMessageDialog(null, "Ordine accettato!", "Conferma", JOptionPane.INFORMATION_MESSAGE);
                                SwingUtilities.getWindowAncestor(table).dispose(); // Chiude la finestra degli ordini disponibili
                                showOrdersButton.doClick(); // Riapre la finestra aggiornata
                            } else {
                                JOptionPane.showMessageDialog(null, "Errore nell'accettazione: " + acceptResult.getErrorMessage(), "Errore", JOptionPane.ERROR_MESSAGE);
                            }
                        }
                    }
                }
            });

            JDialog dialog = new JDialog(this, "Ordini disponibili", true);
            dialog.setSize(400, 300);
            dialog.setLocationRelativeTo(this);

            JScrollPane scrollPane = new JScrollPane(table);
            dialog.add(scrollPane);
            dialog.setVisible(true);
        });

        viewAcceptedButton.addActionListener(e -> {
            // Ottieni gli ordini accettati dal DAO
            Collection<AcceptedOrder> acceptedOrders;
            var result = AcceptedOrder.DAO.list(conn);
            if (result.isSuccess()) {
                // Filtra solo quelli del fattorino corrente
                acceptedOrders = result.getValue().stream()
                    .filter(order -> order.getDeliveryman().getUsername().equals(username))
                    .toList();
            } else {
                JOptionPane.showMessageDialog(
                    this,
                    "Errore nel recupero degli ordini accettati: " + result.getErrorMessage(),
                    "Errore",
                    JOptionPane.ERROR_MESSAGE
                );
                return;
            }

            // Prepara i dati per la tabella
            String[][] data = new String[acceptedOrders.size()][5];
            int i = 0;
            ArrayList<AcceptedOrder> acceptedOrdersList = new ArrayList<>(acceptedOrders);
            for (AcceptedOrder order : acceptedOrders) {
                data[i][0] = String.valueOf(order.getId());
                data[i][1] = order.getClient().getUsername();
                data[i][2] = order.getRestaurant().getRestaurantName();
                data[i][3] = String.valueOf(order.getFoodRequested().size());
                data[i][4] = order.getAcceptanceTime().toString();
                i++;
            }
            String[] columns = { "ID Ordine", "Cliente", "Ristorante", "N. Piatti", "Accettato il" };

            JTable table = new JTable(data, columns);
            table.setEnabled(true);
            table.setRowSelectionAllowed(true);
            table.setShowGrid(false);
            table.setFont(new Font("Dialog", Font.PLAIN, 16));
            table.setRowHeight(28);

            // Puoi aggiungere qui un MouseListener per gestire la consegna dell'ordine
            // Ad esempio, mostrare i dettagli e permettere di segnare come "consegnato"

            JDialog dialog = new JDialog(this, "Ordini da consegnare", true);
            dialog.setSize(500, 300);
            dialog.setLocationRelativeTo(this);

            JScrollPane scrollPane = new JScrollPane(table);
            dialog.add(scrollPane);
            dialog.setVisible(true);
        });
    }

    private JButton createButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.PLAIN, 12));
        button.setPreferredSize(new Dimension(160, 30));
        button.setFocusPainted(false);
        return button;
    }
}