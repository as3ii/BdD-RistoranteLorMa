package it.ristorantelorma.view.admin;

import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.BoxLayout;
import javax.swing.Box;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import it.ristorantelorma.model.DatabaseConnectionManager;
import it.ristorantelorma.model.Queries;

/**
 * Finestra per amministratore, visualizzata dopo login con ruolo "admin".
 */
public final class AdminDashboard {
    private static final int DASHBOARD_WIDTH = 800;
    private static final int DASHBOARD_HEIGHT = 600;
    private static final int REVIEWS_WIDTH = 600;
    private static final int REVIEWS_HEIGHT = 400;
    private static final int COMBO_WIDTH = 200;
    private static final int COMBO_HEIGHT = 30;

    private final JFrame frame;
    private final JComboBox<String> restaurantComboBox;
    private final java.util.List<String> restaurantNames = new java.util.ArrayList<>();

    /**
     * Costruttore della dashboard admin.
     */
    public AdminDashboard() {
        frame = new JFrame("DeliveryDB");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(DASHBOARD_WIDTH, DASHBOARD_HEIGHT);
        frame.setLocationRelativeTo(null);
        frame.setLayout(new BorderLayout());

        // Top panel con Logout
        final JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        final JButton logoutButton = new JButton("Logout");
        topPanel.add(logoutButton);
        frame.add(topPanel, BorderLayout.NORTH);

        // Centro con View Reviews e ComboBox
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

        restaurantComboBox = new JComboBox<>();
        restaurantComboBox.setMaximumSize(new Dimension(COMBO_WIDTH, COMBO_HEIGHT));
        centerPanel.add(restaurantComboBox);
        centerPanel.add(Box.createVerticalGlue());

        frame.add(centerPanel, BorderLayout.CENTER);

        // Bottom panel con i bottoni
        final JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bottomPanel.add(new JButton("Top dish"));
        bottomPanel.add(new JButton("Most popular cuisine type"));
        bottomPanel.add(new JButton("5 most chosen restaurants"));
        bottomPanel.add(new JButton("Worst restaurant"));
        bottomPanel.add(new JButton("Best deliverer"));
        frame.add(bottomPanel, BorderLayout.SOUTH);

        // Carica i nomi dei ristoranti dal database
        loadRestaurants();

        // Listener per il bottone View Reviews
        viewReviewsButton.addActionListener(e -> {
            final String selectedRestaurant = (String) restaurantComboBox.getSelectedItem();
            if (selectedRestaurant != null) {
                new ReviewsWindow(selectedRestaurant).show();
            }
        });
    }

    /**
     * Carica i nomi dei ristoranti dal database e li inserisce nella ComboBox.
     */
    private void loadRestaurants() {
    try (Connection conn = DatabaseConnectionManager.getInstance().getConnection();
         PreparedStatement stmt = conn.prepareStatement(Queries.LIST_RESTAURANTS);
         ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                final String name = rs.getString("nome_attività");
                restaurantNames.add(name);
                restaurantComboBox.addItem(name);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(frame, "Errore SQL nel caricamento dei ristoranti: " + ex.getMessage());
        }
    }

    /**
     * Finestra che mostra le recensioni di un ristorante.
     */
    private static class ReviewsWindow {

        private final JFrame frame;
        private final JTable table;
        private final DefaultTableModel tableModel;

        ReviewsWindow(final String restaurantName) {
            frame = new JFrame("Recensioni di " + restaurantName);
            frame.setSize(REVIEWS_WIDTH, REVIEWS_HEIGHT);
            frame.setLocationRelativeTo(null);
            frame.setLayout(new BorderLayout());

            // Colonne: codice, utente, data, voto, commento
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

            // Bottone elimina recensione
            final JButton deleteButton = new JButton("Elimina recensione");
            deleteButton.addActionListener(e -> deleteSelectedReview(restaurantName));
            final JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            buttonPanel.add(deleteButton);
            frame.add(buttonPanel, BorderLayout.SOUTH);

            // Carica le recensioni dal database
            loadReviews(restaurantName);
        }

        private void loadReviews(final String restaurantName) {
            tableModel.setRowCount(0);
            try (Connection conn = DatabaseConnectionManager.getInstance().getConnection();
                 PreparedStatement stmt = conn.prepareStatement(Queries.LIST_REVIEWS_OF_RESTAURANT)) {
                stmt.setString(1, restaurantName);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        final Object[] row = {
                            rs.getInt("codice"),
                            rs.getString("username"),
                            rs.getString("data"),
                            rs.getString("voto"),
                            rs.getString("commento")
                        };
                        tableModel.addRow(row);
                    }
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(frame, "Errore SQL nel caricamento delle recensioni: " + ex.getMessage());
            }
        }

        private void deleteSelectedReview(final String restaurantName) {
            final int selectedRow = table.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(frame, "Seleziona una recensione da eliminare.");
                return;
            }
            final int codice = (int) tableModel.getValueAt(selectedRow, 0);
            final int confirm = JOptionPane.showConfirmDialog(
                frame,
                "Vuoi eliminare la recensione selezionata?",
                "Conferma eliminazione",
                JOptionPane.YES_NO_OPTION
            );
            if (confirm != JOptionPane.YES_OPTION) {
                return;
            }
            try (Connection conn = DatabaseConnectionManager.getInstance().getConnection();
                 PreparedStatement stmt = conn.prepareStatement(Queries.DELETE_REVIEW)) {
                stmt.setInt(1, codice);
                final int affected = stmt.executeUpdate();
                if (affected > 0) {
                    JOptionPane.showMessageDialog(frame, "Recensione eliminata.");
                    loadReviews(restaurantName);
                } else {
                    JOptionPane.showMessageDialog(frame, "Errore: recensione non trovata.");
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(frame, "Errore SQL nell'eliminazione: " + ex.getMessage());
            }
        }

        public void show() {
            frame.setVisible(true);
        }
    }

    /**
     * Mostra la finestra della dashboard admin.
     */
    public void show() {
        frame.setVisible(true);
    }
}
