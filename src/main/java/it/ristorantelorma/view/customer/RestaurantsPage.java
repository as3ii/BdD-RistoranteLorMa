package it.ristorantelorma.view.customer;

import it.ristorantelorma.view.authentication.LoginPage;
import it.ristorantelorma.model.Restaurant;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collection;

public class RestaurantsPage extends JFrame {

    private final LoginPage loginPage;
    private final String username; // aggiungi questo campo

    public RestaurantsPage(LoginPage loginPage, Connection connection, String username) {
        this.loginPage = loginPage;
        this.username = username; // salva lo username
        setTitle("RestaurantsPage");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(500, 500);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout());

        // Ottieni i ristoranti dal database
        Collection<Restaurant> restaurants = new ArrayList<>();
        var result = Restaurant.DAO.list(connection);
        if (result.isSuccess()) {
            restaurants = result.getValue();
        } else {
            JOptionPane.showMessageDialog(this, "Errore nel caricamento dei ristoranti: " + result.getErrorMessage());
        }

        // Prepara i dati per la tabella
        String[][] data = new String[restaurants.size()][3];
        int i = 0;
        for (Restaurant r : restaurants) {
            data[i][0] = r.getRestaurantName();
            data[i][1] = formatTime(r.getOpeningTime()); // Solo orario
            data[i][2] = formatTime(r.getClosingTime()); // Solo orario
            i++;
        }
        String[] columns = { "Nome Attività", "Apertura", "Chiusura" };

        JTable table = new JTable(data, columns);
        table.setEnabled(true); // Permetti la selezione
        table.setRowSelectionAllowed(true);
        table.setShowGrid(false);
        table.setTableHeader(null);
        table.setFont(new Font("Dialog", Font.PLAIN, 18));
        table.setRowHeight(28);

        // Imposta larghezza colonne orario
        table.getColumnModel().getColumn(1).setMinWidth(60);
        table.getColumnModel().getColumn(1).setMaxWidth(70);
        table.getColumnModel().getColumn(2).setMinWidth(60);
        table.getColumnModel().getColumn(2).setMaxWidth(70);

        table.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                int row = table.rowAtPoint(evt.getPoint());
                if (row >= 0) {
                    String restaurantName = data[row][0];
                    // Apri la finestra del menu del ristorante selezionato
                    SwingUtilities.invokeLater(() -> {
                        RestaurantsPage.this.setVisible(false);
                        ResMenu resMenu = new ResMenu(restaurantName, RestaurantsPage.this, username); // passa lo username
                        resMenu.setVisible(true);
                    });
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        JButton logoutButton = new JButton("Logout");
        logoutButton.addActionListener(e -> handleLogout());
        buttonPanel.add(logoutButton);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(mainPanel);
    }

    private void handleLogout() {
        this.setVisible(false); // Chiude la finestra RestaurantsPage
        loginPage.handleReset(); // Resetta i campi di LoginPage
        loginPage.show(); // Mostra la finestra di LoginPage

    }

    private String formatTime(java.sql.Timestamp timestamp) {
        if (timestamp == null) return "";
        return timestamp.toLocalDateTime().toLocalTime().toString();
    }
}