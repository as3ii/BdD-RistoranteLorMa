package it.ristorantelorma.view.customer;

import it.ristorantelorma.model.DatabaseConnectionManager;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ResMenu extends JFrame {

    private double balance;

    public ResMenu(String restaurantName, RestaurantsPage restaurantsPage, String username) {
        setTitle("DeliveryDB");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 600);
        setLocationRelativeTo(null);

        List<String[]> menuData = new ArrayList<>();
        balance = 0.0;
        Connection conn = null;
        try {
            conn = DatabaseConnectionManager.getInstance().getConnection();
            // Query vivande
            PreparedStatement psVivande = null;
            ResultSet rsVivande = null;
            try {
                psVivande = conn.prepareStatement(
                    "SELECT nome, nome_attività, prezzo FROM vivande WHERE nome_attività = ?");
                psVivande.setString(1, restaurantName);
                rsVivande = psVivande.executeQuery();
                while (rsVivande.next()) {
                    String nome = rsVivande.getString("nome");
                    String nome_attività = rsVivande.getString("nome_attività");
                    String prezzo = rsVivande.getString("prezzo");
                    menuData.add(new String[]{nome, nome_attività, prezzo});
                }
            } finally {
                if (rsVivande != null) try { rsVivande.close(); } catch (SQLException ignore) {}
                if (psVivande != null) try { psVivande.close(); } catch (SQLException ignore) {}
            }

            // Query saldo utente
            PreparedStatement psSaldo = null;
            ResultSet rsSaldo = null;
            try {
                psSaldo = conn.prepareStatement(
                    "SELECT credito FROM utenti WHERE username = ?");
                psSaldo.setString(1, username);
                rsSaldo = psSaldo.executeQuery();
                if (rsSaldo.next()) {
                    balance = rsSaldo.getDouble("credito");
                }
            } finally {
                if (rsSaldo != null) try { rsSaldo.close(); } catch (SQLException ignore) {}
                if (psSaldo != null) try { psSaldo.close(); } catch (SQLException ignore) {}
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Errore nel caricamento dei dati vivande o saldo: " + e.getMessage());
        } finally {
            //if (conn != null) try { conn.close(); } catch (SQLException ignore) {}
        }

        JPanel mainPanel = new JPanel(new BorderLayout());

        JPanel menuPanel = new JPanel(new BorderLayout());
        JPanel menuTablePanel = new JPanel();
        menuTablePanel.setLayout(new BoxLayout(menuTablePanel, BoxLayout.Y_AXIS));

        JPanel header = new JPanel(new GridLayout(1, 4));
        header.add(new JLabel("Nome"));
        header.add(new JLabel("Nome Vivanda"));
        header.add(new JLabel("Quantità"));
        header.add(new JLabel("Prezzo"));
        menuTablePanel.add(header);

        JSpinner[] quantitySpinners = new JSpinner[menuData.size()];
        for (int i = 0; i < menuData.size(); i++) {
            JPanel row = new JPanel(new GridLayout(1, 4));
            row.add(new JLabel(menuData.get(i)[0]));
            row.add(new JLabel(menuData.get(i)[1]));
            quantitySpinners[i] = new JSpinner(new SpinnerNumberModel(0, 0, 99, 1));
            row.add(quantitySpinners[i]);
            row.add(new JLabel(menuData.get(i)[2]));
            menuTablePanel.add(row);
        }

        JScrollPane menuScroll = new JScrollPane(menuTablePanel);
        menuScroll.setPreferredSize(new Dimension(500, 400));
        menuPanel.add(menuScroll, BorderLayout.CENTER);

        // Pulsanti Back e Send Order
        JPanel buttonPanel = new JPanel();
        JButton backButton = new JButton("Back");
        JButton sendOrderButton = new JButton("Send Order");
        sendOrderButton.setBackground(Color.RED);
        sendOrderButton.setForeground(Color.WHITE);

        // Implementazione azioni bottoni
        backButton.addActionListener(e -> {
            this.dispose();
            restaurantsPage.setVisible(true);
        });
        sendOrderButton.addActionListener(e -> {
            double total = 0.0;
            int[] quantities = new int[quantitySpinners.length];
            for (int i = 0; i < quantitySpinners.length; i++) {
                quantities[i] = (Integer) quantitySpinners[i].getValue();
                double price = Double.parseDouble(menuData.get(i)[2]);
                total += quantities[i] * price;
            }
            if (total > balance) {
                JOptionPane.showMessageDialog(this, "Saldo insufficiente!");
                return;
            }
            try (Connection conn1 = DatabaseConnectionManager.getInstance().getConnection()) {
                conn1.setAutoCommit(false);

                // Aggiorna saldo utente
                try (PreparedStatement psUpdate = conn1.prepareStatement(
                        "UPDATE utenti SET credito = ? WHERE username = ?")) {
                    psUpdate.setDouble(1, balance - total);
                    psUpdate.setString(2, username);
                    psUpdate.executeUpdate();
                }

                // Inserisci ordine
                int orderId = -1;
                try (PreparedStatement psOrder = conn1.prepareStatement(
                        "INSERT INTO ordini (nome_attività, data_ora, stato, tariffa_spedizione, username_cliente) VALUES (?, NOW(), 'pronto', ?, ?)",
                        Statement.RETURN_GENERATED_KEYS)) {
                    psOrder.setString(1, menuData.get(0)[1]);
                    psOrder.setDouble(2, 2.5);
                    psOrder.setString(3, username);
                    psOrder.executeUpdate();
                    try (ResultSet rsOrder = psOrder.getGeneratedKeys()) {
                        if (rsOrder.next()) {
                            orderId = rsOrder.getInt(1);
                        }
                    }
                }

                // Inserisci dettaglio ordini
                try (PreparedStatement psDetail = conn1.prepareStatement(
                        "INSERT INTO dettaglio_ordini (codice_vivanda, codice_ordine, quantità) VALUES (?, ?, ?)")) {
                    for (int i = 0; i < menuData.size(); i++) {
                        if (quantities[i] > 0) {
                            int codiceVivanda = -1;
                            try (PreparedStatement psViv = conn1.prepareStatement(
                                    "SELECT codice FROM vivande WHERE nome = ? AND nome_attività = ?")) {
                                psViv.setString(1, menuData.get(i)[0]);
                                psViv.setString(2, menuData.get(i)[1]);
                                try (ResultSet rsViv = psViv.executeQuery()) {
                                    if (rsViv.next()) {
                                        codiceVivanda = rsViv.getInt("codice");
                                    }
                                }
                            }
                            if (codiceVivanda != -1) {
                                psDetail.setInt(1, codiceVivanda);
                                psDetail.setInt(2, orderId);
                                psDetail.setInt(3, quantities[i]);
                                psDetail.addBatch();
                            }
                        }
                    }
                    psDetail.executeBatch();
                }

                conn1.commit();
                JOptionPane.showMessageDialog(this, "Ordine inviato con successo!");
                this.dispose();
                restaurantsPage.setVisible(true);
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Errore nell'invio dell'ordine: " + ex.getMessage());
            }
        });

        buttonPanel.add(backButton);
        buttonPanel.add(sendOrderButton);
        menuPanel.add(buttonPanel, BorderLayout.SOUTH);

        mainPanel.add(menuPanel, BorderLayout.CENTER);

        // --- DESTRA: Info, riepilogo, saldo ---
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setPreferredSize(new Dimension(300, 400));

        JLabel balanceLabel = new JLabel("Balance: $" + String.format("%.2f", balance));
        JLabel orderSummaryLabel = new JLabel("Order Summary:");
        JTextArea orderSummaryArea = new JTextArea(6, 20);
        orderSummaryArea.setEditable(false);
        JLabel totalLabel = new JLabel("Total: $0.0");

        JPanel restaurantInfoPanel = new JPanel();
        restaurantInfoPanel.setLayout(new BoxLayout(restaurantInfoPanel, BoxLayout.Y_AXIS));
        restaurantInfoPanel.add(new JLabel("Restaurant Info:"));
        restaurantInfoPanel.add(new JLabel("Restaurant: " + restaurantName));
        restaurantInfoPanel.add(new JLabel("Opening Hours: 12:30 - 22:30"));
        JButton reviewsButton = new JButton("Reviews");
        reviewsButton.setBackground(Color.BLUE);
        reviewsButton.setForeground(Color.WHITE);

        // Implementazione azione bottone Reviews
        reviewsButton.addActionListener(e -> {
            ReviewDialog dialog = new ReviewDialog(this, restaurantName, username);
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

        setContentPane(mainPanel);

        // Dopo aver creato totalLabel:
        for (int i = 0; i < quantitySpinners.length; i++) {
            quantitySpinners[i].addChangeListener(e -> {
                double total = 0.0;
                for (int j = 0; j < quantitySpinners.length; j++) {
                    int qty = (Integer) quantitySpinners[j].getValue();
                    double price = Double.parseDouble(menuData.get(j)[2]);
                    total += qty * price;
                }
                totalLabel.setText("Total: $" + String.format("%.2f", total));
            });
        }
    }
}
