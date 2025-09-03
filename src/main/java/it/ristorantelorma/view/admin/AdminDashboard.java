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
import java.sql.ResultSetMetaData;

/**
 * Finestra per amministratore, visualizzata dopo login con ruolo "admin".
 */
public final class AdminDashboard {
    private static final String NOME = "nome";
    private static final String ERRORE_SQL = "Errore SQL: ";
    private static final String MOST_POPULAR_CUISINE_TYPE = "Most popular cuisine type";
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
    final JButton topDishButton = new JButton(TOP_DISH_LABEL);
        topDishButton.addActionListener(e -> {
            try (Connection conn = DatabaseConnectionManager.getInstance().getConnection();
                 PreparedStatement stmt = conn.prepareStatement(
                        "SELECT v.nome, SUM(d.quantità) AS quantità_totale "
                        + "FROM VIVANDE v, DETTAGLIO_ORDINI d "
                        + "WHERE v.codice = d.codice_vivanda "
                        + "GROUP BY codice_vivanda, v.nome "
                        + "ORDER BY quantità_totale DESC LIMIT 1;");
                 ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    final String nomeVivanda = rs.getString("nome");
                    final int quantitaTotale = rs.getInt("quantità_totale");
                    JOptionPane.showMessageDialog(frame,
                        "Vivanda più acquistata: " + nomeVivanda + "\nQuantità totale: " + quantitaTotale,
                        TOP_DISH_LABEL, JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(frame,
                        "Nessuna vivanda trovata.", TOP_DISH_LABEL, JOptionPane.INFORMATION_MESSAGE);
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(frame,
                    "Errore SQL: " + ex.getMessage(), TOP_DISH_LABEL, JOptionPane.ERROR_MESSAGE);
            }
        });
        bottomPanel.add(topDishButton);
    final JButton mostPopularCuisineButton = new JButton(MOST_POPULAR_CUISINE_TYPE);
        mostPopularCuisineButton.addActionListener(e -> {
                try (Connection conn = DatabaseConnectionManager.getInstance().getConnection();
                      PreparedStatement stmt = conn.prepareStatement(
                          "SELECT t.*, SUM(d.quantità) AS totale "
                          + "FROM TIPO_VIVANDE t, VIVANDE v, DETTAGLIO_ORDINI d "
                          + "WHERE t.nome = v.tipologia AND v.codice = d.codice_vivanda "
                          + "GROUP BY t.nome ORDER BY totale DESC LIMIT 1;");
                      ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    final String nomeTipologia = rs.getString(NOME);
                    final int totale = rs.getInt("totale");
                    final ResultSetMetaData meta = rs.getMetaData();
                    final StringBuilder info = new StringBuilder(128);
                    info.append("Tipologia di cucina più acquistata: ").append(nomeTipologia)
                        .append("\nTotale piatti acquistati: ").append(totale).append('\n');
                    for (int i = 1; i <= meta.getColumnCount(); i++) {
                        final String colName = meta.getColumnName(i);
                        if (!NOME.equalsIgnoreCase(colName) && !"totale".equalsIgnoreCase(colName)) {
                            info.append(colName).append(": ").append(rs.getString(colName)).append('\n');
                        }
                    }
                    JOptionPane.showMessageDialog(frame, info.toString(), MOST_POPULAR_CUISINE_TYPE,
                                                    JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(frame, "Nessuna tipologia trovata.", MOST_POPULAR_CUISINE_TYPE,
                                                    JOptionPane.INFORMATION_MESSAGE);
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(frame, ERRORE_SQL + ex.getMessage(), MOST_POPULAR_CUISINE_TYPE,
                                                JOptionPane.ERROR_MESSAGE);
            }
        });
        bottomPanel.add(mostPopularCuisineButton);
    final String worstRestaurantsLabel = "Worst restaurants";
    final JButton worstRestaurantsButton = new JButton(worstRestaurantsLabel);
        worstRestaurantsButton.addActionListener(e -> {
            try (Connection conn = DatabaseConnectionManager.getInstance().getConnection();
                 PreparedStatement stmt = conn.prepareStatement(
                     "SELECT ris.*, AVG(CAST(rec.voto AS UNSIGNED)) AS average "
                     + "FROM RISTORANTI ris, RECENSIONI rec "
                     + "WHERE ris.nome_attività = rec.nome_attività "
                     + "GROUP BY rec.nome_attività ORDER BY average ASC LIMIT 1;");
                 ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    final String nome = rs.getString("nome_attività");
                    final double average = rs.getDouble("average");
                    final StringBuilder info = new StringBuilder(128);
                    info.append("Ristorante con più recensioni negative:\nNome: ")
                        .append(nome)
                        .append("\nMedia voti: ")
                        .append(average)
                        .append('\n');
                    // Mostra altre info se necessario
                    JOptionPane.showMessageDialog(frame, info.toString(), worstRestaurantsLabel,
                            JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(frame, "Nessun ristorante trovato.", worstRestaurantsLabel,
                            JOptionPane.INFORMATION_MESSAGE);
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(frame, ERRORE_SQL + ex.getMessage(), worstRestaurantsLabel,
                        JOptionPane.ERROR_MESSAGE);
            }
        });
        bottomPanel.add(worstRestaurantsButton);
        // Bottone Best restaurant con ActionListener
    final JButton bestRestaurantButton = new JButton(BEST_RESTAURANT_LABEL);
        bestRestaurantButton.addActionListener(e -> {
         try (Connection conn = DatabaseConnectionManager.getInstance().getConnection();
           PreparedStatement stmt = conn.prepareStatement(
               "SELECT r.nome_attività, COUNT(o.nome_attività) AS numero_ordini "
               + "FROM RISTORANTI r, ORDINI o "
               + "WHERE r.nome_attività = o.nome_attività "
               + "GROUP BY o.nome_attività ORDER BY numero_ordini DESC LIMIT 1;");
           ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
            final String nome = rs.getString("nome_attività"); // nome_attività non è duplicato, lasciato invariato
            final int numeroOrdini = rs.getInt("numero_ordini");
            final String info = "Ristorante con più ordini:\n"
                + "Nome: " + nome + "\n"
                + "Numero ordini: " + numeroOrdini;
                    JOptionPane.showMessageDialog(frame, info, BEST_RESTAURANT_LABEL, JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(frame, "Nessun ristorante trovato.", BEST_RESTAURANT_LABEL,
                            JOptionPane.INFORMATION_MESSAGE);
                }
            } catch (SQLException ex) {
        JOptionPane.showMessageDialog(frame, ERRORE_SQL + ex.getMessage(), BEST_RESTAURANT_LABEL,
            JOptionPane.ERROR_MESSAGE);
            }
        });
        bottomPanel.add(bestRestaurantButton);
        final JButton bestDelivererButton = new JButton(BEST_DELIVERER_LABEL);
        bestDelivererButton.addActionListener(e -> {
                try (Connection conn = DatabaseConnectionManager.getInstance().getConnection();
                      PreparedStatement stmt = conn.prepareStatement(
                          "SELECT u.*, COUNT(o.username_fattorino) AS numero_ordini "
                          + "FROM UTENTI u, ORDINI o "
                          + "WHERE u.username = o.username_fattorino AND ora_consegna IS NOT NULL "
                          + "GROUP BY o.username_fattorino ORDER BY numero_ordini DESC LIMIT 1;");
                      ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    final String info = "Miglior fattorino:\n"
                        + "Username: " + rs.getString("username") + "\n"
                        + "Nome: " + rs.getString("nome") + "\n"
                        + "Cognome: " + rs.getString("cognome") + "\n"
                        + "Numero ordini consegnati: " + rs.getInt("numero_ordini");
                    JOptionPane.showMessageDialog(frame, info, BEST_DELIVERER_LABEL, JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(frame, "Nessun fattorino trovato.", BEST_DELIVERER_LABEL,
                                                    JOptionPane.INFORMATION_MESSAGE);
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(frame, ERRORE_SQL + ex.getMessage(), BEST_DELIVERER_LABEL,
                                                JOptionPane.ERROR_MESSAGE);
            }
        });
        bottomPanel.add(bestDelivererButton);
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
        } catch (final SQLException ex) {
            JOptionPane.showMessageDialog(frame, ERRORE_SQL + "nel caricamento dei ristoranti: " + ex.getMessage());
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
                JOptionPane.showMessageDialog(frame, ERRORE_SQL + "nel caricamento delle recensioni: " + ex.getMessage());
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
                JOptionPane.showMessageDialog(frame, ERRORE_SQL + "nell'eliminazione: " + ex.getMessage());
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
