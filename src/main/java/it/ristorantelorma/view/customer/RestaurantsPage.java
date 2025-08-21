package it.ristorantelorma.view.customer;

import it.ristorantelorma.view.authentication.LoginPage;
import it.ristorantelorma.model.Restaurant;
import it.ristorantelorma.model.Result;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.util.Collection;

public class RestaurantsPage extends JFrame {

    private final LoginPage loginPage;

    public RestaurantsPage(final LoginPage loginPage, final Connection connection, final String username) {
        this.loginPage = loginPage;
        setTitle("RestaurantsPage");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(500, 500);
        setLocationRelativeTo(null);

        final JPanel mainPanel = new JPanel(new BorderLayout());

        // Ottieni i ristoranti dal database
        final Collection<Restaurant> restaurants;
        final Result<Collection<Restaurant>> result = Restaurant.DAO.list(connection);
        if (result.isSuccess()) {
            restaurants = result.getValue();
        } else {
            JOptionPane.showMessageDialog(
                this,
                "Errore nel caricamento dei ristoranti: " + result.getErrorMessage()
            );
            return;
        }

        // Prepara i dati per la tabella
        final String[][] data = new String[restaurants.size()][3];
        int i = 0;
        for (final Restaurant r : restaurants) {
            data[i][0] = r.getRestaurantName();
            data[i][1] = r.getOpeningTime().toLocalDateTime().toLocalTime().toString();
            data[i][2] = r.getClosingTime().toLocalDateTime().toLocalTime().toString();
            i++;
        }
        final String[] columns = { "Nome Attività", "Apertura", "Chiusura" };

        final JTable table = new JTable(data, columns);
        table.setEnabled(true);
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

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(final MouseEvent evt) {
                final int row = table.rowAtPoint(evt.getPoint());
                if (row >= 0) {
                    final String restaurantName = data[row][0];
                    // Apri la finestra del menu del ristorante selezionato
                    SwingUtilities.invokeLater(() -> {
                        RestaurantsPage.this.setVisible(false);
                        final ResMenu resMenu = new ResMenu(restaurantName, RestaurantsPage.this, username); // passa lo username
                        resMenu.setVisible(true);
                    });
                }
            }
        });

        final JScrollPane scrollPane = new JScrollPane(table);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        final JPanel buttonPanel = new JPanel();
        final JButton logoutButton = new JButton("Logout");
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
}
