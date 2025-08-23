package it.ristorantelorma.view.customer;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.util.Collection;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import it.ristorantelorma.model.Restaurant;
import it.ristorantelorma.model.Result;
import it.ristorantelorma.view.authentication.LoginPage;

/**
 * Restaurant selection window.
 */
public final class RestaurantsPage extends JFrame {

    public static final long serialVersionUID = 201742972L; // Random
    private static final String ERROR_WINDOW_TITLE = "Errore";
    private static final int WINDOW_WIDTH = 500;
    private static final int WINDOW_HEIGHT = 500;
    private static final int FONT_SIZE = 18;
    private static final int TABLE_ROW_HEIGHT = 28;
    private static final int TABLE_COLUMN_MIN_WIDTH = 60;
    private static final int TABLE_COLUMN_MAX_WIDTH = 70;

    private final LoginPage loginPage;

    /**
     * @param loginPage
     * @param connection
     * @param username
     */
    @SuppressFBWarnings(
        value = "EI_EXPOSE_REP",
        justification = "loginPage can freely be modified by others without issues"
    )
    public RestaurantsPage(final LoginPage loginPage, final Connection connection, final String username) {
        this.loginPage = loginPage;
        setTitle("RestaurantsPage");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        setLocationRelativeTo(null);

        final JPanel mainPanel = new JPanel(new BorderLayout());

        // Read restaurant list from database
        final Collection<Restaurant> restaurants;
        final Result<Collection<Restaurant>> result = Restaurant.DAO.list(connection);
        if (result.isSuccess()) {
            restaurants = result.getValue();
        } else {
            JOptionPane.showMessageDialog(
                this,
                "Errore nel caricamento dei ristoranti: " + result.getErrorMessage(),
                ERROR_WINDOW_TITLE,
                JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        // Prepare data for the table
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
        table.setFont(new Font("Dialog", Font.PLAIN, FONT_SIZE));
        table.setRowHeight(TABLE_ROW_HEIGHT);

        table.getColumnModel().getColumn(1).setMinWidth(TABLE_COLUMN_MIN_WIDTH);
        table.getColumnModel().getColumn(1).setMaxWidth(TABLE_COLUMN_MAX_WIDTH);
        table.getColumnModel().getColumn(2).setMinWidth(TABLE_COLUMN_MIN_WIDTH);
        table.getColumnModel().getColumn(2).setMaxWidth(TABLE_COLUMN_MAX_WIDTH);

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(final MouseEvent evt) {
                final int row = table.rowAtPoint(evt.getPoint());
                if (row >= 0) {
                    final String restaurantName = data[row][0];
                    // Open the menu window for the selected restaurant
                    SwingUtilities.invokeLater(() -> {
                        RestaurantsPage.this.setVisible(false);
                        final ResMenu resMenu = new ResMenu(restaurantName, RestaurantsPage.this, username);
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
        this.setVisible(false); // Close this window
        loginPage.handleReset(); // Reset fields of loginPage
        loginPage.show(); // Show loginPage window
    }
}
